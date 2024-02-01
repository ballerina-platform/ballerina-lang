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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.ballerina.runtime.transactions.TransactionConstants.IN_MEMORY_CHECKPOINT_INTERVAL;

public class InMemoryRecoveryLog implements RecoveryLog {

    private static final Logger log = LoggerFactory.getLogger(InMemoryRecoveryLog.class);
    private Map<String, TransactionLogRecord> transactionLogs;
    private int numOfPutsSinceLastCheckpoint;

    public InMemoryRecoveryLog() {
        this.transactionLogs = new ConcurrentHashMap<>();
        this.numOfPutsSinceLastCheckpoint = 0;
    }

    @Override
    public void put(TransactionLogRecord trxRecord) {
        transactionLogs.put(trxRecord.getCombinedId(), trxRecord);
        ifNeedWriteCheckpoint();
    }

    /**
     * Write a checkpoint to the in-memory log (not needed if you don't need checkpoints).
     */
    public void ifNeedWriteCheckpoint() {
        if (numOfPutsSinceLastCheckpoint >= IN_MEMORY_CHECKPOINT_INTERVAL) {
            Map<String, TransactionLogRecord> pendingTransactions = getFailedTransactions();
            transactionLogs.clear();
            transactionLogs.putAll(pendingTransactions);
        }
    }

    /**
     * Retrieve all pending transactions from the in-memory log.
     *
     * @return Map of pending transactions
     */
    public Map<String, TransactionLogRecord> getFailedTransactions() {
        Map<String, TransactionLogRecord> failedTransactions = new HashMap<>();

        Iterator<Map.Entry<String, TransactionLogRecord>> iterator = transactionLogs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TransactionLogRecord> entry = iterator.next();
            String trxId = entry.getKey();
            TransactionLogRecord trxRecord = entry.getValue();

            if (trxRecord.isCompleted()) {
                iterator.remove();
                continue;
            }
            failedTransactions.put(trxId, trxRecord);
            iterator.remove();
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
