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
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.runtime.worker.WorkerCallback;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * This class contains helper functions to invoke workers.
 *
 * @since 0.88
 */
public class BLangVMWorkers {

    public static void invoke(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                              StackFrame callerSF, int[] argRegs) {
        BType[] paramTypes = callableUnitInfo.getParamTypes();

        for (WorkerInfo workerInfo : callableUnitInfo.getWorkerInfoMap().values()) {
            Context workerContext = new Context(programFile);
            WorkerCallback workerCallback = new WorkerCallback(workerContext);
            workerContext.setBalCallback(workerCallback);
            workerContext.setStartIP(workerInfo.getCodeAttributeInfo().getCodeAddrs());

            ControlStackNew controlStack = workerContext.getControlStackNew();
            StackFrame calleeSF = new StackFrame(callableUnitInfo, workerInfo, -1, new int[0]);
            controlStack.pushFrame(calleeSF);

            // Copy arg values from the current StackFrame to the new StackFrame
            // TODO fix this. Move the copyArgValues method to another util function
            BLangVM.copyArgValuesWorker(callerSF, calleeSF, argRegs, paramTypes);

            BLangVM bLangVM = new BLangVM(programFile);
            ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
            WorkerExecutor workerRunner = new WorkerExecutor(bLangVM, workerContext, workerInfo);
            executor.submit(workerRunner);
        }

    }

    static class WorkerExecutor implements Callable<WorkerResult> {

        private static final Logger log = LoggerFactory.getLogger(WorkerExecutor.class);
        private static PrintStream outStream = System.out;

        private BLangVM bLangVM;
        private Context bContext;
        private WorkerInfo workerInfo;

        public WorkerExecutor(BLangVM bLangVM, Context bContext, WorkerInfo workerInfo) {
            this.bLangVM = bLangVM;
            this.bContext = bContext;
            this.workerInfo = workerInfo;
        }

        @Override
        public WorkerResult call() throws BallerinaException {
            BRefValueArray bRefValueArray = new BRefValueArray(new BArrayType(BTypes.typeAny));
            bLangVM.execWorker(bContext, workerInfo.getCodeAttributeInfo().getCodeAddrs());
            if (bContext.getError() != null) {
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

            return new WorkerResult(workerInfo.getWorkerName(), bRefValueArray);
        }
    }
}
