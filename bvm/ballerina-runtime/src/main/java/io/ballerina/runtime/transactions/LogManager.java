/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org).
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.transactions;

import java.nio.file.Path;
import java.util.Map;

/**
 * The log manager is responsible for managing the transaction logs.
 *
 * @since 2201.9.0
 */
public class LogManager {

    private final FileRecoveryLog fileRecoveryLog;
    private final InMemoryRecoveryLog inMemoryRecoveryLog;
    private static LogManager instance;

    /**
     * Create a log manager with the given base file name, checkpoint interval, recovery log directory, and delete old
     * logs flag.
     *
     * @param baseFileName       the base file name
     * @param checkpointInterval the checkpoint interval
     * @param recoveryLogDir     the recovery log directory
     * @param deleteOldLogs      the delete old logs flag
     */
    private LogManager(String baseFileName, int checkpointInterval, Path recoveryLogDir, boolean deleteOldLogs) {
        this.fileRecoveryLog =
                FileRecoveryLog.getInstance(baseFileName, checkpointInterval, recoveryLogDir, deleteOldLogs);
        this.inMemoryRecoveryLog = InMemoryRecoveryLog.getInstance();
        init();
    }

    public static LogManager getInstance(String baseFileName, int checkpointInterval, Path recoveryLogDir,
                                         boolean deleteOldLogs) {
        if (instance == null) {
            instance = new LogManager(baseFileName, checkpointInterval, recoveryLogDir, deleteOldLogs);
        }
        return instance;
    }

    private void init() {
        // Read pending transactions from the file recovery log and add them to the in-memory log.
        Map<String, TransactionLogRecord> pendingTransactions = fileRecoveryLog.getPendingLogs();
        inMemoryRecoveryLog.putAll(pendingTransactions);
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

    /**
     * Get the failed transaction logs from the in-memory log.
     *
     * @return the failed transaction logs
     */
    public Map<String, TransactionLogRecord> getFailedTransactionLogs() {
        return inMemoryRecoveryLog.getFailedTransactions();
    }

    /**
     * Close the file recovery log releasing the file lock.
     */
    public void close() {
        fileRecoveryLog.close();
        inMemoryRecoveryLog.close();
    }
}
