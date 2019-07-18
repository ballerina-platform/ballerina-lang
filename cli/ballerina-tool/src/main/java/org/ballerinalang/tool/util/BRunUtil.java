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
package org.ballerinalang.tool.util;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.bre.old.WorkerExecutionContext;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;

/**
 * Utility methods for run Ballerina functions.
 *
 * @since 0.94
 */
public class BRunUtil {

//    Methods to run in stateful manner
    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String functionName) {
        BValue[] args = {};
        return invokeStateful(compileResult, functionName, args);
    }

    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
        return invokeStateful(compileResult, compileResult.getProgFile().getEntryPkgName(), functionName, args);
    }

    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String packageName, String functionName) {
        BValue[] args = {};
        return invokeStateful(compileResult, packageName, functionName, args);
    }

    /**
     * Invoke a ballerina function with state. Need to use compileAndSetup method in BCompileUtil to use this.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invokeStateful(CompileResult compileResult, String packageName,
                                          String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
        ProgramFile programFile = compileResult.getProgFile();
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        PackageInfo packageInfo = programFile.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        int requiredArgNo = functionInfo.getParamTypes().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                    providedArgNo + ".");
        }

        BValue[] response = BVMExecutor.executeFunction(programFile, functionInfo, args);

        return spreadToBValueArray(response);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String packageName, String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }

        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        BValue[] response = new BValue[]{BLangProgramRunner.runProgram(programFile, functionInfo, args)};

        return spreadToBValueArray(response);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String packageName, String functionName) {
        BValue[] args = {};
        return invoke(compileResult, packageName, functionName, args);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName, BValue[] args) {
        BValue[] response = invokeFunction(compileResult, functionName, args);
        return spreadToBValueArray(response);
    }

    /**
     * Invoke a ballerina function to get BReference Value Objects.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invokeFunction(CompileResult compileResult, String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }

        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(programFile.getEntryPkgName());
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        return new BValue[]{BLangProgramRunner.runProgram(programFile, functionInfo, args)};
    }

    /**
     * Invoke a ballerina function to get BReference Value Objects.
     *
     * @param compileResult CompileResult instance
     * @param functionName Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invokeFunction(CompileResult compileResult, String functionName) {
        return invokeFunction(compileResult, functionName, new BValue[] {});
    }

    private static BValue[] spreadToBValueArray(BValue[] response) {
        if (!(response != null && response.length > 0 && response[0] instanceof BValueArray)) {
            return response;
        }

        BValueArray refValueArray = (BValueArray) response[0];
        BType elementType = refValueArray.elementType;
        if (elementType == BTypes.typeString || elementType == BTypes.typeInt || elementType == BTypes.typeFloat
                || elementType == BTypes.typeBoolean || elementType == BTypes.typeByte) {
            return response;
        }

        int length = (int) refValueArray.size();
        BValue[] arr = new BValue[length];
        for (int i = 0; i < length; i++) {
            arr[i] = refValueArray.getRefValue(i);
        }
        return arr;
    }


    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName) {
        BValue[] args = {};
        return invoke(compileResult, functionName, args);
    }

    /**
     * Invoke a ballerina function given context.
     *
     * @param compileResult CompileResult instance.
     * @param initFuncInfo Function to invoke.
     * @param context invocation context.
     */
    public static void invoke(CompileResult compileResult, FunctionInfo initFuncInfo,
            WorkerExecutionContext context) {
        Debugger debugger = new Debugger(compileResult.getProgFile());
        compileResult.getProgFile().setDebugger(debugger);

        BVMExecutor.executeFunction(compileResult.getProgFile(), initFuncInfo);
    }
}
