/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.DefaultBalCallback;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class contains helper functions to invoke workers.
 *
 * @since 0.88
 */
public class BLangVMWorkers {

    public static void invoke(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                              StackFrame callerSF, int[] argRegs, Context bContext, WorkerInfo defaultWorkerInfo,
                              int[] retRegs, Map<String, Object> properties) {

        BType[] paramTypes = callableUnitInfo.getParamTypes();
        List<WorkerExecutor> workerRunnerList = new ArrayList<>();
        BlockingQueue<StackFrame> resultChannel = new LinkedBlockingQueue<>();

        createWorkerStackFrame(programFile, callableUnitInfo, bContext, defaultWorkerInfo, callerSF, retRegs,
                argRegs, null, paramTypes, properties, workerRunnerList, resultChannel);


        for (WorkerInfo workerInfo : callableUnitInfo.getWorkerInfoMap().values()) {
            createWorkerStackFrame(programFile, callableUnitInfo, bContext, workerInfo, callerSF, retRegs,
                    argRegs, null, paramTypes, properties, workerRunnerList, resultChannel);
        }

        // Execute the workers
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        for (WorkerExecutor workerExecutor : workerRunnerList) {
            executor.submit(workerExecutor);
        }

        copyResults(callerSF, resultChannel);
    }

    public static void invoke(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                              StackFrame callerSF, Context bContext, WorkerInfo defaultWorkerInfo, BValue[] args,
                              int[] retRegs, Map<String, Object> properties) {

        BType[] paramTypes = callableUnitInfo.getParamTypes();
        List<WorkerExecutor> workerRunnerList = new ArrayList<>();
        BlockingQueue<StackFrame> resultChannel = new LinkedBlockingQueue<>();

        createWorkerStackFrame(programFile, callableUnitInfo, bContext, defaultWorkerInfo, callerSF, retRegs,
                null, args, paramTypes, properties, workerRunnerList, resultChannel);


        for (WorkerInfo workerInfo : callableUnitInfo.getWorkerInfoMap().values()) {
            createWorkerStackFrame(programFile, callableUnitInfo, bContext, workerInfo, callerSF, retRegs,
                    null, args, paramTypes, properties, workerRunnerList, resultChannel);
        }

        //resultMsgs = invokeAllWorkers(workerRunnerList, timeout);
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        for (WorkerExecutor workerExecutor : workerRunnerList) {
            executor.submit(workerExecutor);
        }

        copyResults(callerSF, resultChannel);
    }

    private static void createWorkerStackFrame(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                               Context bContext,
                                               WorkerInfo defaultWorkerInfo, StackFrame stackFrame, int[] retRegs,
                                               int[] argRegs, BValue[] args, BType[] paramTypes,
                                               Map<String, Object> properties,
                                               List<WorkerExecutor> workerRunnerList,
                                               BlockingQueue<StackFrame> resultChannel) {

        Context defaultWorkerContext = new Context(programFile);
        DefaultBalCallback defaultWorkerCallback = new DefaultBalCallback(bContext.getBalCallback());
        defaultWorkerContext.setBalCallback(defaultWorkerCallback);
        defaultWorkerContext.setStartIP(defaultWorkerInfo.getCodeAttributeInfo().getCodeAddrs());

        if (properties != null) {
            properties.forEach((property, value) -> defaultWorkerContext.setProperty(property, value));
        }

        ControlStackNew defaultControlStack = defaultWorkerContext.getControlStackNew();
        org.ballerinalang.bre.bvm.StackFrame defaultCallerSF =
                new org.ballerinalang.bre.bvm.StackFrame(callableUnitInfo, defaultWorkerInfo, -1, retRegs);
        defaultControlStack.pushFrame(defaultCallerSF);

        defaultCallerSF.setLongRegs(new long[stackFrame.getLongRegs().length]);
        defaultCallerSF.setDoubleRegs(new double[stackFrame.getDoubleRegs().length]);
        defaultCallerSF.setStringRegs(new String[stackFrame.getStringRegs().length]);
        defaultCallerSF.setIntRegs(new int[stackFrame.getIntRegs().length]);
        defaultCallerSF.setRefRegs(new BRefType[stackFrame.getRefRegs().length]);
        defaultCallerSF.setByteRegs(new byte[stackFrame.getByteRegs().length][]);

        org.ballerinalang.bre.bvm.StackFrame defaultCalleeSF =
                new org.ballerinalang.bre.bvm.StackFrame(callableUnitInfo, defaultWorkerInfo, -1, retRegs);
        defaultControlStack.pushFrame(defaultCalleeSF);

        if (argRegs != null) {
            BLangVM.copyArgValuesWorker(stackFrame, defaultCalleeSF, argRegs, paramTypes);
        } else if (args != null) {
            setupCalleeSF(defaultWorkerInfo, paramTypes, args, defaultCalleeSF);
        }

        BLangVM bLangVM = new BLangVM(programFile);
        BLangVMWorkers.WorkerExecutor workerRunner = new BLangVMWorkers.WorkerExecutor(bLangVM,
                defaultWorkerContext, defaultWorkerInfo, resultChannel, callableUnitInfo.getRetParamTypes());
        if (callableUnitInfo instanceof ResourceInfo) {
            workerRunner.setFromResource(true);
        }
        workerRunnerList.add(workerRunner);
    }

