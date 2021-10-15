/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.test.resolution.packages;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains test cases that uses direct dependencies coming from local repos.
 *
 * @since 2.0.0
 */
public class LocalRepoDependencyTest extends AbstractPackageResolutionTest {

    @Test(dataProvider = "unpublishedNewPackagesTestCaseProvider")
    public void testUnpublishedNewPackages(String testSuite, String testCase, boolean sticky) {
        runTestCase(testSuite, testCase, sticky);
    }

    @DataProvider(name = "unpublishedNewPackagesTestCaseProvider")
    public static Object[][] unpublishedNewPackagesTestCaseProvider() {
        return new Object[][]{
                {"suite-local_deps", "case-0001", true},
                {"suite-local_deps", "case-0001", false},
                {"suite-local_deps", "case-0002", true},
                {"suite-local_deps", "case-0002", false},
                {"suite-local_deps", "case-0003", true},
                {"suite-local_deps", "case-0003", false},
                {"suite-local_deps", "case-0004", true},
                {"suite-local_deps", "case-0004", false},
                {"suite-local_deps", "case-0005", true},
                {"suite-local_deps", "case-0005", false},
                {"suite-local_deps", "case-0006", true},
                {"suite-local_deps", "case-0006", false},
                {"suite-local_deps", "case-0007", true},
                {"suite-local_deps", "case-0007", false},
                {"suite-local_deps", "case-0008", true},
                {"suite-local_deps", "case-0008", false},
        };
    }

    @Test(dataProvider = "patchedExistingPackagesTestCaseProvider")
    public void testPatchedExistingPackages(String testSuite, String testCase, boolean sticky) {
        runTestCase(testSuite, testCase, sticky);
    }

    @DataProvider(name = "patchedExistingPackagesTestCaseProvider")
    public static Object[][] patchedExistingPackagesTestCaseProvider() {
        return new Object[][]{
                {"suite-local_deps", "case-0101", true},
                {"suite-local_deps", "case-0101", true},
                {"suite-local_deps", "case-0102", true},
                {"suite-local_deps", "case-0102", true},
                {"suite-local_deps", "case-0103", true},
                {"suite-local_deps", "case-0103", true},
        };
    }
}
