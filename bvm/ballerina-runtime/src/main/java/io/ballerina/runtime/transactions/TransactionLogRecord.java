package io.ballerina.runtime.transactions;

public class TransactionLogRecord {
    public String transactionId;
    private String transactionBlockId;
    private RecoveryStatus transactionStatus;


    /**
     * Create a transaction log record.
     *
     * @param transactionId      the transaction id
     * @param transactionBlockId the transaction block id
     * @param transactionStatus  the current status of the transaction
     * TODO: add the following fields to the TransactionRecord
     *          expirationTime;
     *          parentTransactionId;
     *          RecoveryDomain;
     */
    public TransactionLogRecord(String transactionId, String transactionBlockId, RecoveryStatus transactionStatus) {
        this.transactionId = transactionId;
        this.transactionBlockId = transactionBlockId;
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionLogRecord() {
        return transactionId + ":" + transactionBlockId + "|" + transactionStatus + System.getProperty("line.separator");
    }

    /**
     * Parse a transaction log record from a log line.
     *
     * @param logLine the log as a string
     * @return the transaction log record
     *
     * TODO: change as required, after adding more information to the log record
     */
    public static TransactionLogRecord parseTransactionLogRecord(String logLine) {
        String[] logBlocks = logLine.split("\\|");
        if (logBlocks.length == 2) {
            String[] combinedId = logBlocks[0].split(":");
            String transactionStatusString = logBlocks[1];
            String transactionId = combinedId[0];
            String transactionBlockId = combinedId[1];
            RecoveryStatus transactionStatus = RecoveryStatus.getRecoveryStatus(transactionStatusString);
            return new TransactionLogRecord(transactionId, transactionBlockId, transactionStatus);
        }
        // If parsing fails.. TODO: handle parsing fail properly
        return null;
    }

    /**
     * Check whether the transaction is in a final state.
     *
     * @return true if the transaction is completed
     */
    public boolean isCompleted() {
        return transactionStatus.equals(RecoveryStatus.TERMINATED);
    }

}