    private static void setupCalleeSF(WorkerInfo defaultWorkerInfo, BType[] paramTypes, BValue[] args,
                                      StackFrame calleeSF) {
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

        if (args.length > 0) {
            for (int i = 0; i < paramTypes.length; i++) {
                BType argType = paramTypes[i];
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
        }

        calleeSF.setLongLocalVars(longLocalVars);
        calleeSF.setDoubleLocalVars(doubleLocalVars);
        calleeSF.setStringLocalVars(stringLocalVars);
        calleeSF.setIntLocalVars(intLocalVars);
        calleeSF.setByteLocalVars(byteLocalVars);
        calleeSF.setRefLocalVars(refLocalVars);
    }

    private static void copyResults(StackFrame callerSF, BlockingQueue<StackFrame> resultChannel) {

        int longRegCount = callerSF.getLongRegs().length;
        int doubleRegCount = callerSF.getDoubleRegs().length;
        int stringRegCount = callerSF.getStringRegs().length;
        int intRegCount = callerSF.getIntRegs().length;
        int refRegCount = callerSF.getRefRegs().length;
        int byteRegCount = callerSF.getByteRegs().length;

        try {
            // Taking the results from the blocking queue. Whoever puts results in to this queue will win the
            // return race.
            StackFrame stackFrame = resultChannel.poll(60000, TimeUnit.MILLISECONDS);
            if (stackFrame != null) {

                for (int i = 0; i < longRegCount; i++) {
                    callerSF.longRegs[i] = stackFrame.getLongRegs()[i];
                }

                for (int i = 0; i < doubleRegCount; i++) {
                    callerSF.doubleRegs[i] = stackFrame.getDoubleRegs()[i];
                }

                for (int i = 0; i < stringRegCount; i++) {
                    if (stackFrame.getStringRegs()[i] != null) {
                        callerSF.stringRegs[i] = stackFrame.getStringRegs()[i];
                    }
                }

                for (int i = 0; i < intRegCount; i++) {
                    callerSF.intRegs[i] = stackFrame.getIntRegs()[i];
                }

                for (int i = 0; i < refRegCount; i++) {
                    if (stackFrame.getRefRegs()[i] != null) {
                        callerSF.refRegs[i] = stackFrame.getRefRegs()[i];
                    }
                }

                for (int i = 0; i < byteRegCount; i++) {
                    if (stackFrame.getByteRegs()[i] != null) {
                        callerSF.byteRegs[i] = stackFrame.getByteRegs()[i];
                    }
                }
            }


        } catch (InterruptedException e) {
            //Ignore the error here
        }
    }

    public static void invoke(ProgramFile programFile, CallableUnitInfo callableUnitInfo, StackFrame callerSF,
                              int[] argRegs) {
        invoke(programFile, callableUnitInfo, callerSF, argRegs, null, null, null, null);
    }

    static class WorkerExecutor implements Callable<WorkerResult> {

        private static final Logger log = LoggerFactory.getLogger(WorkerExecutor.class);
        private static PrintStream outStream = System.out;

        private BLangVM bLangVM;
        private Context bContext;
        private WorkerInfo workerInfo;
        private BlockingQueue<StackFrame> resultChannel;
        BType[] returnTypes;
        private boolean isFromResource;

        public WorkerExecutor(BLangVM bLangVM, Context bContext, WorkerInfo workerInfo) {
            this.bLangVM = bLangVM;
            this.bContext = bContext;
            this.workerInfo = workerInfo;
        }

        public WorkerExecutor(BLangVM bLangVM, Context bContext, WorkerInfo workerInfo,
                              BlockingQueue<StackFrame> resultChannel, BType[] returnTypes) {
            this.bLangVM = bLangVM;
            this.bContext = bContext;
            this.workerInfo = workerInfo;
            this.resultChannel = resultChannel;
            this.returnTypes = returnTypes;
        }

        public void setFromResource(boolean fromResource) {
            isFromResource = fromResource;
        }

        @Override
        public WorkerResult call() throws BallerinaException {
            BRefValueArray bRefValueArray = new BRefValueArray(new BArrayType(BTypes.typeAny));
            bLangVM.execWorker(bContext, workerInfo.getCodeAttributeInfo().getCodeAddrs());
            if (bContext.getError() != null && resultChannel == null) {
                String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
                outStream.println("error in worker '" + workerInfo.getWorkerName() + "': " + stackTraceStr);
            }

            if (workerInfo.getWorkerDataChannelInfoForForkJoin() != null) {
                BValue[] results = (BValue[]) workerInfo.getWorkerDataChannelInfoForForkJoin().takeData();
                BType[] types = workerInfo.getWorkerDataChannelInfoForForkJoin().getTypes();
                for (int i = 0; i < types.length; i++) {
                    BType paramType = types[i];
                    switch (paramType.getTag()) {
                        case TypeTags.INT_TAG:
                            bRefValueArray.add(i, ((BInteger) results[i]));
                            break;
                        case TypeTags.FLOAT_TAG:
                            bRefValueArray.add(i, ((BFloat) results[i]));
                            break;
                        case TypeTags.STRING_TAG:
                            bRefValueArray.add(i, ((BString) results[i]));
                            break;
                        case TypeTags.BOOLEAN_TAG:
                            bRefValueArray.add(i, ((BBoolean) results[i]));
                            break;
                        case TypeTags.BLOB_TAG:
                            bRefValueArray.add(i, ((BBlob) results[i]));
                            break;
                        default:
                            bRefValueArray.add(i, ((BRefType) results[i]));
                    }
                }
            }

            if (returnTypes != null && returnTypes.length > 0 && resultChannel != null &&
                    bContext.getControlStackNew() != null &&
                    bContext.getControlStackNew().getCurrentFrame() != null &&
                    bContext.getControlStackNew().getCurrentFrame().isCalleeReturned()) {
                try {
                    resultChannel.put(bContext.getControlStackNew().getCurrentFrame());
                } catch (InterruptedException e) {
                    // Ignore the error. May be someone else is trying to add to the channel.
                }
            } else if (returnTypes != null && returnTypes.length == 0 &&
                    bContext.getControlStackNew() != null &&
                    bContext.getControlStackNew().getCurrentFrame() != null) {
                if (isFromResource) {
                    if (bContext.getControlStackNew().getCurrentFrame().isCalleeReturned()) {
                        try {
                            resultChannel.put(bContext.getControlStackNew().getCurrentFrame());
                        } catch (InterruptedException e) {
                            // Ignore the error. May be someone else is trying to add to the channel.
                        }
                    }
                } else {
                    try {
                        resultChannel.put(bContext.getControlStackNew().getCurrentFrame());
                    } catch (InterruptedException e) {
                        // Ignore the error. May be someone else is trying to add to the channel.
                    }
                }

            }
            WorkerResult workerResult = new WorkerResult(workerInfo.getWorkerName(), bRefValueArray);
            return workerResult;
        }
    }
}
