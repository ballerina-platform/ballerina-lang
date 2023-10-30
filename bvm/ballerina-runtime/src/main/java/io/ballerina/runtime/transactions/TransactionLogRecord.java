package io.ballerina.runtime.transactions;

public class TransactionLogRecord {
    public String transactionId;
    private String transactionBlockId;
    private RecoveryStatus transactionStatus;
    // TODO: add the following fields to the TransactionRecord
    //         expirationTime;
    //         parentTransactionId;
    //         RecoveryManagerId;

    public TransactionLogRecord(String transactionId, String transactionBlockId, RecoveryStatus transactionStatus) {
        this.transactionId = transactionId;
        this.transactionBlockId = transactionBlockId;
        this.transactionStatus = transactionStatus;
    }

    public String getLogRecord() {
        return transactionId + ":" + transactionBlockId + "|" + transactionStatus + System.getProperty("line.separator");
    }

    public boolean isCompleted() {
        return transactionStatus.equals(RecoveryStatus.TERMINATED);
    }

}
