/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.test.coverage;

import org.ballerinalang.bre.coverage.ExecutedInstruction;
import org.ballerinalang.testerina.core.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.coverage.CoverageDataFormatter;
import org.ballerinalang.testerina.coverage.CoverageManager;
import org.ballerinalang.testerina.coverage.LCovData;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for ballerina.test coverage package.
 */
public class LCovCoverageDataFormatterImplTest {

    private String sourceRoot = "src/test/resources/coverage/project";
    private Path[] filePaths = { Paths.get("example_module1") };

    CoverageManager coverageManager = CoverageManager.getInstance();
    TesterinaRegistry registry = TesterinaRegistry.getInstance();

    @BeforeTest
    public void beforeTest() {
        registry.setOrgName("project");
        registry.setVersion("0.0.1");
    }

    @Test(description = "bal project and module coverage report happy path")
    public void getFormattedCoverageDataTest() {
        BTestRunner testRunner = new BTestRunner();
        testRunner.setCoverageDisabled(true);
        testRunner.runTest(sourceRoot, filePaths, new ArrayList<>());
        CoverageDataFormatter coverageDataFormatter = coverageManager.getCoverageDataFormatter();
        Map<String, List<ExecutedInstruction>> executedInstructionOrderMap =
                coverageManager.getExecutedInstructionOrderMap();
        Map<String, TestSuite> testSuiteForProject = registry.getTestSuites();
        List<LCovData> lCovDataList = coverageDataFormatter
                .getFormattedCoverageData(executedInstructionOrderMap, testSuiteForProject);
        Assert.assertEquals(lCovDataList.get(0).getlCovSourceFileList().get(0).getSourceFilePath(),
                "project/example_module1:0.0.1/source.bal");
    }

    @AfterMethod
    private void cleanup() {
        TesterinaRegistry.getInstance().setProgramFiles(new ArrayList<>());
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
    }

}
