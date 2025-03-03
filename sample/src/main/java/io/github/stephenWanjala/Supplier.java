package io.github.stephenWanjala;

public class Supplier {
    private String ledgerName;
    private String ledgerNumber;

    public Supplier() {
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

    @Override
    public String toString() {
        return "Supplier{" +
                "ledgerName='" + ledgerName + '\'' +
                ", ledgerNumber='" + ledgerNumber + '\'' +
                '}';
    }
}
