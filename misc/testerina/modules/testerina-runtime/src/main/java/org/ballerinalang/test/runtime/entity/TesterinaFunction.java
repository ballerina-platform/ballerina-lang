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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TesterinaFunction entity class.
 */
public class TesterinaFunction {

    public Scheduler scheduler;

    private String bFunctionName;
    private Class<?> programFile;
    private static final Pattern JVM_RESERVED_CHAR_SET = Pattern.compile("[\\.:/<>]");

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
    public Object directInvoke(Class[] types) {
        return run(programFile, bFunctionName, scheduler, types);
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

    private static Object run(Class<?> initClazz, String name, Scheduler scheduler,
                              Class[] paramTypes) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, paramTypes);
            return method.invoke(null, new Object[]{});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new BallerinaException("Failed to invoke the function '" +
                                                     funcName + " due to " + e.getMessage());
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
                    return e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Error while invoking function '" + funcName + "'", e);
                }
            };
            final BFuture out = scheduler.schedule(params, func, null, null, null, PredefinedTypes.TYPE_ANY,
                                                   null, null);
            scheduler.start();
            final Throwable t = out.getPanic();
            final Object result = out.getResult();
            if (result instanceof BError) {
                throw new BallerinaException((BError) result);
            }
            if (result instanceof Exception) {
                throw new BallerinaException((Exception) result);
            }
            if (t != null) {
                throw new BallerinaException("Error while invoking function '" + funcName + "'", t.getMessage());
            }
            return out.getResult();
        } catch (NoSuchMethodException e) {
            throw new BallerinaException("Error while invoking function '" + funcName + "'\n" +
                    "If you are using data providers please check if types return from data provider " +
                    "match test function parameter types.", e);
        }
    }

    private static String cleanupFunctionName(String name) {
        Matcher matcher = JVM_RESERVED_CHAR_SET.matcher(name);
        return matcher.find() ? "$" + matcher.replaceAll("_") : name;
    }
}
