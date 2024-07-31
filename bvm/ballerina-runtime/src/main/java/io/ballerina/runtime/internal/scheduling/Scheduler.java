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
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.BalRuntime;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.FPValue;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.ValueCreator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Strand scheduler for JBallerina.
 *
 * @since 0.995.0
 */
public class Scheduler {

    public final ReentrantLock globalNonIsolatedLock = new ReentrantLock();

    public final RuntimeRegistry runtimeRegistry;

    public final BalRuntime runtime;

    private boolean listenerDeclarationFound;

    private static final ThreadLocal<StrandHolder> strandHolder = ThreadLocal.withInitial(StrandHolder::new);

    private static Strand daemonStrand = null;

    public Scheduler(BalRuntime runtime) {
        this.runtimeRegistry = new RuntimeRegistry(this);
        this.runtime = runtime;
    }

    public Scheduler(Module rootModule) {
        this.runtimeRegistry = new RuntimeRegistry(this);
        this.runtime = new BalRuntime(rootModule);
    }

    public Object call(Module module, String functionName, Strand parentStrand, Object... args) {
        parentStrand.resume();
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module.getOrg(),
                module.getName(), module.getMajorVersion(), module.isTestPkg()));
        return valueCreator.call(parentStrand, functionName, args);
    }

    public Object call(BObject object, String methodName, Strand parentStrand, Object... args) {
        parentStrand.resume();
        return ((ObjectValue) object).call(parentStrand, methodName, args);
    }

    public Object call(FPValue fp, Strand parentStrand, Object... args) {
        parentStrand.resume();
        return fp.function.apply(args);
    }

    public FutureValue startIsolatedWorker(String functionName, Module module, String strandName,
                                           StrandMetadata metadata, Map<String, Object> properties, Object... args) {
        Strand parentStrand = getStrand();
        strandName = getStrandName(functionName, strandName);
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module.getOrg(),
                module.getName(), module.getMajorVersion(), module.isTestPkg()));
        FunctionType functionType = valueCreator.getFunctionType(functionName);
        FutureValue future = createFuture(parentStrand, true, properties, functionType.getReturnType(), strandName,
                metadata);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(functionType, parentStrand, args);
        Thread.startVirtualThread(() -> {
            try {
                Object result = valueCreator.call(future.strand, functionName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Exception e) {
                future.completableFuture.completeExceptionally(e);
            }
        }).setName(strandName);
        return future;
    }

    public FutureValue startIsolatedWorker(BObject object, String methodName, String strandName,
                                           StrandMetadata metadata, Map<String, Object> properties, Object... args) {
        Strand parentStrand = getStrand();
        strandName = getStrandName(object, methodName, strandName);
        ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(object.getOriginalType());
        MethodType methodType = getObjectMethodType(methodName, objectType);
        if (methodType == null) {
            throw ErrorCreator.createError(StringUtils.fromString("No such method: " + methodName));
        }
        FutureValue future = createFuture(parentStrand, true, properties, methodType.getReturnType(), strandName,
                metadata);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(objectType, methodType, parentStrand, args);

        Thread.startVirtualThread(() -> {
            try {
                Object result = ((ObjectValue) object).call(future.strand, methodName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Exception e) {
                future.completableFuture.completeExceptionally(e);
            }
        }).setName(strandName);
        return future;
    }

    public FutureValue startNonIsolatedWorker(String functionName, Module module, String strandName,
                                              StrandMetadata metadata, Map<String, Object> properties, Object... args) {
        Strand parentStrand = getStrand();
        strandName = getStrandName(functionName, strandName);
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module.getOrg(),
                module.getName(), module.getMajorVersion(), module.isTestPkg()));
        FunctionType functionType = valueCreator.getFunctionType(functionName);
        FutureValue future = createFuture(parentStrand, false, properties, functionType.getReturnType(), strandName,
                metadata);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(functionType, parentStrand, args);
        Thread.startVirtualThread(() -> {
            try {
                globalNonIsolatedLock.lock();
                Object result = valueCreator.call(future.strand, functionName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Exception e) {
                future.completableFuture.completeExceptionally(e);
            } finally {
                globalNonIsolatedLock.unlock();
            }
        }).setName(strandName);
        return future;
    }

    public FutureValue startNonIsolatedWorker(BObject object, String methodName, String strandName,
                                              StrandMetadata metadata, Map<String, Object> properties, Object... args) {
        Strand parentStrand = getStrand();
        strandName = getStrandName(object, methodName, strandName);
        ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(object.getOriginalType());
        MethodType methodType = getObjectMethodType(methodName, objectType);
        if (methodType == null) {
            throw ErrorCreator.createError(StringUtils.fromString("No such method: " + methodName));
        }
        FutureValue future = createFuture(parentStrand, false, properties, methodType.getReturnType(),  strandName,
                metadata);
        Object[] argsWithDefaultValues = getArgsWithDefaultValues(objectType, methodType, parentStrand, args);
        Thread.startVirtualThread(() -> {
            try {
                globalNonIsolatedLock.lock();
                Object result = ((ObjectValue) object).call(future.strand, methodName, argsWithDefaultValues);
                future.completableFuture.complete(result);
            } catch (Exception e) {
                future.completableFuture.completeExceptionally(e);
            }
        }).setName(strandName);
        return future;
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen isolated function pointer start call
     */
    public FutureValue startIsolatedWorker(FPValue fp, Strand parentStrand, Type returnType,
                                           String strandName, StrandMetadata metadata, Object[] params) {
        FutureValue future = createFuture(parentStrand, true, null, returnType, strandName, metadata);
        params[0] = future.strand;
        Thread.startVirtualThread(() -> {
            try {
                Object result = fp.function.apply(params);
                future.completableFuture.complete(result);
            } catch (Exception e) {
                future.completableFuture.completeExceptionally(e);
            }
        }).setName(strandName);
        return future;
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen non isolated function pointer start call
     */
    public FutureValue startNonIsolatedWorker(FPValue fp, Strand parentStrand, Type returnType,
                                              String strandName, StrandMetadata metadata, Object[] params) {
        FutureValue future = createFuture(parentStrand, false, null, returnType, strandName, metadata);
        params[0] = future.strand;
        Thread.startVirtualThread(() -> {
            try {
                globalNonIsolatedLock.lock();
                Object result = fp.function.apply(params);
                future.completableFuture.complete(result);
            } catch (Exception e) {
                future.completableFuture.completeExceptionally(e);
            } finally {
                globalNonIsolatedLock.unlock();
            }
        }).setName(strandName);
        return future;
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen isolated function pointer start call on daemon thread
     */
    public Object startIsolatedWorkerOnDaemonThread(FPValue fp, Strand parentStrand, Type returnType,
                                                    String strandName, StrandMetadata metadata,
                                                    Object[] params) {
        params[0] = getStrand(parentStrand, true, null, strandName, metadata);
        return fp.function.apply(params);
    }

    public static void setDaemonStrand(Strand strand) {
        daemonStrand = strand;
    }

    public static Strand getStrand() {
        Strand strand = strandHolder.get().strand;
        if (strand == null) {
            return daemonStrand;
        }
        return strand;
    }

    private Object[] getArgsWithDefaultValues(ObjectType objectType, MethodType methodType, Strand strand,
                                              Object... args) {
        Module module = objectType.getPackage();
        if (args.length == 0 || module == null) {
            return new Object[]{};
        }
        return getArgsWithDefaultValues(strand, args, methodType, module);
    }

    private Object[] getArgsWithDefaultValues(FunctionType functionType, Strand strand, Object... args) {
        Module module = functionType.getPackage();
        if (args.length == 0 || module == null) {
            return new Object[]{};
        }
        return getArgsWithDefaultValues(strand, args, functionType, module);
    }

    private Object[] getArgsWithDefaultValues(Strand strand, Object[] args, FunctionType functionType,
                                              Module module) {
        Parameter[] parameters = functionType.getParameters();
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(module, false));
        Object[] argsWithDefaultValues = new Object[]{parameters.length};
        System.arraycopy(args, 0, argsWithDefaultValues, 0, args.length);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (args.length >= i && parameter.isDefault) {
                Object defaultValue = valueCreator.call(strand, parameter.defaultFunctionName, argsWithDefaultValues);
                argsWithDefaultValues[i] = defaultValue;
            }
        }
        return argsWithDefaultValues;
    }

    private MethodType getObjectMethodType(String methodName, ObjectType objectType) {
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

    public FutureValue createFuture(Strand parentStrand, boolean isIsolated, Map<String, Object> properties,
                                    Type constraint, String name, StrandMetadata metadata) {
        Strand newStrand = getStrand(parentStrand, isIsolated, properties, name, metadata);
        return createFuture(constraint, newStrand);
    }

    private Strand getStrand(Strand parentStrand, boolean isIsolated, Map<String, Object> properties,
                             String name, StrandMetadata metadata) {
        return new Strand(name, metadata, this, parentStrand, isIsolated, properties,
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

    private static String getStrandName(BObject bObject, String methodName, String strandName) {
        if (strandName == null) {
            strandName = bObject.getOriginalType().getName() + ":" + methodName;
        }
        return strandName;
    }

    public void poison() {
    }

    public void setListenerDeclarationFound(boolean listenerDeclarationFound) {
        this.listenerDeclarationFound = listenerDeclarationFound;
    }

    public boolean isListenerDeclarationFound() {
        return listenerDeclarationFound;
    }

    public RuntimeRegistry getRuntimeRegistry() {
        return runtimeRegistry;
    }

    public void gracefulExit() {

    }
}
