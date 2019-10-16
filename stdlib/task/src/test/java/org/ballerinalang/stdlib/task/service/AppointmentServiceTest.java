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

package org.ballerinalang.stdlib.task.service;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.stdlib.task.utils.TaskTestUtils.getFilePath;

/**
 * Tests for Ballerina Task Appointment Library.
 */
@Test
public class AppointmentServiceTest {
    @Test(description = "Tests the functionality of initiating a Task Timer Listener with AppointmentData record.")
    public void testAppointmentDataConfigs() {
        CompileResult compileResult = BCompileUtil.compile(true,
                getFilePath(Paths.get("listener", "appointment", "appointment_data_configs.bal")));
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] configs = BRunUtil.invoke(compileResult, "getCount");
            Assert.assertEquals(configs.length, 1);
            return (((BInteger) configs[0]).intValue() > 3);
        });
    }

    @Test(
            description = "Test an Appointment with invalid cron expression.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Cron Expression \"invalid cron expression\" is invalid.*"
    )
    public void testInvalidCronExpression() {
        BCompileUtil.compile(getFilePath(Paths.get("listener", "appointment", "invalid_cron_expression.bal")));
    }

    @Test(
            description = "Test an Appointment with invalid appointment data.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*AppointmentData .* is invalid.*"
    )
    public void testInvalidAppointmentData() {
        BCompileUtil.compile(getFilePath(Paths.get("listener", "appointment", "invalid_appointment_data.bal")));
    }

    @Test(description = "Test invalid appointmentData crecord type")
    public void testInvalidAppointmentDataRecordType() {
        CompileResult compileResult = BCompileUtil.compile(true,
                getFilePath(Paths.get("listener", "appointment", "invalid_appointment_data_record.bal")));
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        BAssertUtil.validateError(compileResult, 0, "incompatible types: expected " +
                        "'(string|ballerina/task:AppointmentData)', found 'DuplicateAppointmentData'",
                40, 25);
    }
}
