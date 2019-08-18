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
package org.ballerinalang.testerina.core.entity;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * TesterinaFunction entity class.
 */
public class TesterinaFunction {

    private String name;
    public Scheduler scheduler;

    public BLangFunction getbFunction() {
        return bFunction;
    }

    public BLangFunction bFunction;
    private Class<?> programFile;
    private boolean runTest = true;

    // Annotation info
    private List<String> groups = new ArrayList<>();

    public TesterinaFunction(Class<?> programFile, BLangFunction bFunction) {
        this.name = bFunction.getName().getValue();
        this.bFunction = bFunction;
        this.programFile = programFile;
    }

    public Object invoke() throws BallerinaException {
        if (scheduler == null) {
            throw new AssertionError("Scheduler is not initialized in " + bFunction.name);
        }
        return runOnSchedule(programFile, bFunction.name, scheduler, new Class[]{Strand.class}, new Object[1]);
    }

    public Object invoke(Class[] types, Object[] args) {
        if (scheduler == null) {
            throw new AssertionError("Scheduler is not initialized in " + bFunction.name);
        }
        return runOnSchedule(programFile, bFunction.name, scheduler, types, args);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public boolean getRunTest() {
        return runTest;
    }

    public void setRunTest() {
        this.runTest = false;
    }

    private static Object runOnSchedule(Class<?> initClazz, BLangIdentifier name, Scheduler scheduler,
                                        Class[] paramTypes, Object[] params) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, paramTypes);
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return method.invoke(null, objects);
                } catch (InvocationTargetException e) {
                    //throw new BallerinaException(e);
                    return e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Error while invoking function '" + funcName + "'", e);
                }
            };
            final FutureValue out = scheduler.schedule(params, func, null, null, null);
            scheduler.start();
            final Throwable t = out.panic;
            final Object result = out.result;
            if (result instanceof ErrorValue) {
                throw new BallerinaException((ErrorValue) result);
            }
            if (t != null) {
                throw new BallerinaException("Error while invoking function '" + funcName + "'", t.getMessage());
            }
            return out.result;
        } catch (NoSuchMethodException e) {
            throw new BallerinaException("Error while invoking function '" + funcName + "'", e);
        }
    }

    private static String cleanupFunctionName(BLangIdentifier name) {
        return name.value.replaceAll("[.:/<>]", "_");
    }

}
