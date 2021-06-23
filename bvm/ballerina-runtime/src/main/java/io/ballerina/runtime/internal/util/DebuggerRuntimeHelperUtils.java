/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.util;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * This class contains the set of runtime helper util methods to support debugger expression evaluation.
 * <p>
 * These utils methods must be class-loaded into the program JVM to evaluate
 * <ul>
 *  <li> function
 *  <li> object method
 *  <li> remote call action
 *  <li> wait action
 *  </ul>
 * invocations using the debugger expression evaluation engine.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class DebuggerRuntimeHelperUtils {

    /**
     * Invokes Ballerina object methods in blocking manner.
     *
     * @param bObject    ballerina object instance
     * @param methodName name of the object method to be invoked
     * @param args       object method arguments
     * @return return values
     */
    public static Object invokeObjectMethod(BObject bObject, String methodName, Object... args) {
        try {
            Scheduler scheduler = new Scheduler(1, false);
            Runtime runtime = new Runtime(scheduler);
            CountDownLatch latch = new CountDownLatch(1);
            final Object[] finalResult = new Object[1];

            Object resultFuture = runtime.invokeMethodAsync(bObject, methodName, "evaluator-strand", null,
                    new Callback() {
                        @Override
                        public void notifySuccess(Object result) {
                            latch.countDown();
                            finalResult[0] = result;
                        }

                        @Override
                        public void notifyFailure(BError error) {
                            latch.countDown();
                            finalResult[0] = error;
                        }
                    }, args);

            scheduler.start();
            latch.await();
            return finalResult[0];
        } catch (Exception e) {
            throw new BallerinaException("invocation failed: " + e.getMessage());
        }
    }

    /**
     * Invoke Ballerina functions in blocking manner.
     *
     * @param classLoader normal classLoader
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object invokeFunction(ClassLoader classLoader, String className, String methodName,
                                        Object... paramValues) {
        try {
            Scheduler scheduler = new Scheduler(1, false);
            Class<?> clazz = classLoader.loadClass(className);
            Method method = getMethod(methodName, clazz);

            Function<Object[], Object> func = args -> {
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BallerinaException(methodName + " function invocation failed: " + e.getMessage());
                }
            };

            final Object[] finalResult = new Object[1];
            CountDownLatch latch = new CountDownLatch(1);
            BFuture futureValue = scheduler.schedule(paramValues, func, null, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    latch.countDown();
                    finalResult[0] = result;
                }

                @Override
                public void notifyFailure(BError error) {
                    latch.countDown();
                    finalResult[0] = error;
                }
            }, new HashMap<>(), PredefinedTypes.TYPE_NULL, "evaluation-strand", null);
            scheduler.start();
            latch.await();
            return finalResult[0];
        } catch (Exception e) {
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

    private DebuggerRuntimeHelperUtils() {
    }
}
