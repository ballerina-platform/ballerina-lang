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

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.ballerinalang.stdlib.task.utils.TaskTestUtils.getFilePath;

/**
 * Tests for Ballerina Task Service validation.
 */
@Test
public class ListenerServiceValidationTest {
    private static final String TEST_DIRECTORY = "service-validation";

    @Test(
            description = "Tests compiler error for a task with more than two resource functions.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Invalid number of resources found in service 'timerService'. "
                    + "Task service should include only one resource.*",
            enabled = false
    )
    public void testMoreThanTwoResourceFunctions() {
        BCompileUtil.compile(getFilePath(Paths.get(TEST_DIRECTORY, "more_than_one_resource.bal")));
    }

    @Test(
            description = "Tests compiler error for a task without ant resource functions.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Invalid number of resources found in service 'timerService'. "
                    + "Task service should include only one resource.*",
            enabled = false
    )
    public void testNoResourceFunctions() {
        BCompileUtil.compile(getFilePath(Paths.get(TEST_DIRECTORY, "no_resource_functions.bal")));
    }

    @Test(
            description = "Tests compiler error for a task with an invalid resource name.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    ".*Invalid resource function found: timerStart. Expected: 'onTrigger'.*",
            enabled = false
    )

    public void testInvalidResourceName() {
        BCompileUtil.compile(getFilePath(Paths.get(TEST_DIRECTORY, "invalid_resource.bal")));
    }
}
