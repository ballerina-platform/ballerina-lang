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

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.State;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.AsyncFunctionCallBack;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    /**
     * Invoke Function Pointer synchronously. This method will only work for function pointers with synchronous code.
     * Please refer @scheduleFunctionPointer method for schedule function pointer asynchronously.
     *
     * @param func Function Pointer to be invoked.
     * @param args Ballerina function arguments.
     * @param <R>  Return Type.
     * @return Invoked function result.
     */
    public <R> R invokeMethodSync(FPValue<Object, R> func, Object[] args) {
        return func.getFunction().apply(args);
    }

    /**
     * Invoke Function Pointer asynchronously. This will schedule the function and block the strand.
     *
     * @param func   Function Pointer to be invoked.
     * @param strand Strand passed to function.
     * @param args   Ballerina function arguments.
     */
    public void invokeFunctionPointerAsync(FPValue<?, ?> func, Strand strand, Object[] args) {
        AtomicReference<Object> returnVal = new AtomicReference<>();
        invokeFunctionPointerAsync(func, strand, args, future -> returnVal.set(future.result),
                                   () -> true, returnVal::get);
    }

    /**
     * Invoke Function Pointer asynchronously. This will schedule the function and block the strand.
     *
     * @param func                   Function Pointer to be invoked.
     * @param strand                 Strand passed to function.
     * @param args                   Ballerina function arguments.
     * @param futureResultConsumer   Consumer used to process the future value received after execution of function.
     *                               Future value result will have the return object of the function pointer.
     * @param strandUnblockCondition Suppiler provides condition for unblock the strand and resume the execution.
     * @param resultSupplier         Suppiler used to set the final return value for the parent function invocation.
     */
    public void invokeFunctionPointerAsync(FPValue<?, ?> func, Strand strand, Object[] args,
                                           Consumer<FutureValue> futureResultConsumer,
                                           Supplier<Boolean> strandUnblockCondition,
                                           Supplier<Object> resultSupplier) {
        AsyncFunctionCallBack callback = new AsyncFunctionCallBack(strand, futureResultConsumer,
                                                                   strandUnblockCondition, resultSupplier);
        if (!strand.blockedOnExtern) {
            strand.blockedOnExtern = true;
            strand.setState(State.BLOCK_AND_YIELD);
            strand.setReturnValues(null);
        }
        FutureValue future = scheduler.createFuture(strand, callback, null, ((BFunctionType) func.getType()).retType);
        callback.setFuture(future);
        strand.scheduler.scheduleLocal(args, func, strand, future);
    }

    /**
     * Invoke Function Pointer asynchronously. This will schedule the function and block the strand.
     *
     * @param func                 Function Pointer to be invoked.
     * @param strand               Strand passed to function.
     * @param collectionSize       Used to decide the no of iterations need to schedule the function pointer.
     * @param argsFunction         Used to computer the args for each iteration.
     * @param futureResultConsumer Consumer used to process the future value received after execution of function.
     *                             Future value result will have the return object of the function pointer.
     * @param returnValueSupplier  Suppiler used to set the final return value for the parent function invocation.
     */
    public void invokeFunctionPointerAsyncIteratively(FPValue<?, ?> func, Strand strand, int collectionSize,
                                                      Function<Integer, Object[]> argsFunction,
                                                      BiConsumer<Integer, FutureValue> futureResultConsumer,
                                                      Supplier<Object> returnValueSupplier) {
        if (collectionSize <= 0) {
            return;
        }
        AtomicInteger callCount = new AtomicInteger(0);
        scheduleNextFunction(func, strand, argsFunction, callCount, 0, collectionSize, futureResultConsumer,
                             returnValueSupplier);
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param args       Ballerina function arguments.
     */
    public void invokeMethodAsync(ObjectValue object, String methodName, Object... args) {
        Function<?, ?> func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
        scheduler.schedule(new Object[1], func, null, null);
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param callback   Callback which will get notify once method execution done.
     * @param args       Ballerina function arguments.
     */
    public void invokeMethodAsync(ObjectValue object, String methodName,
                                  CallableUnitCallback callback, Object... args) {
        Function<?, ?> func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
        scheduler.schedule(new Object[1], func, null, callback);
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param callback   Callback which will get notify once method execution done.
     * @param properties Set of properties for strand
     * @param args       Ballerina function arguments.
     */
    public void invokeMethodAsync(ObjectValue object, String methodName,
                                  CallableUnitCallback callback, Map<String, Object> properties, Object... args) {
        Function<Object[], Object> func = objects -> {
            Strand strand = (Strand) objects[0];
            if (ObserveUtils.isObservabilityEnabled() && properties != null &&
                    properties.containsKey(ObservabilityConstants.KEY_OBSERVER_CONTEXT)) {
                strand.observerContext =
                        (ObserverContext) properties.remove(ObservabilityConstants.KEY_OBSERVER_CONTEXT);
            }
            return object.call(strand, methodName, args);
        };
        scheduler.schedule(new Object[1], func, null, callback, properties, BTypes.typeNull);
    }

    /**
     * Invoke Object method synchronously. This will block the thread.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param args       Ballerina function arguments.
     */
    public void invokeMethodSync(ObjectValue object, String methodName, Object... args) {
        Function<?, ?> func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
        Semaphore semaphore = new Semaphore(0);
        final ErrorValue[] errorValue = new ErrorValue[1];
        scheduler.schedule(new Object[1], func, null, new CallableUnitCallback() {
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
    }

    /**
     * Invoke Ballerina function and get the result.
     *
     * @param object     Ballerina object in which the function is defined.
     * @param methodName Ballerina function name, to invoke.
     * @param timeout    Timeout in milliseconds to wait until acquiring the semaphore.
     * @param args       Ballerina function arguments.
     * @return Ballerina function invoke result.
     */
    public Object getSyncMethodInvokeResult(ObjectValue object, String methodName, int timeout, Object... args) {
        Function<?, ?> func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
        Semaphore semaphore = new Semaphore(0);
        final ErrorValue[] errorValue = new ErrorValue[1];
        // Add 1 more element to keep null for add the strand later.
        Object[] params = new Object[]{null, args};
        FutureValue futureValue = scheduler.schedule(params, func, null, new CallableUnitCallback() {
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
            semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // Ignore
        }
        if (errorValue[0] != null) {
            throw errorValue[0];
        }
        return futureValue.result;
    }

    private void scheduleNextFunction(FPValue<?, ?> func, Strand strand, Function<Integer, Object[]> argsFunction,
                                      AtomicInteger callCount, int nextIndex, int dataSetSize,
                                      BiConsumer<Integer, FutureValue> futureResultConsumer,
                                      Supplier<Object> returnValueSupplier) {
        invokeFunctionPointerAsync(func, strand, argsFunction.apply(nextIndex), future -> {
            futureResultConsumer.accept(nextIndex, future);
            if (callCount.incrementAndGet() != dataSetSize) {
                scheduleNextFunction(func, strand, argsFunction, callCount, nextIndex + 1, dataSetSize,
                                     futureResultConsumer, returnValueSupplier);
            }
        }, () -> callCount.get() == dataSetSize, returnValueSupplier);
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
