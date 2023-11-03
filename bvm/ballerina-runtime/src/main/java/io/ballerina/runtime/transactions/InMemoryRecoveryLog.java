package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static io.ballerina.runtime.transactions.TransactionConstants.IN_MEMORY_CHECKPOINT_INTERVAL;

public class InMemoryRecoveryLog implements RecoveryLog{
     private static final Logger log = LoggerFactory.getLogger(InMemoryRecoveryLog.class);
    private Map<String, TransactionLogRecord> transactionLogs;
    private int numOfPutsSinceLastCheckpoint;

    public InMemoryRecoveryLog() {
        this.transactionLogs = new HashMap<>();
        this.numOfPutsSinceLastCheckpoint = 0;
    }

    @Override
    public void put(TransactionLogRecord trxRecord) {
        transactionLogs.put(trxRecord.transactionId, trxRecord);
        ifNeedWriteCheckpoint();
    }

    /**
     * Write a checkpoint to the in-memory log (not needed if you don't need checkpoints).
     */
    public void ifNeedWriteCheckpoint() {
        if (numOfPutsSinceLastCheckpoint >= IN_MEMORY_CHECKPOINT_INTERVAL){
            Map<String, TransactionLogRecord> pendingTransactions = getPendingTransactions();
            transactionLogs.clear();
            transactionLogs.putAll(pendingTransactions);
        }
    }

    /**
     * Retrieve all pending transactions from the in-memory log.
     *
     * @return Map of pending transactions
     */
    private Map<String, TransactionLogRecord> getPendingTransactions() {
        Map<String, TransactionLogRecord> pendingTransactions = new HashMap<>();

        Iterator<Map.Entry<String, TransactionLogRecord>> iterator = transactionLogs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TransactionLogRecord> entry = iterator.next();
            String trxId = entry.getKey();
            TransactionLogRecord trxRecord = entry.getValue();

            if (!trxRecord.isCompleted()) {
                pendingTransactions.put(trxId, trxRecord);
                iterator.remove();
            }
        }

        return pendingTransactions;
    }

    public Map<String, TransactionLogRecord> getAllLogs() {
        return transactionLogs;
    }

    public void clearAllLogs() {
        transactionLogs.clear();
    }

    @Override
    public void close() {

    }
}
