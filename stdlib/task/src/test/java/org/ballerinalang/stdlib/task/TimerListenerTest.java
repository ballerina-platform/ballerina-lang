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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_DELAY;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_INTERVAL;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_NO_OF_RUNS;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.LISTENER_CONFIGURATION_MEMBER_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.LISTENER_STRUCT_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TIMER_CONFIGURATION_STRUCT_NAME;

@Test
public class TimerListenerTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setupTests() {
        compileResult = BCompileUtil.compile("listener-test-src/timer/dynamic_actions.bal");
    }

    @Test(description = "Tests the functionality of initiating a Task Timer Listener.")
    public void testCreateTimer() {
        int interval = 1000;
        int delay = 2000;
        int noOfRecurrings = 3;
        BValue[] inputs = {new BInteger(interval), new BInteger(delay), new BInteger(noOfRecurrings)};
        BValue[] configs = BRunUtil.invokeStateful(compileResult, "getConfigurations", inputs);
        Assert.assertEquals(configs.length, 1);
        Assert.assertEquals(TIMER_CONFIGURATION_STRUCT_NAME, configs[0].getType().getName());

        BValue[] timer = BRunUtil.invokeStateful(compileResult, "createTimer", configs);
        Assert.assertEquals(timer.length, 1);
        Assert.assertEquals(timer[0].getType().getPackagePath(), PACKAGE_STRUCK_NAME);
        Assert.assertEquals(timer[0].getType().getName(), LISTENER_STRUCT_NAME);

        BMap<String, BValue> timerConfigs = ((BMap) ((BMap) timer[0]).get(LISTENER_CONFIGURATION_MEMBER_NAME));
        checkIntVariableValues(timerConfigs, FIELD_NAME_INTERVAL, interval);
        checkIntVariableValues(timerConfigs, FIELD_NAME_DELAY, delay);
        checkIntVariableValues(timerConfigs, FIELD_NAME_NO_OF_RUNS, noOfRecurrings);
    }

    @Test(description = "Tests the functionality of initiating a Task Timer Listener.")
    public void testCreateTimerWithoutDelay() {
        int interval = 1000;
        int noOfRecurrings = 3;
        BValue[] inputs = {new BInteger(interval), new BInteger(noOfRecurrings)};
        BValue[] configs = BRunUtil.invokeStateful(compileResult, "getConfigurationsWithoutDelay", inputs);
        Assert.assertEquals(configs.length, 1);
        Assert.assertEquals(TIMER_CONFIGURATION_STRUCT_NAME, configs[0].getType().getName());

        BValue[] timer = BRunUtil.invokeStateful(compileResult, "createTimer", configs);
        Assert.assertEquals(timer.length, 1);
        Assert.assertEquals(timer[0].getType().getPackagePath(), PACKAGE_STRUCK_NAME);
        Assert.assertEquals(timer[0].getType().getName(), LISTENER_STRUCT_NAME);

        BMap<String, BValue> timerConfigs = ((BMap) ((BMap) timer[0]).get(LISTENER_CONFIGURATION_MEMBER_NAME));
        checkIntVariableValues(timerConfigs, FIELD_NAME_INTERVAL, interval);
        // When we do not provide delay, it is set to interval
        checkIntVariableValues(timerConfigs, FIELD_NAME_DELAY, interval);
        checkIntVariableValues(timerConfigs, FIELD_NAME_NO_OF_RUNS, noOfRecurrings);
    }

    @Test(description = "Tests the functionality of Task Timer start")
    public void testAttachTimer() {

    }

    private void checkIntVariableValues(BMap<String, BValue> struct, String fieldName, long expectedValue) {
        long actualValue = ((BInteger) struct.get(fieldName)).intValue();
        Assert.assertEquals(actualValue, expectedValue);
    }

    //private void
}
