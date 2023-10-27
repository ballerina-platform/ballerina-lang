package io.ballerina.runtime.transactions;

import java.util.HashMap;

public interface RecoveryLog {
    /**
     * Write a log entry to the recovery log file.
     *
     * @param trxId the transaction id
     * @param trxRecord the transaction record
     */
    void put(String trxId, TransactionRecord trxRecord);

    void writeCheckpoint();

    void close();
}
