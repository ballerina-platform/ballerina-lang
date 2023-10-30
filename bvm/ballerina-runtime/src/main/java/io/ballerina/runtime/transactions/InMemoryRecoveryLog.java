package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class InMemoryRecoveryLog implements RecoveryLog{
    private static final Logger log = LoggerFactory.getLogger(InMemoryRecoveryLog.class);
    private Map<String, TransactionLogRecord> transactionLogs;

    public InMemoryRecoveryLog() {
        this.transactionLogs = new HashMap<>();
    }



    /**
     * Retrieve all transaction logs from the in-memory log.
     *
     * @return Map of transaction logs
     */
    public Map<String, TransactionLogRecord> getAllLogs() {
        return transactionLogs;
    }

    /**
     * Clear all transaction logs from the in-memory log.
     */
    public void clearAllLogs() {
        transactionLogs.clear();
    }

    @Override
    public void put(TransactionLogRecord trxRecord) {
        transactionLogs.put(trxRecord.transactionId, trxRecord);
    }

    /**
     * Write a checkpoint to the in-memory log (not needed if you don't need checkpoints).
     */
    public void writeCheckpoint() {
        // TODO: Implement a better checkpoint logic.
        if (transactionLogs.size() >= 25) {
            Map<String, TransactionLogRecord> pendingTransactions = getPendingTransactions();
            transactionLogs.clear();
            transactionLogs.putAll(pendingTransactions);
        }
    }

    private Map<String, TransactionLogRecord> getPendingTransactions() {
        Map<String, TransactionLogRecord> pendingTransactions = new HashMap<>();

        for (Map.Entry<String, TransactionLogRecord> entry : transactionLogs.entrySet()) {
            String trxId = entry.getKey();
            TransactionLogRecord trxRecord = entry.getValue();

            if (!trxRecord.isCompleted()) {
                pendingTransactions.put(trxId, trxRecord);
            }
        }
        return pendingTransactions;
    }

    @Override
    public void close() {

    }
}
