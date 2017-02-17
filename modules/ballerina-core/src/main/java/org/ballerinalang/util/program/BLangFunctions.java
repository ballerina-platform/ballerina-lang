/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.program;

import org.ballerinalang.bre.BLangExecutor;
import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.nonblocking.BLangNonBlockingExecutor;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.builder.BLangExecutionFlowBuilder;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.nodes.StartNode;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;

/**
 * This class contains helper methods to invoke Ballerina functions.
 *
 * @since 0.8.0
 */
public class BLangFunctions {

    private BLangFunctions() {
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bLangProgram parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @return return values from the function
     */
    public static BValue[] invoke(BLangProgram bLangProgram, String functionName) {
        BValue[] args = {};
        return invoke(bLangProgram, functionName, args, new Context());
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bLangProgram parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @return return values from the function
     */
    public static BValue[] invoke(BLangProgram bLangProgram, String functionName, BValue[] args) {
        return invoke(bLangProgram, functionName, args, new Context());
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bLangProgram parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @return return values from the function
     */
    public static BValue[] invoke(BLangProgram bLangProgram, String functionName, BValue[] args, Context bContext) {
        Function function = getFunction(bLangProgram.getLibraryPackages()[0].getFunctions(), functionName, args);
        if (function == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        if (function.getParameterDefs().length != args.length) {
            throw new RuntimeException("Size of input argument arrays is not equal to size of function parameters");
        }

        BValue[] argValues = new BValue[function.getStackFrameSize()];

        int stackIndex = 0;
        for (int i = 0; i < args.length; i++) {
            argValues[i] = args[i];
            stackIndex++;
        }

        for (ParameterDef returnParam : function.getReturnParameters()) {
            if (returnParam.getName() == null) {
                break;
            }

            argValues[stackIndex] = returnParam.getType().getDefaultValue();
            stackIndex++;
        }

        BValue[] returnValues = new BValue[function.getReturnParameters().length];
        CallableUnitInfo functionInfo = new CallableUnitInfo(function.getName(), function.getPackagePath(),
                function.getNodeLocation());
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);

        if (ModeResolver.getInstance().isNonblockingEnabled()) {
            // TODO: Fix this properly.
            Expression[] exprs = new Expression[args.length];
            for (int i = 0; i < args.length; i++) {
                VariableRefExpr variableRefExpr = new VariableRefExpr(function.getNodeLocation(),
                        new SymbolName("arg" + i));

                variableRefExpr.setVariableDef(function.getParameterDefs()[i]);
                StackVarLocation location = new StackVarLocation(i);
                variableRefExpr.setMemoryLocation(location);
                exprs[i] = variableRefExpr;
            }

            // 3) Create a function invocation expression
            FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(
                    function.getNodeLocation(), functionName, null, null, exprs);
            funcIExpr.setOffset(args.length);
            funcIExpr.setCallableUnit(function);
            // Linking.
            BLangExecutionFlowBuilder flowBuilder = new BLangExecutionFlowBuilder();
            funcIExpr.setParent(new StartNode(StartNode.Originator.TEST));
            funcIExpr.accept(flowBuilder);
            BValue[] cacheValues = new BValue[100];
            StackFrame stackFrame = new StackFrame(argValues, new BValue[0], cacheValues, functionInfo);
            bContext.getControlStack().pushFrame(stackFrame);

            // Invoke main function
            BLangNonBlockingExecutor nonBlockingExecutor = new BLangNonBlockingExecutor(runtimeEnv, bContext);
            nonBlockingExecutor.execute(funcIExpr);
            int length = funcIExpr.getCallableUnit().getReturnParameters().length;
            BValue[] result = new BValue[length];
            for (int i = 0; i < length; i++) {
                result[i] = bContext.getControlStack().getCurrentFrame().tempValues[funcIExpr.getTempOffset() + i];
            }
            return result;
        } else {
            StackFrame stackFrame = new StackFrame(argValues, returnValues, functionInfo);
            bContext.getControlStack().pushFrame(stackFrame);

            // Invoke main function
            BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
            function.getCallableUnitBody().execute(executor);
            return returnValues;
        }
    }

    /**
     * Util method to get Given function.
     *
     * @param bLangProgram Ballerina program .
     * @param functionName name of the function.
     * @return Function instance or null if function doesn't exist.
     */
    public static Function getFunction(BLangProgram bLangProgram, String functionName) {
        return getFunction(bLangProgram.getLibraryPackages()[0].getFunctions(), functionName, null);
    }

    private static Function getFunction(Function[] functions, String funcName, BValue[] args) {

        Function firstMatch = null;
        int count = 0;

        for (Function function : functions) {
            if (function.getName().equals(funcName)) {
                firstMatch = function;
                count++;
            }
        }

        // If there are no overloading functions, return the first match
        if (count == 1) {
            return firstMatch;
        }

        for (Function function : functions) {
            if (function.getName().equals(funcName) && matchArgTypes(function.getArgumentTypes(), args)) {
                return function;
            }
        }
        return null;
    }

    /**
     * Compare argument types matches with the provided types.
     *
     * @param argTypes  List of {@link BType} that are accepted as arguments
     * @param argValues List of {@link BValue} that are provided as arguments
     * @return True if a matching type if found for each
     */
    private static boolean matchArgTypes(BType[] argTypes, BValue[] argValues) {
        boolean matching = false;

        if (argTypes.length == argValues.length) {
            matching = true;
            for (int i = 0; i < argTypes.length; i++) {
                BType resolvedType = resolveBType(argValues[i]);

                if (resolvedType == null || !argTypes[i].equals(resolvedType)) {
                    matching = false;
                    break;
                }
            }
        }

        return matching;
    }

    /**
     * Resolve the {@link BType} of a  given {@link BValue} for built in types.
     *
     * @param bValue The {@link BValue} to resolve
     * @return The {@link BType} corresponding to the {@link BValue}
     */
    private static BType resolveBType(BValue bValue) {
        BType bType = null;

        if (bValue instanceof BString) {
            bType = BTypes.typeString;
        } else if (bValue instanceof BInteger) {
            bType = BTypes.typeInt;
        } else if (bValue instanceof BLong) {
            bType = BTypes.typeLong;
        } else if (bValue instanceof BFloat) {
            bType = BTypes.typeFloat;
        } else if (bValue instanceof BDouble) {
            bType = BTypes.typeDouble;
        } else if (bValue instanceof BBoolean) {
            bType = BTypes.typeBoolean;
        } else if (bValue instanceof BXML) {
            bType = BTypes.typeXML;
        } else if (bValue instanceof BJSON) {
            bType = BTypes.typeXML;
        } else if (bValue instanceof BMessage) {
            bType = BTypes.typeMessage;
        } else if (bValue instanceof BMap) {
            bType = BTypes.typeMap;
        } else if (bValue instanceof BDataTable) {
            bType = BTypes.typeDatatable;
        }

        return bType;

    }
}
