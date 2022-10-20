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
package org.ballerinalang.testerina.natives;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * {@code Executor} Is the entry point from server connector side to ballerina side. After doing the dispatching and
 * finding the resource, server connector implementations can use this API to invoke Ballerina engine.
 *
 * @since 0.995.0
 */
public class Executor {

    private Executor() {
    }

    /**
     * This method will invoke Ballerina function in blocking manner.
     *
     * @param scheduler   current scheduler
     * @param strandName  name for newly creating strand which is used to execute the function pointer.
     * @param metaData    meta data of new strand.
     * @param classLoader normal classLoader
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object executeFunction(Scheduler scheduler, String strandName, StrandMetadata metaData,
                                         ClassLoader classLoader, String className, String methodName,
                                         Object... paramValues) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            int paramCount = paramValues.length + 1;
            Object[] jvmArgs = new Object[paramCount];
            jvmArgs[0] = scheduler;
            for (int i = 0, j = 1; i < paramValues.length; i++) {
                jvmArgs[j++] = paramValues[i];
            }
            Method method = getMethod(methodName, clazz);
            Function<Object[], Object> func = args -> {
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BallerinaException(methodName + " function invocation failed: " + e.getMessage());
                }
            };
            CountDownLatch completeFunction = new CountDownLatch(1);
            BFuture futureValue = scheduler.schedule(jvmArgs, func, null, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    completeFunction.countDown();
                }

                @Override
                public void notifyFailure(BError error) {
                    completeFunction.countDown();
                }
            }, new HashMap<>(), PredefinedTypes.TYPE_NULL, strandName, metaData);
            completeFunction.await();
            return futureValue.getResult();
        } catch (NoSuchMethodException | ClassNotFoundException | InterruptedException e) {
            throw new BallerinaException("invocation failed: " + e.getMessage());
        }
    }

    private static Method getMethod(String functionName, Class<?> funcClass) throws NoSuchMethodException {
        Method declaredMethod = Arrays.stream(funcClass.getDeclaredMethods())
                .filter(method -> functionName.equals(method.getName()))
                .findAny()
                .orElse(null);

        if (declaredMethod != null) {
            return declaredMethod;
        } else {
            throw new NoSuchMethodException(functionName + " is not found");
        }
    }

}
