/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Test class to test report using a ballerina project.
 */
public class TestReportTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;
    private Path resultsJsonPath;
    private JsonObject resultObj;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = reportTestProjectPath.toString();
        resultsJsonPath = reportTestProjectPath.resolve("target").resolve("test_results.json");
    }

    @Test
    public void testWithCoverage() throws BallerinaTestException, IOException {
        runCommand(true);
        validateStatuses();
        validateCoverage();
    }

    @Test (enabled = false)
    public void testWithoutCoverage() throws BallerinaTestException, IOException {
        runCommand(false);
        validateStatuses();
        Assert.assertEquals(resultObj.get("moduleCoverage").getAsJsonArray().size(), 0);
    }

    private void runCommand(boolean coverage) throws BallerinaTestException, IOException {
        String[] args;
        if (coverage) {
            args = new String[]{"--code-coverage", "--all"};
        } else {
            args = new String[]{ "--all"};
        }

        balClient.runMain("test", args, null, new String[]{},
                new LogLeecher[]{}, projectPath);

        Gson gson = new Gson();
        BufferedReader bufferedReader = Files.newBufferedReader(resultsJsonPath, StandardCharsets.UTF_8);
        resultObj = gson.fromJson(bufferedReader, JsonObject.class);
    }

    private void validateStatuses() {
        int total = 5, totalPassed = 3, totalFailed = 1, totalSkipped = 1;
        int serviceTotal = 1, servicePassed = 1, serviceFailed = 0, serviceSkipped = 0;
        int mathTotal = 2, mathPassed = 2, mathFailed = 0, mathSkipped = 0;
        int fooTotal = 2, fooPassed = 0, fooFailed = 1, fooSkipped = 1;

        // Verify module level numbers
        for (JsonElement obj : resultObj.get("moduleStatus").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) obj);
            if ("myService".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(serviceTotal, moduleObj.get("totalTests").getAsInt());
                Assert.assertEquals(servicePassed, moduleObj.get("passed").getAsInt());
                Assert.assertEquals(serviceFailed, moduleObj.get("failed").getAsInt());
                Assert.assertEquals(serviceSkipped, moduleObj.get("skipped").getAsInt());
            } else if ("math".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(mathTotal, moduleObj.get("totalTests").getAsInt());
                Assert.assertEquals(mathPassed, moduleObj.get("passed").getAsInt());
                Assert.assertEquals(mathFailed, moduleObj.get("failed").getAsInt());
                Assert.assertEquals(mathSkipped, moduleObj.get("skipped").getAsInt());
            } else if ("foo".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(fooTotal, moduleObj.get("totalTests").getAsInt());
                Assert.assertEquals(fooPassed, moduleObj.get("passed").getAsInt());
                Assert.assertEquals(fooFailed, moduleObj.get("failed").getAsInt());
                Assert.assertEquals(fooSkipped, moduleObj.get("skipped").getAsInt());
            } else {
                Assert.fail("unrecognized module found: " + moduleObj.get("name").getAsString());
            }
        }

        // Verify project level numbers
        Assert.assertEquals(total, resultObj.get("totalTests").getAsInt());
        Assert.assertEquals(totalPassed, resultObj.get("passed").getAsInt());
        Assert.assertEquals(totalFailed, resultObj.get("failed").getAsInt());
        Assert.assertEquals(totalSkipped, resultObj.get("skipped").getAsInt());
    }

    private void validateCoverage() {
        JsonParser parser = new JsonParser();
        // service module
        int[] serviceMainCovered = new int[]{10, 19, 20, 23, 24}, serviceMainMissed = new int[]{21};
        float serviceMainPercentageVal =
                (float) (serviceMainCovered.length) / (serviceMainCovered.length + serviceMainMissed.length) * 100;
        float serviceMainPercentage =
                (float) (Math.round(serviceMainPercentageVal * 100.0) / 100.0);

        int serviceCovered = serviceMainCovered.length, serviceMissed = serviceMainMissed.length;

        //math module
        int[] mathAddCovered = new int[] {6, 7, 8, 10, 13, 15}, mathAddMissed = new int[] {11};
        float mathAddPercentageVal =
                (float) (mathAddCovered.length) / (mathAddCovered.length + mathAddMissed.length) * 100;
        float mathAddPercentage =
                (float) (Math.round(mathAddPercentageVal * 100.0) / 100.0);

        int[] mathDivideCovered = new int[] {6, 7, 9, 10, 14}, mathDivideMissed = new int[] {12};
        float mathDividePercentageVal =
                (float) (mathDivideCovered.length) / (mathDivideCovered.length + mathDivideMissed.length) * 100;
        float mathDividePercentage =
                (float) (Math.round(mathDividePercentageVal * 100.0) / 100.0);

        int mathCovered = mathAddCovered.length + mathDivideCovered.length,
                mathMissed = mathAddMissed.length + mathDivideMissed.length;
        float mathPercentageVal = (float) mathCovered / (mathCovered + mathMissed) * 100;
        float mathPercentage = (float) (Math.round(mathPercentageVal * 100.0) / 100.0);

        //foo module
        int[] fooMainCovered = new int[] {9, 10, 11, 16, 17, 23, 24}, fooMainMissed = new int[] {13};
        float fooMainPercentageVal =
                (float) (fooMainCovered.length) / (fooMainCovered.length + fooMainMissed.length) * 100;
        float fooMainPercentage =
                (float) (Math.round(fooMainPercentageVal * 100.0) / 100.0);

        int fooCovered = fooMainCovered.length, fooMissed = fooMainMissed.length;

        // project
        int totalCovered = serviceCovered + mathCovered + fooCovered;
        int totalMissed =  serviceMissed + mathMissed + fooMissed;
        float coveragePercentageVal = (float) (totalCovered) / (totalCovered + totalMissed) * 100;
        float coveragePercentage = (float) (Math.round(coveragePercentageVal * 100.0) / 100.0);

        // Verify module level coverage
        for (JsonElement element : resultObj.get("moduleCoverage").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) element);
            if ("myService".equals(moduleObj.get("name").getAsString())) {
                // Verify coverage of source file
                JsonObject fileObj = (JsonObject) moduleObj.get("sourceFiles").getAsJsonArray().get(0);
                Assert.assertEquals("main.bal", fileObj.get("name").getAsString());
                Assert.assertEquals(parser.parse(Arrays.toString(serviceMainCovered)),
                        parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                Assert.assertEquals(parser.parse(Arrays.toString(serviceMainMissed)),
                        parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                Assert.assertEquals(serviceMainPercentage, fileObj.get("coveragePercentage").getAsFloat());

                // verify coverage of module
                Assert.assertEquals(serviceCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(serviceMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(serviceMainPercentage, moduleObj.get("coveragePercentage").getAsFloat());
            } else if ("math".equals(moduleObj.get("name").getAsString())) {
                // Verify coverage of individual source file
                for (JsonElement element1 :  moduleObj.get("sourceFiles").getAsJsonArray()) {
                    JsonObject fileObj = (JsonObject) element1;
                    if ("add.bal".equals(fileObj.get("name").getAsString())) {
                        Assert.assertEquals(parser.parse(Arrays.toString(mathAddCovered)),
                                parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                        Assert.assertEquals(parser.parse(Arrays.toString(mathAddMissed)),
                                parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                        Assert.assertEquals(mathAddPercentage, fileObj.get("coveragePercentage").getAsFloat());
                    } else if ("divide.bal".equals(fileObj.get("name").getAsString())) {
                        Assert.assertEquals(parser.parse(Arrays.toString(mathDivideCovered)),
                                parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                        Assert.assertEquals(parser.parse(Arrays.toString(mathDivideMissed)),
                                parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                        Assert.assertEquals(mathDividePercentage, fileObj.get("coveragePercentage").getAsFloat());
                    } else {
                        Assert.fail("unrecognized file: " + fileObj.get("name").getAsString() + "in module " +
                                moduleObj.get("name").getAsString());
                    }
                }

                // Verify coverage of module
                Assert.assertEquals(mathCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(mathMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(mathPercentage, moduleObj.get("coveragePercentage").getAsFloat());

            } else if ("foo".equals(moduleObj.get("name").getAsString())) {
                // Verify coverage of source file
                JsonObject fileObj = (JsonObject) moduleObj.get("sourceFiles").getAsJsonArray().get(0);
                Assert.assertEquals("mainBal/main.bal", fileObj.get("name").getAsString());
                Assert.assertEquals(parser.parse(Arrays.toString(fooMainCovered)),
                        parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                Assert.assertEquals(parser.parse(Arrays.toString(fooMainMissed)),
                        parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                Assert.assertEquals(fooMainPercentage, fileObj.get("coveragePercentage").getAsFloat());

                // Verify coverage of module
                Assert.assertEquals(fooCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(fooMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(fooMainPercentage, moduleObj.get("coveragePercentage").getAsFloat());
            } else {
                Assert.fail("unrecognized module: " + moduleObj.get("name").getAsString());
            }
        }

        // Verify project level coverage details
        Assert.assertEquals(totalCovered, resultObj.get("coveredLines").getAsInt());
        Assert.assertEquals(totalMissed, resultObj.get("missedLines").getAsInt());
        Assert.assertEquals(coveragePercentage, resultObj.get("coveragePercentage").getAsFloat());
    }
}
