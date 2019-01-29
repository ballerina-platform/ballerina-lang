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
package org.ballerinalang.stdlib.task.timer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.TaskExecutor;
import org.ballerinalang.stdlib.task.TaskRegistry;
import org.ballerinalang.util.codegen.FunctionInfo;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.stdlib.task.TaskConstants.RESOURCE_ON_TRIGGER;
import static org.ballerinalang.stdlib.task.TaskIdGenerator.generate;

/**
 * Represents a timer.
 */
public class Timer {
    private String id = generate();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ArrayList<Service> serviceList = new ArrayList<>();
    private long interval, delay;


    /**
     * Triggers the timer.
     *
     * @param ctx               The ballerina context.
     * @param delay             The initial delay.
     * @param interval          The interval between two task executions.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     * @throws SchedulingException if cannot create the scheduler.
     */
    public Timer(Context ctx, long delay, long interval,
                 FunctionInfo onTriggerFunction,
                 FunctionInfo onErrorFunction) throws SchedulingException {

        if (delay < 0 || interval < 0) {
            throw new SchedulingException("Timer scheduling delay and interval should be non-negative values");
        }

        final Runnable schedulerFunc = () -> {
            callTriggerFunction(ctx, onTriggerFunction, onErrorFunction);
        };

        executorService.scheduleWithFixedDelay(schedulerFunc, delay, interval, TimeUnit.MILLISECONDS);
        TaskRegistry.getInstance().addTimer(this);
        //BLangScheduler.workerCountUp();
    }

    /**
     * Creates a Timer object.
     *
     * @param context               The ballerina context.
     * @param delay                 The initial delay.
     * @param interval              The interval between two task executions.
     * @param service               Service attached to the listener.
     * @throws SchedulingException if cannot create the scheduler.
     */
    public Timer(Context context, long delay, long interval, Service service) throws SchedulingException {

        if (delay < 0 || interval < 0) {
            throw new SchedulingException("Timer scheduling delay and interval should be non-negative values");
        }

        this.interval = interval;
        this.delay = delay;
        this.serviceList.add(service);

        TaskRegistry.getInstance().addTimer(this);
    }

    /**
     * Calls the onTrigger and onError functions.
     *
     * @param parentCtx         The ballerina context.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     */
    private static void callTriggerFunction(Context parentCtx, FunctionInfo onTriggerFunction,
                                            FunctionInfo onErrorFunction) {
        TaskExecutor.execute(parentCtx, onTriggerFunction, onErrorFunction);
    }

    /**
     * Calls the onTrigger and onError functions.
     *
     * @param parentCtx         The ballerina context.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     */
    private static void callTriggerFunction(Context parentCtx, FunctionInfo onTriggerFunction,
                                            FunctionInfo onErrorFunction, Service service) {
        TaskExecutor.execute(parentCtx, onTriggerFunction, onErrorFunction, service);
    }

    public String getId() {
        return id;
    }

    public void stop() {
        //BLangScheduler.workerCountDown();
        executorService.shutdown();
        TaskRegistry.getInstance().remove(id);
    }

    public void addService(Service service) {
        this.serviceList.add(service);
    }

    public ArrayList<Service> getServices() {
        return this.serviceList;
    }

    public void runServices(Context context) {
        final Runnable schedulerFunc = () -> {
            for (Service service : serviceList) {
                FunctionInfo onTriggerFunction, onErrorFunction;
                if (RESOURCE_ON_TRIGGER.equals(service.getResources()[0].getName())) {
                    onTriggerFunction = service.getResources()[0].getResourceInfo();
                    onErrorFunction = service.getResources()[1].getResourceInfo();
                } else {
                    onErrorFunction = service.getResources()[0].getResourceInfo();
                    onTriggerFunction = service.getResources()[1].getResourceInfo();
                }
                callTriggerFunction(context, onTriggerFunction, onErrorFunction, service);
            }
        };
        executorService.scheduleWithFixedDelay(schedulerFunc, delay, interval, TimeUnit.MILLISECONDS);
    }
}
