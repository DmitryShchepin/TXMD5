package com.taixiu.unbalanced.core;

import java.util.Arrays;

public enum TransactionAction {

    BET(1),
    CANCEL(2),
    RESERVE(3),
    RELEASE(4),
    WIN(5),
    LOSE(6);

    public final int id;

    TransactionAction(int id) {
        this.id = id;
    }

    public static final TransactionAction getById(int id) {
        return Arrays.stream(TransactionAction.values()).filter(t -> t.id == id).findAny().get();
    }
}
