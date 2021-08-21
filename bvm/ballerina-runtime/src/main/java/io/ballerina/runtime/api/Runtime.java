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

package io.ballerina.runtime.api;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.FutureValue;

import java.util.Map;
import java.util.function.Function;

import static io.ballerina.runtime.api.TypeTags.SERVICE_TAG;

/**
 * External API to be used by the interop users to control Ballerina runtime behavior.
 *
 * @since 1.0.0
 */
public class Runtime {

    private Scheduler scheduler;

    Runtime(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Gets the instance of ballerina runtime.
     *
     * @return Ballerina runtime instance.
     * @deprecated use {@link Environment#getRuntime()} instead.
     */
    @Deprecated
    public static Runtime getCurrentRuntime() {
        Strand strand = Scheduler.getStrand();
        return new Runtime(strand.scheduler);
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     *
     * @param object           Object Value.
     * @param methodName       Name of the method.
     * @param strandName       Name for newly creating strand which is used to execute the function pointer. This is
     *                         optional and can be null.
     * @param metadata         Meta data of new strand.
     * @param callback         Callback which will get notify once method execution done.
     * @param callConcurrently Ensures method can be invoked concurrently.
     * @param properties       Set of properties for strand
     * @param returnType       Expected return type of this method
     * @param args             Ballerina function arguments.
     * @return {@link FutureValue} containing return value of executing this method.
     */
    public BFuture invokeMethodAsync(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                     boolean callConcurrently, Callback callback, Map<String, Object> properties,
                                     Type returnType, Object... args) {
        try {
            if (object == null) {
                throw new IllegalArgumentException("object cannot be null");
            }
            if (methodName == null) {
                throw new IllegalArgumentException("method name cannot be null");
            }
            Function<?, ?> func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
            if (callConcurrently) {
                return scheduler.schedule(new Object[1], func, null, callback, properties, returnType, strandName,
                                          metadata);
            } else {
                return scheduler.scheduleToObjectGroup(object, new Object[1], func, null, callback, properties,
                                                       returnType, strandName, metadata);
            }
        } catch (Throwable e) {
            callback.notifyFailure(ErrorCreator.createError(StringUtils.fromString(e.getMessage())));
        }
        return null;
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly creating strand which is used to execute the function pointer. This is optional
     *                   and can be null.
     * @param metadata   Meta data of new strand.
     * @param callback   Callback which will get notify once method execution done.
     * @param args       Ballerina function arguments.
     * @return the result of the function invocation
     * @deprecated Use {@link #invokeMethodAsync(BObject, String, String, StrandMetadata, boolean,
     * Callback, Map, Type, Object...)} providing isolation of method call.
     */
    public Object invokeMethodAsync(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                    Callback callback, Object... args) {
        return invokeMethodAsync(object, methodName, strandName, metadata, callback, null,
                                 PredefinedTypes.TYPE_NULL, args);
    }

    /**
     * Invoke Object method asynchronously. This will schedule the function and block the strand.
     *
     * @param object     Object Value.
     * @param methodName Name of the method.
     * @param strandName Name for newly creating strand which is used to execute the function pointer. This is
     *                   optional and can be null.
     * @param metadata   Meta data of new strand.
     * @param callback   Callback which will get notify once method execution done.
     * @param properties Set of properties for strand
     * @param returnType Expected return type of this method
     * @param args       Ballerina function arguments.
     * @return {@link FutureValue} containing return value of executing this method.
     * @deprecated Use {@link #invokeMethodAsync(BObject, String, String, StrandMetadata, boolean,
     * Callback, Map, Type, Object...)} providing isolation of method call.
     */
    public BFuture invokeMethodAsync(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                     Callback callback, Map<String, Object> properties,
                                     Type returnType, Object... args) {
        try {
            if (object == null) {
                throw new IllegalArgumentException("object cannot be null");
            }
            if (methodName == null) {
                throw new IllegalArgumentException("method name cannot be null");
            }
            boolean isIsolated = isIsolated(object.getType(), methodName);
            Function<?, ?> func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
            if (isIsolated) {
                return scheduler.schedule(new Object[1], func, null, callback, properties, returnType, strandName,
                                          metadata);
            } else {
                return scheduler.scheduleToObjectGroup(object, new Object[1], func, null, callback, properties,
                                                       returnType, strandName, metadata);
            }
        } catch (Throwable e) {
            callback.notifyFailure(ErrorCreator.createError(StringUtils.fromString(e.getMessage())));
        }
        return null;
    }

    private boolean isIsolated(ObjectType objectType, String methodName) {
        if (!objectType.isIsolated()) {
            return false;
        }
        for (MethodType method : objectType.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method.isIsolated();
            }
        }
        if (objectType.getTag() == SERVICE_TAG) {
            for (ResourceMethodType method : ((BServiceType) objectType).getResourceMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.isIsolated();
                }
            }
        }
        throw new IllegalArgumentException("No such method: " + methodName);
    }

    public void registerListener(BObject listener) {
        scheduler.getListenerRegistry().registerListener(listener);
    }

    public void deregisterListener(BObject listener) {
        scheduler.getListenerRegistry().deregisterListener(listener);
    }
}
