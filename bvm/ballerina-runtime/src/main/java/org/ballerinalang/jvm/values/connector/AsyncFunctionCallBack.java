/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm.values.connector;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.RuntimeUtils;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The callback implementation to handle non-blocking native function behaviour.
 *
 * @since 1.3.0
 */

public class AsyncFunctionCallBack implements CallableUnitCallback {

    private FutureValue future;
    private Strand strand;
    private Consumer<FutureValue> futureResultConsumer;
    private Supplier<Boolean> strandUnblockCondition;
    private Supplier<Object> resultSupplier;

    public AsyncFunctionCallBack(Strand strand,
                                 Consumer<FutureValue> futureResultConsumer,
                                 Supplier<Boolean> strandUnblockCondition,
                                 Supplier<Object> returnValueSupplier) {
        this.strand = strand;
        this.futureResultConsumer = futureResultConsumer;
        this.strandUnblockCondition = strandUnblockCondition;
        this.resultSupplier = returnValueSupplier;
    }

    public void setFuture(FutureValue future) {
        this.future = future;
    }

    public void setResultSupplier(Supplier<Object> resultSupplier) {
        this.resultSupplier = resultSupplier;
    }

    @Override
    public void notifySuccess() {
        futureResultConsumer.accept(future);
        if (strandUnblockCondition.get()) {
            strand.setReturnValues(resultSupplier.get());
            strand.scheduler.unblockStrand(strand);
        }
    }

    @Override
    public void notifyFailure(ErrorValue error) {
        RuntimeUtils.handleRuntimeErrors(error);
        strand.setReturnValues(error);
        strand.scheduler.unblockStrand(strand);
    }
}
