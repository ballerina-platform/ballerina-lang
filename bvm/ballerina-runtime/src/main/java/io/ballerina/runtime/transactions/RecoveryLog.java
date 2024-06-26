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

/**
 * The RecoveryLog interface for the recovery logs.
 *
 * @since 2201.9.0
 */
public interface RecoveryLog {

    /**
     * Write a log entry to the recovery log.
     *
     * @param trxRecord the transaction log record
     */
    void put(TransactionLogRecord trxRecord);

    /**
     * Inserts all the transaction log records into the current map.
     *
     * @param trxRecords A map of transaction log records to be inserted
     */
    void putAll(Map<String, TransactionLogRecord> trxRecords);

    /**
     * Close the recovery log file.
     */
    void close();
}
