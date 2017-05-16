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

import org.ballerinalang.bre.BLangExecutor;
import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Arrays;

/**
 * TesterinaFunction entity class
 */
public class TesterinaFunction {

    private String name;
    private Type type;
    private Function bFunction;
    private BLangProgram bLangProgram;

    public static final String PREFIX_TEST = "TEST";
    public static final String PREFIX_BEFORETEST = "BEFORETEST";
    public static final String PREFIX_AFTERTEST = "AFTERTEST";

    /**
     * Prefixes for the test function names
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

    TesterinaFunction(BLangProgram bLangProgram, Function bFunction, Type type) {
        this.name = bFunction.getName();
        this.type = type;
        this.bFunction = bFunction;
        this.bLangProgram = bLangProgram;
    }

    public BValue[] invoke() throws BallerinaException {
        return invoke(new BValue[] {});
    }

    private BValue[] invoke(BValue[] args) {
        return invoke(args, new Context());
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bContext ballerina context
     * @param args     function arguments
     * @return return values from the function
     */
    public BValue[] invoke(BValue[] args, Context bContext) {

        // 1) Check whether the given function is defined in the source file.
        Function function = this.bFunction;
        if (function == null) {
            throw new RuntimeException("Function '" + name + "' is not defined");
        }

        if (function.getParameterDefs().length != args.length) {
            throw new RuntimeException("Size of input argument array is not equal to size of function parameters");
        }

        // 2) Create variable reference expressions for each argument value;
        Expression[] exprs = new Expression[args.length];
        for (int i = 0; i < args.length; i++) {
            VariableRefExpr variableRefExpr = new VariableRefExpr(function.getNodeLocation(), null, 
                    new SymbolName("arg" + i));

            variableRefExpr.setVariableDef(function.getParameterDefs()[i]);
            StackVarLocation location = new StackVarLocation(i);
            variableRefExpr.setMemoryLocation(location);
            // TODO Set the type
            //    variableRefExpr.setType();
            exprs[i] = variableRefExpr;
        }

        // 3) Create a function invocation expression
        FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(function.getNodeLocation(), null, name, null,
                bFunction.getPackagePath(), exprs);
        funcIExpr.setOffset(args.length);
        funcIExpr.setCallableUnit(function);

        // 4) Prepare function arguments
        BValue[] functionArgs = args;
        if (function.getReturnParameters().length != 0) {
            functionArgs = Arrays.copyOf(args, args.length + function.getReturnParameters().length);
        }

        // 5) Create the RuntimeEnvironment
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);

        // 6) Create the control stack and the stack frame to invoke the functions
        SymbolName functionSymbolName = function.getSymbolName();
        CallableUnitInfo functionInfo = new CallableUnitInfo(functionSymbolName.getName(),
                functionSymbolName.getPkgPath(), function.getNodeLocation());

        StackFrame currentStackFrame = new StackFrame(functionArgs, new BValue[0], functionInfo);
        bContext.getControlStack().pushFrame(currentStackFrame);

        // 7) Invoke the function
        BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
        return funcIExpr.executeMultiReturn(executor);

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

    public Function getbFunction() {
        return this.bFunction;
    }

    public void setbFunction(Function bFunction) {
        this.bFunction = bFunction;
    }

}
