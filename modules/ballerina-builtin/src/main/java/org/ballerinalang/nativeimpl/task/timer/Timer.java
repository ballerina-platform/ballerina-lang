/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.ballerinalang.nativeimpl.task.timer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.WorkerContext;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.task.SchedulingException;
import org.ballerinalang.nativeimpl.task.TaskException;
import org.ballerinalang.nativeimpl.task.TaskIdGenerator;
import org.ballerinalang.nativeimpl.task.TaskRegistry;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a timer.
 */
public class Timer {
    private String id = TaskIdGenerator.generate();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private Context context;

    /**
     * Triggers the timer.
     *
     * @param ctx               The ballerina context.
     * @param delay             The initial delay.
     * @param interval          The interval between two task executions.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     */
    public Timer(Context ctx, long delay, long interval,
                 FunctionRefCPEntry onTriggerFunction,
                 FunctionRefCPEntry onErrorFunction) throws SchedulingException {

        if (delay < 0 || interval < 0) {
            throw new SchedulingException("Timer scheduling delay and interval should be non-negative values");
        }

        final Runnable schedulerFunc = () -> {
            callTriggerFunction(ctx, onTriggerFunction, onErrorFunction);
        };
        ctx.startTrackWorker();
        this.context = ctx;
        executorService.scheduleWithFixedDelay(schedulerFunc, delay, interval, TimeUnit.MILLISECONDS);
        TaskRegistry.getInstance().addTimer(this);
    }

    /**
     * Calls the onTrigger and onError functions.
     *
     * @param parentCtx         The ballerina context.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     */
    private static void callTriggerFunction(Context parentCtx, FunctionRefCPEntry onTriggerFunction,
                                            FunctionRefCPEntry onErrorFunction) {
        ProgramFile programFile = parentCtx.getProgramFile();
        //Create new instance of the context and set required properties.
        Context newContext = new WorkerContext(programFile, parentCtx);
        try {
            //Invoke the onTrigger function.
            BValue[] results =
                    BLangFunctions.invokeFunction(programFile, onTriggerFunction.getFunctionInfo(), null, newContext);
            // If there are results, that mean an error has been returned
            if (onErrorFunction != null && results.length > 0 && results[0] != null) {
                BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), results, newContext);
            }
        } catch (BLangRuntimeException e) {

            //Call the onError function in case of error.
            if (onErrorFunction != null) {
                BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), newContext);
            }
            parentCtx.endTrackWorker();
        }
    }

    public String getId() {
        return id;
    }

    public void stop() throws TaskException {
        executorService.shutdown();
        TaskRegistry.getInstance().remove(id);
        context.endTrackWorker();
    }

}
