package repository

import (
	"errors"
	"time"

	"github.com/google/uuid"
	"github.com/ssajudn/barebudget-server/internal/apperr"
	"github.com/ssajudn/barebudget-server/internal/models"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

// Store is the persistence surface available inside and outside transactions.
type Store interface {
	UpsertUser(user *models.User) error
	MigrateGuestData(guestUserID, targetUserID string) error

	CreateWallet(w *models.Wallet) error
	GetWalletsByUserID(userID string) ([]models.Wallet, error)
	GetWalletByID(userID string, id uuid.UUID) (*models.Wallet, error)
	GetWalletForUpdate(userID, walletID string) (*models.Wallet, error)
	UpdateWallet(w *models.Wallet) error
	DeleteWallet(userID string, id uuid.UUID) error

	CreateTransaction(t *models.Transaction) error
	GetTransactionByID(userID string, id uuid.UUID) (*models.Transaction, error)
	GetTransactionsByUserID(userID string, startDate, endDate time.Time, category string, limit, offset int) ([]models.Transaction, int64, error)
	DeleteTransactionByID(userID string, id uuid.UUID) error

	CreateDueBill(d *models.DueBill) error
	GetDueBillsByUserID(userID string, status string) ([]models.DueBill, error)
	GetDueBillForUpdate(userID string, id uuid.UUID) (*models.DueBill, error)
	UpdateDueBill(userID string, id uuid.UUID, patch models.DueBillPatch) error
	SettleDueBill(userID string, id uuid.UUID, status models.DueBillStatus, paidAt *time.Time, paidWalletID *string) error
	DeleteDueBill(userID string, id uuid.UUID) error

	CreateGoal(g *models.Goal) error
	GetGoalsByUserID(userID string) ([]models.Goal, error)
	GetGoalForUpdate(userID string, id uuid.UUID) (*models.Goal, error)
	SetGoalCurrentAmount(userID string, id uuid.UUID, amount int64) error
	UpdateGoal(userID string, id uuid.UUID, patch models.GoalPatch) error
	DeleteGoal(userID string, id uuid.UUID) error

	GetBudget(userID string, monthYear string) (*models.Budget, error)
	CreateBudget(b *models.Budget) error
	GetMonthlySpent(userID string, startOfMonth, endOfMonth time.Time) (int64, error)
	GetMonthlyCategoryBreakdown(userID string, startOfMonth, endOfMonth time.Time) ([]CategorySummary, error)
	GetMonthlyCashflow(userID string, monthsCount int) ([]CashflowDataPoint, error)
	GetMonthlyNetWorthTrend(userID string, monthsCount int) ([]NetWorthDataPoint, error)
}

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

// Transactional runs fn atomically; the Store passed to fn shares the
// underlying transaction.
func (r Repository) Transactional(fn func(Store) error) error {
	return r.db.Transaction(func(tx *gorm.DB) error {
		return fn(Repository{db: tx})
	})
}

func wrapNotFound(err error, msg string) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return apperr.NotFound(msg)
	}
	return err
}

// User Repo
func (r Repository) UpsertUser(user *models.User) error {
	return r.db.Save(user).Error
}

// Wallet Repo
func (r Repository) CreateWallet(w *models.Wallet) error {
	return r.db.Create(w).Error
}

func (r Repository) GetWalletsByUserID(userID string) ([]models.Wallet, error) {
	var list []models.Wallet
	err := r.db.Where("user_id = ?", userID).Order("created_at asc").Find(&list).Error
	return list, err
}

func (r Repository) GetWalletByID(userID string, id uuid.UUID) (*models.Wallet, error) {
	var w models.Wallet
	err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&w).Error
	if err != nil {
		return nil, wrapNotFound(err, "wallet not found")
	}
	return &w, nil
}

func (r Repository) GetWalletForUpdate(userID, walletID string) (*models.Wallet, error) {
	var w models.Wallet
	err := r.db.Clauses(clause.Locking{Strength: "UPDATE"}).
		Where("id = ? AND user_id = ?", walletID, userID).First(&w).Error
	if err != nil {
		return nil, wrapNotFound(err, "wallet not found")
	}
	return &w, nil
}

func (r Repository) UpdateWallet(w *models.Wallet) error {
	return r.db.Save(w).Error
}

func (r Repository) DeleteWallet(userID string, id uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Wallet{}).Error
}

// Transaction Repo
func (r Repository) CreateTransaction(t *models.Transaction) error {
	return r.db.Create(t).Error
}

func (r Repository) GetTransactionByID(userID string, id uuid.UUID) (*models.Transaction, error) {
	var t models.Transaction
	err := r.db.Where("id = ? AND user_id = ?", id, userID).First(&t).Error
	if err != nil {
		return nil, wrapNotFound(err, "transaction not found")
	}
	return &t, nil
}

