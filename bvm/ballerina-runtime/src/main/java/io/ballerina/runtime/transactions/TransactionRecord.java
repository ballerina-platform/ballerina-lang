package io.ballerina.runtime.transactions;

public class TransactionRecord {

    public final String transactionId;
    public final String transactionBlockId;
    public final String transactionStatus;

    // TODO: add the following fields to the TransactionRecord
    //    public final long expirationTime;
    //    public final String parentTransactionId;
    //    public final String RecoveryManagerId;


    public TransactionRecord(String transactionId, String transactionBlockId, String transactionStatus) {
        this.transactionId = transactionId;
        this.transactionBlockId = transactionBlockId;
        this.transactionStatus = transactionStatus;
    }

    public String getLogRecord() {
        return transactionId + ":" + transactionBlockId + "|" + transactionStatus + System.getProperty("line.separator");
    }

    public boolean isCompleted() {
        return transactionStatus.equals("COMMITTED") || transactionStatus.equals("ABORTED");
    }
}
