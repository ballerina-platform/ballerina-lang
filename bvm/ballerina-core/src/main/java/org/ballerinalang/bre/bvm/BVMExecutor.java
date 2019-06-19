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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.bvm.Strand.State;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.observability.ObserveUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.TransactionLocalContext;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the executor class which initialize the execution.
 *
 * @since 0.985.0
 */
public class BVMExecutor {

    /**
     * This will invoke package stop functions.
     *
     * @param programFile to be invoked.
     */
    public static void stopProgramFile(ProgramFile programFile) {
        invokePackageStopFunctions(programFile);
    }

    /**
     * Execution API to execute just a function.
     *
     * @param programFile  to be executed
     * @param functionInfo to be executed
     * @param args         to be passed to invokable unit
     * @return results
     */
    public static BValue[] executeFunction(ProgramFile programFile, FunctionInfo functionInfo, BValue... args) {
        int requiredArgNo = functionInfo.getParamTypes().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                                               providedArgNo + ".");
        }
        return new BValue[]{execute(programFile, functionInfo, args, null, true)};
    }

    /**
     * Execution API to execute a resource.
     *
     * @param programFile      to be executed
     * @param resourceInfo     to be executed
     * @param responseCallback to be used for notifications
     * @param properties       to be passed in the context
     * @param observerContext  to be used
     * @param serviceInfo      to be used
     * @param args             to be passed to the resource
     */
    public static void executeResource(ProgramFile programFile, FunctionInfo resourceInfo,
                                       CallableUnitCallback responseCallback, Map<String, Object> properties,
                                       ObserverContext observerContext, ServiceInfo serviceInfo, BValue... args) {
        Strand strand = populateAndGetStrand(programFile, resourceInfo, responseCallback, properties, observerContext,
                                             serviceInfo, args);
        BVMScheduler.schedule(strand);
    }

    /**
     * Execution API to execute a resource in the current thread.
     *
     * @param programFile      to be executed
     * @param resourceInfo     to be executed
     * @param responseCallback to be used for notifications
     * @param properties       to be passed in the context
     * @param observerContext  to be used
     * @param serviceInfo      to be used
     * @param args             to be passed to the resource
     */
    public static void execute(ProgramFile programFile, FunctionInfo resourceInfo,
                               CallableUnitCallback responseCallback, Map<String, Object> properties,
                               ObserverContext observerContext, ServiceInfo serviceInfo, BValue... args) {
        Strand strand = populateAndGetStrand(programFile, resourceInfo, responseCallback, properties, observerContext,
                                             serviceInfo, args);
        BVMScheduler.execute(strand);
    }

    private static Strand populateAndGetStrand(ProgramFile programFile, FunctionInfo resourceInfo,
                                               CallableUnitCallback responseCallback, Map<String, Object> properties,
                                               ObserverContext observerContext, ServiceInfo serviceInfo,
                                               BValue[] args) {
        Map<String, Object> globalProps = new HashMap<>();
        if (properties != null) {
            globalProps.putAll(properties);
        }

        StrandResourceCallback strandCallback = new StrandResourceCallback(null, responseCallback,
                                                                           resourceInfo.workerSendInChannels);
        Strand strand = new Strand(programFile, resourceInfo.getName(), globalProps, strandCallback);

        infectResourceFunction(strandCallback, strand);
        BLangVMUtils.setServiceInfo(strand, serviceInfo);

        StackFrame idf = new StackFrame(resourceInfo.getPackageInfo(), resourceInfo,
                                        resourceInfo.getDefaultWorkerInfo().getCodeAttributeInfo(), -1,
                                        FunctionFlags.NOTHING, resourceInfo.workerSendInChannels);
        copyArgValues(args, idf, resourceInfo.getParamTypes());
        strand.pushFrame(idf);
        // Start observation after pushing the stack frame
        ObserveUtils.startResourceObservation(strand, observerContext);

        BVMScheduler.stateChange(strand, State.NEW, State.RUNNABLE);
        return strand;
    }

    private static void infectResourceFunction(StrandResourceCallback strandResourceCallback, Strand strand) {
        String gTransactionId = (String) strand.globalProps.get(Constants.GLOBAL_TRANSACTION_ID);
        if (gTransactionId != null) {
            String globalTransactionId = strand.globalProps.get(Constants.GLOBAL_TRANSACTION_ID).toString();
            String url = strand.globalProps.get(Constants.TRANSACTION_URL).toString();
            TransactionLocalContext transactionLocalContext = TransactionLocalContext.create(globalTransactionId,
                                                                                             url, "2pc");
            strand.setLocalTransactionContext(transactionLocalContext);
            strandResourceCallback.setTransactionLocalContext(transactionLocalContext);
        }
    }

    private static BValue execute(ProgramFile programFile, CallableUnitInfo callableInfo,
                                  BValue[] args, Map<String, Object> properties, boolean waitForResponse) {
        Map<String, Object> globalProps = new HashMap<>();
        if (properties != null) {
            globalProps.putAll(properties);
        }

        StrandWaitCallback strandCallback = new StrandWaitCallback(callableInfo.getRetParamTypes()[0],
                                                                   callableInfo.workerSendInChannels);
        Strand strand = new Strand(programFile, callableInfo.getName(), globalProps, strandCallback);

        StackFrame idf = new StackFrame(callableInfo.getPackageInfo(), callableInfo,
                                        callableInfo.getDefaultWorkerInfo().getCodeAttributeInfo(), -1,
                                        FunctionFlags.NOTHING,
                                        callableInfo.workerSendInChannels);
        copyArgValues(args, idf, callableInfo.getParamTypes());
        strand.pushFrame(idf);

        BVMScheduler.stateChange(strand, State.NEW, State.RUNNABLE);
        BVMScheduler.execute(strand);

        if (waitForResponse) {
            strandCallback.waitForResponse();
            if (strand.getError() != null) {
                throw new BLangRuntimeException("error: " + BLangVMErrors.getPrintableStackTrace(strand.getError()));
            }
        }

        return populateReturnData(strandCallback, callableInfo);
    }

    private static void copyArgValues(BValue[] args, StackFrame callee, BType[] paramTypes) {
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
                    callee.longRegs[++longRegIndex] = ((BValueType) args[i]).byteValue();
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
                                ((BString) args[i]).value().equalsIgnoreCase("true") ? 1 : 0;
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
                returnValue = new BByte(strandCallback.getByteRetVal());
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
    public static void invokePackageInitFunctions(ProgramFile programFile) {
        for (PackageInfo info : programFile.getPackageInfoEntries()) {
            BValue result = execute(programFile, info.getInitFunctionInfo(), new BValue[0], null, true);
            validateInvocationError(result);
        }
    }

    /**
     * This will invoke package start functions, this should be invoked after invoking "invokePackageInitFunctions".
     *
     * @param programFile to be invoked.
     */
    public static void invokePackageStartFunctions(ProgramFile programFile) {
        for (PackageInfo info : programFile.getPackageInfoEntries()) {
            BValue result = execute(programFile, info.getStartFunctionInfo(), new BValue[0], null, true);
            validateInvocationError(result);
        }
    }

    /**
     * This will invoke package start functions, this should be invoked after invoking "invokePackageInitFunctions".
     *
     * @param programFile to be invoked.
     */
    private static void invokePackageStopFunctions(ProgramFile programFile) {
        for (PackageInfo info : programFile.getPackageInfoEntries()) {
            BValue result = execute(programFile, info.getStopFunctionInfo(), new BValue[0], null, true);
            validateInvocationError(result);
        }
    }

    /**
     * This will validate given BValue is an BError and then convert it into Ballerina exception. This method will be
     * used to validate module life cycle methods.
     *
     * @value BValue to be validated.
     */
    private static void validateInvocationError(BValue value) {
        if (value != null && value.getType().getTag() == TypeTags.ERROR_TAG) {
            throw new BLangRuntimeException("error: " + BLangVMErrors.getPrintableStackTrace((BError) value));
        }
    }

    private BVMExecutor() {
    }
}
