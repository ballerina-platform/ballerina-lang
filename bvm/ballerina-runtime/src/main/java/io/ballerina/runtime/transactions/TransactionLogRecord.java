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
        return transactionId + ":" + transactionBlockId;
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
