/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.transactions;

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFunctionPointer;

/**
 * Decorate {@link CallableUnitCallback} to allow notifying {@link TransactionResourceManager}
 * about success or failure of Callable Unit.
 *
 * @since 0.985.0
 */
public class TransactableCallableUnitCallback implements CallableUnitCallback {
    private final CallableUnitCallback callback;
    private final String transactionId;
    private BFunctionPointer transactionOnCommit;
    private BFunctionPointer transactionOnAbort;
    private int transactionBlockId;

    public TransactableCallableUnitCallback(CallableUnitCallback callableUnitCallback, String globalTransactionId) {
        this.callback = callableUnitCallback;
        this.transactionId = globalTransactionId;
    }

    @Override
    public void notifySuccess() {
        TransactionResourceManager.getInstance().notifySuccess(transactionId);
        callback.notifySuccess();
    }

    @Override
    public void notifyFailure(BError error) {
        TransactionResourceManager.getInstance().notifyResourceFailure(transactionId);
        callback.notifyFailure(error);
    }

    public void setTransactionOnCommit(BFunctionPointer transactionOnCommit) {
        this.transactionOnCommit = transactionOnCommit;
    }

    public BFunctionPointer getTransactionOnCommit() {
        return transactionOnCommit;
    }

    public void setTransactionOnAbort(BFunctionPointer transactionOnAbort) {
        this.transactionOnAbort = transactionOnAbort;
    }

    public BFunctionPointer getTransactionOnAbort() {
        return transactionOnAbort;
    }

    public void setTransactionBlockId(int transactionBlockId) {
        this.transactionBlockId = transactionBlockId;
    }

    public int getTransactionBlockId() {
        return transactionBlockId;
    }
}
