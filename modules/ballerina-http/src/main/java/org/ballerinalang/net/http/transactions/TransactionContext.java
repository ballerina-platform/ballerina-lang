/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.transactions;

import org.ballerinalang.bre.MicroTransactionContext;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.model.values.BValue;

import static java.lang.Thread.sleep;

/**
 * {@code TransactionContext} represents micro-transaction context which returned by coordinator.
 *
 * @since 0.96.0
 */
public class TransactionContext implements MicroTransactionContext{

    private BValue balContext = null;

    public TransactionContext(BValue balContext) {
        this.balContext = balContext;
    }

    @Override
    public void commit() {
        BValue[] inputArg = {this.getBalContext()};
        BValue[] resultValues = BRunUtil.invokeStateful(MicroTransactionManager.getInstance()
                        .getTxnCoordinatorProgFile(), "transactions.coordinator", "commitTransaction"
                , inputArg, MicroTransactionManager.getInstance().getBalContext());
    }

    @Override
    public void abort() {
        BValue[] inputArg = {this.getBalContext()};
        BValue[] resultValues = BRunUtil.invokeStateful(MicroTransactionManager.getInstance()
                        .getTxnCoordinatorProgFile(), "transactions.coordinator", "abortTransaction"
                , inputArg, MicroTransactionManager.getInstance().getBalContext());
    }

    public BValue getBalContext() {
        return balContext;
    }
}
