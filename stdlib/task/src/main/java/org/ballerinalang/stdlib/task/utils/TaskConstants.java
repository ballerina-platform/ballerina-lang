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

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Task related constants.
 */
public class TaskConstants {

    // Package related constants
    public static final String PACKAGE_NAME = "task";
    public static final String PACKAGE_VERSION = "1.1.0";
    public static final Module TASK_PACKAGE_ID =
            new Module(BALLERINA_BUILTIN_PKG_PREFIX, PACKAGE_NAME, PACKAGE_VERSION);

    // Record types used
    public static final String RECORD_TIMER_CONFIGURATION = "TimerConfiguration";
    static final String RECORD_APPOINTMENT_DATA = "AppointmentData";

    // Member names used in records
    public static final BString MEMBER_LISTENER_CONFIGURATION = StringUtils.fromString("listenerConfiguration");
    public static final BString MEMBER_APPOINTMENT_DETAILS = StringUtils.fromString("appointmentDetails");

    // Allowed resource function names
    public static final String RESOURCE_ON_TRIGGER = "onTrigger";

    // Common field for TimerConfiguration and AppointmentConfiguration
    public static final BString FIELD_NO_OF_RUNS = StringUtils.fromString("noOfRecurrences");

    // Fields used in TimerConfiguration
    public static final BString FIELD_INTERVAL = StringUtils.fromString("intervalInMillis");
    public static final BString FIELD_DELAY = StringUtils.fromString("initialDelayInMillis");

    // Fields used in AppointmentData
    static final BString FIELD_SECONDS = StringUtils.fromString("seconds");
    static final BString FIELD_MINUTES = StringUtils.fromString("minutes");
    static final BString FIELD_HOURS = StringUtils.fromString("hours");
    static final BString FIELD_DAYS_OF_MONTH = StringUtils.fromString("daysOfMonth");
    static final BString FIELD_MONTHS = StringUtils.fromString("months");
    static final BString FIELD_DAYS_OF_WEEK = StringUtils.fromString("daysOfWeek");
    static final BString FIELD_YEAR = StringUtils.fromString("year");

    // Fields related to TaskError record
    public static final String SCHEDULER_ERROR = "SchedulerError";
    static final String LISTENER_ERROR = "ListenerError";
    static final String DETAIL_RECORD_NAME = "Detail";

    // Fields used in Appointment job map
    public static final String TASK_OBJECT = "ballerina.task";

    // ID of the Task object in native data
    public static final String NATIVE_DATA_TASK_OBJECT = "TaskObject";

    // Quarts property names
    public static final String QUARTZ_THREAD_COUNT = "org.quartz.threadPool.threadCount";
    public static final String QUARTZ_MISFIRE_THRESHOLD = "org.quartz.jobStore.misfireThreshold";

    // Quartz property values
    public static final String QUARTZ_THREAD_COUNT_VALUE = "10";
    // Defines how late the trigger should be to be considered misfired
    public static final String QUARTZ_MISFIRE_THRESHOLD_VALUE = "5000";
}
