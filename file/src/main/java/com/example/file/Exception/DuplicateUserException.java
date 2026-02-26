package com.example.file.Exception;

public class DuplicateUserException extends RuntimeException {

    private final int rowNumber;

    public DuplicateUserException(int rowNumber, String message) {
        super(message);
        this.rowNumber = rowNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }
}

