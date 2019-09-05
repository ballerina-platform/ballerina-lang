/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jvm.streams;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * The {@link DefaultStreamSubscription} represents a stream subscription in Ballerina.
 *
 * @since 0.995.0
 */
public class DefaultStreamSubscription extends StreamSubscription {

    private StreamValue stream;
    private FPValue<Object[], Object> functionPointer;
    private Executor executor = Executors.newSingleThreadExecutor();

    DefaultStreamSubscription(StreamValue stream, FPValue<Object[], Object> functionPointer,
                              StreamSubscriptionManager streamSubscriptionManager) {
        super(streamSubscriptionManager);
        this.stream = stream;
        this.functionPointer = functionPointer;
    }

    public void execute(Object[] fpParams) {
        executor.execute(() -> {
            Strand strand = (Strand) fpParams[0];
            Semaphore semaphore = new Semaphore(0);
            final ErrorValue[] errorValue = new ErrorValue[1];
            strand.scheduler.schedule(fpParams, functionPointer.getConsumer(), strand, new CallableUnitCallback() {
                @Override
                public void notifySuccess() {
                    semaphore.release();
                }

                @Override
                public void notifyFailure(ErrorValue error) {
                    errorValue[0] = error;
                    semaphore.release();
                }
            });
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                // Ignore
            }
            if (errorValue[0] != null) {
                throw errorValue[0];
            }
        });
    }

    public StreamValue getStream() {
        return stream;
    }
}
