/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

/**
 * Test class to test report for Codegen ballerina projects.
 */
public class CodegenCodeCoverageTest extends BaseTestCase {
    private static final String TOTAL_TESTS = "totalTests";
    private static final String PASSED_TESTS = "passed";
    private static final String FAILED_TESTS = "failed";
    private static final String SKIPPED_TESTS = "skipped";
    private static final String FOO_COVERED = "fooCovered";
    private static final String FOO_MISSED = "fooMissed";
    private static final String MAIN_COVERED = "mainCovered";
    private static final String MAIN_MISSED = "mainMissed";
    Path repoBalaPath;
    private BMainInstance balClient;
    private JsonObject resultObj;

    @BeforeClass
    public void setup() throws BallerinaTestException, IOException {
        balClient = new BMainInstance(balServer);
        FileUtils.copyFolder(Paths.get("build").resolve("compiler-plugin-jars"),
                projectBasedTestsPath.resolve("compiler-plugin-jars"));
        repoBalaPath = Paths.get(balServer.getServerHome()).resolve("repo");
    }

    @DataProvider(name = "provideCoverageData")
    public Object[][] provideCoverageData() {
        return new Object[][]{
                {
                        // adds a new function to all files
                        "line-insert-test",
                        "package_comp_plugin_code_modify_add_function",
                        Map.of(TOTAL_TESTS, 2, PASSED_TESTS, 2, FAILED_TESTS, 0, SKIPPED_TESTS, 0),
                        Map.of(
                                FOO_COVERED, new int[]{3, 4},
                                FOO_MISSED, new int[]{8, 11, 12},
                                MAIN_COVERED, new int[]{8, 9},
                                MAIN_MISSED, new int[]{3, 4, 5}
                        )
                },
                {
                        // remove "bar" function if available from all files
                        "line-remove-test",
                        "package_comp_plugin_code_modify_remove_function",
                        Map.of(TOTAL_TESTS, 2, PASSED_TESTS, 2, FAILED_TESTS, 0, SKIPPED_TESTS, 0),
                        Map.of(
                                FOO_COVERED, new int[]{3, 4},
                                FOO_MISSED, new int[]{11, 12},
                                MAIN_COVERED, new int[]{8, 9},
                                MAIN_MISSED, new int[]{3, 4, 5}
                        )
                },
                {
                        // remove empty functions and add a new function.
                        "line-insert-and-remove-test",
                        "package_comp_plugin_code_modify_add_remove_function",
                        Map.of(TOTAL_TESTS, 2, PASSED_TESTS, 2, FAILED_TESTS, 0, SKIPPED_TESTS, 0),
                        Map.of(
                                FOO_COVERED, new int[]{3, 4},
                                FOO_MISSED, new int[]{9, 10},
                                MAIN_COVERED, new int[]{17, 18},
                                MAIN_MISSED, new int[]{3, 4, 5}
                        )
                }
        };
    }

    @Test(description = "Test code coverage report generation for a codegen project",
            dataProvider = "provideCoverageData")
    public void codegenCoverageTest(String projectName, String compilerPluginName, Map<String,
            Integer> status, Map<String, int[]> coverage) throws BallerinaTestException, IOException {
        publishCompilerPlugin(compilerPluginName);
        String[] args = new String[]{"--code-coverage"};
        runCommand(projectName, args);
        validateStatus(status);
        validateCoverage(coverage);
    }

    private void publishCompilerPlugin(String compilerPluginName) throws BallerinaTestException, IOException {
        String compilerPluginBalaPath = projectBasedTestsPath.resolve("compiler-plugins")
                .resolve(compilerPluginName).toString();
        balClient.runMain("pack", new String[]{}, null, null, new LogLeecher[]{}, compilerPluginBalaPath);
        Path balaPath = projectBasedTestsPath.resolve(compilerPluginBalaPath).resolve("target").resolve("bala")
                .resolve("samjs-" + compilerPluginName + "-java17-0.1.0.bala");
        BCompileUtil.copyBalaToExtractedDist(balaPath, "samjs", compilerPluginName, "0.1.0", repoBalaPath);
    }


