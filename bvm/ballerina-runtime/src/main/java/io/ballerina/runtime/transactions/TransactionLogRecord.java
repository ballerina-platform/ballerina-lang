package io.ballerina.runtime.transactions;

public class TransactionLogRecord {
    public String transactionId;
    private String transactionBlockId;
    private RecoveryStatus transactionStatus;
    // TODO: add the following fields to the TransactionRecord
    //         expirationTime;
    //         parentTransactionId;
    //         RecoveryDomain;

    public TransactionLogRecord(String transactionId, String transactionBlockId, RecoveryStatus transactionStatus) {
        this.transactionId = transactionId;
        this.transactionBlockId = transactionBlockId;
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionLogRecord() {
        return transactionId + ":" + transactionBlockId + "|" + transactionStatus + System.getProperty("line.separator");
    }

    public static TransactionLogRecord parseTransactionLogRecord(String logLine) {
        String[] logBlocks = logLine.split("\\|");
        if (logBlocks.length == 2) {
            String[] combinedId = logBlocks[0].split(":");
            String transactionStatusString = logBlocks[1];
            String transactionId = combinedId[0];
            String transactionBlockId = combinedId[1];
            // match with RecoveryStatus enum
            RecoveryStatus transactionStatus = RecoveryStatus.getRecoveryStatus(transactionStatusString);
            return new TransactionLogRecord(transactionId, transactionBlockId, transactionStatus);
        }
        // If parsing fails.. TODO: handle parsing fail properly
//        log.error("Error while parsing transaction log record: " + logLine + "\nLog file may be corrupted.");
        return null;
    }

    public boolean isCompleted() {
        return transactionStatus.equals(RecoveryStatus.TERMINATED);
    }

}
