package io.ballerina.runtime.transactions;

public class TransactionLogRecord {
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private final static String FIELD_SEPARATOR = "|";
    private final static String COMBINED_ID_SEPARATOR = ":";

    public String transactionId;
    private String transactionBlockId;
    private RecoveryState transactionStatus;
    private long logTime;

    /**
     * Create a transaction log record.
     *
     * @param transactionId      the transaction id
     * @param transactionBlockId the transaction block id
     * @param transactionStatus  the current status of the transaction
     */

    public TransactionLogRecord(String transactionId, String transactionBlockId, RecoveryState transactionStatus) {
        this.transactionId = transactionId;
        this.transactionBlockId = transactionBlockId;
        this.transactionStatus = transactionStatus;
        this.logTime = System.currentTimeMillis();
    }

    public TransactionLogRecord(String transactionId, String transactionBlockId, RecoveryState transactionStatus, long logTime) {
        this.transactionId = transactionId;
        this.transactionBlockId = transactionBlockId;
        this.transactionStatus = transactionStatus;
        this.logTime = logTime;
    }

    public RecoveryState getTransactionState() {
        return transactionStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionBlockId() {
        return transactionBlockId;
    }

    // Need to move or change
    public String getCombinedId(){
        String combinedId =  transactionId + ":" + transactionBlockId;
        return combinedId;
    }

    public String getTransactionLogRecord() {
        return transactionId + COMBINED_ID_SEPARATOR + transactionBlockId + FIELD_SEPARATOR + transactionStatus +
                FIELD_SEPARATOR + logTime + LINE_SEPARATOR;
    }

    /**
     * Parse a transaction log record from a log line.
     *
     * @param logLine the log as a string
     * @return the transaction log record
     *
     */
    public static TransactionLogRecord parseTransactionLogRecord(String logLine) {
        String[] logBlocks = logLine.split("\\|");
        if (logBlocks.length == 3) {
            String[] combinedId = logBlocks[0].split(COMBINED_ID_SEPARATOR);
            String transactionStatusString = logBlocks[1];
            Long logTime = Long.parseLong(logBlocks[2]);
            String transactionId = combinedId[0];
            String transactionBlockId = combinedId[1];
            RecoveryState transactionStatus = RecoveryState.getRecoveryStatus(transactionStatusString);
            return new TransactionLogRecord(transactionId, transactionBlockId, transactionStatus, logTime);
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
        return transactionStatus.equals(RecoveryState.TERMINATED);
    }

}
