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
 *
 */
package org.ballerinalang.stdlib.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.Test;

public class ListenerTest {
    private static final Log log = LogFactory.getLog(AppointmentTest.class);

    @Test(description = "Tests running an appointment and stopping it")
    public void testListenerTimer() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/listener_timer_service.bal");
        BServiceUtil.runService(compileResult);
    }

    @Test(description = "Tests a timer listener with inline configurations")
    public void testListenerTimerInlineConfigs() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/listener_timer_service_inline_configs.bal");
        BServiceUtil.runService(compileResult);
    }

    @Test(description = "Tests a timer listener without delay field")
    public void testListenerTimerWithoutDelay() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/listener_timer_service_without_delay.bal");
        BServiceUtil.runService(compileResult);
    }

    @Test(
            description = "Tests a timer listener with inline configurations",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Task scheduling configuration is invalid.*"
    )
    public void testListenerTimerNegativeValues() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/listener_timer_service_negative_values.bal");
        BServiceUtil.runService(compileResult);
    }
}
