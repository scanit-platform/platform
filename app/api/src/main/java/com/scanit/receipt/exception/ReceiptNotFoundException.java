package com.scanit.receipt.exception;

public class ReceiptNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReceiptNotFoundException(Long id) {
        super("Receipt with id " + id + " not found");
    }
}
