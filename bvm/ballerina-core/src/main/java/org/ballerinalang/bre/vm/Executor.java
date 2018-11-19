/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.vm;

import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.observability.ObserverContext;

import java.util.Map;

/**
 * This is the executor class which initialize the execution.
 *
 * @since 0.985.0
 */
public class Executor {

    public static void initProgramFile(ProgramFile programFile) {
        invokePackageInitFunctions(programFile);
        invokePackageStartFunctions(programFile);
    }

    /**
     * Execution API to execute program file starting from the given callable unit.
     *
     * @param programFile  to be executed
     * @param functionInfo to be executed
     * @param args         to be passed to the invokable unit
     */
    public static BValue[] executeEntryFunction(ProgramFile programFile, FunctionInfo functionInfo, BValue... args) {
        int requiredArgNo = functionInfo.getParamTypes().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                    providedArgNo + ".");
        }
        initProgramFile(programFile);
        //Add compensation table TODO fix - rajith
//        parentCtx.globalProps.put(Constants.COMPENSATION_TABLE, CompensationTable.getInstance());
        BValue[] result = new BValue[]{execute(programFile, functionInfo, args, true)};
        BLangScheduler.waitForWorkerCompletion();
        return result;
    }

    public static BValue[] executeFunction(ProgramFile programFile, FunctionInfo functionInfo, BValue... args) {
        int requiredArgNo = functionInfo.getParamTypes().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                    providedArgNo + ".");
        }
        //Add compensation table TODO fix - rajith
//        parentCtx.globalProps.put(Constants.COMPENSATION_TABLE, CompensationTable.getInstance());
        BValue[] result = new BValue[]{execute(programFile, functionInfo, args, true)};
        BLangScheduler.waitForWorkerCompletion();
        return result;
    }

    public static void executeResource(ProgramFile programFile, ResourceInfo resourceInfo, Map<String,
            Object> properties, ObserverContext observerContext, BValue... args) {
        if (properties != null) {
            //TODO fix - rajith
//            Object interruptible = properties.get(Constants.IS_INTERRUPTIBLE);
//            if (interruptible != null && (boolean) interruptible) {
//                String stateId = UUID.randomUUID().toString();
//                properties.put(Constants.STATE_ID, stateId);
//                RuntimeStates.add(new State(context, stateId));
//                context.interruptible = true;
//            }
//            context.globalProps.putAll(properties);
//            if (properties.get(Constants.GLOBAL_TRANSACTION_ID) != null) {
//                context.setLocalTransactionInfo(new LocalTransactionInfo(
//                        properties.get(Constants.GLOBAL_TRANSACTION_ID).toString(),
//                        properties.get(Constants.TRANSACTION_URL).toString(), "2pc"));
//            }
        }
        //required for tracking compensations
//        context.globalProps.put(Constants.COMPENSATION_TABLE, CompensationTable.getInstance());
//        BLangVMUtils.setServiceInfo(context, resourceInfo.getServiceInfo());
        execute(programFile, resourceInfo, args, false);
//        BLangFunctions.invokeServiceCallable(resourceInfo, context, observerContext, bValues, responseCallback);
    }


    private static BValue execute(ProgramFile programFile, CallableUnitInfo functionInfo, BValue[] args, boolean waitForResponse) {
        StrandWaitCallback strandCallback = new StrandWaitCallback(functionInfo, functionInfo.getRetParamTypes()[0]); //TODO change to have single return type - rajith
        Strand strand = new Strand(programFile, strandCallback);

        DataFrame idf = new DataFrame(functionInfo.getPackageInfo(), functionInfo,
                functionInfo.getDefaultWorkerInfo().getCodeAttributeInfo(), -1);
        copyArgValues(args, idf, functionInfo.getParamTypes());
        strand.pushFrame(idf);

        BVM.execute(strand);

        if (waitForResponse) {
            strandCallback.waitForResponse();
            if (strand.getError() != null) {
                throw new BLangRuntimeException("error: " + BLangVMErrors.getPrintableStackTrace(strand.getError()));
            }
        }

        BValue result = populateReturnData(strandCallback, functionInfo);
        BLangScheduler.waitForWorkerCompletion();
        return result;
    }

    private static void copyArgValues(BValue[] args, DataFrame callee, BType[] paramTypes) {
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        for (int i = 0; i < paramTypes.length; i++) {
            BType paramType = paramTypes[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    callee.longRegs[++longRegIndex] = ((BValueType) args[i]).intValue();
                    break;
                case TypeTags.BYTE_TAG:
                    callee.intRegs[++booleanRegIndex] = ((BValueType) args[i]).byteValue();
                    break;
                case TypeTags.FLOAT_TAG:
                    callee.doubleRegs[++doubleRegIndex] = ((BValueType) args[i]).floatValue();
                    break;
                case TypeTags.STRING_TAG:
                    callee.stringRegs[++stringRegIndex] = args[i].stringValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    if (args[i] instanceof BString) {
                        callee.intRegs[++booleanRegIndex] =
                                ((BString) args[i]).value().toLowerCase().equals("true") ? 1 : 0;
                    } else {
                        callee.intRegs[++booleanRegIndex] = ((BValueType) args[i]).booleanValue() ? 1 : 0;
                    }
                    break;
                default:
                    callee.refRegs[++refRegIndex] = (BRefType) args[i];
                    break;
            }
        }
    }

    private static BValue populateReturnData(StrandCallback strandCallback, CallableUnitInfo callableUnitInfo) {
        BValue returnValue;
        BType retType = callableUnitInfo.getRetParamTypes()[0]; //TODO this should have single type instead of an array
        switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                returnValue = new BInteger(strandCallback.getIntRetVal());
                break;
            case TypeTags.BYTE_TAG:
                returnValue = new BByte((byte) strandCallback.getByteRetVal());
                break;
            case TypeTags.FLOAT_TAG:
                returnValue = new BFloat(strandCallback.getFloatRetVal());
                break;
            case TypeTags.STRING_TAG:
                returnValue = new BString(strandCallback.getStringRetVal());
                break;
            case TypeTags.BOOLEAN_TAG:
                boolean boolValue = strandCallback.getBooleanRetVal() == 1;
                returnValue = new BBoolean(boolValue);
                break;
            default:
                returnValue = strandCallback.getRefRetVal();
                break;
        }
        return returnValue;
    }

    /**
     * This will order the package imports and inovke each package init function.
     *
     * @param programFile to be invoked.
     */
    private static void invokePackageInitFunctions(ProgramFile programFile) {
        for (PackageInfo info : programFile.getPackageInfoEntries()) {
            execute(programFile, info.getInitFunctionInfo(), new BValue[0], true);
        }
    }

    /**
     * This will invoke package start functions, this should be invoked after
     * invoking "invokePackageInitFunctions".
     *
     * @param programFile to be invoked.
     */
    private static void invokePackageStartFunctions(ProgramFile programFile) {
        for (PackageInfo info : programFile.getPackageInfoEntries()) {
            execute(programFile, info.getStartFunctionInfo(), new BValue[0], true);
        }
    }
}
