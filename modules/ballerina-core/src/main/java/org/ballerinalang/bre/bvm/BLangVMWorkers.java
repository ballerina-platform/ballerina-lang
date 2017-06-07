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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.runtime.worker.WorkerCallback;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * This class contains helper functions to invoke workers
 *
 * @since 0.88
 */
public class BLangVMWorkers {

    public static void invoke(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                              StackFrame callerSF, int[] argRegs) {
        BType[] paramTypes = callableUnitInfo.getParamTypes();

        for (WorkerInfo workerInfo : callableUnitInfo.getWorkerInfoMap().values()) {
            Context workerContext = new Context();
            WorkerCallback workerCallback = new WorkerCallback(workerContext);
            workerContext.setBalCallback(workerCallback);

            ControlStackNew controlStack = workerContext.getControlStackNew();
            StackFrame calleeSF = new StackFrame(callableUnitInfo, workerInfo, -1, new int[0]);
            controlStack.pushFrame(calleeSF);

            // Copy arg values from the current StackFrame to the new StackFrame
            // TODO fix this. Move the copyArgValues method to another util function
            BLangVM.copyArgValues(callerSF, calleeSF, argRegs, paramTypes);

            BLangVM bLangVM = new BLangVM(programFile);
            ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
            WorkerExecutor workerRunner = new WorkerExecutor(bLangVM, callableUnitInfo, workerContext, workerInfo);
            executor.submit(workerRunner);
        }

    }

    static class WorkerExecutor implements Callable<BValue[]> {

        private static final Logger log = LoggerFactory.getLogger(org.ballerinalang.bre.WorkerExecutor.class);
//        private static PrintStream outStream = System.err;

        private BLangVM bLangVM;
        private CallableUnitInfo callableUnitInfo;
        private Context bContext;
        private WorkerInfo workerInfo;

        public WorkerExecutor(BLangVM bLangVM, CallableUnitInfo callableUnitInfo,
                              Context bContext, WorkerInfo workerInfo) {
            this.bLangVM = bLangVM;
            this.callableUnitInfo = callableUnitInfo;
            this.bContext = bContext;
            this.workerInfo = workerInfo;
        }

        @Override
        public BValue[] call() throws BallerinaException {
            try {
                bLangVM.execWorker(callableUnitInfo.getPackageInfo(), bContext,
                        workerInfo.getCodeAttributeInfo().getCodeAddrs(), workerInfo.getWorkerEndIP());
                BValue[] results = new BValue[0];
                if (workerInfo.getWorkerDataChannelForForkJoin() != null) {
                    results = (BValue[]) workerInfo.getWorkerDataChannelForForkJoin().takeData();
                }
                return results;
//                worker.getCallableUnitBody().execute(executor);
            } catch (RuntimeException throwable) {
                String errorMsg = ErrorHandlerUtils.getErrorMessage(throwable);
                String stacktrace = ErrorHandlerUtils.getServiceStackTrace(bContext, throwable);
                String errorWithTrace = "exception in worker" + workerInfo.getWorkerName() + " : " + errorMsg +
                                            "\n" + stacktrace;
                log.error(errorWithTrace);
                //outStream.println(errorWithTrace);
            }
            return new BValue[0];
        }
    }
}
