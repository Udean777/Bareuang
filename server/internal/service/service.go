package service

import (
	"errors"
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/ssajudn/barebudget-server/internal/apperr"
	"github.com/ssajudn/barebudget-server/internal/i18n"
	"github.com/ssajudn/barebudget-server/internal/models"
	"github.com/ssajudn/barebudget-server/internal/repository"
	"gorm.io/gorm"
)

// Repository is defined consumer-side so Service depends on an abstraction,
// not the concrete GORM implementation.
type Repository interface {
	Transactional(fn func(repository.Store) error) error

	UpsertUser(user *models.User) error
	MigrateGuestData(guestUserID, targetUserID string) error

	CreateWallet(w *models.Wallet) error
	GetWalletsByUserID(userID string) ([]models.Wallet, error)
	GetWalletByID(userID string, id uuid.UUID) (*models.Wallet, error)
	UpdateWallet(w *models.Wallet) error
	DeleteWallet(userID string, id uuid.UUID) error

	CreateTransaction(t *models.Transaction) error
	GetTransactionsByUserID(userID string, startDate, endDate time.Time, category string, limit, offset int) ([]models.Transaction, int64, error)
	DeleteTransactionByID(userID string, id uuid.UUID) error

	CreateDueBill(d *models.DueBill) error
	GetDueBillsByUserID(userID string, status string) ([]models.DueBill, error)
	UpdateDueBill(userID string, id uuid.UUID, patch models.DueBillPatch) error
	SettleDueBill(userID string, id uuid.UUID, status models.DueBillStatus, paidAt *time.Time, paidWalletID *string) error
	DeleteDueBill(userID string, id uuid.UUID) error

	CreateGoal(g *models.Goal) error
	GetGoalsByUserID(userID string) ([]models.Goal, error)
	SetGoalCurrentAmount(userID string, id uuid.UUID, amount int64) error
	UpdateGoal(userID string, id uuid.UUID, patch models.GoalPatch) error
	DeleteGoal(userID string, id uuid.UUID) error

	GetBudget(userID string, monthYear string) (*models.Budget, error)
	CreateBudget(b *models.Budget) error
	GetMonthlySpent(userID string, startOfMonth, endOfMonth time.Time) (int64, error)
	GetMonthlyCategoryBreakdown(userID string, startOfMonth, endOfMonth time.Time) ([]repository.CategorySummary, error)
	GetMonthlyCashflow(userID string, monthsCount int) ([]repository.CashflowDataPoint, error)
	GetMonthlyNetWorthTrend(userID string, monthsCount int) ([]repository.NetWorthDataPoint, error)
}

type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func applyBalanceChange(w *models.Wallet, txType models.TransactionType, amount int64, reverse bool) {
	income := txType == models.TypeIncome
	expense := txType == models.TypeExpense || txType == ""
	if reverse {
		if income {
			w.Balance -= amount
		} else if expense {
			w.Balance += amount
		}
		return
	}
	if income {
		w.Balance += amount
	} else if expense {
		w.Balance -= amount
	}
}

// User Services
func (s *Service) SyncUser(user *models.User) error {
	return s.repo.UpsertUser(user)
}

func (s *Service) MigrateGuestData(guestUserID, targetUserID string) error {
	return s.repo.MigrateGuestData(guestUserID, targetUserID)
}

// Wallet Services
func (s *Service) CreateWallet(w *models.Wallet) error {
	return s.repo.CreateWallet(w)
}

func (s *Service) GetWallets(userID string) ([]models.Wallet, error) {
	return s.repo.GetWalletsByUserID(userID)
}

func (s *Service) GetWalletByID(userID string, id uuid.UUID) (*models.Wallet, error) {
	return s.repo.GetWalletByID(userID, id)
}

func (s *Service) UpdateWallet(w *models.Wallet) error {
	return s.repo.UpdateWallet(w)
}

func (s *Service) DeleteWallet(userID string, id uuid.UUID) error {
	return s.repo.DeleteWallet(userID, id)
}

