package service

import (
	"testing"

	"github.com/ssajudn/barebudget-server/internal/models"
)

func TestApplyBalanceChange(t *testing.T) {
	cases := []struct {
		name    string
		balance int64
		txType  models.TransactionType
		amount  int64
		reverse bool
		want    int64
	}{
		{"expense deducts", 1000, models.TypeExpense, 300, false, 700},
		{"income adds", 1000, models.TypeIncome, 300, false, 1300},
		{"empty type treated as expense", 1000, "", 300, false, 700},
		{"reverse expense refunds", 1000, models.TypeExpense, 300, true, 1300},
		{"reverse income deducts", 1000, models.TypeIncome, 300, true, 700},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			w := &models.Wallet{Balance: tc.balance}
			applyBalanceChange(w, tc.txType, tc.amount, tc.reverse)
			if w.Balance != tc.want {
				t.Fatalf("balance = %d, want %d", w.Balance, tc.want)
			}
		})
	}
}

func TestFormatCurrency(t *testing.T) {
	cases := map[int64]string{
		0:     "0",
		5:     "5",
		999:   "999",
		1000:  "1.000",
		12345: "12.345",
		-500:  "-500",
	}
	for in, want := range cases {
		if got := formatCurrency(in); got != want {
			t.Fatalf("formatCurrency(%d) = %q, want %q", in, got, want)
		}
	}
}
