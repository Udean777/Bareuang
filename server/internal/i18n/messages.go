package i18n

import "fmt"

// In-memory localized message dictionary supporting English and Indonesian string templates.
var messages = map[string]map[string]string{
	"en": {
		"runway.no_budget":       "Monthly budget is not set yet. Set your budget to track your financial runway!",
		"runway.exhausted":       "Runway exhausted! Your budget was exceeded on day %d.",
		"runway.will_run_out":    "At your current spending rate (Rp %s/day), your budget will run out on day %d!",
		"runway.safe":            "Your financial runway is safe until the end of the month. Keep it up!",
		"runway.no_expenses":     "No expenses recorded this month yet. Your budget is untouched!",
		"bill.paid.notes":        "Bill payment: %s",
		"bill.refund.merchant":   "Refund: %s",
		"bill.refund.notes":      "Bill payment canceled: %s",
		"goal.deposit.merchant":  "Savings: %s",
		"goal.deposit.notes":     "Deposit to savings %s",
		"goal.withdraw.merchant": "Withdrawal: %s",
		"goal.withdraw.notes":    "Withdrawal from savings %s",
	},
	"id": {
		"runway.no_budget":       "Budget bulanan belum diatur. Atur budget untuk melacak runway finansialmu!",
		"runway.exhausted":       "Runway habis! Budget kamu terlewati pada hari ke-%d.",
		"runway.will_run_out":    "Dengan laju pengeluaran saat ini (Rp %s/hari), budget akan habis pada hari ke-%d!",
		"runway.safe":            "Runway finansial aman hingga akhir bulan. Pertahankan!",
		"runway.no_expenses":     "Belum ada pengeluaran bulan ini. Budget masih utuh!",
		"bill.paid.notes":        "Pembayaran tagihan: %s",
		"bill.refund.merchant":   "Refund: %s",
		"bill.refund.notes":      "Pembatalan pembayaran tagihan %s",
		"goal.deposit.merchant":  "Tabungan: %s",
		"goal.deposit.notes":     "Setor ke tabungan %s",
		"goal.withdraw.merchant": "Penarikan: %s",
		"goal.withdraw.notes":    "Penarikan dari tabungan %s",
	},
}

func T(lang, key string, args ...interface{}) string {
	if m, ok := messages[lang]; ok {
		if tmpl, ok := m[key]; ok {
			if len(args) > 0 {
				return fmt.Sprintf(tmpl, args...)
			}
			return tmpl
		}
	}
	// fallback en
	if tmpl, ok := messages["en"][key]; ok {
		if len(args) > 0 {
			return fmt.Sprintf(tmpl, args...)
		}
		return tmpl
	}
	return key
}
