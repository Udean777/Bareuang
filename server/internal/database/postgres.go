package database

import (
	"log"

	"github.com/ssajudn/barebudget-server/internal/models"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func Connect(databaseURL string, isProduction bool) (*gorm.DB, error) {
	logLevel := logger.Warn
	if isProduction {
		logLevel = logger.Silent
	}
	db, err := gorm.Open(postgres.Open(databaseURL), &gorm.Config{
		Logger:         logger.Default.LogMode(logLevel),
		TranslateError: true,
	})
	if err != nil {
		return nil, err
	}

	log.Println("Database connection established")

	err = db.AutoMigrate(
		&models.User{},
		&models.Wallet{},
		&models.Transaction{},
		&models.DueBill{},
		&models.Budget{},
		&models.Goal{},
	)
	if err != nil {
		log.Printf("AutoMigrate error: %v", err)
		return nil, err
	}

	log.Println("Database migration completed")
	return db, nil
}
