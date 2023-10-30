package io.ballerina.runtime.transactions;

public interface RecoveryLog {

    /**
     * Write a log entry to the recovery log file.
     *
     * @param trxRecord the transaction log record
     */
    void put(TransactionLogRecord trxRecord);

    /**
     * Write a checkpoint to the recovery log file (not needed if checkpoint are not needed)
     */
    void writeCheckpoint();

    /**
     * Close the recovery log file.
     */
    void close();
}
