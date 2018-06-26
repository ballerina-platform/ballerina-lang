/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.task;

/**
 * Task related constants.
 */
public class TaskConstants {

    public static final String APPOINTMENT_ON_TRIGGER_FIELD = "onTrigger";
    public static final String APPOINTMENT_ON_ERROR_FIELD = "onError";
    public static final String APPOINTMENT_CRON_EXPR_FIELD = "scheduleCronExpression";
    public static final String APPOINTMENT_TASK_ID_FIELD = "taskId";
    public static final String APPOINTMENT_IS_RUNNING_FIELD = "isRunning";
    
    public static final String TIMER_ON_TRIGGER_FIELD = "onTrigger";
    public static final String TIMER_ON_ERROR_FIELD = "onError";
    public static final String TIMER_DELAY = "delay";
    public static final String TIMER_INTERVAL = "interval";
    public static final String TIMER_TASK_ID_FIELD = "taskId";
    public static final String TIMER_IS_RUNNING_FIELD = "isRunning";
}
