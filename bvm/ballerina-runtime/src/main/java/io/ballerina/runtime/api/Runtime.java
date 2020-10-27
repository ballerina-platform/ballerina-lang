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
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.observability.ObservabilityConstants;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;

import java.util.Map;
import java.util.function.Function;

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
     * @deprecated use {@link Environment#getRuntime()} instead.
     * @return Ballerina runtime instance.
     */
    @Deprecated
    public static Runtime getCurrentRuntime() {
        Strand strand = Scheduler.getStrand();
        return new Runtime(strand.scheduler);
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
      */
     public Object invokeMethodAsync(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                     Callback callback, Object... args) {
         Function<?, ?> func = o -> object.call((Strand) (((Object[]) o)[0]), methodName, args);
         return scheduler.schedule(new Object[1], func, null, callback, strandName, metadata).result;
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
     * @return           {@link FutureValue} containing return value of executing this method.
     */
    public FutureValue invokeMethodAsync(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                         Callback callback, Map<String, Object> properties,
                                         BType returnType, Object... args) {
        Function<Object[], Object> func = objects -> {
            Strand strand = (Strand) objects[0];
            if (ObserveUtils.isObservabilityEnabled() && properties != null &&
                    properties.containsKey(ObservabilityConstants.KEY_OBSERVER_CONTEXT)) {
                strand.observerContext =
                        (ObserverContext) properties.remove(ObservabilityConstants.KEY_OBSERVER_CONTEXT);
            }
            return object.call(strand, methodName, args);
        };
        return scheduler.schedule(new Object[1], func, null, callback, properties, returnType, strandName,
                metadata);
    }

    /**
     * Get a value stored in a stored resource of a service object.
     *
     * @param object       Service object containing the resource field.
     * @param resourceName Name of the resource field.
     * @return             Value stored in the field.
     */
    public Object getStoredResource(BObject object, String resourceName) {
        return object.get(BStringUtils.fromString(resourceName));
    }

    /**
     * Get a value stored in a hierarchical stored resource of a service object.
     *
     * @param object        Service object containing the resource field.
     * @param resourceNames Names of each step of the hierarchy.
     * @return              Value stored in the field.
     */
    public Object getStoredResource(BObject object, String[] resourceNames) {
        BObject parent = object;
        Object storedResource = null;
        for (int i = 0; i < resourceNames.length; i++) {
            String name = resourceNames[i];
            storedResource = getStoredResource(parent, name);

            if (isServiceResource(storedResource)) {
                parent = (BObject) storedResource;
            } else if (i + 1 < resourceNames.length) {
                throw new BLangRuntimeException("Invalid hierarchical resource: " + name);
            }
        }
        return storedResource;
    }

    private boolean isServiceResource(Object storedResource) {
        return (storedResource instanceof BObject)
                && ((BObject) storedResource).getType().getTag() == TypeTags.SERVICE_TAG;
    }

}
