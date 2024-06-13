/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.testerina.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test cases to verify optimized BIRNodes from `bal test --optimize` command.
 *
 * @since 2201.10.0
 */
public class OptimizedExecutableTestingTest extends BaseTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimizedExecutableTestingTest.class);
    private BMainInstance balClient;
    private String projectPath;
    private static final String TARGET = "target";
    private static final String PROJECT_NAME = "optimized_test_run_test";
    private static final String OPTIMIZATION_REPORT_JSON = "codegen_optimization_report.json";
    private static final Path EXPECTED_CODEGEN_OPTIMIZATION_REPORTS_DIR =
            Paths.get("src", "test", "resources", "codegen-optimization-reports", PROJECT_NAME);

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve(PROJECT_NAME).toString();
    }

    @Test()
    public void testWithCommonDependencies() throws BallerinaTestException, IOException {
        String output = balClient.runMainAndReadStdOut("test", new String[]{"--optimize", "--verbose"}, new HashMap<>(),
                projectPath, true);
        AssertionUtils.assertOutput("OptimizedExecutableTestingTest-testWithCommonDependencies.txt",
                AssertionUtils.replaceBIRNodeAnalysisTime(output));
        assertBuildProjectJsonReportsAreSimilar(Path.of(projectPath));
    }

    private void assertBuildProjectJsonReportsAreSimilar(Path buildProjectPath) {
        Path actualJsonPath = buildProjectPath.resolve(TARGET).resolve(OPTIMIZATION_REPORT_JSON);
        JsonObject expectedJsonObject =
                fileContentAsObject(EXPECTED_CODEGEN_OPTIMIZATION_REPORTS_DIR.resolve(OPTIMIZATION_REPORT_JSON));
        JsonObject actualJsonObject = fileContentAsObject(actualJsonPath);
        Assert.assertEquals(actualJsonObject, expectedJsonObject);
    }

    private static JsonObject fileContentAsObject(Path filePath) {
        String contentAsString = "";
        try {
            contentAsString = new String(Files.readAllBytes(filePath));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return JsonParser.parseString(contentAsString).getAsJsonObject();
    }
}