func (r Repository) GetTransactionsByUserID(userID string, startDate, endDate time.Time, category string, limit, offset int) ([]models.Transaction, int64, error) {
	var list []models.Transaction
	var total int64

	query := r.db.Model(&models.Transaction{}).Where("user_id = ?", userID)

	if !startDate.IsZero() && !endDate.IsZero() {
		query = query.Where("date >= ? AND date <= ?", startDate, endDate)
	}

	if category != "" {
		query = query.Where("category = ?", category)
	}

	query.Count(&total)

	err := query.Order("date desc, created_at desc").Limit(limit).Offset(offset).Find(&list).Error
	return list, total, err
}

func (r Repository) DeleteTransactionByID(userID string, id uuid.UUID) error {
	res := r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Transaction{})
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return apperr.NotFound("transaction not found")
	}
	return nil
}

// DueBill Repo
type CategorySummary struct {
	Category models.TransactionCategory `json:"category"`
	Total    int64                      `json:"total"`
	Count    int64                      `json:"count"`
}

func (r Repository) CreateDueBill(d *models.DueBill) error {
	return r.db.Create(d).Error
}

func (r Repository) GetDueBillsByUserID(userID string, status string) ([]models.DueBill, error) {
	var list []models.DueBill
	query := r.db.Where("user_id = ?", userID)
	if status != "" {
		query = query.Where("status = ?", status)
	}
	err := query.Order("due_date asc").Find(&list).Error
	return list, err
}

func (r Repository) GetDueBillForUpdate(userID string, id uuid.UUID) (*models.DueBill, error) {
	var d models.DueBill
	err := r.db.Clauses(clause.Locking{Strength: "UPDATE"}).
		Where("id = ? AND user_id = ?", id, userID).First(&d).Error
	if err != nil {
		return nil, wrapNotFound(err, "bill not found")
	}
	return &d, nil
}

func (r Repository) UpdateDueBill(userID string, id uuid.UUID, patch models.DueBillPatch) error {
	res := r.db.Model(&models.DueBill{}).
		Where("id = ? AND user_id = ?", id, userID).
		Updates(patch)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return apperr.NotFound("bill not found")
	}
	return nil
}

// SettleDueBill writes the settlement columns of a bill in one update.
func (r Repository) SettleDueBill(userID string, id uuid.UUID, status models.DueBillStatus, paidAt *time.Time, paidWalletID *string) error {
	return r.db.Model(&models.DueBill{}).
		Where("id = ? AND user_id = ?", id, userID).
		Updates(map[string]interface{}{
			"status":         status,
			"paid_at":        paidAt,
			"paid_wallet_id": paidWalletID,
		}).Error
}

func (r Repository) DeleteDueBill(userID string, id uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.DueBill{}).Error
}

// Budget Repo
func (r Repository) GetBudget(userID string, monthYear string) (*models.Budget, error) {
	var b models.Budget
	err := r.db.Where("user_id = ? AND month_year = ?", userID, monthYear).First(&b).Error
	if err != nil {
		return nil, err
	}
	return &b, nil
}

func (r Repository) CreateBudget(b *models.Budget) error {
	return r.db.Create(b).Error
}

// Data Migration (Guest -> Authenticated User)
func (r Repository) MigrateGuestData(guestUserID, targetUserID string) error {
	if guestUserID == "" || targetUserID == "" || guestUserID == targetUserID {
		return nil
	}

	return r.db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Model(&models.Transaction{}).
			Where("user_id = ?", guestUserID).
			Update("user_id", targetUserID).Error; err != nil {
			return err
		}

		if err := tx.Model(&models.DueBill{}).
			Where("user_id = ?", guestUserID).
			Update("user_id", targetUserID).Error; err != nil {
			return err
		}

		var guestBudgets []models.Budget
		if err := tx.Where("user_id = ?", guestUserID).Find(&guestBudgets).Error; err != nil {
			return err
		}

		for _, gb := range guestBudgets {
			var targetBudget models.Budget
			err := tx.Where("user_id = ? AND month_year = ?", targetUserID, gb.MonthYear).First(&targetBudget).Error
			if err != nil {
				if !errors.Is(err, gorm.ErrRecordNotFound) {
					return err
				}
				if err := tx.Model(&models.Budget{}).Where("id = ?", gb.ID).Update("user_id", targetUserID).Error; err != nil {
					return err
				}
				continue
			}
			// Target already has a budget for this month; drop the guest duplicate.
			if err := tx.Where("id = ?", gb.ID).Delete(&models.Budget{}).Error; err != nil {
				return err
			}
		}

		if err := tx.Model(&models.Goal{}).
			Where("user_id = ?", guestUserID).
			Update("user_id", targetUserID).Error; err != nil {
			return err
		}

		return nil
	})
}

// Goal Repo
func (r Repository) CreateGoal(g *models.Goal) error {
	return r.db.Create(g).Error
}

func (r Repository) GetGoalsByUserID(userID string) ([]models.Goal, error) {
	var list []models.Goal
	err := r.db.Where("user_id = ?", userID).Order("created_at desc").Find(&list).Error
	return list, err
}

