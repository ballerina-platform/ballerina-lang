/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.test.resolution.packages;

import io.ballerina.projects.environment.PackageLockingMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains test cases that uses direct dependencies coming from local repos.
 *
 * @since 2201.13.0
 */
public class WorkspaceRepoDependencyTest extends AbstractPackageResolutionTest {

    @Test(dataProvider = "resolutionTestCaseProvider")
    public void testcase(String testSuite, String testCase, PackageLockingMode lockingMode) {
        runTestCase(testSuite, testCase, lockingMode);
    }

    @Test(dataProvider = "resolutionTestCaseProviderForOldFormat")
    public void testcaseForOldStickyFlag(String testSuite, String testCase, boolean sticky) {
        runTestCase(testSuite, testCase, sticky);
    }

    @DataProvider(name = "resolutionTestCaseProvider")
    public static Object[][] testCaseProvider() {
        return new Object[][]{
                {"suite-workspace_deps", "case-0001", PackageLockingMode.HARD},
                {"suite-workspace_deps", "case-0001", PackageLockingMode.MEDIUM},
                {"suite-workspace_deps", "case-0001", PackageLockingMode.SOFT},
                {"suite-workspace_deps", "case-0002", PackageLockingMode.HARD},
                {"suite-workspace_deps", "case-0002", PackageLockingMode.MEDIUM},
                {"suite-workspace_deps", "case-0002", PackageLockingMode.SOFT},
                {"suite-workspace_deps", "case-0003", PackageLockingMode.HARD},
                {"suite-workspace_deps", "case-0003", PackageLockingMode.MEDIUM},
                {"suite-workspace_deps", "case-0003", PackageLockingMode.SOFT},
                {"suite-workspace_deps", "case-0004", PackageLockingMode.HARD},
                {"suite-workspace_deps", "case-0004", PackageLockingMode.MEDIUM},
                {"suite-workspace_deps", "case-0004", PackageLockingMode.SOFT},
                {"suite-workspace_deps", "case-0005", PackageLockingMode.HARD},
                {"suite-workspace_deps", "case-0005", PackageLockingMode.MEDIUM},
                {"suite-workspace_deps", "case-0005", PackageLockingMode.SOFT},
                {"suite-workspace_deps", "case-0006", PackageLockingMode.HARD},
                {"suite-workspace_deps", "case-0006", PackageLockingMode.MEDIUM},
                {"suite-workspace_deps", "case-0006", PackageLockingMode.SOFT},
        };
    }

    @DataProvider(name = "resolutionTestCaseProviderForOldFormat")
    public static Object[][] testCaseProviderForOldFormat() {
        return new Object[][]{
                {"suite-workspace_deps", "case-0001", true},
                {"suite-workspace_deps", "case-0001", false},
                {"suite-workspace_deps", "case-0002", true},
                {"suite-workspace_deps", "case-0002", false},
                {"suite-workspace_deps", "case-0003", true},
                {"suite-workspace_deps", "case-0003", false},
                {"suite-workspace_deps", "case-0004", true},
                {"suite-workspace_deps", "case-0004", false},
                {"suite-workspace_deps", "case-0005", true},
                {"suite-workspace_deps", "case-0005", false},
                {"suite-workspace_deps", "case-0006", true},
                {"suite-workspace_deps", "case-0006", false},
        };
    }
}
