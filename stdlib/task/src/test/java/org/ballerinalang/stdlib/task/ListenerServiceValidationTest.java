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
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.Test;

/*
 * TODO:
 * We check resources at runtime for now. Hence need to use compile and setup.
 * If compiler plugin works, replace BCompileUtil.compileAndSetup() with BCompileUtil.compile().
 */

/**
 * Tests for Task service validation.
 */
public class ListenerServiceValidationTest {

    @Test(
            description = "Tests compiler error for a task with an invalid resource name.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Invalid resource function found:.*Expected: onTrigger or onError.*"
    )
    public void testInvalidResourceName() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/invalid_resource.bal");
    }

    @Test(
            description = "Tests compiler error for a task without onTrigger resource.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid resource function found: onStart. Expected: onTrigger or onError.*"
    )
    public void testResourceWithoutOnTrigger() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/missing_on_trigger_resource.bal");
    }

    @Test(
            description = "Tests compiler error for a task with more than two resource functions.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid number of resources found in service.*"
    )
    public void testInvalidNumberOfResources() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/invalid_number_of_resources.bal");
    }

    @Test(
            description = "Tests compiler error for a task with invalid single resource function.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid resource definition. If there's only one resource, it should be.*"
    )
    public void testInvalidSingleResources() {
        BCompileUtil.compileAndSetup("listener-test-src/service-validation/invalid_single_resource.bal");
    }
}
