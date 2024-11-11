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

package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BNever;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.BalRuntime;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.FPValue;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.ValueCreator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {

    public final ReentrantLock globalNonIsolatedLock = new ReentrantLock();

    private static final ThreadLocal<StrandHolder> strandHolder = ThreadLocal.withInitial(StrandHolder::new);

    public  final BalRuntime runtime;

    public Scheduler(BalRuntime runtime) {
        this.runtime = runtime;
    }

    public static Strand getStrand() {
        return strandHolder.get().strand;
    }
    public Object callFunction(Module module, String functionName, StrandMetadata metadata, Object... args) {
        Strand parentStrand = Scheduler.getStrand();
        if (parentStrand != null) {
            boolean runnable = parentStrand.isRunnable();
            if (!runnable) {
                parentStrand.resume();
            }
            ValueCreatorAndFunctionType functionType = getGetValueCreatorAndFunctionType(module, functionName);
            Object[] argsWithDefaultValues = getArgsWithDefaultValues(functionType.valueCreator(),
                    functionType.functionType(), parentStrand, args);
            Object result = functionType.valueCreator().call(parentStrand, functionName, argsWithDefaultValues);
            if (!runnable) {
                parentStrand.yield();
            }
            return result;
        }
        if (metadata != null && metadata.isConcurrentSafe()) {
            return AsyncUtils.getFutureResult(
                    startIsolatedFunction(module, functionName, metadata, args).completableFuture);
        }
        return AsyncUtils.getFutureResult(startNonIsolatedFunction(module, functionName, metadata, args)
                .completableFuture);
    }

    public Object callMethod(BObject object, String methodName, StrandMetadata metadata, Object... args) {
        Strand parentStrand = Scheduler.getStrand();
        if (parentStrand != null) {
            boolean runnable = parentStrand.isRunnable();
            if (!runnable) {
                parentStrand.resume();
            }
            ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(object.getOriginalType());
            MethodType methodType = getObjectMethodType(methodName, objectType);
            Object[] argsWithDefaultValues = getArgsWithDefaultValues(objectType, methodType, parentStrand, args);
            Object result = ((ObjectValue) object).call(parentStrand, methodName, argsWithDefaultValues);
            if (!runnable) {
                parentStrand.yield();
            }
            return result;
        }
        if (metadata != null && metadata.isConcurrentSafe()) {
            return AsyncUtils.getFutureResult(startIsolatedMethod(object, methodName, metadata, args)
                    .completableFuture);
        }
        return AsyncUtils.getFutureResult(startNonIsolatedMethod(object, methodName, metadata, args)
                .completableFuture);
    }

    public Object callFP(FPValue fp, StrandMetadata metadata, Object... args) {
        Strand parentStrand = Scheduler.getStrand();
        if (parentStrand != null) {
            boolean runnable = parentStrand.isRunnable();
            if (!runnable) {
                parentStrand.resume();
            }
            FunctionType functionType = (FunctionType) TypeUtils.getImpliedType(TypeUtils.getType(fp));
            Object[] argsWithDefaultValues = getArgsWithDefaultValues(parentStrand, args, functionType);
            Object[] argsWithStrand = getArgsWithStrand(parentStrand, argsWithDefaultValues);
            Object result = fp.function.apply(argsWithStrand);
            if (!runnable) {
                parentStrand.yield();
            }
            return result;
        }
        if (metadata != null && metadata.isConcurrentSafe()) {
            return AsyncUtils.getFutureResult(startIsolatedFP(fp, metadata, args).completableFuture);
        }
        return AsyncUtils.getFutureResult(startNonIsolatedFP(fp, metadata, args).completableFuture);
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen isolated function pointer start call
     */
    public FutureValue startIsolatedWorker(FPValue fp, Strand parentStrand, Type returnType, String strandName,
                                           WorkerChannelMap workerChannelMap, Object[] args) {
        FutureValue future = createFuture(parentStrand, strandName, true, returnType,
                null, workerChannelMap);
        args[0] = future.strand;
        Thread.startVirtualThread(() -> {
            try {
                strandHolder.get().strand = future.strand;
                Object result = fp.function.apply(args);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            }
        }).setName(future.strand.name);
        return future;
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen non isolated function pointer start call
     */
    public FutureValue startNonIsolatedWorker(FPValue fp, Strand parentStrand, Type returnType, String strandName,
                                              WorkerChannelMap workerChannelMap, Object[] args) {
        FutureValue future = createFuture(parentStrand, strandName, false, returnType, null, workerChannelMap);
        args[0] = future.strand;
        Thread.startVirtualThread(() -> {
            try {
                future.strand.resume();
                strandHolder.get().strand = future.strand;
                Object result = fp.function.apply(args);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            } finally {
                future.strand.done();
            }
        }).setName(future.strand.name);
        return future;
    }

    public FutureValue startIsolatedFunction(Module module, String functionName, StrandMetadata metadata,
                                             Object... args) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module.getOrg(),
                module.getName(), module.getMajorVersion(), module.isTestPkg()));
        FunctionType functionType = valueCreator.getFunctionType(functionName);
        FutureValue future = createFutureWithMetadata(null, functionName, true, functionType.getReturnType(),
                metadata, null);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(valueCreator, functionType, future.strand, args);
        Thread.startVirtualThread(() -> {
            try {
                strandHolder.get().strand = future.strand;
                Object result = valueCreator.call(future.strand, functionName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            }
        }).setName(future.strand.name);
        return future;
    }

    private FutureValue startIsolatedMethod(BObject object, String methodName, StrandMetadata metadata,
                                            Object... args) {
        String strandName = getStrandName(object, methodName);
        ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(object.getOriginalType());
        MethodType methodType = getObjectMethodType(methodName, objectType);
        FutureValue future = createFutureWithMetadata(null, strandName, true, methodType.getReturnType(), metadata,
                null);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(objectType, methodType, future.strand, args);
        Thread.startVirtualThread(() -> {
            try {
                strandHolder.get().strand = future.strand;
                Object result = ((ObjectValue) object).call(future.strand, methodName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            }
        }).setName(future.strand.name);
        return future;
    }

    private FutureValue startIsolatedFP(FPValue fp, StrandMetadata metadata, Object... args) {
        BFunctionType functionType = (BFunctionType) fp.getType();
        FutureValue future = createFutureWithMetadata(null, fp.getName(), true, functionType.getReturnType(),
                metadata, null);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(future.strand, args, functionType);
        Object[] argsWithStrand = getArgsWithStrand(future.strand, argsWithDefaultValues);
        Thread.startVirtualThread(() -> {
            try {
                strandHolder.get().strand = future.strand;
                Object result = fp.function.apply(argsWithStrand);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            }
        }).setName(future.strand.name);
        return future;
    }



    private FutureValue startNonIsolatedFunction(Module module, String functionName, StrandMetadata metadata,
                                                 Object... args) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module));
        FunctionType functionType = valueCreator.getFunctionType(functionName);
        FutureValue future =
                createFutureWithMetadata(null, functionName, false, functionType.getReturnType(), metadata, null);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(valueCreator, functionType, future.strand, args);
        Thread.startVirtualThread(() -> {
            try {
                future.strand.resume();
                strandHolder.get().strand = future.strand;
                Object result = valueCreator.call(future.strand, functionName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            } finally {
               future.strand.done();
            }
        }).setName(future.strand.name);
        return future;
    }

    private FutureValue startNonIsolatedMethod(BObject object, String methodName, StrandMetadata metadata,
                                              Object... args) {
        String strandName = getStrandName(object, methodName);
        ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(object.getOriginalType());
        MethodType methodType = getObjectMethodType(methodName, objectType);
        FutureValue future = createFutureWithMetadata(null, strandName, false, methodType.getReturnType(), metadata,
                null);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(objectType, methodType, future.strand, args);
        Thread.startVirtualThread(() -> {
            try {
                future.strand.resume();
                strandHolder.get().strand = future.strand;
                Object result = ((ObjectValue) object).call(future.strand, methodName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            } finally {
               future.strand.done();
            }
        }).setName(future.strand.name);
        return future;
    }

    private FutureValue startNonIsolatedFP(FPValue fp, StrandMetadata metadata, Object... args) {
        BFunctionType functionType = (BFunctionType) fp.getType();
        String strandName = getStrandName("$anon", fp.getName());
        FutureValue future = createFutureWithMetadata(null, strandName, true, functionType.getReturnType(), metadata,
                null);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(future.strand, args, functionType);
        Object[] argsWithStrand = getArgsWithStrand(future.strand, argsWithDefaultValues);
        Thread.startVirtualThread(() -> {
            try {
                future.strand.resume();
                strandHolder.get().strand = future.strand;
                Object result = fp.function.apply(argsWithStrand);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            } finally {
               future.strand.done();
            }
        }).setName(future.strand.name);
        return future;
    }

    private Object[] getArgsWithDefaultValues(Strand parentStrand, Object[] args, FunctionType functionType) {
        Module module = functionType.getPackage();
        if (module == null) {
            return args;
        }
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module));
        return getArgsWithDefaultValues(valueCreator, functionType, parentStrand, args);
    }

    /*
        Only use for tests
    */
    public FutureValue startNonIsolatedWorker(Function<Object[], Object> function, Strand parentStrand, Type returnType,
                                              String strandName, StrandMetadata metadata, Object[] args) {
        FutureValue future = createFutureWithMetadata(parentStrand, strandName, false, returnType, metadata, null);
        Object[] argsWithStrand = getArgsWithStrand(future.strand, args);
        Thread.startVirtualThread(() -> {
            try {
                future.strand.resume();
                strandHolder.get().strand = future.strand;
                Object result = function.apply(argsWithStrand);
                future.completableFuture.complete(result);
            } catch (Throwable t) {
                future.completableFuture.completeExceptionally(ErrorUtils.createErrorFromThrowable(t));
            } finally {
                future.strand.done();
            }
        }).setName(strandName);
        return future;
    }

    private ValueCreatorAndFunctionType getGetValueCreatorAndFunctionType(Module module, String functionName) {

        ValueCreator valueCreator;
        FunctionType functionType;
        try {
            valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module.getOrg(),
                    module.getName(), module.getMajorVersion(), false));
            functionType = valueCreator.getFunctionType(functionName);
        } catch (BError error) {
            valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module.getOrg(),
                    module.getName(), module.getMajorVersion(), true));
            functionType = valueCreator.getFunctionType(functionName);
        }
        return new ValueCreatorAndFunctionType(valueCreator, functionType);
    }

    private record ValueCreatorAndFunctionType(ValueCreator valueCreator, FunctionType functionType) {

    }

    private Object[] getArgsWithDefaultValues(ObjectType objectType, MethodType methodType, Strand strand,
                                              Object... args) {

        Module module = objectType.getPackage();
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module));
        return getArgsWithDefaultValues(valueCreator, methodType, strand, args);
    }

    private Object[] getArgsWithDefaultValues(ValueCreator valueCreator, FunctionType functionType, Strand strand,
                                              Object... args) {
        Parameter[] parameters = functionType.getParameters();
        if (args.length == 0 && parameters.length == 0) {
            return new Object[]{};
        }
        int length = functionType.getRestType() == null ? parameters.length : parameters.length + 1;
        if (length < args.length) {
            length = args.length;
        }
        Object[] argsWithDefaultValues = new Object[length];
        System.arraycopy(args, 0, argsWithDefaultValues, 0, args.length);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isDefault && (args.length <= i || args[i] == BNever.getValue())) {
                Object defaultValue = valueCreator.call(strand, parameter.defaultFunctionName, argsWithDefaultValues);
                argsWithDefaultValues[i] = defaultValue;
            }
        }
        return argsWithDefaultValues;
    }

    public MethodType getObjectMethodType(String methodName, ObjectType objectType) {
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
        MethodType methodType = methodTypesMap.get(methodName);
        if (methodType != null) {
            return methodType;
        }
        throw ErrorCreator.createError(StringUtils.fromString("No such method: " + methodName));
    }

    public FutureValue createFutureWithMetadata(Strand parentStrand, String strandName, boolean isIsolated,
                                                Type constraint,  StrandMetadata metadata,
                                                WorkerChannelMap workerChannelMap) {
        if (metadata != null) {
            return createFuture(parentStrand, strandName, isIsolated, constraint, metadata.properties(),
                    workerChannelMap);
        }
        return createFuture(parentStrand, strandName, isIsolated, constraint, null, workerChannelMap);
    }
    public FutureValue createFuture(Strand parentStrand, String strandName, boolean isIsolated, Type constraint,
                                    Map<String, Object> properties, WorkerChannelMap workerChannelMap) {
        return createFuture(constraint, createStrand(parentStrand, strandName, isIsolated, properties,
                workerChannelMap));
    }

    private Strand createStrand(Strand parentStrand, String strandName, boolean isIsolated,
                                Map<String, Object> properties, WorkerChannelMap workerChannelMap) {
        return new Strand(this, strandName, parentStrand, isIsolated, properties, workerChannelMap,
                parentStrand != null ? parentStrand.currentTrxContext : null);
    }

    private FutureValue createFuture(Type constraint, Strand newStrand) {
        return new FutureValue(newStrand, constraint);
    }

    private static String getStrandName(String functionName, String strandName) {
        if (strandName == null) {
            strandName = functionName;
        }
        return strandName;
    }

    private static String getStrandName(BObject bObject, String methodName) {
        return bObject.getOriginalType().getName() + ":" + methodName;
    }

    private static Object[] getArgsWithStrand(Strand parentStrand, Object[] args) {
        Object[] argsWithStrand = new Object[args.length + 1];
        System.arraycopy(args, 0, argsWithStrand, 1, args.length);
        argsWithStrand[0] = parentStrand;
        return argsWithStrand;
    }
}
