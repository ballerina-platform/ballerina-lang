/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jvm.values.connector;

import org.ballerinalang.jvm.Scheduler;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.Map;
import java.util.function.Function;

/**
 * {@code Executor} Is the entry point from server connector side to ballerina side. After doing the dispatching and
 * finding the resource, server connector implementations can use this API to invoke Ballerina engine.
 *
 * @since 0.995.0
 */
public class Executor {

    /**
     * This method will execute Ballerina resource in non-blocking manner. It will use Ballerina worker-pool for the
     * execution and will return the connector thread immediately.
     *
     * @param service      to be executed.
     * @param resourceName to be executed.
     * @param callback     to be executed when execution completes.
     * @param properties   to be passed to context.
     * @param args         required for the resource.
     */
    @Deprecated
    public static void submit(ObjectValue service, String resourceName, CallableUnitCallback callback,
                              Map<String, Object> properties, Object... args) {
        submit(null, service, resourceName, callback, properties, args);
    }


    /**
     * This method will execute Ballerina resource in non-blocking manner. It will use Ballerina worker-pool for the
     * execution and will return the connector thread immediately.
     *
     * @param scheduler    available scheduler.
     * @param service      to be executed.
     * @param resourceName to be executed.
     * @param callback     to be executed when execution completes.
     * @param properties   to be passed to context.
     * @param args         required for the resource.
     */
    public static void submit(Scheduler scheduler, ObjectValue service, String resourceName,
                              CallableUnitCallback callback, Map<String, Object> properties, Object... args) {

        //TODO Remove null check once scheduler logic is migrated for WebSocket. Scheduler cannot be null
        if (scheduler == null) {
            scheduler = new Scheduler(4, false);
            scheduler.start();
        }
        Function<Object[], Object> func = objects -> service.call((Strand) objects[0], resourceName, args);
        scheduler.schedule(new Object[1], func, null, callback, properties);
    }

    /**
     * Execution API to execute just a function.
     *
     * @param strand   current strand
     * @param service  to be executed
     * @param resource to be executed
     * @param args     to be passed to invokable unit
     * @return results
     */
    public static Object executeFunction(Strand strand, ObjectValue service, AttachedFunction resource,
                                         Object... args) {
        int requiredArgNo = resource.type.paramTypes.length;
        int providedArgNo = (args.length / 2); // due to additional boolean args being added for each arg
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                                               providedArgNo + ".");
        }
        return service.call(new Strand(strand.scheduler), resource.getName(), args);
    }

    private Executor() {
    }
}
