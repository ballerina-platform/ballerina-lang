package io.ballerina.runtime.transactions;

import java.nio.file.Path;
import java.util.Map;

public class LogManager {
    private final FileRecoveryLog fileRecoveryLog;
    private final InMemoryRecoveryLog inMemoryRecoveryLog;

    public LogManager(String baseFileName, int checkpointInterval, Path recoveryLogDir, boolean deleteOldLogs) {
        this.fileRecoveryLog = new FileRecoveryLog(baseFileName, checkpointInterval, recoveryLogDir, deleteOldLogs);
        this.inMemoryRecoveryLog = new InMemoryRecoveryLog();
        init();
    }

    private void init() {
        // Read pending transactions from the file recovery log and add them to the in-memory log.
        Map<String, TransactionLogRecord> pendingTransactions = fileRecoveryLog.getPendingLogs();
        if (pendingTransactions == null) {
            return;
        }
        for (Map.Entry<String, TransactionLogRecord> entry : pendingTransactions.entrySet()) {
            inMemoryRecoveryLog.put(entry.getValue());
        }
    }

    /**
     * Write a log entry to both the in-memory log and log file.
     *
     * @param trxRecord the transaction log record
     */
    public void put(TransactionLogRecord trxRecord) {
        inMemoryRecoveryLog.put(trxRecord);
        fileRecoveryLog.put(trxRecord);
    }

    public FileRecoveryLog getFileRecoveryLog() {
        return fileRecoveryLog;
    }

    public InMemoryRecoveryLog getInMemoryRecoveryLog() {
        return inMemoryRecoveryLog;
    }

    public Map<String, TransactionLogRecord> getPendingLogs() {
        return inMemoryRecoveryLog.getPendingTransactions();
    }


    public void close() {
        fileRecoveryLog.close();
    }
}