// Transaction Services
func (s *Service) CreateTransaction(t *models.Transaction) error {
	return s.repo.Transactional(func(r repository.Store) error {
		if err := r.CreateTransaction(t); err != nil {
			return err
		}
		if t.WalletID == nil || *t.WalletID == "" {
			return nil
		}
		wallet, err := r.GetWalletForUpdate(t.UserID, *t.WalletID)
		if err != nil {
			return err
		}
		applyBalanceChange(wallet, t.Type, t.Amount, false)
		return r.UpdateWallet(wallet)
	})
}

func (s *Service) GetTransactions(userID string, startDate, endDate time.Time, category string, page, limit int) ([]models.Transaction, int64, error) {
	if limit <= 0 {
		limit = 20
	}
	if limit > 100 {
		limit = 100
	}
	if page <= 0 {
		page = 1
	}
	offset := (page - 1) * limit
	return s.repo.GetTransactionsByUserID(userID, startDate, endDate, category, limit, offset)
}

func (s *Service) DeleteTransaction(userID string, id uuid.UUID) error {
	return s.repo.Transactional(func(r repository.Store) error {
		t, err := r.GetTransactionByID(userID, id)
		if err != nil {
			return err
		}
		if t.WalletID != nil && *t.WalletID != "" {
			wallet, err := r.GetWalletForUpdate(userID, *t.WalletID)
			if err != nil {
				return err
			}
			applyBalanceChange(wallet, t.Type, t.Amount, true)
			if err := r.UpdateWallet(wallet); err != nil {
				return err
			}
		}
		return r.DeleteTransactionByID(userID, id)
	})
}

// DueBill Services
func (s *Service) CreateDueBill(d *models.DueBill) error {
	return s.repo.CreateDueBill(d)
}

func (s *Service) GetDueBills(userID string, status string) ([]models.DueBill, error) {
	return s.repo.GetDueBillsByUserID(userID, status)
}

func (s *Service) UpdateDueBill(userID string, id uuid.UUID, patch models.DueBillPatch) error {
	return s.repo.UpdateDueBill(userID, id, patch)
}

func (s *Service) UpdateDueBillStatus(userID string, id uuid.UUID, status models.DueBillStatus, walletID *string, lang string) error {
	return s.repo.Transactional(func(r repository.Store) error {
		bill, err := r.GetDueBillForUpdate(userID, id)
		if err != nil {
			return err
		}

		if status == models.DueBillPaid {
			now := time.Now()
			var paidAt = &now
			if walletID != nil && *walletID != "" {
				wallet, err := r.GetWalletForUpdate(userID, *walletID)
				if err != nil {
					return err
				}
				wallet.Balance -= bill.TotalAmount
				if err := r.UpdateWallet(wallet); err != nil {
					return err
				}
				payment := models.Transaction{
					UserID:   userID,
					Amount:   bill.TotalAmount,
					Type:     models.TypeExpense,
					Category: models.CategoryBills,
					Merchant: bill.ProviderName,
					Date:     now,
					Notes:    i18n.T(lang, "bill.paid.notes", bill.ProviderName),
					WalletID: walletID,
				}
				if err := r.CreateTransaction(&payment); err != nil {
					return err
				}
			} else {
				paidAt = nil
			}
			return r.SettleDueBill(userID, id, status, paidAt, walletID)
		}

		if status == models.DueBillUnpaid && bill.Status == models.DueBillPaid &&
			bill.PaidWalletID != nil && *bill.PaidWalletID != "" {
			refundWalletID := *bill.PaidWalletID
			now := time.Now()
			wallet, err := r.GetWalletForUpdate(userID, refundWalletID)
			if err != nil {
				return err
			}
			wallet.Balance += bill.TotalAmount
			if err := r.UpdateWallet(wallet); err != nil {
				return err
			}
			refund := models.Transaction{
				UserID:   userID,
				Amount:   bill.TotalAmount,
				Type:     models.TypeIncome,
				Category: models.CategoryBills,
				Merchant: i18n.T(lang, "bill.refund.merchant", bill.ProviderName),
				Date:     now,
				Notes:    i18n.T(lang, "bill.refund.notes", bill.ProviderName),
				WalletID: &refundWalletID,
			}
			if err := r.CreateTransaction(&refund); err != nil {
				return err
			}
			return r.SettleDueBill(userID, id, status, nil, nil)
		}

		if status == models.DueBillUnpaid {
			return r.SettleDueBill(userID, id, status, nil, nil)
		}
		return r.SettleDueBill(userID, id, status, bill.PaidAt, bill.PaidWalletID)
	})
}

