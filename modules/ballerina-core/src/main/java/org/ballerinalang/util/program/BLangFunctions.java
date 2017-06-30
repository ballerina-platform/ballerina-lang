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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMWorkers;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.codegen.CodeAttributeInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

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
    public static BValue[] invokeNew(ProgramFile bLangProgram, String functionName) {
        BValue[] args = {};
        return invokeNew(bLangProgram, ".", functionName, args, new Context(bLangProgram));
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName) {
        BValue[] args = {};
        return invokeNew(bLangProgram, packageName, functionName, args, new Context(bLangProgram));
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String functionName, BValue[] args, Context bContext) {
        return invokeNew(bLangProgram, ".", functionName, args, bContext);
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bLangProgram parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @param args         arguments for the function
     * @return return values from the function
     */
    public static BValue[] invokeNew(ProgramFile bLangProgram, String functionName, BValue[] args) {
        return invokeNew(bLangProgram, ".", functionName, args, new Context(bLangProgram));
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName, BValue[] args) {
        return invokeNew(bLangProgram, packageName, functionName, args, new Context(bLangProgram));
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName,
                                     BValue[] args, Context context) {
        PackageInfo packageInfo = bLangProgram.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);

        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        if (functionInfo.getParamTypes().length != args.length) {
            throw new RuntimeException("Size of input argument arrays is not equal to size of function parameters");
        }

        ControlStackNew controlStackNew = context.getControlStackNew();
        invokeFunction(bLangProgram, packageInfo, packageInfo.getInitFunctionInfo(), context);

        // First Create the caller's stack frame. This frame contains zero local variables, but it contains enough
        // registers to hold function arguments as well as return values from the callee.
        org.ballerinalang.bre.bvm.StackFrame callerSF =
                new org.ballerinalang.bre.bvm.StackFrame(packageInfo, -1, new int[0]);
        controlStackNew.pushFrame(callerSF);
        // TODO Create registers to hold return values

        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;
        int byteRegCount = 0;

        // Calculate registers to store return values
        BType[] retTypes = functionInfo.getRetParamTypes();
        int[] retRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            switch (retType.getTag()) {
                case TypeTags.INT_TAG:
                    retRegs[i] = longRegCount++;
                    break;
                case TypeTags.FLOAT_TAG:
                    retRegs[i] = doubleRegCount++;
                    break;
                case TypeTags.STRING_TAG:
                    retRegs[i] = stringRegCount++;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    retRegs[i] = intRegCount++;
                    break;
                case TypeTags.BLOB_TAG:
                    retRegs[i] = byteRegCount++;
                    break;
                default:
                    retRegs[i] = refRegCount++;
                    break;
            }
        }

        callerSF.setLongRegs(new long[longRegCount]);
        callerSF.setDoubleRegs(new double[doubleRegCount]);
        callerSF.setStringRegs(new String[stringRegCount]);
        callerSF.setIntRegs(new int[intRegCount]);
        callerSF.setRefRegs(new BRefType[refRegCount]);
        callerSF.setByteRegs(new byte[byteRegCount][]);

        // Now create callee's stackframe
        WorkerInfo defaultWorkerInfo = functionInfo.getDefaultWorkerInfo();
        org.ballerinalang.bre.bvm.StackFrame calleeSF =
                new org.ballerinalang.bre.bvm.StackFrame(functionInfo, defaultWorkerInfo, -1, retRegs);
        controlStackNew.pushFrame(calleeSF);

        int longParamCount = 0;
        int doubleParamCount = 0;
        int stringParamCount = 0;
        int intParamCount = 0;
        int refParamCount = 0;
        int byteParamCount = 0;

        CodeAttributeInfo codeAttribInfo = defaultWorkerInfo.getCodeAttributeInfo();

        long[] longLocalVars = new long[codeAttribInfo.getMaxLongLocalVars()];
        double[] doubleLocalVars = new double[codeAttribInfo.getMaxDoubleLocalVars()];
        String[] stringLocalVars = new String[codeAttribInfo.getMaxStringLocalVars()];
        // Setting the zero values for strings
        Arrays.fill(stringLocalVars, "");

        int[] intLocalVars = new int[codeAttribInfo.getMaxIntLocalVars()];
        byte[][] byteLocalVars = new byte[codeAttribInfo.getMaxByteLocalVars()][];
        BRefType[] refLocalVars = new BRefType[codeAttribInfo.getMaxRefLocalVars()];

        for (int i = 0; i < functionInfo.getParamTypes().length; i++) {
            BType argType = functionInfo.getParamTypes()[i];
            switch (argType.getTag()) {
                case TypeTags.INT_TAG:
                    longLocalVars[longParamCount] = ((BInteger) args[i]).intValue();
                    longParamCount++;
                    break;
                case TypeTags.FLOAT_TAG:
                    doubleLocalVars[doubleParamCount] = ((BFloat) args[i]).floatValue();
                    doubleParamCount++;
                    break;
                case TypeTags.STRING_TAG:
                    stringLocalVars[stringParamCount] = args[i].stringValue();
                    stringParamCount++;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    intLocalVars[intParamCount] = ((BBoolean) args[i]).booleanValue() ? 1 : 0;
                    intParamCount++;
                    break;
                case TypeTags.BLOB_TAG:
                    byteLocalVars[byteParamCount] = ((BBlob) args[i]).blobValue();
                    byteParamCount++;
                    break;
                default:
                    refLocalVars[refParamCount] = (BRefType) args[i];
                    refParamCount++;
                    break;
            }
        }

        calleeSF.setLongLocalVars(longLocalVars);
        calleeSF.setDoubleLocalVars(doubleLocalVars);
        calleeSF.setStringLocalVars(stringLocalVars);
        calleeSF.setIntLocalVars(intLocalVars);
        calleeSF.setByteLocalVars(byteLocalVars);
        calleeSF.setRefLocalVars(refLocalVars);

        // Execute workers
        BLangVMWorkers.invoke(bLangProgram, functionInfo, calleeSF, retRegs);

        BLangVM bLangVM = new BLangVM(bLangProgram);
        context.setStartIP(codeAttribInfo.getCodeAddrs());
        bLangVM.run(context);

        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }

        longRegCount = 0;
        doubleRegCount = 0;
        stringRegCount = 0;
        intRegCount = 0;
        refRegCount = 0;
        byteRegCount = 0;
        BValue[] returnValues = new BValue[retTypes.length];
        for (int i = 0; i < returnValues.length; i++) {
            BType retType = retTypes[i];
            switch (retType.getTag()) {
                case TypeTags.INT_TAG:
                    returnValues[i] = new BInteger(callerSF.getLongRegs()[longRegCount++]);
                    break;
                case TypeTags.FLOAT_TAG:
                    returnValues[i] = new BFloat(callerSF.getDoubleRegs()[doubleRegCount++]);
                    break;
                case TypeTags.STRING_TAG:
                    returnValues[i] = new BString(callerSF.getStringRegs()[stringRegCount++]);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    boolean boolValue = callerSF.getIntRegs()[intRegCount++] == 1;
                    returnValues[i] = new BBoolean(boolValue);
                    break;
                case TypeTags.BLOB_TAG:
                    returnValues[i] = new BBlob(callerSF.getByteRegs()[byteRegCount++]);
                    break;
                default:
                    returnValues[i] = callerSF.getRefRegs()[refRegCount++];
                    break;
            }
        }

        return returnValues;

    }

    public static void invokePackageInitFunction(ProgramFile programFile, PackageInfo packageInfo, Context context) {
        FunctionInfo initFuncInfo = packageInfo.getInitFunctionInfo();
        WorkerInfo defaultWorker = initFuncInfo.getDefaultWorkerInfo();
        org.ballerinalang.bre.bvm.StackFrame stackFrame = new org.ballerinalang.bre.bvm.StackFrame(initFuncInfo,
                defaultWorker, -1, new int[0]);
        context.getControlStackNew().pushFrame(stackFrame);

        BLangVM bLangVM = new BLangVM(programFile);
        context.setStartIP(defaultWorker.getCodeAttributeInfo().getCodeAddrs());
        bLangVM.run(context);
    }

    public static void invokeFunction(ProgramFile programFile, PackageInfo packageInfo,
                                                 FunctionInfo initFuncInfo, Context context) {
        WorkerInfo defaultWorker = initFuncInfo.getDefaultWorkerInfo();
        org.ballerinalang.bre.bvm.StackFrame stackFrame = new org.ballerinalang.bre.bvm.StackFrame(initFuncInfo,
                defaultWorker, -1, new int[0]);
        context.getControlStackNew().pushFrame(stackFrame);

        BLangVM bLangVM = new BLangVM(programFile);
        context.setStartIP(defaultWorker.getCodeAttributeInfo().getCodeAddrs());
        bLangVM.run(context);
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

        if (bValue instanceof BInteger) {
            bType = BTypes.typeInt;
        } else if (bValue instanceof BFloat) {
            bType = BTypes.typeFloat;
        } else if (bValue instanceof BString) {
            bType = BTypes.typeString;
        } else if (bValue instanceof BBoolean) {
            bType = BTypes.typeBoolean;
        } else if (bValue instanceof BBlob) {
            bType = BTypes.typeBlob;
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
