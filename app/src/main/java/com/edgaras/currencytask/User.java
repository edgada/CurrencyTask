package com.edgaras.currencytask;

import java.util.List;

public class User {
    private List<Currency> wallet;

    public User(List<Currency> wallet) {
        this.wallet = wallet;
    }

    public List<Currency> getWallet() {
        return wallet;
    }
}
