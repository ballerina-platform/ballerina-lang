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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.ballerina.runtime.transactions.TransactionConstants.IN_MEMORY_CHECKPOINT_INTERVAL;

/**
 * {@code InMemoryRecoveryLog} the in-memory recovery log for transaction recovery.
 *
 * @since 2201.9.0
 */
public final class InMemoryRecoveryLog implements RecoveryLog {

    private final Map<String, TransactionLogRecord> transactionLogs = new ConcurrentHashMap<>();
    private int numOfPutsSinceLastCheckpoint = 0;
    private static InMemoryRecoveryLog instance;

    private InMemoryRecoveryLog() {
    }

    public static InMemoryRecoveryLog getInstance() {
        if (instance == null) {
            instance = new InMemoryRecoveryLog();
        }
        return instance;
    }

    @Override
    public void put(TransactionLogRecord trxRecord) {
        transactionLogs.put(trxRecord.getCombinedId(), trxRecord);
        writeCheckpointIfNeeded();
        numOfPutsSinceLastCheckpoint++;
    }

    @Override
    public void putAll(Map<String, TransactionLogRecord> trxRecords) {
        transactionLogs.putAll(trxRecords);
    }

    /**
     * Write a checkpoint to the in-memory log (not needed if you don't need checkpoints).
     */
    private void writeCheckpointIfNeeded() {
        if (numOfPutsSinceLastCheckpoint >= IN_MEMORY_CHECKPOINT_INTERVAL) {
            Map<String, TransactionLogRecord> pendingTransactions = getFailedTransactions();
            clearAllLogs();
            transactionLogs.putAll(pendingTransactions);
        }
    }

    /**
     * Retrieve all pending transactions from the in-memory log.
     *
     * @return Map of pending transactions
     */
    public Map<String, TransactionLogRecord> getFailedTransactions() {
        Map<String, TransactionLogRecord> failedTransactions = new ConcurrentHashMap<>();
        synchronized (transactionLogs) {
            for (Map.Entry<String, TransactionLogRecord> entry : transactionLogs.entrySet()) {
                String trxId = entry.getKey();
                TransactionLogRecord trxRecord = entry.getValue();
                if (!trxRecord.isCompleted()) {
                    failedTransactions.put(trxId, trxRecord);
                }
            }
        }
        return failedTransactions;
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