func (s *Service) DeleteDueBill(userID string, id uuid.UUID) error {
	return s.repo.DeleteDueBill(userID, id)
}

// Goal Services
func (s *Service) CreateGoal(g *models.Goal) error {
	return s.repo.CreateGoal(g)
}

func (s *Service) GetGoals(userID string) ([]models.Goal, error) {
	return s.repo.GetGoalsByUserID(userID)
}

func (s *Service) DepositToGoal(userID string, id uuid.UUID, walletID string, amount int64, lang string) error {
	return s.repo.Transactional(func(r repository.Store) error {
		goal, err := r.GetGoalForUpdate(userID, id)
		if err != nil {
			return err
		}
		newAmount := goal.CurrentAmount + amount
		if newAmount < 0 {
			return apperr.BadRequest("insufficient goal balance")
		}
		if err := r.SetGoalCurrentAmount(userID, id, newAmount); err != nil {
			return err
		}

		wallet, err := r.GetWalletForUpdate(userID, walletID)
		if err != nil {
			return err
		}

		now := time.Now()
		if amount > 0 {
			if wallet.Balance < amount {
				return apperr.BadRequest("insufficient wallet balance")
			}
			wallet.Balance -= amount
			if err := r.UpdateWallet(wallet); err != nil {
				return err
			}
			deposit := models.Transaction{
				UserID:   userID,
				Amount:   amount,
				Type:     models.TypeExpense,
				Category: models.CategoryOther,
				Merchant: i18n.T(lang, "goal.deposit.merchant", goal.Name),
				Date:     now,
				Notes:    i18n.T(lang, "goal.deposit.notes", goal.Name),
				WalletID: &walletID,
			}
			return r.CreateTransaction(&deposit)
		}

		withdrawAmt := -amount
		wallet.Balance += withdrawAmt
		if err := r.UpdateWallet(wallet); err != nil {
			return err
		}
		withdrawal := models.Transaction{
			UserID:   userID,
			Amount:   withdrawAmt,
			Type:     models.TypeIncome,
			Category: models.CategoryOther,
			Merchant: i18n.T(lang, "goal.withdraw.merchant", goal.Name),
			Date:     now,
			Notes:    i18n.T(lang, "goal.withdraw.notes", goal.Name),
			WalletID: &walletID,
		}
		return r.CreateTransaction(&withdrawal)
	})
}

func (s *Service) UpdateGoal(userID string, id uuid.UUID, patch models.GoalPatch) error {
	return s.repo.UpdateGoal(userID, id, patch)
}

func (s *Service) DeleteGoal(userID string, id uuid.UUID) error {
	return s.repo.DeleteGoal(userID, id)
}

// Budget Services
func (s *Service) SetBudget(userID string, limit int64, monthYear string) error {
	b := &models.Budget{
		UserID:       userID,
		MonthlyLimit: limit,
		MonthYear:    monthYear,
	}
	err := s.repo.CreateBudget(b)
	if errors.Is(err, gorm.ErrDuplicatedKey) {
		return apperr.Conflict("budget already set for %s: only one update per month allowed", monthYear)
	}
	return err
}

// Budget & Dashboard Runway Services
type DashboardSummary struct {
	MonthlyBudget      int64                        `json:"monthly_budget"`
	TotalSpent         int64                        `json:"total_spent"`
	RemainingBudget    int64                        `json:"remaining_budget"`
	DaysPassed         int                          `json:"days_passed"`
	DaysInMonth        int                          `json:"days_in_month"`
	AverageDailySpend  int64                        `json:"average_daily_spend"`
	EstimatedDeathDay  int                          `json:"estimated_death_day"`
	RunwayMessage      string                       `json:"runway_message"`
	TopCategories      []repository.CategorySummary `json:"top_categories"`
	UnpaidDueBillsSum  int64                        `json:"unpaid_due_bills_sum"`
	NetWorth           int64                        `json:"net_worth"`
	RecentTransactions []models.Transaction         `json:"recent_transactions"`
}

