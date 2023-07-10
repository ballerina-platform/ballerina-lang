/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Test class to test code coverage exclusions using a ballerina project.
 */

public class ExcludeFromCodeCoverageTest extends BaseTestCase {
    private BMainInstance balClient;
    private Path projectPath;
    private Path resultsJsonPath;
    private JsonObject resultObj;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("code-cov-exclusion");
        resultsJsonPath = projectBasedTestsPath.resolve("code-cov-exclusion").resolve("target").resolve("report")
                .resolve("test_results.json");
    }

    @Test(description = "Exclude coverage with relative source paths and wildcards", enabled = false)
    public void testExcludingBalFileCoverage() throws BallerinaTestException, IOException {
        String [][] exclusionListOfList = {{"./main.bal", "./modules/util/util.bal", "./generated/util/util_gen.bal",
                "./generated/util2/util2_gen.bal", "./generated/main_gen.bal"},
                {"./*", "./modules/util/ut*.bal", "./generated**"},
                {"./*", "modules/util/ut*.bal", "generated"},
                {"./*", "modules/util/ut*.bal", "generated/"},
                {"./*.bal", "modules/util/ut*.bal", "/generated"},
                {"./*.bal", "modules/util/ut*.bal", "/generated/"},
                {"./*.bal", "modules/util/uti?.bal", "/generated/"},
                {"./*.bal", "modules/util/uti[k-m].bal", "/generated/"},
                {"./*.bal", "**util/util.bal", "/generated/"},
                {"./"},
                {"./**"},
                {"/**"},
                {"*.bal"}
        };
        for (String [] exclusionList : exclusionListOfList) {
            String[] args = mergeCoverageArgs(new String[]{"--test-report", "--coverage-format=xml",
                    "--excludes=" + String.join(",", exclusionList)});
            String output = balClient.runMainAndReadStdOut("test", args,
                    new HashMap<>(), projectPath.toString(), false);
            Gson gson = new Gson();

            try (BufferedReader bufferedReader = Files.newBufferedReader(resultsJsonPath, StandardCharsets.UTF_8)) {
                resultObj = gson.fromJson(bufferedReader, JsonObject.class);
            } catch (IOException e) {
                throw new BallerinaTestException("Failed to read test_results.json");
            }
            validateModuleWiseCoverage();
        }
    }

    @Test(description = "Exclude a source file from coverage exclusion list", enabled = false)
    public void testExcludesSrcFileFromExclusionList() throws BallerinaTestException, IOException {
        String [] exclusionList =  {"./*", "!./main.bal"};
        String[] args = mergeCoverageArgs(new String[]{"--test-report", "--coverage-format=xml",
                "--excludes=" + String.join(",", exclusionList)});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath.toString(), false);
        Gson gson = new Gson();

        try (BufferedReader bufferedReader = Files.newBufferedReader(resultsJsonPath, StandardCharsets.UTF_8)) {
            resultObj = gson.fromJson(bufferedReader, JsonObject.class);
        } catch (IOException e) {
            throw new BallerinaTestException("Failed to read test_results.json");
        }
        validateModuleWiseCoverage(40F, 100F, 0F, 2, 2,
                1, 8, 5, 61.54F);
    }

    @Test(description = "Exclude a folder from coverage exclusion list", enabled = false)
    public void testExcludesSrcFolderFromExclusionList() throws BallerinaTestException, IOException {
        String [] exclusionList = {"./generated", "!./generated/util"};
        String[] args = mergeCoverageArgs(new String[]{"--test-report", "--coverage-format=xml",
                "--excludes=" + String.join(",", exclusionList)});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath.toString(), false);
        Gson gson = new Gson();

        try (BufferedReader bufferedReader = Files.newBufferedReader(resultsJsonPath, StandardCharsets.UTF_8)) {
            resultObj = gson.fromJson(bufferedReader, JsonObject.class);
        } catch (IOException e) {
            throw new BallerinaTestException("Failed to read test_results.json");
        }
        validateModuleWiseCoverage(0F, 100F, 0F, 1, 2,
                0,  6, 3, 66.67F);
    }

    private void validateModuleWiseCoverage() {
        validateModuleWiseCoverage(0F, 0F, 0F, 0, 0,
                 0, 0, 0, 0F);
    }

    private void validateModuleWiseCoverage(Float defaultModuleCov, Float utilCov, Float util2Cov,
                                            int defaultModuleFilesCount, int utilFilesCount, int util2FilesCount,
                                            int coveredLines, int missedLines, Float coveragePercentage) {
        for (JsonElement element : resultObj.get("moduleCoverage").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) element);

            if ("code_cov_exclusion".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(defaultModuleCov, moduleObj.get("coveragePercentage").getAsFloat());
                Assert.assertEquals(defaultModuleFilesCount, moduleObj.get("sourceFiles").getAsJsonArray().size());

            } else if ("code_cov_exclusion.util".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(utilCov, moduleObj.get("coveragePercentage").getAsFloat());
                Assert.assertEquals(utilFilesCount, moduleObj.get("sourceFiles").getAsJsonArray().size());
            } else if ("code_cov_exclusion.util2".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(util2Cov, moduleObj.get("coveragePercentage").getAsFloat());
                Assert.assertEquals(util2FilesCount, moduleObj.get("sourceFiles").getAsJsonArray().size());
            }

            // Verify project level coverage details
            Assert.assertEquals(coveredLines, resultObj.get("coveredLines").getAsInt());
            Assert.assertEquals(missedLines, resultObj.get("missedLines").getAsInt());
            Assert.assertEquals(coveragePercentage, resultObj.get("coveragePercentage").getAsFloat());
        }
    }
}