    private void validateStatus(Map<String, Integer> status) {
        String moduleName = "codegen_coverage_test";
        for (JsonElement obj : resultObj.get("moduleStatus").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) obj);
            if (moduleName.equals(moduleObj.get("name").getAsString())) {
                String msg = "Status check failed for module : " + moduleName + ".";
                validateStatus(status, moduleObj, msg);
                return;
            }
        }
        Assert.fail("Module status for " + moduleName + " could not be found");
    }

    private void validateCoverage(Map<String, int[]> coverage) {
        // foo file
        int[] fooCovered = coverage.get(FOO_COVERED), fooMissed = coverage.get(FOO_MISSED);
        float fooPercentageVal =
                (float) (fooCovered.length) / (fooCovered.length + fooMissed.length) * 100;
        float fooPercentage =
                (float) (Math.round(fooPercentageVal * 100.0) / 100.0);
        int fooCoveredVal = fooCovered.length, fooMissedVal = fooMissed.length;
        // main file
        int[] mainCovered = coverage.get(MAIN_COVERED), mainMissed = coverage.get(MAIN_MISSED);
        float mainPercentageVal =
                (float) (mainCovered.length) / (mainCovered.length + mainMissed.length) * 100;
        float mainPercentage =
                (float) (Math.round(mainPercentageVal * 100.0) / 100.0);
        int mainCoveredVal = mainCovered.length, mainMissedVal = mainMissed.length;

        // project
        int totalCovered = mainCoveredVal + fooCoveredVal;
        int totalMissed = mainMissedVal + fooMissedVal;
        float coveragePercVal = (float) (totalCovered) / (totalCovered + totalMissed) * 100;
        float coveragePercentage = (float) (Math.round(coveragePercVal * 100.0) / 100.0);

        // Verify module level coverage
        for (JsonElement element : resultObj.get("moduleCoverage").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) element);
            for (JsonElement sourceFiles : moduleObj.get("sourceFiles").getAsJsonArray()) {
                JsonObject fileObj = (JsonObject) sourceFiles;
                if ("foo.bal".equals(fileObj.get("name").getAsString())) {
                    Assert.assertEquals(JsonParser.parseString(Arrays.toString(fooCovered)),
                            JsonParser.parseString(fileObj.get("coveredLines").getAsJsonArray().toString()));
                    Assert.assertEquals(JsonParser.parseString(Arrays.toString(fooMissed)),
                            JsonParser.parseString(fileObj.get("missedLines").getAsJsonArray().toString()));
                    Assert.assertEquals(fooPercentage, fileObj.get("coveragePercentage").getAsFloat());
                } else if ("main.bal".equals(fileObj.get("name").getAsString())) {
                    Assert.assertEquals(JsonParser.parseString(Arrays.toString(mainCovered)),
                            JsonParser.parseString(fileObj.get("coveredLines").getAsJsonArray().toString()));
                    Assert.assertEquals(JsonParser.parseString(Arrays.toString(mainMissed)),
                            JsonParser.parseString(fileObj.get("missedLines").getAsJsonArray().toString()));
                    Assert.assertEquals(mainPercentage, fileObj.get("coveragePercentage").getAsFloat());
                }
            }

            // Verify coverage of module
            Assert.assertEquals(coveragePercentage, moduleObj.get("coveragePercentage").getAsFloat());
        }

    }

    private void validateStatus(Map<String, Integer> status, JsonObject obj, String msg) {
        Assert.assertEquals(obj.get(TOTAL_TESTS).getAsInt(), status.get(TOTAL_TESTS), msg);
        Assert.assertEquals(obj.get(PASSED_TESTS).getAsInt(), status.get(PASSED_TESTS), msg);
        Assert.assertEquals(obj.get(FAILED_TESTS).getAsInt(), status.get(FAILED_TESTS), msg);
        Assert.assertEquals(obj.get(SKIPPED_TESTS).getAsInt(), status.get(SKIPPED_TESTS), msg);
    }

    private void runCommand(String projectName, String[] args) throws BallerinaTestException {
        Path projectPath = projectBasedTestsPath.resolve("code-coverage-report-test").resolve(projectName);
        Path resultsJsonPath = projectPath.resolve("target").resolve("report").resolve("test_results.json");
        balClient.runMain("test", args, null, new String[]{}, new LogLeecher[]{}, projectPath.toString());
        Gson gson = new Gson();
        try (BufferedReader bufferedReader = Files.newBufferedReader(resultsJsonPath, StandardCharsets.UTF_8)) {
            resultObj = gson.fromJson(bufferedReader, JsonObject.class);
        } catch (IOException e) {
            throw new BallerinaTestException("Failed to read test_results.json");
        }
    }
}
