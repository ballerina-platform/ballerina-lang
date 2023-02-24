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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test class to test positive scenarios of testerina using a ballerina project.
 */
public class MockTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException, IOException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.toString();
        FileUtils.copyFolder(Paths.get("build/libs"),
                Paths.get(projectPath, "object-mocking-tests", "libs"));
        // Build and push config Lib project.
        compilePackageAndPushToLocal(projectBasedTestsPath.resolve("mockLibProject").toString(),
                "testOrg-mockLib-any-0.1.0");
    }

    @Test()
    public void testFunctionMocking() throws BallerinaTestException {
        String msg1 = "14 passing";
        String msg2 = "3 failing";
        String[] args = mergeCoverageArgs(new String[]{"function-mocking-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to function mocking failure in test framework..\nOutput:\n" + output);
        }
    }

    @Test
    public void testFunctionMockingLegacy() throws BallerinaTestException {
        String msg1 = "1 passing";
        String msg2 = "0 failing";
        String[] args = mergeCoverageArgs(new String[]{"legacy-function-mocking-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to function mocking failure in test framework..\nOutput:\n" + output);
        }
    }

    @Test()
    public void testObjectMocking() throws BallerinaTestException {
        String msg1 = "9 passing";
        String msg2 = "7 failing";

        String[] args = mergeCoverageArgs(new String[]{"object-mocking-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to object mocking failure in test framework.\nOutput:\n" + output);
        }
    }

    /**
     * Test object mock using test double when client implementation contains a resource function.
     *
     * @throws BallerinaTestException
     */
    @Test()
    public void testObjectMockDouble() throws BallerinaTestException {
        String msg1 = "1 passing";
        String[] args = mergeCoverageArgs(new String[]{"object-mocking-tests2"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1)) {
            Assert.fail("Test failed due to object mocking failure with resource functions in test framework." +
                    "\nOutput:\n" + output);
        }
    }

    @Test()
    public void testFunctionMockingModuleLevel() throws BallerinaTestException {
        String msg1 = "3 passing";

        String[] args = mergeCoverageArgs(new String[]{"function-mocking-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1)) {
            Assert.fail("Test failed due to function mocking failure in test framework..\nOutput:\n" + output);
        }
    }

    @Test()
    public void testCoverageWithMocking() throws BallerinaTestException {
        String msg1 = "2 passing";
        String[] args = mergeCoverageArgs(new String[]{"mocking-coverage-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1)) {
            Assert.fail("Test failed due to object mocking failure in test framework.\nOutput:\n" + output);
        }
        Path resultsJsonPath = projectBasedTestsPath.resolve("mocking-coverage-tests").resolve("target")
                .resolve("report").resolve("test_results.json");
        JsonObject resultObj;
        Gson gson = new Gson();
        try (BufferedReader bufferedReader = Files.newBufferedReader(resultsJsonPath, StandardCharsets.UTF_8)) {
            resultObj = gson.fromJson(bufferedReader, JsonObject.class);
        } catch (IOException e) {
            throw new BallerinaTestException("Failed to read test_results.json");
        }
        JsonArray moduleCoverage = resultObj.get("moduleCoverage").getAsJsonArray();
        Assert.assertEquals(moduleCoverage.size(), 3);
        JsonObject moduleObj = (JsonObject) moduleCoverage.get(0);
        Assert.assertEquals(moduleObj.get("name").toString(), "\"mocking_coverage\"");
        Assert.assertEquals(moduleObj.get("coveredLines").toString(), "5");
        Assert.assertEquals(moduleObj.get("missedLines").toString(), "2");
        Assert.assertEquals(moduleObj.get("coveragePercentage").toString(), "71.43");
        moduleObj = (JsonObject) moduleCoverage.get(1);
        Assert.assertEquals(moduleObj.get("name").toString(), "\"mocking_coverage.mod1\"");
        Assert.assertEquals(moduleObj.get("coveredLines").toString(), "3");
        Assert.assertEquals(moduleObj.get("missedLines").toString(), "3");
        Assert.assertEquals(moduleObj.get("coveragePercentage").toString(), "50.0");
        moduleObj = (JsonObject) moduleCoverage.get(2);
        Assert.assertEquals(moduleObj.get("name").toString(), "\"mocking_coverage.mod2\"");
        Assert.assertEquals(moduleObj.get("coveredLines").toString(), "4");
        Assert.assertEquals(moduleObj.get("missedLines").toString(), "3");
        Assert.assertEquals(moduleObj.get("coveragePercentage").toString(), "57.14");
    }

    @Test
    public void testFunctionMockingInMultipleModulesWithDependencies() throws BallerinaTestException {
        String msg1 = "function_mocking_with_dependencies.a_dependency\n\t\t[pass] test1\n\n\n\t\t" +
                "1 passing\n\t\t0 failing\n\t\t0 skipped";
        String msg2 = "function_mocking_with_dependencies.b_dependent\n\t\t[pass] test1\n\n\n\t\t" +
                "1 passing\n\t\t0 failing\n\t\t0 skipped";
        String[] args = mergeCoverageArgs(new String[]{"function-mocking-tests-with-dependencies"});
        String output = balClient.runMainAndReadStdOut("test", args, new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to function mocking failure in test framework.\nOutput:\n" + output);
        }
    }

    @Test(dataProvider = "testNegativeCases")
    public void testObjectMocking_NegativeCases(String message, String test) throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", test});
        String output =
                balClient.runMainAndReadStdOut("test", args, new HashMap<>(),
                        projectBasedTestsPath.resolve("object-mocking-tests").toString(), false);
        if (!output.contains(message)) {
            throw new BallerinaTestException(
                    "Test failed due to default module single test failure.\nOutput:\n" + output);
        }
    }

    @DataProvider(name = "testNegativeCases")
    public static Object[][] negativeCases() {
        return new Object[][] {
                {"incorrect type of argument provided at position '1' to mock the function 'get()'",
                        "object_mocking:testDefaultIncompatibleArgs"},
                {"return value provided does not match the type of 'url'",
                        "object_mocking:testDefaultInvalidMemberReturnValue"},
                {"invalid field name 'invalidField' provided",
                        "object_mocking:testDefaultMockInvalidFieldName"},
                {"return value provided does not match the return type of function 'get()'",
                        "object_mocking:testDefaultMockInvalidReturnValue"},
                {"return value provided does not match the return type of function 'get()'",
                        "object_mocking:testDefaultMockWrongAction"},
                {"too many argument provided to mock the function 'get()'",
                        "object_mocking:testDefaultTooManyArgs"},
                {"return value provided does not match the return type of function 'get_stream()'",
                        "object_mocking:testMockInvalidStream"}
        };
    }

    private void compilePackageAndPushToLocal(String packagPath, String balaFileName) throws BallerinaTestException {
        LogLeecher buildLeecher = new LogLeecher("target" + File.separator + "bala" + File.separator +
                balaFileName + ".bala");
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed target" + File.separator + "bala" +
                File.separator + balaFileName + ".bala to 'local' repository.");
        balClient.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                packagPath);
        buildLeecher.waitForText(5000);
        balClient.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                packagPath);
        pushLeecher.waitForText(5000);
    }
}
