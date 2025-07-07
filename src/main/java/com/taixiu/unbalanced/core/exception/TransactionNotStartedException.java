package com.taixiu.unbalanced.core.exception;

public class TransactionNotStartedException extends RuntimeException {

    public TransactionNotStartedException() {
        this("Transaction haven't started yet!");
    }
    
    public TransactionNotStartedException(String message) {
        super(message);
    }
}