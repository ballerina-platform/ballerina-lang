/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.transactions;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.BmpStringValue;
import org.ballerinalang.jvm.values.connector.Executor;

import static org.ballerinalang.jvm.transactions.TransactionConstants.COORDINATOR_ABORT_TRANSACTION;
import static org.ballerinalang.jvm.transactions.TransactionConstants.TRANSACTION_BLOCK_CLASS_NAME;
import static org.ballerinalang.jvm.transactions.TransactionConstants.TRANSACTION_MODULE_NAME;
import static org.ballerinalang.jvm.transactions.TransactionConstants.TRANSACTION_PACKAGE_VERSION;
import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Utility methods used in transaction handling.
 *
 * @since 1.0
 */
public class TransactionUtils {

    public static void notifyTransactionAbort(Strand strand, String globalTransactionId, String transactionBlockId) {
        Executor.executeFunction(strand.scheduler, TransactionUtils.class.getClassLoader(), BALLERINA_BUILTIN_PKG_PREFIX,
                TRANSACTION_MODULE_NAME, TRANSACTION_PACKAGE_VERSION, TRANSACTION_BLOCK_CLASS_NAME, COORDINATOR_ABORT_TRANSACTION,
                new BmpStringValue(globalTransactionId), new BmpStringValue(transactionBlockId));
    }

}
