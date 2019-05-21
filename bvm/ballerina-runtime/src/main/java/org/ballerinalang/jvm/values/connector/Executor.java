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
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.Map;
import java.util.concurrent.Executors;

/**
 * {@code Executor} Is the entry point from server connector side to ballerina side. After doing the dispatching and
 * finding the resource, server connector implementations can use this API to invoke Ballerina engine.
 *
 * @since 0.995.0
 */
public class Executor {

    public static void submit(ObjectValue service, String resourceName, CallableUnitCallback callback,
                              Map<String, Object> properties, Object... bValues) {
        //TODO this is temp fix till we get the service.start() API
        Executors.newSingleThreadExecutor().submit(() -> {
            //TODO check the scheduler thread count
            Object returnValues = service.call(new Strand(new Scheduler(4), properties), resourceName, bValues);
            if (returnValues instanceof ErrorValue) {
                callback.notifyFailure((ErrorValue) returnValues);
            } else {
                callback.notifySuccess();
            }
        });
    }

    /**
     * Execution API to execute just a function.
     *
     * @param service  to be executed
     * @param resource to be executed
     * @param args     to be passed to invokable unit
     * @return results
     */
    public static Object executeFunction(ObjectValue service, AttachedFunction resource, Object... args) {
        int requiredArgNo = resource.getParameterType().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                                               providedArgNo + ".");
        }
        //TODO check the need of existing strand here
        return service.call(new Strand(new Scheduler(4)), resource.getName(), args);
    }

    private Executor() {
    }
}
