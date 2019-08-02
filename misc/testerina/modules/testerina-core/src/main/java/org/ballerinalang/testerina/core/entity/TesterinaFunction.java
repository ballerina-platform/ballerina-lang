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
import org.ballerinalang.model.values.BValue;
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
    private Type type;

    public BLangFunction getbFunction() {
        return bFunction;
    }

    private BLangFunction bFunction;
    private Class<?> programFile;
    private boolean runTest = true;

    // Annotation info
    private List<String> groups = new ArrayList<>();

    static final String PREFIX_TEST = "TEST";
    static final String PREFIX_UTIL = "UTIL";
    static final String PREFIX_BEFORETEST = "BEFORETEST";
    static final String PREFIX_AFTERTEST = "AFTERTEST";
    static final String PREFIX_MOCK = "MOCK";
    static final String INIT_SUFFIX = ".<INIT>";
    static final String TEST_INIT_SUFFIX = ".<TESTINIT>";

    /**
     * Prefixes for the test function names.
     */
    public enum Type {
        TEST(PREFIX_TEST), BEFORE_TEST(PREFIX_BEFORETEST), AFTER_TEST(PREFIX_AFTERTEST), MOCK(PREFIX_MOCK), INIT
                (INIT_SUFFIX), UTIL(PREFIX_UTIL), TEST_INIT(TEST_INIT_SUFFIX);

        String prefix;

        Type(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return prefix;
        }
    }

    public TesterinaFunction(Class<?> programFile, BLangFunction bFunction, Type type) {
        this.name = bFunction.getName().getValue();
        this.type = type;
        this.bFunction = bFunction;
        this.programFile = programFile;
    }

    public BValue[] invoke() throws BallerinaException {
        final Scheduler scheduler = new Scheduler(4, false);
        runOnSchedule(programFile, bFunction.name, scheduler);
        return new BValue[0];
    }

    /**
     * Invoke package stop functions.
     *
     * @throws BallerinaException exception is thrown
     */
    public void invokeStopFunctions() throws BallerinaException {
        /*for (PackageInfo info : programFile.getPackageInfoEntries()) {
            BVMExecutor.executeFunction(programFile, info.getStopFunctionInfo());
        }

        for (PackageInfo info : programFile.getPackageInfoEntries()) {
            if (info.getTestStopFunctionInfo() != null) {
                BVMExecutor.executeFunction(programFile, info.getTestStopFunctionInfo());
            }
        }*/
    }

    /*public BValue[] invoke(BValue[] args) {
        // return BVMExecutor.executeFunction(programFile, bFunction, args);
        return new BValue[0];
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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


    private static void runOnSchedule(Class<?> initClazz, BLangIdentifier name, Scheduler scheduler1) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, Strand.class);
            Scheduler scheduler = scheduler1;
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return method.invoke(null, objects[0]);
                } catch (InvocationTargetException e) {
                    //throw new BallerinaException(e);

                    return e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Error while invoking function '" + funcName + "'", e);
                }
            };
            final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null);
            scheduler.start();
            final Throwable t = out.panic;
            final Object result = out.result;
            if (result instanceof ErrorValue) {
                throw new BallerinaException((ErrorValue) result);
            }
            if (t != null) {
                if (t instanceof org.ballerinalang.jvm.util.exceptions.BLangRuntimeException) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                }
                if (t instanceof org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                }
                if (t instanceof ErrorValue) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(
                            "error: " + ((ErrorValue) t).getPrintableStackTrace());
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException e) {
            throw new BallerinaException("Error while invoking function '" + funcName + "'", e);
        }
    }

    private static String cleanupFunctionName(BLangIdentifier name) {
        return name.value.replaceAll("[.:/<>]", "_");
    }

}
