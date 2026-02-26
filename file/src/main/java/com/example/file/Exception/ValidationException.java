package com.example.file.Exception;

public class ValidationException extends RuntimeException {
    private final int rowNumber;

    public ValidationException(int rowNumber, String message) {
        super(message);
        this.rowNumber = rowNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }
}
