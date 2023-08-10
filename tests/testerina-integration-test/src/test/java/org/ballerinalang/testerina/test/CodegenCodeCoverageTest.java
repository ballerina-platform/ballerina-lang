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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Test class to test report for Codegen ballerina projects.
 */
public class CodegenCodeCoverageTest extends BaseTestCase {
    private BMainInstance balClient;
    private String projectPath;
    private Path resultsJsonPath;
    private JsonObject resultObj;
    private static final int TOTAL_TESTS = 0;
    private static final int PASSED = 1;
    private static final int SKIPPED = 2;
    private static final int FAILED = 3;

    @BeforeClass
    public void setup() throws BallerinaTestException, IOException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("codegen-coverage-test").toString();
        resultsJsonPath = projectBasedTestsPath.resolve("codegen-coverage-test").resolve("target").resolve("report")
                .resolve("test_results.json");
        Path repoBalaPath = Paths.get(balServer.getServerHome()).resolve("repo");
        Path balaPath = projectBasedTestsPath.resolve(
                "codegen-coverage-test/balas/package_comp_plugin_code_modify_add_function.bala");
        BCompileUtil.copyBalaToExtractedDist(balaPath,
                "samjs",
                "package_comp_plugin_code_modify_add_function",
                "0.1.0",
                repoBalaPath);
    }

    @Test(enabled = false)
    public void codegenCoverageTest() throws BallerinaTestException {
        String[] args = new String[] {"--code-coverage"};
        runCommand(args);
        int total = 2, passed = 2, failed = 0, skipped = 0;
        int[] status = {total, passed, failed, skipped};
        validateStatus(status, "codegen_coverage_test");
        validateCoverage();
    }

    private void validateCoverage() {
        JsonParser parser = new JsonParser();
        // foo file
        int[] fooCovered = new int[] {3, 4}, fooMissed = new int[] {8, 11, 12};
        float fooPercentageVal =
                (float) (fooCovered.length) / (fooCovered.length + fooMissed.length) * 100;
        float fooPercentage =
                (float) (Math.round(fooPercentageVal * 100.0) / 100.0);
        int fooCoveredVal = fooCovered.length, fooMissedVal = fooMissed.length;
        // main file
        int[] mainCovered = new int[] {8, 9}, mainMissed = new int[] {3, 4, 5};
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
                    Assert.assertEquals(parser.parse(Arrays.toString(fooCovered)),
                            parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                    Assert.assertEquals(parser.parse(Arrays.toString(fooMissed)),
                            parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                    Assert.assertEquals(fooPercentage, fileObj.get("coveragePercentage").getAsFloat());
                } else if ("main.bal".equals(fileObj.get("name").getAsString())) {
                    Assert.assertEquals(parser.parse(Arrays.toString(mainCovered)),
                            parser.parse(fileObj.get("coveredLines").getAsJsonArray().toString()));
                    Assert.assertEquals(parser.parse(Arrays.toString(mainMissed)),
                            parser.parse(fileObj.get("missedLines").getAsJsonArray().toString()));
                    Assert.assertEquals(mainPercentage, fileObj.get("coveragePercentage").getAsFloat());
                }
            }

            // Verify coverage of module
            Assert.assertEquals(coveragePercentage, moduleObj.get("coveragePercentage").getAsFloat());
        }

    }


    private void validateStatus(int[] status, String moduleName) {
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

    private void validateStatus(int[] status, JsonObject obj, String msg) {
        Assert.assertEquals(obj.get("totalTests").getAsInt(), status[TOTAL_TESTS], msg);
        Assert.assertEquals(obj.get("passed").getAsInt(), status[PASSED], msg);
        Assert.assertEquals(obj.get("failed").getAsInt(), status[FAILED], msg);
        Assert.assertEquals(obj.get("skipped").getAsInt(), status[SKIPPED], msg);
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
}
