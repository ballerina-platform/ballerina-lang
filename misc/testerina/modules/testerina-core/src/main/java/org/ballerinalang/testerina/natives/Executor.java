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
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.ObjectValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
     * @param orgName     org which the package belongs to
     * @param packageName package which the class belongs to
     * @param version     version which the class belongs to
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object executeFunction(Scheduler scheduler, String strandName, StrandMetadata metaData,
                                         ClassLoader classLoader, final String orgName,
                                         String packageName, String version, String className, String methodName,
                                         Object... paramValues) {
        try {
            Class<?> clazz = classLoader.loadClass(
                    orgName + "." + packageName + "." + version.replace(".", "_") + "." + className);
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
            BFuture futureValue = scheduler.schedule(jvmArgs, func, null, new Callback() {
                @Override
                public void notifySuccess() {
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

    private static Class<?> getJvmType(Object paramValue) {
        if (paramValue instanceof MapValue) {
            return MapValue.class;
        } else if (paramValue instanceof ObjectValue) {
            return ObjectValue.class;
        } else if (paramValue instanceof Boolean) {
            return boolean.class;
        } else if (paramValue instanceof BString) {
            return BString.class;
        } else if (paramValue instanceof String) {
            return String.class;
        } else if (paramValue instanceof Integer) {
            return int.class;
        } else if (paramValue instanceof Long) {
            return long.class;
        } else if (paramValue instanceof Double) {
            return double.class;
        } else if (paramValue instanceof Float) {
            return double.class;
        } else if (paramValue instanceof ArrayValueImpl) {
            return ArrayValue.class;
        } else {
            // This is done temporarily, until blocks are added here for all possible cases.
            throw new RuntimeException("unknown param type: " + paramValue.getClass());
        }
    }
}
