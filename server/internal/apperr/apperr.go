package apperr

import (
	"errors"
	"fmt"
	"net/http"
)

type AppError struct {
	Status int
	Msg    string
}

func (e *AppError) Error() string { return e.Msg }

func New(status int, format string, args ...interface{}) *AppError {
	return &AppError{Status: status, Msg: fmt.Sprintf(format, args...)}
}

func BadRequest(format string, args ...interface{}) *AppError {
	return New(http.StatusBadRequest, format, args...)
}

func NotFound(msg string) *AppError {
	return New(http.StatusNotFound, "%s", msg)
}

func Conflict(format string, args ...interface{}) *AppError {
	return New(http.StatusConflict, format, args...)
}

// Status returns the mapped HTTP status for err, defaulting to 500.
func Status(err error) int {
	var ae *AppError
	if errors.As(err, &ae) {
		return ae.Status
	}
	return http.StatusInternalServerError
}
