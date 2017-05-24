/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre;

import org.ballerinalang.model.Worker;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.concurrent.Callable;

/**
 * A {@code WorkerRunner} Callable class which is running in a separate worker thread when worker is invoked.
 * <p>
 *
 * @since 0.8.0
 */
public class WorkerRunner implements Callable<BValue[]> {

    private static final Logger log = LoggerFactory.getLogger(WorkerRunner.class);
    private static PrintStream outStream = System.err;

    private BLangExecutor executor;
    private Context bContext;
    private Worker worker;

    public WorkerRunner(BLangExecutor executor, Context bContext, Worker worker) {
        this.executor = executor;
        this.bContext = bContext;
        this.worker = worker;
    }


    @Override
    public BValue[] call() throws BallerinaException {
        try {
            worker.getCallableUnitBody().execute(executor);
            BValue[] results;
            if (bContext.getControlStack().getCurrentFrame().returnValues[0] instanceof BArray) {
                BArray resultArray = (BArray) (bContext.getControlStack().
                        getCurrentFrame().returnValues[0]);
                results = new BValue[resultArray.size()];
                for (int i = 0; i < results.length; i++) {
                    results[i] = resultArray.get(i);
                }
                return results;
            } else {
                return bContext.getControlStack().getCurrentFrame().returnValues;
            }
        } catch (RuntimeException throwable) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(throwable);
            String stacktrace = ErrorHandlerUtils.getServiceStackTrace(bContext, throwable);
            String errorWithTrace = "exception in worker" + worker.getName() + " : " + errorMsg + "\n" + stacktrace;
            log.error(errorWithTrace);
            outStream.println(errorWithTrace);
            return new BValue[0];
        }
    }

    public Worker getWorker() {
        return worker;
    }
}
