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
import org.ballerinalang.services.ErrorHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

/**
 * A {@code WorkerExecutor} Runnable class which is running in a separate worker thread when worker is invoked.
 * <p>
 *
 * @since 0.9
 */
public class WorkerExecutor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WorkerRunner.class);
    private static PrintStream outStream = System.err;

    private BLangExecutor executor;
    private Context bContext;
    private Worker worker;

    public WorkerExecutor(BLangExecutor executor, Context bContext, Worker worker) {
        this.executor = executor;
        this.bContext = bContext;
        this.worker = worker;
    }

    @Override
    public void run() {
        try {
            worker.getCallableUnitBody().execute(executor);
        } catch (RuntimeException throwable) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(throwable);
            String stacktrace = ErrorHandlerUtils.getServiceStackTrace(bContext, throwable);
            String errorWithTrace = "exception in worker" + worker.getName() + " : " + errorMsg + "\n" + stacktrace;
            log.error(errorWithTrace);
            outStream.println(errorWithTrace);
        }
    }
}
