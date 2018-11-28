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

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.vm.Strand.State;
import org.ballerinalang.config.ConfigRegistry;
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
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.LocalTransactionInfo;
import org.ballerinalang.util.transactions.TransactableCallableUnitCallback;
import org.ballerinalang.util.transactions.TransactionResourceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the executor class which initialize the execution.
 *
 * @since 0.985.0
 */
public class BVMExecutor {

    /**
     * Execution API to execute package init and package start functions for the whole program file.
     *
     * @param programFile to be initialized
     */
    public static void initProgramFile(ProgramFile programFile) {
        invokePackageInitFunctions(programFile);
        invokePackageStartFunctions(programFile);
    }

    /**
     * This will invoke package start functions.
     *
     * @param programFile to be invoked.
     */
    public static void stopProgramFile(ProgramFile programFile) {
        invokePackageStopFunctions(programFile);
    }

    /**
     * Execution API to execute program file starting from the given callable unit.
     * this will call package init and start functions as well.
     *
     * @param programFile  to be executed
     * @param functionInfo to be executed
     * @param args         to be passed to the invokable unit
     * @return results
     */
    public static BValue[] executeEntryFunction(ProgramFile programFile, FunctionInfo functionInfo, BValue... args) {
        int requiredArgNo = functionInfo.getParamTypes().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                    providedArgNo + ".");
        }
        initProgramFile(programFile);
//        new BalxEmitter().emit(programFile);
        BValue[] result = new BValue[]{execute(programFile, functionInfo, args, null, true)};
        BVMScheduler.waitForStrandCompletion();
        return result;
    }

    /**
     * Execution API to execute just a function.
     *
     * @param programFile   to be executed
     * @param functionInfo  to be executed
     * @param args          to be passed to invokable unit
     * @return results
     */
    public static BValue[] executeFunction(ProgramFile programFile, FunctionInfo functionInfo, BValue... args) {
        int requiredArgNo = functionInfo.getParamTypes().length;
        int providedArgNo = args.length;
        if (requiredArgNo != providedArgNo) {
            throw new RuntimeException("Wrong number of arguments. Required: " + requiredArgNo + " , found: " +
                    providedArgNo + ".");
        }
        BValue[] result = new BValue[]{execute(programFile, functionInfo, args, null, true)};
        return result;
    }

    /**
     * Execution API to execute a resource.
     *
     * @param programFile       to be executed
     * @param resourceInfo      to be executed
     * @param responseCallback  to be used for notifications
     * @param properties        to be passed in the context
     * @param observerContext   to be used
     * @param serviceInfo       to be used
     * @param args              to be passed to the resource
     */
    public static void executeResource(ProgramFile programFile, FunctionInfo resourceInfo,
                                       CallableUnitCallback responseCallback, Map<String, Object> properties,
                                       ObserverContext observerContext, ServiceInfo serviceInfo, BValue... args) {
        Map<String, Object> globalProps = new HashMap<>();

        StrandResourceCallback strandCallback = new StrandResourceCallback(null, responseCallback);
        Strand strand = new Strand(programFile, resourceInfo.getName(), properties, strandCallback, null);
        configureDistributedTransactions(strand);

        if (properties != null) {
            //TODO fix - rajith
//            Object interruptible = properties.get(Constants.IS_INTERRUPTIBLE);
//            if (interruptible != null && (boolean) interruptible) {
//                String stateId = UUID.randomUUID().toString();
//                properties.put(Constants.STATE_ID, stateId);
//                RuntimeStates.add(new State(context, stateId));
//                context.interruptible = true;
//            }
            globalProps.putAll(properties);
            String gTransactionId = (String) properties.get(Constants.GLOBAL_TRANSACTION_ID);
            if (gTransactionId != null) {
                String globalTransactionId = properties.get(Constants.GLOBAL_TRANSACTION_ID).toString();
                LocalTransactionInfo localTransactionInfo = new LocalTransactionInfo(
                        globalTransactionId,
                        properties.get(Constants.TRANSACTION_URL).toString(), "2pc");
                strand.setLocalTransactionInfo(localTransactionInfo);
                registerTransactionInfection(responseCallback, gTransactionId, strand);
            }
        }

        BLangVMUtils.setServiceInfo(strand, serviceInfo);

        StackFrame idf = new StackFrame(resourceInfo.getPackageInfo(), resourceInfo,
                resourceInfo.getDefaultWorkerInfo().getCodeAttributeInfo(), -1);
        copyArgValues(args, idf, resourceInfo.getParamTypes());
        strand.pushFrame(idf);

        BVMScheduler.stateChange(strand, State.NEW, State.RUNNABLE);
        BVMScheduler.schedule(strand);
    }

    private static void registerTransactionInfection(CallableUnitCallback responseCallBack, String globalTransactionId,
                                                     Strand strand) {
        if (globalTransactionId != null && responseCallBack instanceof TransactableCallableUnitCallback) {
            TransactableCallableUnitCallback trxCallBack = (TransactableCallableUnitCallback) responseCallBack;
            TransactionResourceManager manager = TransactionResourceManager.getInstance();
            manager.registerParticipation(globalTransactionId, trxCallBack.getTransactionBlockId(),
                    trxCallBack.getTransactionOnCommit(),
                    trxCallBack.getTransactionOnAbort(),
                    strand);
        }
    }

    private static BValue execute(ProgramFile programFile, CallableUnitInfo callableInfo,
                                  BValue[] args, Map<String, Object> properties, boolean waitForResponse) {
        StrandWaitCallback strandCallback = new StrandWaitCallback(callableInfo.getRetParamTypes()[0]);
        Strand strand = new Strand(programFile, callableInfo.getName(), properties, strandCallback,  null);
        configureDistributedTransactions(strand);

        StackFrame idf = new StackFrame(callableInfo.getPackageInfo(), callableInfo,
                callableInfo.getDefaultWorkerInfo().getCodeAttributeInfo(), -1);
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

        BValue result = populateReturnData(strandCallback, callableInfo);
        return result;
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
            execute(programFile, info.getInitFunctionInfo(), new BValue[0], null, true);
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
            execute(programFile, info.getStartFunctionInfo(), new BValue[0], null, true);
        }
    }

    /**
     * This will invoke package start functions, this should be invoked after
     * invoking "invokePackageInitFunctions".
     *
     * @param programFile to be invoked.
     */
    private static void invokePackageStopFunctions(ProgramFile programFile) {
        for (PackageInfo info : programFile.getPackageInfoEntries()) {
            execute(programFile, info.getStopFunctionInfo(), new BValue[0], null, true);
        }
    }


    private static final String FALSE = "false";
    private static final String DISTRIBUTED_TRANSACTIONS = "b7a.distributed.transactions.enabled";
    private static void configureDistributedTransactions(Strand strand) {
        String distributedTransactionsEnabledConfig = ConfigRegistry.getInstance()
                .getAsString(DISTRIBUTED_TRANSACTIONS);
        boolean distributedTransactionEnabled = true;
        if (distributedTransactionsEnabledConfig != null && distributedTransactionsEnabledConfig.equals(FALSE)) {
            distributedTransactionEnabled = false;
        }
        BLangVMUtils.setGlobalTransactionEnabledStatus(strand, distributedTransactionEnabled);
    }
}
