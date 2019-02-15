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

package org.ballerinalang.stdlib.task.listener;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.Test;

/*
 * TODO:
 * We check resources at runtime for now. Hence need to use compile and setup.
 * If compiler plugin works, replace BCompileUtil.compileAndSetup() with BCompileUtil.compile().
 */

@Test
public class ListenerServiceValidationTest {

    @Test(
            description = "Tests compiler error for a task with more than two resource functions.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid number of resources found in service 'timerService'." +
                            " Task service cannot include more than two resource functions.*"
    )
    public void testMoreThanTwoResourceFunctions() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/more_than_two_resource_functions.bal");
    }

    @Test(
            description = "Tests compiler error for a task without ant resource functions.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*No resource functions found in service 'timerService'." +
                            " Task service should include at least one resource function.*"
    )
    public void testNoResourceFunctions() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/no_resource_functions.bal");
    }

    @Test(
            description = "Tests compiler error for a task with invalid parameter for onError resource function.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Invalid resource function signature."
                    + "*function should have an error as the first input parameter.*"
    )
    public void testInvalidOnErrorResourceSignatureInvalidParam() {
        BCompileUtil.compileAndSetup(
                "listener-test-src/service-validation/invalid_on_error_resource_invalid_param.bal");
    }

    @Test(
            description = "Tests compiler error for a task with more than one parameter for onError resource function.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Invalid resource function signature."
                    + "*should have one input parameter.*",
            enabled = false
    )
    public void testInvalidOnErrorResourceSignatureMoreParams() {
        BCompileUtil.compileAndSetup(
                "listener-test-src/service-validation/invalid_on_error_resource_more_params.bal");
    }

    @Test(
            description = "Tests compiler error for a task with an invalid resource name.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid resource function found: timerStart. Expected: 'onTrigger' or 'onError'.*"
    )
    public void testInvalidResourceName() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/invalid_resource.bal");
    }

    @Test(
            description = "Tests compiler error for a task without onTrigger resource.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid resource function found: onStart. Expected: 'onTrigger' or 'onError'.*"
    )
    public void testResourceWithoutOnTrigger() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/missing_on_trigger_resource.bal");
    }

    @Test(
            description = "Tests compiler error for a task with invalid single resource function.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Resource validation failed. Service timerService must include resource function: 'onTrigger'.*"
    )
    public void testOnlyOnErrorResource() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/only_on_error_function.bal");
    }

    @Test(
            description = "Tests compiler error for a task with invalid single resource function.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid resource function found: timerStart. Expected: 'onTrigger' or 'onError'.*"
    )
    public void testInvalidSingleResources() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/invalid_single_resource.bal");
    }
}
