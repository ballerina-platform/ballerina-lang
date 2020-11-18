/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.transaction;

import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.transactions.TransactionLocalContext;
import io.ballerina.runtime.transactions.TransactionResourceManager;

/**
 * Extern function transaction:onRollback.
 *
 * @since 2.0.0-preview1
 */
public class OnRollback {

    public static void onRollback(BFunctionPointer bFunctionPointer) {
        TransactionLocalContext transactionLocalContext = Scheduler.getStrand().currentTrxContext;
        TransactionResourceManager transactionResourceManager = TransactionResourceManager.getInstance();
        transactionResourceManager.registerAbortedFunction(transactionLocalContext.getGlobalTransactionId(),
                bFunctionPointer);
    }
}
