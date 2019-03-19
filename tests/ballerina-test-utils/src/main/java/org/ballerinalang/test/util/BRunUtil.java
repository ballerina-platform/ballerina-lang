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
package org.ballerinalang.test.util;

import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.bre.old.WorkerExecutionContext;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for run Ballerina functions.
 *
 * @since 0.94
 */
public class BRunUtil {

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

//    Package init helpers
    /**
     * Invoke package init function.
     *
     * @param compileResult CompileResult instance
     */
    public static void invokePackageInit(CompileResult compileResult) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
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
            throw new IllegalStateException(compileResult.toString());
        }
        ProgramFile programFile = compileResult.getProgFile();
        WorkerExecutionContext context = new WorkerExecutionContext(programFile);
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        compileResult.setContext(context);

        BVMExecutor.initProgramFile(programFile);
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
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);

        PackageInfo packageInfo = programFile.getPackageInfo(programFile.getEntryPkgName());
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        BValue[] response = BVMExecutor.executeEntryFunction(programFile, functionInfo, args);

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
        BValue[] response;
        if (BCompileUtil.jBallerinaTestsEnabled()) {
            response = invokeOnJBallerina(compileResult, functionName, args);
        } else {
            response = invokeFunction(compileResult, functionName, args);
        }
        return spreadToBValueArray(response);
    }

    /**
     * This method takes care of invocation on JBallerina and the mapping of input and output values. It will use the
     * given BVM based argument and function details to invoke on JBallerina and return results as BValues to maintain
     * backward compatibility with existing invoke methods in BRunUtil.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    private static BValue[] invokeOnJBallerina(CompileResult compileResult, String functionName, BValue[] args) {
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        return invoke(compileResult.getEntryClass(), function, functionName, args);
    }

    /**
     * This method handles the input arguments and output result mapping between BVM types, values to JVM types, values.
     *
     * @param clazz the class instance to be used to invoke methods
     * @param function function model instance from BIR model
     * @param functionName name of the function to be invoked
     * @param bvmArgs input arguments to be used with function invocation
     * @return return the result from function invocation
     */
    private static BValue[] invoke(Class<?> clazz, BIRNode.BIRFunction function, String functionName,
                                   BValue[] bvmArgs) {
        List<org.wso2.ballerinalang.compiler.semantics.model.types.BType> bvmParamTypes = function.type.paramTypes;
        Class<?>[] jvmParamTypes = new Class[bvmParamTypes.size() + 1];
        Object[] jvmArgs = new Object[bvmParamTypes.size() + 1];
        jvmParamTypes[0] = Strand.class;
        jvmArgs[0] = new Strand();

        for (int i = 0; i < bvmParamTypes.size(); i++) {
            org.wso2.ballerinalang.compiler.semantics.model.types.BType type = bvmParamTypes.get(i);
            Class<?> typeClazz;
            Object argument;
            switch (type.tag) {
                case TypeTags.INT_TAG:
                    typeClazz = long.class;
                    argument = ((BInteger) bvmArgs[i]).intValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    typeClazz = boolean.class;
                    argument = ((BBoolean) bvmArgs[i]).booleanValue();
                    break;
                case TypeTags.STRING_TAG:
                    typeClazz = String.class;
                    argument = bvmArgs[i].stringValue();
                    break;
                default:
                    throw new RuntimeException("Function signature type '" + type + "' is not supported");
            }

            jvmParamTypes[i + 1] = typeClazz;
            jvmArgs[i + 1] = argument;
        }

        Object jvmResult;

        try {
            Method method = clazz.getDeclaredMethod(functionName, jvmParamTypes);
            jvmResult = method.invoke(null, jvmArgs);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
        }

        BValue result = getBVMValue(jvmResult);
        return new BValue[] { result };
    }

    private static BRefType<?> getBVMValue(Object value) {
        org.ballerinalang.jvm.types.BType type = TypeChecker.getType(value);
        switch (type.getTag()) {
            case org.ballerinalang.jvm.types.TypeTags.INT_TAG:
                return new BInteger((long) value);
            case org.ballerinalang.jvm.types.TypeTags.BOOLEAN_TAG:
                return new BBoolean((boolean) value);
            case org.ballerinalang.jvm.types.TypeTags.STRING_TAG:
                return new BString((String) value);
            case org.ballerinalang.jvm.types.TypeTags.TUPLE_TAG:
            case org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG:
                ArrayValue jvmTuple = ((ArrayValue) value);
                BRefType<?>[] tupleValues = new BRefType<?>[jvmTuple.size()];
                for (int i = 0; i < jvmTuple.size(); i++) {
                    tupleValues[i] = getBVMValue(jvmTuple.getRefValue(i));
                }
                return new BValueArray(tupleValues, getBVMType(jvmTuple.getType()));
            default:
                throw new RuntimeException("Function invocation result for type '" + type + "' is not supported");
        }
    }

    private static BType getBVMType(org.ballerinalang.jvm.types.BType jvmType) {
        switch (jvmType.getTag()) {
            case org.ballerinalang.jvm.types.TypeTags.INT_TAG:
                return BTypes.typeInt;
            case org.ballerinalang.jvm.types.TypeTags.FLOAT_TAG:
                return BTypes.typeFloat;
            case org.ballerinalang.jvm.types.TypeTags.STRING_TAG:
                return BTypes.typeString;
            case org.ballerinalang.jvm.types.TypeTags.BOOLEAN_TAG:
                return BTypes.typeBoolean;
            case org.ballerinalang.jvm.types.TypeTags.TUPLE_TAG:
                org.ballerinalang.jvm.types.BTupleType tupleType = (org.ballerinalang.jvm.types.BTupleType) jvmType;
                List<BType> memberTypes = new ArrayList<>();
                for (org.ballerinalang.jvm.types.BType type : tupleType.getTupleTypes()) {
                    memberTypes.add(getBVMType(type));
                }
                return new BTupleType(memberTypes);
            case org.ballerinalang.jvm.types.TypeTags.ARRAY_TAG:
                org.ballerinalang.jvm.types.BArrayType arrayType = (org.ballerinalang.jvm.types.BArrayType) jvmType;
                return new BArrayType(getBVMType(arrayType.getElementType()));
            case org.ballerinalang.jvm.types.TypeTags.ANY_TAG:
                return BTypes.typeAny;
            case org.ballerinalang.jvm.types.TypeTags.ANYDATA_TAG:
                return BTypes.typeAnydata;
            default:
                throw new RuntimeException("Unsupported jvm type: " + jvmType + "' ");
        }
    }

    private static BIRNode.BIRFunction getInvokedFunction(CompileResult compileResult, String functionName) {

        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }

        BIRNode.BIRPackage birPackage = ((BLangPackage) compileResult.getAST()).symbol.bir;
        return birPackage.functions.stream()
                .filter(function -> functionName.equals(function.name.value))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Function '" + functionName + "' is not defined"));
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
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);

        PackageInfo packageInfo = programFile.getPackageInfo(programFile.getEntryPkgName());
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }


        return BVMExecutor.executeEntryFunction(programFile, functionInfo, args);
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
