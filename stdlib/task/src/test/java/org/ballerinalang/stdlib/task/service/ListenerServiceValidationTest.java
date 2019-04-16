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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.Test;

/*
 * TODO: We check resources at runtime for now. Hence need to use compile and setup.
 *      If compiler plugin works, replace BCompileUtil.compileAndSetup() with BCompileUtil.compile().
 *      Issue: https://github.com/ballerina-platform/ballerina-lang/issues/14148
 */

/**
 * Tests for Ballerina Task Service validation.
 */
@Test
public class ListenerServiceValidationTest {

    @Test(
            description = "Tests compiler error for a task with more than two resource functions.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Invalid number of resources found in service 'timerService'. "
                    + "Task service should include only one resource.*"
    )
    public void testMoreThanTwoResourceFunctions() {
        BCompileUtil.compileAndSetup("service-validation/more_than_one_resource.bal");
    }

    @Test(
            description = "Tests compiler error for a task without ant resource functions.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Invalid number of resources found in service 'timerService'. "
                    + "Task service should include only one resource.*"
    )
    public void testNoResourceFunctions() {
        BCompileUtil.compileAndSetup("service-validation/no_resource_functions.bal");
    }

    @Test(
            description = "Tests compiler error for a task with an invalid resource name.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid resource function found: timerStart. Expected: 'onTrigger'.*"
    )
    public void testInvalidResourceName() {
        BCompileUtil.compileAndSetup("service-validation/invalid_resource.bal");
    }
}
