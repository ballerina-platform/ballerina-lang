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
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.ballerinalang.testerina.test.utils.CommonUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Test class to test report using a ballerina project.
 */
public class TestReportTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;
    private Path resultsJsonPath;
    private JsonObject resultObj;

    private static final int TOTAL_TESTS = 0;
    private static final int PASSED = 1;
    private static final int SKIPPED = 2;
    private static final int FAILED = 3;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("test-report-tests").toString();
        resultsJsonPath = projectBasedTestsPath.resolve("test-report-tests").resolve("target").resolve("report")
                .resolve("test_results.json");
    }

    @Test()
    public void testWarningForReportTools() throws BallerinaTestException, IOException {
        String msg = "warning: Could not find the required HTML report tools for code coverage";
        String[] args = mergeCoverageArgs(new String[]{"--test-report"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        String firstString = "Generating Test Report\n\t";
        String endString = "project-based-tests";
        output = CommonUtils.replaceVaryingString(firstString, endString, output);
        AssertionUtils.assertOutput("TestReportTest-testWarningForReportTools.txt", output);
    }

    @Test ()
    public void testWarningForCoverageFormatFlag() throws BallerinaTestException, IOException {
        String msg = "warning: ignoring --coverage-format flag since code coverage is not enabled";
        String[] args = new String[]{"--coverage-format=xml"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("TestReportTest-testWarningForCoverageFormatFlag.txt", output);
    }

    @Test()
    public void testWithCoverage() throws BallerinaTestException {
        //runCommand(true);
        String[] args = new String[]{"--code-coverage"};
        runCommand(args);

        int mathTotal = 2, mathPassed = 2, mathFailed = 0, mathSkipped = 0;
        int fooTotal = 3, fooPassed = 1, fooFailed = 1, fooSkipped = 1;
        int bartestTotal = 1, bartestPassed = 1, bartestFailed = 0, bartestSkipped = 0;

        int[] mathStatus =  {mathTotal, mathPassed, mathFailed, mathSkipped};
        int[] fooStatus =  {fooTotal, fooPassed, fooFailed, fooSkipped};
        int[] bartestStatus =  {bartestTotal, bartestPassed, bartestFailed, bartestSkipped};

        validateStatuses(mathStatus, fooStatus, bartestStatus);
        validateCoverage();
    }

    @Test(enabled = false)
    public void testWithoutCoverage() throws BallerinaTestException {
        String[] args = new String[]{"--test-report"};
        runCommand(args);

        int mathTotal = 2, mathPassed = 2, mathFailed = 0, mathSkipped = 0;
        int fooTotal = 2, fooPassed = 0, fooFailed = 1, fooSkipped = 1;
        int bartestTotal = 1, bartestPassed = 1, bartestFailed = 0, bartestSkipped = 0;

        int[] mathStatus =  {mathTotal, mathPassed, mathFailed, mathSkipped};
        int[] fooStatus =  {fooTotal, fooPassed, fooFailed, fooSkipped};
        int[] bartestStatus =  {bartestTotal, bartestPassed, bartestFailed, bartestSkipped};

        validateStatuses(mathStatus, fooStatus, bartestStatus);
        Assert.assertEquals(resultObj.get("moduleCoverage").getAsJsonArray().size(), 0);
    }

    @Test()
    public void testModuleWiseCoverage() throws BallerinaTestException {
        String[] args = new String[]{"--code-coverage", "--tests", "foo.math:*"};
        runCommand(args);

        int mathTotal = 2, mathPassed = 2, mathFailed = 0, mathSkipped = 0;
        int fooTotal = 0, fooPassed = 0, fooFailed = 0, fooSkipped = 0;
        int bartestTotal = 0, bartestPassed = 0, bartestFailed = 0, bartestSkipped = 0;

        int[] mathStatus =  {mathTotal, mathPassed, mathFailed, mathSkipped};
        int[] fooStatus =  {fooTotal, fooPassed, fooFailed, fooSkipped};
        int[] bartestStatus =  {bartestTotal, bartestPassed, bartestFailed, bartestSkipped};

        validateStatuses(mathStatus, fooStatus, bartestStatus);
        validateModuleWiseCoverage();
    }

    private void runCommand(String[] args) throws BallerinaTestException {
        balClient.runMain("test", args, null, new String[]{}, new LogLeecher[]{}, projectPath);

        Gson gson = new Gson();

        try (BufferedReader bufferedReader = Files.newBufferedReader(resultsJsonPath, StandardCharsets.UTF_8)) {
            resultObj = gson.fromJson(bufferedReader, JsonObject.class);
        } catch (IOException e) {
            throw new BallerinaTestException("Failed to read test_results.json");
        }

    }

    private void validateStatuses(int[] mathStatus, int[] fooStatus, int[] bartestStatus) {
        validateModuleStatus(mathStatus, "foo.math");
        validateModuleStatus(fooStatus, "foo");
        validateModuleStatus(bartestStatus, "foo.bar.tests");

        int[] overallStatus = {
                mathStatus[TOTAL_TESTS] + fooStatus[TOTAL_TESTS] + bartestStatus[TOTAL_TESTS],
                mathStatus[PASSED] + fooStatus[PASSED] + bartestStatus[PASSED],
                mathStatus[FAILED] + fooStatus[FAILED] + bartestStatus[FAILED],
                mathStatus[SKIPPED] + fooStatus[SKIPPED] + bartestStatus[SKIPPED]
        };

        validateProjectLevelStatus(overallStatus, resultObj);
    }

    private void validateModuleStatus(int[] moduleStatus, String moduleName) {
        for (JsonElement obj : resultObj.get("moduleStatus").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) obj);
            if (moduleName.equals(moduleObj.get("name").getAsString())) {
                String msg = "Status check failed for module : " + moduleName + ".";
                validateStatus(moduleStatus, moduleObj, msg);
                return;
            }
        }

        Assert.fail("Module status for " + moduleName + " could not be found");
    }

    private void validateProjectLevelStatus(int[] overallStatus, JsonObject moduleObj) {
        String msg = "Status check failed for project";
        validateStatus(overallStatus, moduleObj, msg);
    }

    private void validateStatus(int[] status, JsonObject obj, String msg) {
        Assert.assertEquals(obj.get("totalTests").getAsInt(), status[TOTAL_TESTS], msg);
        Assert.assertEquals(obj.get("passed").getAsInt(), status[PASSED], msg);
        Assert.assertEquals(obj.get("failed").getAsInt(), status[FAILED], msg);
        Assert.assertEquals(obj.get("skipped").getAsInt(), status[SKIPPED], msg);
    }

    private void validateCoverage() {
        JsonParser parser = new JsonParser();
        //math module
        int[] mathAddCovered = new int[] {22, 23, 24, 26, 29, 31, 32}, mathAddMissed = new int[] {27};
        float mathAddPercentageVal =
                (float) (mathAddCovered.length) / (mathAddCovered.length + mathAddMissed.length) * 100;
        float mathAddPercentage =
                (float) (Math.round(mathAddPercentageVal * 100.0) / 100.0);

        int[] mathDivideCovered = new int[] {22, 23, 25, 26, 30, 31}, mathDivideMissed = new int[] {28};
        float mathDividePercentageVal =
                (float) (mathDivideCovered.length) / (mathDivideCovered.length + mathDivideMissed.length) * 100;
        float mathDividePercentage =
                (float) (Math.round(mathDividePercentageVal * 100.0) / 100.0);

        int mathCovered = mathAddCovered.length + mathDivideCovered.length,
                mathMissed = mathAddMissed.length + mathDivideMissed.length;
        float mathPercentageVal = (float) mathCovered / (mathCovered + mathMissed) * 100;
        float mathPercentage = (float) (Math.round(mathPercentageVal * 100.0) / 100.0);

        //foo module
        int[] fooMainCovered = new int[]{19, 22, 23, 24, 29, 30, 36, 37, 42, 43, 50, 55, 56, 57, 60, 61, 64, 65, 69,
                70, 71, 74, 75}, fooMainMissed = new int[]{26};
        float fooMainPercentageVal =
                (float) (fooMainCovered.length) / (fooMainCovered.length + fooMainMissed.length) * 100;
        float fooMainPercentage =
                (float) (Math.round(fooMainPercentageVal * 100.0) / 100.0);

        int fooCovered = fooMainCovered.length, fooMissed = fooMainMissed.length;

        //bar module
        int[] barMainCovered = new int[]{19, 20}, barMainMissed = new int[]{};
        float barMainPercentageVal =
                (float) (barMainCovered.length) / (barMainMissed.length + barMainCovered.length) * 100;
        float barMainPercentage =
                (float) (Math.round(barMainPercentageVal * 100.0) / 100.0);

        int barCovered = barMainCovered.length, barMissed = barMainMissed.length;

        // project
        int totalCovered = mathCovered + barCovered + fooCovered;
        int totalMissed = mathMissed + barMissed + fooMissed;
        float coveragePercentageVal = (float) (totalCovered) / (totalCovered + totalMissed) * 100;
        float coveragePercentage = (float) (Math.round(coveragePercentageVal * 100.0) / 100.0);

        // Verify module level coverage
        for (JsonElement element : resultObj.get("moduleCoverage").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) element);

            if ("foo.math".equals(moduleObj.get("name").getAsString())) {
                // Verify coverage of individual source file
                for (JsonElement element1 : moduleObj.get("sourceFiles").getAsJsonArray()) {
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
                        Assert.fail("unrecognized file: " + fileObj.get(
                                "name").getAsString() + "in module " + moduleObj.get("name").getAsString());
                    }
                }

                // Verify coverage of module
                Assert.assertEquals(mathCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(mathMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(mathPercentage, moduleObj.get("coveragePercentage").getAsFloat());

            } else if ("foo".equals(moduleObj.get("name").getAsString())) {
                // Verify coverage of source file
                JsonObject fileObj = (JsonObject) moduleObj.get("sourceFiles").getAsJsonArray().get(0);
                Assert.assertEquals("main.bal", fileObj.get("name").getAsString());
                Assert.assertEquals(parser.parse(Arrays.toString(fooMainCovered)),
                        parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                Assert.assertEquals(parser.parse(Arrays.toString(fooMainMissed)),
                        parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                Assert.assertEquals(fooMainPercentage, fileObj.get("coveragePercentage").getAsFloat());

                // Verify coverage of module
                Assert.assertEquals(fooCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(fooMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(fooMainPercentage, moduleObj.get("coveragePercentage").getAsFloat());
            } else if ("foo.bar".equals(moduleObj.get("name").getAsString())) {
                JsonObject fileObj = (JsonObject) moduleObj.get("sourceFiles").getAsJsonArray().get(0);
                Assert.assertEquals("main.bal", fileObj.get("name").getAsString());
                Assert.assertEquals(parser.parse(Arrays.toString(barMainCovered)),
                        parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                Assert.assertEquals(parser.parse(Arrays.toString(barMainMissed)),
                        parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                Assert.assertEquals(barMainPercentage, fileObj.get("coveragePercentage").getAsFloat());

                // Verify coverage of module
                Assert.assertEquals(barCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(barMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(barMainPercentage, moduleObj.get("coveragePercentage").getAsFloat());
            } else if ("foo.bar.tests".equals(moduleObj.get("name").getAsString())) {
                // No module coverage for bar_tests
            } else {
                Assert.fail("unrecognized module: " + moduleObj.get("name").getAsString());
            }
        }

        // Verify project level coverage details
        Assert.assertEquals(totalCovered, resultObj.get("coveredLines").getAsInt());
        Assert.assertEquals(totalMissed, resultObj.get("missedLines").getAsInt());
        Assert.assertEquals(coveragePercentage, resultObj.get("coveragePercentage").getAsFloat());
    }

    private void validateModuleWiseCoverage() {
        JsonParser parser = new JsonParser();
        //math module
        int[] mathAddCovered = new int[]{22, 23, 24, 26, 29, 31, 32}, mathAddMissed = new int[]{27};
        float mathAddPercentageVal =
                (float) (mathAddCovered.length) / (mathAddCovered.length + mathAddMissed.length) * 100;
        float mathAddPercentage = (float) (Math.round(mathAddPercentageVal * 100.0) / 100.0);

        int[] mathDivideCovered = new int[]{22, 23, 25, 26, 30, 31}, mathDivideMissed = new int[]{28};
        float mathDividePercentageVal =
                (float) (mathDivideCovered.length) / (mathDivideCovered.length + mathDivideMissed.length) * 100;
        float mathDividePercentage = (float) (Math.round(mathDividePercentageVal * 100.0) / 100.0);

        int mathCovered =
                mathAddCovered.length + mathDivideCovered.length,
                mathMissed = mathAddMissed.length + mathDivideMissed.length;
        float mathPercentageVal = (float) mathCovered / (mathCovered + mathMissed) * 100;
        float mathPercentage = (float) (Math.round(mathPercentageVal * 100.0) / 100.0);

        //foo module
        int[] fooMainCovered = new int[]{}, fooMainMissed = new int[]{19, 22, 23, 24, 26, 29, 30, 36, 37,
                42, 43, 50, 55, 56, 57, 60, 61, 64, 65, 69, 70, 71, 74, 75};
        float fooMainPercentageVal =
                (float) (fooMainCovered.length) / (fooMainCovered.length + fooMainMissed.length) * 100;
        float fooMainPercentage = (float) (Math.round(fooMainPercentageVal * 100.0) / 100.0);

        int fooCovered = fooMainCovered.length, fooMissed = fooMainMissed.length;

        //bar module
        int[] barMainCovered = new int[]{}, barMainMissed = new int[]{19, 20};
        float barMainPercentageVal =
                (float) (barMainCovered.length) / (barMainMissed.length + barMainCovered.length) * 100;
        float barMainPercentage = (float) (Math.round(barMainPercentageVal * 100.0) / 100.0);

        int barCovered = barMainCovered.length, barMissed = barMainMissed.length;

        // project
        int totalCovered = mathCovered + barCovered + fooCovered;
        int totalMissed = mathMissed + barMissed + fooMissed;
        float coveragePercentageVal = (float) (totalCovered) / (totalCovered + totalMissed) * 100;
        float coveragePercentage = (float) (Math.round(coveragePercentageVal * 100.0) / 100.0);

        // Verify module level coverage
        for (JsonElement element : resultObj.get("moduleCoverage").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) element);

            if ("foo.math".equals(moduleObj.get("name").getAsString())) {
                // Verify coverage of individual source file
                for (JsonElement element1 : moduleObj.get("sourceFiles").getAsJsonArray()) {
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
                        Assert.fail("unrecognized file: " + fileObj.get(
                                "name").getAsString() + "in module " + moduleObj.get("name").getAsString());
                    }
                }

                // Verify coverage of module
                Assert.assertEquals(mathCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(mathMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(mathPercentage, moduleObj.get("coveragePercentage").getAsFloat());

            } else if ("foo".equals(moduleObj.get("name").getAsString())) {
                // Verify coverage of source file
                JsonObject fileObj = (JsonObject) moduleObj.get("sourceFiles").getAsJsonArray().get(0);
                Assert.assertEquals("main.bal", fileObj.get("name").getAsString());
                Assert.assertEquals(parser.parse(Arrays.toString(fooMainCovered)),
                        parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                Assert.assertEquals(parser.parse(Arrays.toString(fooMainMissed)),
                        parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                Assert.assertEquals(fooMainPercentage, fileObj.get("coveragePercentage").getAsFloat());

                // Verify coverage of module
                Assert.assertEquals(fooCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(fooMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(fooMainPercentage, moduleObj.get("coveragePercentage").getAsFloat());
            } else if ("foo.bar".equals(moduleObj.get("name").getAsString())) {
                JsonObject fileObj = (JsonObject) moduleObj.get("sourceFiles").getAsJsonArray().get(0);
                Assert.assertEquals("main.bal", fileObj.get("name").getAsString());
                Assert.assertEquals(parser.parse(Arrays.toString(barMainCovered)),
                        parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                Assert.assertEquals(parser.parse(Arrays.toString(barMainMissed)),
                        parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                Assert.assertEquals(barMainPercentage, fileObj.get("coveragePercentage").getAsFloat());

                // Verify coverage of module
                Assert.assertEquals(barCovered, moduleObj.get("coveredLines").getAsInt());
                Assert.assertEquals(barMissed, moduleObj.get("missedLines").getAsInt());
                Assert.assertEquals(barMainPercentage, moduleObj.get("coveragePercentage").getAsFloat());
            } else if ("foo.bar.tests".equals(moduleObj.get("name").getAsString())) {
                // No module coverage for bar_tests
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
