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

import org.ballerinalang.bre.TransportTransactionManager;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.model.values.BValue;

/**
 * {@code HttpTransactionManager} manages Http related transactions in ballerina.
 *
 * @since 0.96.0
 */
public class HttpTransactionManager implements TransportTransactionManager {

    private BValue createdContext = null;

    public HttpTransactionManager(BValue balContext) {
        this.createdContext = balContext;
    }

    @Override
    public void commit() {
        BValue[] inputArg = {this.getCreatedContext()};
        BRunUtil.invokeStateful(MicroTransactionCoordinator.getInstance()
                        .getTxnCoordinatorProgramFile(), "transactions.coordinator", "commitTransaction"
                , inputArg, MicroTransactionCoordinator.getInstance().getTxnCoordinatorContext());

    }

    @Override
    public void abort() {
        BValue[] inputArg = {this.getCreatedContext()};
        BRunUtil.invokeStateful(MicroTransactionCoordinator.getInstance()
                        .getTxnCoordinatorProgramFile(), "transactions.coordinator", "abortTransaction"
                , inputArg, MicroTransactionCoordinator.getInstance().getTxnCoordinatorContext());
    }

    @Override
    public BValue getCreatedContext() {
        return createdContext;
    }
}
