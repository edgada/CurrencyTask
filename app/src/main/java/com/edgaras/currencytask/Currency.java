package com.edgaras.currencytask;

public class Currency {
    private String shortName;
    private Double balanceLeft;
    private int totalTransactions;
    private Double totalSpentOnCommissionFees;

    public Currency(String shortName, Double balanceLeft, int totalTransactions, Double totalSpentOnCommissionFees) {
        this.shortName = shortName;
        this.balanceLeft = balanceLeft;
        this.totalTransactions = totalTransactions;
        this.totalSpentOnCommissionFees = totalSpentOnCommissionFees;
    }

    public String getShortName() {
        return shortName;
    }

    public Double getBalanceLeft() {
        return balanceLeft;
    }
    public void addToWallet(Double amountToAdd) {
        balanceLeft += amountToAdd;
    }
    public void takeFromWallet(Double amountToTake) {
        if(balanceLeft - amountToTake >= 0){
            balanceLeft -= amountToTake;
        }
    }

    public int getTransactionsCount() {
        return totalTransactions;
    }
    public void addToTransactionsCount(int amountToAdd) {
        totalTransactions += amountToAdd;
    }

    public Double getTotalSpentOnCommissionFees() {
        return totalSpentOnCommissionFees;
    }
    public void addToTotalSpentOnCommissionFees(Double amountToAdd) {
        this.totalSpentOnCommissionFees += amountToAdd;
    }

    public Double getCommissionFee() {
        if(isFreeCommission()){
            return 0.0;
        } else {
            return 0.07;
        }
    }
    private boolean isFreeCommission() {
        if(totalTransactions < 5){
            return true;
        } else if(totalTransactions % 10 == 0){
            return true;
        } else {
            return false;
        }
    }
}
