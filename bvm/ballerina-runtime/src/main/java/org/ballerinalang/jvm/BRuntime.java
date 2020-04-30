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
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

    /**
     * Gets the instance of ballerina runtime.
     *
     * @return Ballerina runtime instance.
     */
    public static BRuntime getCurrentRuntime() {
        Strand strand = Scheduler.getStrand();
        return new BRuntime(strand.scheduler);
    }

    /**
     * Block the current strand to execute asynchronously.
     *
     * @return Future object to unblock the strand.
     */
    public static CompletableFuture<Object> markAsync() {
        Strand strand = Scheduler.getStrand();
        strand.blockedOnExtern = true;
        strand.setState(State.BLOCK_AND_YIELD);
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.whenComplete(new Unblocker(strand));
        return future;
    }

    /**
     * Invoke Function Pointer asynchronously. This will schedule the function and block the strand.
     *
     * @param func                 Function Pointer to be invoked.
     * @param args                 Ballerina function arguments.
     * @param resultHandleFunction Function used to process the result received after execution of function.
     * @return Future Value
     */
    public FutureValue invokeFunctionPointerAsync(FPValue<?, ?> func, Object[] args,
                                           Function<Object, Object> resultHandleFunction) {
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess() {
                setReturnValues(resultHandleFunction.apply(getFutureResult()));
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                handleRuntimeErrors(error);
            }
        };
        return invokeFunctionPointerAsync(func, args, callback);
    }

    /**
     * Invoke Function Pointer asynchronously. This will schedule the function and block the strand.
     *
     * @param func     Function Pointer to be invoked.
     * @param args     Ballerina function arguments.
     * @param callback Asynchronous call back.
     * @return Future value
     */
    public FutureValue invokeFunctionPointerAsync(FPValue<?, ?> func, Object[] args, AsyncFunctionCallback callback) {

        Strand strand = Scheduler.getStrand();
        return invokeFunctionPointerAsync(func, strand, args, callback);
    }

    /**
     * Invoke Function Pointer asynchronously given number of times. This will schedule the function and block the
     * strand. This method can be used with collection of data where we need to invoke the function pointer for each
     * item of the collection.
     *
     * @param func                 Function Pointer to be invoked.
     * @param noOfIterations Number of iterations need to call the function pointer.
     * @param argsSupplier Suppiler provides dyanamic arguments to function pointer execution in each iteration.
     * @param futureResultConsumer Consumer used to process the future value received after execution of function.
     *                             Future value result will have the return object of the function pointer.
     * @param returnValueSupplier Suppiler used to set the final return value for the parent function invocation.
     */
    public void invokeFunctionPointerAsyncIteratively(FPValue<?, ?> func, int noOfIterations,
                                                      Supplier<Object[]> argsSupplier,
                                                      Consumer<Object> futureResultConsumer,
                                                      Supplier<Object> returnValueSupplier) {
        if (noOfIterations <= 0) {
            return;
        }
        Strand strand = Scheduler.getStrand();
        blockStrand(strand);
        AtomicInteger callCount = new AtomicInteger(0);
        scheduleNextFunction(func, strand, noOfIterations, callCount, argsSupplier,
                             futureResultConsumer, returnValueSupplier);
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

    private void scheduleNextFunction(FPValue<?, ?> func, Strand strand, int noOfIterations,
                                      AtomicInteger callCount, Supplier<Object[]> argsSupplier,
                                      Consumer<Object> futureResultConsumer,
                                      Supplier<Object> returnValueSupplier) {
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess() {
                futureResultConsumer.accept(getFutureResult());
                if (callCount.incrementAndGet() != noOfIterations) {
                    scheduleNextFunction(func, strand, noOfIterations, callCount, argsSupplier,
                                         futureResultConsumer, returnValueSupplier);
                } else {
                    setReturnValues(returnValueSupplier.get());
                }
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                handleRuntimeErrors(error);
            }
        };
        invokeFunctionPointerAsync(func, strand, argsSupplier.get(), callback);
    }

    private FutureValue invokeFunctionPointerAsync(FPValue<?, ?> func, Strand strand,
                                            Object[] args, AsyncFunctionCallback callback) {

        blockStrand(strand);
        final FutureValue future = scheduler.createFuture(strand, null, null, ((BFunctionType) func.getType()).retType);
        future.callback = callback;
        callback.setFuture(future);
        callback.setStrand(strand);
        return scheduler.scheduleLocal(args, func, strand, future);
    }

    private void blockStrand(Strand strand) {
        if (!strand.blockedOnExtern) {
            strand.blockedOnExtern = true;
            strand.setState(State.BLOCK_AND_YIELD);
            strand.setReturnValues(null);
        }
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
