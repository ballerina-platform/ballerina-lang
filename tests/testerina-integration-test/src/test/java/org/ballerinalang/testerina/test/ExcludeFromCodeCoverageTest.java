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

    @Test()
    public void testExcludingBalFileCoverage() throws BallerinaTestException, IOException {
        String [] exclusionList = {"./main.bal", "./modules/util/util.bal", "./generated/util/util_gen.bal",
                "./generated/util2/util2_gen.bal", "./generated/main_gen.bal"};
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
    private void validateModuleWiseCoverage() {
        for (JsonElement element : resultObj.get("moduleCoverage").getAsJsonArray()) {
            JsonObject moduleObj = ((JsonObject) element);

            if ("code_cov_exclusion".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(new Float(0.0), moduleObj.get("coveragePercentage").getAsFloat());
                Assert.assertEquals(0, moduleObj.get("sourceFiles").getAsJsonArray().size());

            } else if ("code_cov_exclusion.util".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(new Float(0.0), moduleObj.get("coveragePercentage").getAsFloat());
                Assert.assertEquals(0, moduleObj.get("sourceFiles").getAsJsonArray().size());
            } else if ("code_cov_exclusion.util2".equals(moduleObj.get("name").getAsString())) {
                Assert.assertEquals(new Float(0.0), moduleObj.get("coveragePercentage").getAsFloat());
                Assert.assertEquals(0, moduleObj.get("sourceFiles").getAsJsonArray().size());
            }

            // Verify project level coverage details
            Assert.assertEquals(0, resultObj.get("coveredLines").getAsInt());
            Assert.assertEquals(0, resultObj.get("missedLines").getAsInt());
            Assert.assertEquals(new Float(0.0), resultObj.get("coveragePercentage").getAsFloat());
        }
    }
}
