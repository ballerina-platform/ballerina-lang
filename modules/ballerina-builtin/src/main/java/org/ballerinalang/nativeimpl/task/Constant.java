/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.task;

/**
 * Constants used in ballerina task.
 */
public class Constant {
    //Name of the Hashmap where we keep the ScheduledExecutorServices.
    protected static final String SERVICEMAP = "executorServiceMap";
    //The name of the context property which is used to keep the error.
    protected static final String ERROR = "ERROR";
    //The name of the context property which is used to generate the task id.
    protected static final String ID = "id";
    //The runnable duration.
    protected static final String SCHEDULER_RUNTIME = "period";
    // The string which is used to find the calculated delay
    public static final String DELAY_HINT = " is scheduled with the DELAY [";
    // The string which is used to find the calculated delay
    public static final String SCHEDULER_RUNTIME_HINT = "with the PERIOD: [";
    // The prefix of timer log
    public static final String PREFIX_TIMER = "";
    // The prefix of appointment log
    public static final String PREFIX_APPOINTMENT = "Appointment ";
}
