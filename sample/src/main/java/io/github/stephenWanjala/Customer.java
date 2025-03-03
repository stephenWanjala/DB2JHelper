package io.github.stephenWanjala;

public class Customer {
    private String ledgerName;  private String ledgerNumber;

    public Customer() {
    }

    public String getLedgerName() {
        return ledgerName;
    }

    public void setLedgerName(String ledgerName) {
        this.ledgerName = ledgerName;
    }

    public String getLedgerNumber() {
        return ledgerNumber;
    }

    public void setLedgerNumber(String ledgerNumber) {
        this.ledgerNumber = ledgerNumber;
    }

    Customer(String ledgerName, String ledgerNumber) {
        this.ledgerName = ledgerName;
        this.ledgerNumber = ledgerNumber;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "ledgerName='" + ledgerName + '\'' +
                ", ledgerNumber='" + ledgerNumber + '\'' +
                '}';
    }
}
