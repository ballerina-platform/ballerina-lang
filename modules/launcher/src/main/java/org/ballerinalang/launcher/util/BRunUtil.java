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
package org.ballerinalang.launcher.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;

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
            throw new IllegalStateException("compilation contains errors.");
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
            throw new IllegalStateException("compilation contains errors.");
        }
        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        if (functionInfo.getParamTypes().length != args.length) {
            throw new RuntimeException("Size of input argument arrays is not equal to size of function parameters");
        }
        return BLangFunctions.invokeFunction(programFile, functionInfo, args, compileResult.getContext());
    }

//    Package init helpers
    /**
     * Invoke package init function.
     *
     * @param compileResult CompileResult instance
     */
    protected static void invokePackageInit(CompileResult compileResult) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException("compilation contains errors.");
        }
        invokePackageInit(compileResult, compileResult.getProgFile().getEntryPkgName());
    }

    /**
     * Invoke package init function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     */
    protected static void invokePackageInit(CompileResult compileResult, String packageName) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException("compilation contains errors.");
        }
        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(packageName);
        Context context = new Context(programFile);
        compileResult.setContext(context);
        BLangFunctions.invokePackageInitFunction(programFile, packageInfo.getInitFunctionInfo(), context);
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
            throw new IllegalStateException("compilation contains errors.");
        }
        ProgramFile programFile = compileResult.getProgFile();
        return BLangFunctions.invokeNew(programFile, packageName, functionName, args);
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
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException("compilation contains errors.");
        }
        ProgramFile programFile = compileResult.getProgFile();
        return BLangFunctions.invokeNew(programFile, programFile.getEntryPkgName(), functionName, args);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @param context       context for function invocation
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName, BValue[] args, Context context) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException("compilation contains errors.");
        }
        ProgramFile programFile = compileResult.getProgFile();
        return BLangFunctions.invokeNew(programFile, programFile.getEntryPkgName(), functionName, args, context);
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
    public static void invoke(CompileResult compileResult, FunctionInfo initFuncInfo, Context context) {
        BLangFunctions.invokeFunction(compileResult.getProgFile(), initFuncInfo, context);
    }
}
