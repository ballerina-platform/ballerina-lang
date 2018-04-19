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
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.PrintStream;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * This class contains helper functions to invoke workers.
 *
 * @since 0.88
 */
public class BLangVMWorkers {

    public static void invoke(ProgramFile programFile, CallableUnitInfo callableUnitInfo, Context parent,
                              Map<String, Object> properties) {
//        StackFrame callerSF = parent.getControlStack().currentFrame;
//        WorkerReturnIndex workerReturnIndex = calculateWorkerReturnIndex(callableUnitInfo.getRetParamTypes());
//
//        for (WorkerInfo workerInfo : callableUnitInfo.getWorkerInfoMap().values()) {
//            WorkerContext workerContext = new WorkerContext(programFile, parent);
//            workerContext.setStartIP(workerInfo.getCodeAttributeInfo().getCodeAddrs());
//
//            if (properties != null) {
//                properties.forEach(workerContext::setProperty);
//            }
//
//            populateWorkerStack(callableUnitInfo, workerInfo, workerContext, workerReturnIndex, callerSF);
//
//            BLangVM bLangVM = new BLangVM(programFile);
//            ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
//            WorkerExecutor workerRunner = new WorkerExecutor(bLangVM, workerContext, workerInfo,
//                    new ConcurrentLinkedQueue<>());
//            workerContext.startTrackWorker();
//            executor.submit(workerRunner);
//        }

    }

    public static void invoke(ProgramFile programFile, CallableUnitInfo callableUnitInfo, Context parent) {
        invoke(programFile, callableUnitInfo, parent, parent.getProperties());
    }

    private static void populateWorkerStack(CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, Context ctx,
                                            WorkerReturnIndex returnIndex, StackFrame callerSF) {
        ControlStack controlStack = null; // = ctx.getControlStack();
        StackFrame startSF = new StackFrame(callableUnitInfo.getPackageInfo(), -1, new int[0]);
        controlStack.pushFrame(startSF);

        startSF.setLongRegs(new long[returnIndex.longRegCount]);
        startSF.setDoubleRegs(new double[returnIndex.doubleRegCount]);
        startSF.setStringRegs(new String[returnIndex.stringRegCount]);
        startSF.setIntRegs(new int[returnIndex.intRegCount]);
        startSF.setRefRegs(new BRefType[returnIndex.refRegCount]);
        startSF.setByteRegs(new byte[returnIndex.byteRegCount][]);

        StackFrame calleeSF = new StackFrame(callableUnitInfo, workerInfo, -1, returnIndex.retRegs);
        controlStack.pushFrame(calleeSF);

        // Copy values from the current StackFrame to the new StackFrame
//        BLangVM.copyValues(callerSF, calleeSF);
    }

    private static WorkerReturnIndex calculateWorkerReturnIndex(BType[] retTypes) {
        WorkerReturnIndex index = new WorkerReturnIndex();
        index.retRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i].getSuperType();
            switch (retType.getTag()) {
                case TypeTags.INT_TAG:
                    index.retRegs[i] = index.longRegCount++;
                    break;
                case TypeTags.FLOAT_TAG:
                    index.retRegs[i] = index.doubleRegCount++;
                    break;
                case TypeTags.STRING_TAG:
                    index.retRegs[i] = index.stringRegCount++;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    index.retRegs[i] = index.intRegCount++;
                    break;
                case TypeTags.BLOB_TAG:
                    index.retRegs[i] = index.byteRegCount++;
                    break;
                default:
                    index.retRegs[i] = index.refRegCount++;
                    break;
            }
        }
        return index;
    }

    static class WorkerExecutor implements Runnable {

        private static PrintStream outStream = System.out;

//        private BLangVM bLangVM;
        private Context bContext;
        private WorkerInfo workerInfo;
        private Queue<WorkerResult> resultHolder;
        private Semaphore resultCounter;

        public WorkerExecutor(Context bContext, WorkerInfo workerInfo,
                Queue<WorkerResult> resultHolder) {
//            this.bLangVM = bLangVM;
            this.bContext = bContext;
            this.workerInfo = workerInfo;
            this.resultHolder = resultHolder;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void run() throws BallerinaException {
//            BRefValueArray bRefValueArray = new BRefValueArray(new BArrayType(BTypes.typeAny));
//            CPU.execWorker(bContext, workerInfo.getCodeAttributeInfo().getCodeAddrs());
//            if (bContext.getError() != null) {
//                String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
//                outStream.println("error in worker '" + workerInfo.getWorkerName() + "': " + stackTraceStr);
//            }
//
//            if (workerInfo.getWorkerDataChannelInfoForForkJoin() != null) {
//                //BValue[] results = (BValue[]) workerInfo.getWorkerDataChannelInfoForForkJoin().tryTakeData(null);
//                BValue[] results = null;
//                BType[] types = workerInfo.getWorkerDataChannelInfoForForkJoin().getTypes();
//                for (int i = 0; i < types.length; i++) {
//                    BType paramType = types[i];
//                    switch (paramType.getTag()) {
//                        case TypeTags.INT_TAG:
//                            bRefValueArray.add(i, ((BInteger) results[i]));
//                            break;
//                        case TypeTags.FLOAT_TAG:
//                            bRefValueArray.add(i, ((BFloat) results[i]));
//                            break;
//                        case TypeTags.STRING_TAG:
//                            bRefValueArray.add(i, ((BString) results[i]));
//                            break;
//                        case TypeTags.BOOLEAN_TAG:
//                            bRefValueArray.add(i, ((BBoolean) results[i]));
//                            break;
//                        case TypeTags.BLOB_TAG:
//                            bRefValueArray.add(i, ((BBlob) results[i]));
//                            break;
//                        default:
//                            bRefValueArray.add(i, ((BRefType) results[i]));
//                    }
//                }
//            }
//
//            this.resultHolder.add(new WorkerResult(workerInfo.getWorkerName(), bRefValueArray));
//            if (this.resultCounter != null) {
//                this.resultCounter.release();
//            }
        }
//
//        public void setResultCounterSemaphore(Semaphore resultCounter) {
//            this.resultCounter = resultCounter;
//        }
//
    }

    static class WorkerReturnIndex {
        int[] retRegs;
        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;
        int byteRegCount = 0;
    }
}
