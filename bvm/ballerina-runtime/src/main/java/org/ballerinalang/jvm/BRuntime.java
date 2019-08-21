/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.State;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * External API to be used by the interop users to control Ballerina runtime behavior.
 *
 * @since 1.0.0
 */
public class BRuntime {

    private Scheduler scheduler;

    private BRuntime(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static CompletableFuture<Object> markAsync() {
        Strand strand = Scheduler.getStrand();
        strand.blockedOnExtern = true;
        strand.setState(State.BLOCK_AND_YIELD);
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.whenComplete(new Unblocker(strand));
        return future;
    }

    public static BRuntime getCurrentRuntime() {
        Strand strand = Scheduler.getStrand();
        return new BRuntime(strand.scheduler);
    }

    public void invokeMethod(ObjectValue object, String methodName, Object... args) {
        Consumer func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
        scheduler.schedule(new Object[1], func, null);
    }

    private static class Unblocker implements java.util.function.BiConsumer<Object, Throwable> {

        private Strand strand;

        public Unblocker(Strand strand) {
            this.strand = strand;
        }

        @Override
        public void accept(Object returnValue, Throwable throwable) {
            if (throwable == null) {
                this.strand.setReturnValues(returnValue);
                this.strand.scheduler.unblockStrand(strand);
            }
        }
    }

}