func (r Repository) GetGoalForUpdate(userID string, id uuid.UUID) (*models.Goal, error) {
	var g models.Goal
	err := r.db.Clauses(clause.Locking{Strength: "UPDATE"}).
		Where("id = ? AND user_id = ?", id, userID).First(&g).Error
	if err != nil {
		return nil, wrapNotFound(err, "goal not found")
	}
	return &g, nil
}

func (r Repository) SetGoalCurrentAmount(userID string, id uuid.UUID, amount int64) error {
	return r.db.Model(&models.Goal{}).
		Where("id = ? AND user_id = ?", id, userID).
		Update("current_amount", amount).Error
}

func (r Repository) UpdateGoal(userID string, id uuid.UUID, patch models.GoalPatch) error {
	res := r.db.Model(&models.Goal{}).
		Where("id = ? AND user_id = ?", id, userID).
		Updates(patch)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return apperr.NotFound("goal not found")
	}
	return nil
}

func (r Repository) DeleteGoal(userID string, id uuid.UUID) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).Delete(&models.Goal{}).Error
}

// Analytics Repo
type CashflowDataPoint struct {
	Month   string `json:"month"`
	Label   string `json:"label"`
	Income  int64  `json:"income"`
	Expense int64  `json:"expense"`
}

type NetWorthDataPoint struct {
	Month    string `json:"month"`
	Label    string `json:"label"`
	NetWorth int64  `json:"net_worth"`
}

func (r Repository) GetMonthlySpent(userID string, startOfMonth, endOfMonth time.Time) (int64, error) {
	var total int64
	err := r.db.Model(&models.Transaction{}).
		Where("user_id = ? AND date >= ? AND date <= ? AND (type = ? OR type IS NULL OR type = '') AND category != ?", userID, startOfMonth, endOfMonth, models.TypeExpense, models.CategoryBills).
		Select("COALESCE(SUM(amount), 0)").
		Scan(&total).Error
	return total, err
}

func (r Repository) GetMonthlyCategoryBreakdown(userID string, startOfMonth, endOfMonth time.Time) ([]CategorySummary, error) {
	var result []CategorySummary
	err := r.db.Model(&models.Transaction{}).
		Select("category, COALESCE(SUM(amount), 0) as total, COUNT(id) as count").
		Where("user_id = ? AND date >= ? AND date <= ?", userID, startOfMonth, endOfMonth).
		Group("category").
		Order("total desc").
		Scan(&result).Error
	return result, err
}

type monthlyTotals struct {
	Month   time.Time
	Income  int64
	Expense int64
}

func (r Repository) GetMonthlyCashflow(userID string, monthsCount int) ([]CashflowDataPoint, error) {
	now := time.Now()
	start := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, now.Location()).AddDate(0, -(monthsCount - 1), 0)
	end := start.AddDate(0, monthsCount, 0).Add(-time.Nanosecond)

	var rows []monthlyTotals
	err := r.db.Model(&models.Transaction{}).
		Select(`date_trunc('month', date) as month,
			COALESCE(SUM(CASE WHEN type = ? THEN amount ELSE 0 END), 0) as income,
			COALESCE(SUM(CASE WHEN type = ? OR type IS NULL OR type = '' THEN amount ELSE 0 END), 0) as expense`,
			models.TypeIncome, models.TypeExpense).
		Where("user_id = ? AND date >= ? AND date <= ?", userID, start, end).
		Group("month").
		Scan(&rows).Error
	if err != nil {
		return nil, err
	}

	byMonth := make(map[string]monthlyTotals, len(rows))
	for _, row := range rows {
		byMonth[row.Month.Format("2006-01")] = row
	}

	points := make([]CashflowDataPoint, 0, monthsCount)
	for i := monthsCount - 1; i >= 0; i-- {
		t := start.AddDate(0, i, 0)
		point := CashflowDataPoint{Month: t.Format("2006-01"), Label: t.Format("Jan")}
		if totals, ok := byMonth[point.Month]; ok {
			point.Income = totals.Income
			point.Expense = totals.Expense
		}
		points = append(points, point)
	}
	return points, nil
}

func (r Repository) GetMonthlyNetWorthTrend(userID string, monthsCount int) ([]NetWorthDataPoint, error) {
	wallets, err := r.GetWalletsByUserID(userID)
	if err != nil {
		return nil, err
	}
	var currentNetWorth int64
	for _, w := range wallets {
		currentNetWorth += w.Balance
	}

	cashflow, err := r.GetMonthlyCashflow(userID, monthsCount)
	if err != nil {
		return nil, err
	}

	points := make([]NetWorthDataPoint, len(cashflow))
	runningNetWorth := currentNetWorth

	for i := len(cashflow) - 1; i >= 0; i-- {
		points[i] = NetWorthDataPoint{
			Month:    cashflow[i].Month,
			Label:    cashflow[i].Label,
			NetWorth: runningNetWorth,
		}
		netChange := cashflow[i].Income - cashflow[i].Expense
		runningNetWorth -= netChange
	}

	return points, nil
}
