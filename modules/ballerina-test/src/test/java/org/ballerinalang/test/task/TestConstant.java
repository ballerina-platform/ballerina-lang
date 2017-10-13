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
package org.ballerinalang.test.task;

/**
 * Constants used in ballerina test.
 */
public class TestConstant {
    //The info log message which is printed in the onTrigger function of appointment schedule.
    public static final String APPOINTMENT_SUCCESS_MESSAGE = "Temporary directory /tmp/tmpDir is cleaned up";
    //The info log message which is printed in the onTrigger function of timer.
    public static final String TIMER_SUCCESS_MESSAGE = "Sample JSON object is returned";
    //The onTrigger function of timer.
    public static final String TIMER_ONTRIGGER_FUNCTION = "scheduleTimer";
    //The onTrigger function of appointment.
    public static final String APPOINTMENT_ONTRIGGER_FUNCTION = "scheduleAppointment";
}
