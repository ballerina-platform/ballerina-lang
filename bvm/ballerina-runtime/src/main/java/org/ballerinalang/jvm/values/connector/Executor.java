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

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * {@code Executor} Is the entry point from server connector side to ballerina side. After doing the dispatching and
 * finding the resource, server connector implementations can use this API to invoke Ballerina engine.
 *
 * @since 0.995.0
 */
public class Executor {

    private static final BUnionType OPTIONAL_ERROR_TYPE = new BUnionType(
            new BType[] { BTypes.typeError, BTypes.typeNull },
            TypeFlags.asMask(TypeFlags.NILABLE, TypeFlags.PURETYPE));

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

        Function<Object[], Object> func = objects -> {
            Strand strand = (Strand) objects[0];
            if (ObserveUtils.isObservabilityEnabled() && properties != null &&
                    properties.containsKey(ObservabilityConstants.KEY_OBSERVER_CONTEXT)) {
                strand.observerContext =
                        (ObserverContext) properties.remove(ObservabilityConstants.KEY_OBSERVER_CONTEXT);
            }
            return service.call(strand, resourceName, args);
        };
        scheduler.schedule(new Object[1], func, null, callback, properties, OPTIONAL_ERROR_TYPE);
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

    /**
     * This method will invoke Ballerina function in blocking manner.
     *
     * @param scheduler   current scheduler
     * @param classLoader normal classLoader
     * @param orgName     org which the package belongs to
     * @param packageName package which the class belongs to
     * @param version     version which the class belongs to
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object executeFunction(Scheduler scheduler, ClassLoader classLoader, final String orgName,
                                         String packageName, String version, String className, String methodName,
                                         Object... paramValues) {
        try {
            Class<?> clazz = classLoader.loadClass(orgName + "." + packageName + "." + version.replace(".", "_") +
                                                           "." + className);
            int paramCount = paramValues.length * 2 + 1;
            Class<?>[] jvmParamTypes = new Class[paramCount];
            Object[] jvmArgs = new Object[paramCount];
            jvmParamTypes[0] = Strand.class;
            jvmArgs[0] = scheduler;
            for (int i = 0, j = 1; i < paramValues.length; i++) {
                jvmArgs[j] = paramValues[i];
                jvmParamTypes[j++] = getJvmType(paramValues[i]);
                jvmArgs[j] = true;
                jvmParamTypes[j++] = boolean.class;
            }
            Method method = clazz.getDeclaredMethod(methodName, jvmParamTypes);
            Function<Object[], Object> func = args -> {
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BallerinaException(methodName + " function invocation failed: " + e.getMessage());
                }
            };
            CountDownLatch completeFunction = new CountDownLatch(1);
            FutureValue futureValue = scheduler.schedule(jvmArgs, func, null, new CallableUnitCallback() {
                @Override
                public void notifySuccess() {
                    completeFunction.countDown();
                }

                @Override
                public void notifyFailure(ErrorValue error) {
                    completeFunction.countDown();
                }
            }, new HashMap<>(), BTypes.typeNull);
            completeFunction.await();
            return futureValue.result;
        } catch (NoSuchMethodException | ClassNotFoundException | InterruptedException e) {
            throw new BallerinaException("invocation failed: " + e.getMessage());
        }
    }

    private static Class<?> getJvmType(Object paramValue) {
        if (paramValue instanceof MapValue) {
            return MapValue.class;
        } else if (paramValue instanceof ObjectValue) {
            return ObjectValue.class;
        } else if (paramValue instanceof Boolean) {
            return boolean.class;
        } else if (paramValue instanceof BString) {
            return BString.class;
        } else if (paramValue instanceof Integer) {
            return int.class;
        } else if (paramValue instanceof Float) {
            return double.class;
        } else {
            // This is done temporarily, until blocks are added here for all possible cases.
            throw new RuntimeException("unknown param type: " + paramValue.getClass());
        }
    }

    private Executor() {
    }
}
