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

import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;

import java.util.Arrays;

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
            throw new RuntimeException("Size of input argument array is not equal to size of function parameters");
        }

        BValue[] argValues = new BValue[function.getStackFrameSize()];

        for (int i = 0; i < args.length; i++) {
            argValues[i] = args[i];
        }

        BValue[] returnValues = new BValue[function.getReturnParameters().length];
        CallableUnitInfo functionInfo = new CallableUnitInfo(function.getName(), function.getPackagePath(),
                function.getNodeLocation());

        StackFrame stackFrame = new StackFrame(argValues, returnValues, functionInfo);
        bContext.getControlStack().pushFrame(stackFrame);

        // Invoke main function
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
        BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
        function.getCallableUnitBody().execute(executor);

        bContext.getControlStack().popFrame();
        return returnValues;
    }


    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bFile        parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @param args         function arguments
     * @return return values from the function
     */
    public static BValue[] invoke(BallerinaFile bFile, String functionName, BValue[] args, Context bContext) {

        // 1) Check whether the given function is defined in the source file.
        Function function = getFunction(bFile.getFunctions(), functionName, args);
        if (function == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        if (function.getParameterDefs().length != args.length) {
            throw new RuntimeException("Size of input argument array is not equal to size of function parameters");
        }

        // 2) Create variable reference expressions for each argument value;
        Expression[] exprs = new Expression[args.length];
        for (int i = 0; i < args.length; i++) {
            VariableRefExpr variableRefExpr = new VariableRefExpr(function.getNodeLocation(),
                    new SymbolName("arg" + i));

            variableRefExpr.setVariableDef(function.getParameterDefs()[i]);
            StackVarLocation location = new StackVarLocation(i);
            variableRefExpr.setMemoryLocation(location);
            // TODO Set the type
//            variableRefExpr.setType();
            exprs[i] = variableRefExpr;
        }

        // 3) Create a function invocation expression
        FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(
                function.getNodeLocation(), functionName, null, bFile.getPackagePath(), exprs);
        funcIExpr.setOffset(args.length);
        funcIExpr.setCallableUnit(function);

        // 4) Prepare function arguments
        BValue[] functionArgs = args;
        if (function.getReturnParameters().length != 0) {
            functionArgs = Arrays.copyOf(args, args.length + function.getReturnParameters().length);
        }

        // 5) Create the RuntimeEnvironment
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bFile);

        // 6) Create the control stack and the stack frame to invoke the functions
        CallableUnitInfo functionInfo = new CallableUnitInfo(function.getName(), function.getPackagePath(),
                function.getNodeLocation());

        StackFrame currentStackFrame = new StackFrame(functionArgs, new BValue[0], functionInfo);
        bContext.getControlStack().pushFrame(currentStackFrame);

        // 7) Invoke the function
        BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
        return funcIExpr.executeMultiReturn(executor);

    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bFile        parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @return return values from the function
     */
    public static BValue[] invoke(BallerinaFile bFile, String functionName) {
        BValue[] args = {};
        return invoke(bFile, functionName, args, new Context());
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bFile        parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @return return values from the function
     */
    public static BValue[] invoke(BallerinaFile bFile, String functionName, BValue[] args) {
        return invoke(bFile, functionName, args, new Context());
    }

    /**
     * Invokes a Ballerina function defined in the given language model, given the ballerina context.
     *
     * @param bFile        Parsed, analyzed and linked object model
     * @param functionName Name of the function to be invoked
     * @param bContext     Ballerina Context
     * @return return values from the function
     */
    public static BValue[] invoke(BallerinaFile bFile, String functionName, Context bContext) {
        BValue[] args = {};
        return invoke(bFile, functionName, args, bContext);
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
        }

        return bType;

    }
}
