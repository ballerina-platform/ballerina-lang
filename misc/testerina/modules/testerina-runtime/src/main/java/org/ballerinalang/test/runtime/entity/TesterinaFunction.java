/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.entity;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import org.ballerinalang.test.runtime.exceptions.BallerinaTestException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * TesterinaFunction entity class.
 */
public class TesterinaFunction {

    public Scheduler scheduler;

    private String bFunctionName;
    private Class<?> programFile;

    // Annotation info
    private List<String> groups = new ArrayList<>();

    public TesterinaFunction(Class<?> programFile, String bFunctionName, Scheduler scheduler) {
        this.bFunctionName = bFunctionName;
        this.programFile = programFile;
        this.scheduler = scheduler;
    }

    public Object invoke() {
        return runOnSchedule(programFile, bFunctionName, scheduler, new Class[]{Strand.class}, new Object[1]);
    }

    public Object invoke(Class[] types, Object[] args) {
        return runOnSchedule(programFile, bFunctionName, scheduler, types, args);
    }

    /**
     * Invoke a function without running through a strand.
     *
     * @param types of the function parameters
     * @return output
     */
    public Object directInvoke(Class[] types, Object[] args) {
        return run(programFile, bFunctionName, types, args);
    }

    public String getName() {
        return bFunctionName;
    }

    public void setName(String name) {
        this.bFunctionName = name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    private static Object run(Class<?> initClazz, String name, Class[] paramTypes, Object[] args) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, paramTypes);
            return method.invoke(null, args);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new BallerinaTestException("Failed to invoke the function '" +
                                             funcName + " due to " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            return targetException;
        }
    }

    private static Object runOnSchedule(Class<?> initClazz, String name, Scheduler scheduler,
                                        Class[] paramTypes, Object[] params) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, paramTypes);
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return method.invoke(null, objects);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof BError) {
                        return (BError) targetException;
                    }
                    return targetException;
                } catch (IllegalAccessException e) {
                    return new BallerinaTestException("Error while invoking function '" + funcName + "'", e);
                }
            };
            final BFuture out = scheduler.schedule(params, func, null, null, null, PredefinedTypes.TYPE_ANY,
                                                   null, null);
            scheduler.start();
            final Throwable t = out.getPanic();
            if (t != null) {
                return new BallerinaTestException("Error while invoking function '" + funcName + "'", t.getMessage());
            }
            return out.getResult();
        } catch (NoSuchMethodException e) {
            return new BallerinaTestException("Error while invoking function '" + funcName + "'\n" +
                    "If you are using data providers please check if types return from data provider " +
                    "match test function parameter types.", e);
        }
    }

    private static String cleanupFunctionName(String name) {
        return Utils.encodeFunctionIdentifier(name);
    }
}
