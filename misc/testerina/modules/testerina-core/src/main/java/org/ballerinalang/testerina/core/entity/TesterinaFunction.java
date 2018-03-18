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

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * TesterinaFunction entity class.
 */
public class TesterinaFunction {

    private String name;
    private Type type;
    private FunctionInfo bFunction;
    private ProgramFile programFile;

    private static final int WORKER_TIMEOUT = 10;

    public static final String PREFIX_TEST = "TEST";
    public static final String PREFIX_BEFORETEST = "BEFORETEST";
    public static final String PREFIX_AFTERTEST = "AFTERTEST";
    public static final String INIT_SUFFIX = ".<INIT>";

    /**
     * Prefixes for the test function names.
     */
    public enum Type {
        TEST(PREFIX_TEST), BEFORE_TEST(PREFIX_BEFORETEST), AFTER_TEST(PREFIX_AFTERTEST);

        String prefix;

        Type(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return prefix;
        }
    }

    TesterinaFunction(ProgramFile programFile, FunctionInfo bFunction, Type type) {
        this.name = bFunction.getName();
        this.type = type;
        this.bFunction = bFunction;
        this.programFile = programFile;
    }

    public BValue[] invoke() throws BallerinaException {
        return invoke(new BValue[] {});
    }

    /**
     * Invokes a ballerina test function, in blocking mode.
     *
     * @param args function arguments
     * @return values returned by the ballerina function
     */
    public BValue[] invoke(BValue[] args) {
        WorkerExecutionContext ctx = new WorkerExecutionContext(programFile);
        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);
        return BLangFunctions.invokeCallable(bFunction, ctx, args);
    }


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

    public FunctionInfo getbFunction() {
        return this.bFunction;
    }

    public void setbFunctionInfo(FunctionInfo bFunction) {
        this.bFunction = bFunction;
    }

    private static void initDebugger(ProgramFile programFile, Debugger debugger) {
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }

}