func (s *Service) GetDashboardSummary(userID string, now time.Time) (*DashboardSummary, error) {
	return s.GetDashboardSummaryWithLang(userID, now, "en")
}

func (s *Service) GetDashboardSummaryWithLang(userID string, now time.Time, lang string) (*DashboardSummary, error) {
	monthYear := now.Format("2006-01")
	startOfMonth := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, now.Location())
	endOfMonth := startOfMonth.AddDate(0, 1, 0).Add(-time.Nanosecond)
	daysInMonth := endOfMonth.Day()
	daysPassed := now.Day()

	budget, err := s.repo.GetBudget(userID, monthYear)
	if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, err
	}
	var monthlyBudget int64 = 0
	if budget != nil {
		monthlyBudget = budget.MonthlyLimit
	}

	totalSpent, err := s.repo.GetMonthlySpent(userID, startOfMonth, endOfMonth)
	if err != nil {
		return nil, err
	}

	remainingBudget := monthlyBudget - totalSpent
	var avgDailySpend int64 = 0
	if daysPassed > 0 {
		avgDailySpend = totalSpent / int64(daysPassed)
	}

	var estimatedDeathDay int = daysInMonth
	var runwayMsg string

	if monthlyBudget <= 0 {
		runwayMsg = i18n.T(lang, "runway.no_budget")
	} else if remainingBudget <= 0 {
		estimatedDeathDay = daysPassed
		runwayMsg = i18n.T(lang, "runway.exhausted", daysPassed)
	} else if avgDailySpend > 0 {
		daysLeft := int(remainingBudget / avgDailySpend)
		projectedDay := daysPassed + daysLeft
		if projectedDay < daysInMonth {
			estimatedDeathDay = projectedDay
			runwayMsg = i18n.T(lang, "runway.will_run_out", formatCurrency(avgDailySpend), projectedDay)
		} else {
			estimatedDeathDay = daysInMonth
			runwayMsg = i18n.T(lang, "runway.safe")
		}
	} else {
		runwayMsg = i18n.T(lang, "runway.no_expenses")
	}

	categories, err := s.repo.GetMonthlyCategoryBreakdown(userID, startOfMonth, endOfMonth)
	if err != nil {
		return nil, err
	}

	bills, err := s.repo.GetDueBillsByUserID(userID, string(models.DueBillUnpaid))
	if err != nil {
		return nil, err
	}
	var unpaidSum int64 = 0
	for _, b := range bills {
		unpaidSum += b.TotalAmount
	}

	recentTxs, _, err := s.repo.GetTransactionsByUserID(userID, time.Time{}, time.Time{}, "", 5, 0)
	if err != nil {
		return nil, err
	}

	wallets, err := s.repo.GetWalletsByUserID(userID)
	if err != nil {
		return nil, err
	}
	var netWorth int64 = 0
	for _, w := range wallets {
		netWorth += w.Balance
	}

	return &DashboardSummary{
		MonthlyBudget:      monthlyBudget,
		TotalSpent:         totalSpent,
		RemainingBudget:    remainingBudget,
		DaysPassed:         daysPassed,
		DaysInMonth:        daysInMonth,
		AverageDailySpend:  avgDailySpend,
		EstimatedDeathDay:  estimatedDeathDay,
		RunwayMessage:      runwayMsg,
		TopCategories:      categories,
		UnpaidDueBillsSum:  unpaidSum,
		NetWorth:           netWorth,
		RecentTransactions: recentTxs,
	}, nil
}

func formatCurrency(amount int64) string {
	neg := amount < 0
	if neg {
		amount = -amount
	}
	str := fmt.Sprintf("%d", amount)
	n := len(str)
	var res string
	for i, c := range str {
		if (n-i)%3 == 0 && i != 0 {
			res += "."
		}
		res += string(c)
	}
	if neg {
		return "-" + res
	}
	return res
}

// Analytics Services
func (s *Service) GetCashflowAnalytics(userID string) ([]repository.CashflowDataPoint, error) {
	return s.repo.GetMonthlyCashflow(userID, 6)
}

func (s *Service) GetNetWorthAnalytics(userID string) ([]repository.NetWorthDataPoint, error) {
	return s.repo.GetMonthlyNetWorthTrend(userID, 6)
}
