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

package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.ValueCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Util functions for async invocations.
 */
public class AsyncUtils {

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
     * @param strandName           Name for newly creating strand which is used to execute the function pointer. This is
     *                             optional and can be null.
     * @param metadata             Meta data of new strand.
     * @param args                 Ballerina function arguments.
     * @param resultHandleFunction Function used to process the result received after execution of function.
     * @param scheduler            The scheduler for invoking functions
     * @return Future Value
     */
    public static FutureValue invokeFunctionPointerAsync(BFunctionPointer<?, ?> func, String strandName,
                                                         StrandMetadata metadata, Object[] args, Function<Object,
            Object> resultHandleFunction, Scheduler scheduler) {
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess(Object result) {
                setReturnValues(resultHandleFunction.apply(getFutureResult()));
            }

            @Override
            public void notifyFailure(BError error) {
                handleRuntimeErrors(error);
            }
        };
        return invokeFunctionPointerAsync(func, Scheduler.getStrand(), strandName, metadata, args, callback, scheduler);
    }

    public static FutureValue invokeFunctionPointerAsync(BFunctionPointer<?, ?> func, Strand parent, String name,
                                                         StrandMetadata metadata, Object[] args,
                                                         AsyncFunctionCallback callback, Scheduler scheduler) {

        blockStrand(parent);
        final FutureValue future = scheduler.createFuture(parent, null, null,
                                                          ((BFunctionType) func.getType()).retType, name, metadata);
        future.callback = callback;
        callback.setFuture(future);
        callback.setStrand(parent);
        return scheduler.scheduleLocal(args, func, parent, future);
    }

    public static void blockStrand(Strand strand) {
        if (!strand.blockedOnExtern) {
            strand.blockedOnExtern = true;
            strand.setState(State.BLOCK_AND_YIELD);
            strand.returnValue = null;
        }
    }

    /**
     * Invoke Function Pointer asynchronously given number of times. This will schedule the function and block the
     * strand. This method can be used with collection of data where we need to invoke the function pointer for each
     * item of the collection.
     *
     * @param func                 Function Pointer to be invoked.
     * @param strandName           Name for newly creating strand which is used to execute the function pointer. This is
     *                             optional and can be null.
     * @param metadata             Meta data of new strand.
     * @param noOfIterations       Number of iterations need to call the function pointer.
     * @param argsSupplier         Supplier provides dynamic arguments to function pointer execution in each iteration.
     * @param futureResultConsumer Consumer used to process the future value received after execution of function.
     *                             Future value result will have the return object of the function pointer.
     * @param returnValueSupplier  Suppler used to set the final return value for the parent function invocation.
     * @param scheduler            The scheduler for invoking functions
     */
    public static void invokeFunctionPointerAsyncIteratively(BFunctionPointer<?, ?> func, String strandName,
                                                             StrandMetadata metadata, int noOfIterations,
                                                             Supplier<Object[]> argsSupplier,
                                                             Consumer<Object> futureResultConsumer,
                                                             Supplier<Object> returnValueSupplier,
                                                             Scheduler scheduler) {

        if (noOfIterations <= 0) {
            return;
        }
        Strand strand = Scheduler.getStrand();
        blockStrand(strand);
        AtomicInteger callCount = new AtomicInteger(0);
        scheduleNextFunction(func, strand, strandName, metadata, noOfIterations, callCount, argsSupplier,
                             futureResultConsumer, returnValueSupplier, scheduler);
    }

    private static void scheduleNextFunction(BFunctionPointer<?, ?> func, Strand strand, String strandName,
                                             StrandMetadata metadata, int noOfIterations,
                                             AtomicInteger callCount, Supplier<Object[]> argsSupplier,
                                             Consumer<Object> futureResultConsumer,
                                             Supplier<Object> returnValueSupplier, Scheduler scheduler) {
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess(Object result) {
                futureResultConsumer.accept(getFutureResult());
                if (callCount.incrementAndGet() != noOfIterations) {
                    scheduleNextFunction(func, strand, strandName, metadata, noOfIterations, callCount, argsSupplier,
                                         futureResultConsumer, returnValueSupplier, scheduler);
                } else {
                    setReturnValues(returnValueSupplier.get());
                }
            }

            @Override
            public void notifyFailure(BError error) {
                handleRuntimeErrors(error);
            }
        };
        invokeFunctionPointerAsync(func, strand, strandName, metadata, argsSupplier.get(), callback, scheduler);
    }

    public static void getArgsWithDefaultValues(Scheduler scheduler, BObject object,
                                                String methodName, Callback callback, Object... args) {
        if (args.length == 0) {
            callback.notifySuccess(args);
            return;
        }
        ObjectType objectType = object.getType();
        Module module = objectType.getPackage();
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module, false));
        MethodType methodType = getMethodType(methodName, objectType);
        if (methodType == null) {
            throw ErrorCreator.createError(StringUtils.fromString("No such method: " + methodName));
        }
        Parameter[] parameters = methodType.getParameters();
        getArgsWithDefaultValues(scheduler, callback, valueCreator, 0, args, parameters, new ArrayList<>());
    }

    private static void getArgsWithDefaultValues(Scheduler scheduler, Callback callback, ValueCreator valueCreator,
                                                 int startArg, Object[] args, Parameter[] parameters,
                                                 List<Object> argsWithDefaultValues) {
        int paramCount = startArg / 2;
        Parameter parameter = parameters[paramCount];
        if (!((boolean) args[startArg + 1]) && parameter.isDefault) {
            // If argument not provided and parameter has default value we call default value function to get the value
            callDefaultValueFunction(scheduler, callback, valueCreator, startArg, args, parameters,
                    argsWithDefaultValues, parameter);
            return;
        }
        // else use user given argument value.
        argsWithDefaultValues.add(args[startArg]);
        getNextDefaultParamValue(scheduler, callback, valueCreator, startArg, args, parameters, argsWithDefaultValues);
    }

    private static void getNextDefaultParamValue(Scheduler scheduler, Callback callback, ValueCreator valueCreator,
                                                 int startArg, Object[] args, Parameter[] parameters,
                                                 List<Object> argsWithDefaultValues) {
        int nextArgCount = startArg + 2;
        if (nextArgCount == args.length) {
            // Once all arguments are processed notify the call back
            callback.notifySuccess(argsWithDefaultValues.toArray());
        } else {
            // Process next parameter with arguments
            getArgsWithDefaultValues(scheduler, callback, valueCreator, nextArgCount, args, parameters,
                    argsWithDefaultValues);
        }
    }

    private static void callDefaultValueFunction(Scheduler scheduler, Callback callback, ValueCreator valueCreator,
                                                 int startArg, Object[] args, Parameter[] parameters,
                                                 List<Object> argsWithDefaultValues,
                                                 Parameter parameter) {
        String funcName = parameter.defaultFunctionName;
        Function<?, ?> defaultFunc = o -> valueCreator.call((Strand) (((Object[]) o)[0]), funcName,
                argsWithDefaultValues.toArray());
        Callback defaultFunctionCallback = new Callback() {
            @Override
            public void notifySuccess(Object result) {
                argsWithDefaultValues.add(result);
                getNextDefaultParamValue(scheduler, callback, valueCreator, startArg, args, parameters,
                        argsWithDefaultValues);
            }
            @Override
            public void notifyFailure(BError error) {
                callback.notifyFailure(error);
            }
        };
        FutureValue future = scheduler.createFuture(null, defaultFunctionCallback, null, null, funcName, null);
        scheduler.schedule(args, defaultFunc, future);
    }

    private static MethodType getMethodType(String methodName, ObjectType objectType) {
        Map<String, MethodType> methodTypesMap = new HashMap<>();
        if (objectType.getTag() == TypeTags.SERVICE_TAG) {
            BServiceType serviceType = (BServiceType) objectType;
            ResourceMethodType[] resourceMethods = serviceType.getResourceMethods();
            for (ResourceMethodType resourceMethodType : resourceMethods) {
                methodTypesMap.put(resourceMethodType.getName(), resourceMethodType);
            }
            RemoteMethodType[] remoteMethodTypes = serviceType.getRemoteMethods();
            for (RemoteMethodType remoteMethodType : remoteMethodTypes) {
                methodTypesMap.put(remoteMethodType.getName(), remoteMethodType);
            }
        }
        MethodType[] objectTypeMethods = objectType.getMethods();
        for (MethodType methodType : objectTypeMethods) {
            methodTypesMap.put(methodType.getName(), methodType);
        }
        return methodTypesMap.get(methodName);
    }

    private static class Unblocker implements java.util.function.BiConsumer<Object, Throwable> {

        private Strand strand;

        public Unblocker(Strand strand) {
            this.strand = strand;
        }

        @Override
        public void accept(Object returnValue, Throwable throwable) {
            if (throwable == null) {
                this.strand.returnValue = returnValue;
                this.strand.scheduler.unblockStrand(strand);
            }
        }
    }
}
