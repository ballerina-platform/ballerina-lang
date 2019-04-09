/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*/

package org.ballerinalang.stdlib.task.utils;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * Task related constants.
 */
public class TaskConstants {

    // Package related constants
    public static final String ORGANIZATION_NAME = BALLERINA_BUILTIN_PKG_PREFIX;
    public static final String PACKAGE_NAME = "task";
    public static final String OBJECT_NAME_LISTENER = "Listener";
    public static final String PACKAGE_STRUCK_NAME = BALLERINA_PACKAGE_PREFIX + PACKAGE_NAME;

    // Record types used
    public static final String RECORD_TIMER_CONFIGURATION = "TimerConfiguration";
    public static final String RECORD_APPOINTMENT_DATA = "AppointmentData";

    // Member names used in records
    public static final String MEMBER_LISTENER_CONFIGURATION = "listenerConfiguration";
    public static final String MEMBER_APPOINTMENT_DETAILS = "appointmentDetails";

    // Allowed resource function names
    public static final String RESOURCE_ON_TRIGGER = "onTrigger";

    // Defaultable parameter names
    public static final String PARAMETER_ATTACHMENT = "attachment";

    // Common field for TimerConfiguration and AppointmentConfiguration
    public static final String FIELD_NO_OF_RUNS = "noOfRecurrences";

    // Fields used in TimerConfiguration
    public static final String FIELD_INTERVAL = "interval";
    public static final String FIELD_DELAY = "initialDelay";

    // Fields used in AppointmentData
    public static final String FIELD_SECONDS = "seconds";
    public static final String FIELD_MINUTES = "minutes";
    public static final String FIELD_HOURS = "hours";
    public static final String FIELD_DAYS_OF_MONTH = "daysOfMonth";
    public static final String FIELD_MONTHS = "months";
    public static final String FIELD_DAYS_OF_WEEK = "daysOfWeek";
    public static final String FIELD_YEAR = "year";

    // Fields related to TaskError record
    public static final String TASK_ERROR_CODE = "{ballerina/task}TaskError";
    public static final String TASK_ERROR_RECORD = "TaskError";
    public static final String TASK_ERROR_MESSAGE = "message";

    // Indexes for different fields in the context.
    public static final int REF_ARG_INDEX_TASK_RECORD = 0;
    public static final int REF_ARG_INDEX_TASK_SERVICE = 2;

    // Fields used in Appointment job map
    public static final String TASK_OBJECT = "ballerina.task";

    // ID of the Task object in native data
    public static final String NATIVE_DATA_TASK_OBJECT = "TaskObject";

    // Quarts property names
    public static final String QUARTZ_INSTANCE_NAME = "org.quartz.scheduler.instanceName";
    public static final String QUARTZ_INSTANCE_ID = "org.quartz.scheduler.instanceId";
    public static final String QUARTZ_THREAD_COUNT = "org.quartz.threadPool.threadCount";
    public static final String QUARTZ_THREAD_POOL_CLASS = "org.quartz.threadPool.class";
    public static final String QUARTZ_JOB_STORE_CLASS = "org.quartz.jobStore.class";

    // Quartz property values
    public static final String QUARTZ_THREAD_COUNT_VALUE = "1";
    public static final String QUARTZ_THREAD_POOL_CLASS_VALUE = "org.quartz.simpl.SimpleThreadPool";
    public static final String QUARTZ_JOB_STORE_CLASS_VALUE = "org.quartz.simpl.RAMJobStore";
}
