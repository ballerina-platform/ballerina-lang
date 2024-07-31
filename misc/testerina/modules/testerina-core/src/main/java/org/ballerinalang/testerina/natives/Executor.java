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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

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
     * @param strand   current strand
     * @param classLoader normal classLoader
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object executeFunction(Strand strand, ClassLoader classLoader, String className, String methodName,
                                         Object... paramValues) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            int paramCount = paramValues.length + 1;
            Object[] jvmArgs = new Object[paramCount];
            jvmArgs[0] = strand;
            for (int i = 0, j = 1; i < paramValues.length; i++) {
                jvmArgs[j++] = paramValues[i];
            }
            Method method = getMethod(methodName, clazz);
            try {
                    return method.invoke(null, jvmArgs);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw ErrorCreator.createError(StringUtils.fromString(
                            methodName + " function invocation failed: " + e.getMessage()));
                }

        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw ErrorCreator.createError(StringUtils.fromString("invocation failed: " + e.getMessage()));
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
