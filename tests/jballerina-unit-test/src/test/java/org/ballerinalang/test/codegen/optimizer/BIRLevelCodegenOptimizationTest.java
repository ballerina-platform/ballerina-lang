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

package org.ballerinalang.test.codegen.optimizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.test.BCompileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Test cases to verify optimized BIRNodes from `bal build --optimize` command.
 *
 * @since 2201.10.0
 */
public class BIRLevelCodegenOptimizationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BIRLevelCodegenOptimizationTest.class);
    private static final Path TESTS_SOURCE_PATH = Path.of("src/test/resources").toAbsolutePath().normalize();
    private static final Path EXPECTED_JSON_DIR_PATH =
            Path.of("src/test/resources/test-src/codegen-optimizer/json-files").toAbsolutePath().normalize();
    private static final Path SINGLE_FILE_FUNCTION_TESTS_PATH =
            Path.of(TESTS_SOURCE_PATH.toString(), "test-src/codegen-optimizer/projects/single-file-projects/Functions");
    private static final Path SINGLE_FILE_TYPE_DEFINITION_TESTS_PATH =
            Path.of(TESTS_SOURCE_PATH.toString(),
                    "test-src/codegen-optimizer/projects/single-file-projects/TypeDefinitions");
    private static final Path BUILD_PROJECT_FUNCTION_TESTS_PATH =
            Path.of(TESTS_SOURCE_PATH.toString(), "test-src/codegen-optimizer/projects/build-projects/Functions");
    private static final Path BUILD_PROJECT_TYPE_DEFINITION_TESTS_PATH =
            Path.of(TESTS_SOURCE_PATH.toString(), "test-src/codegen-optimizer/projects/build-projects/TypeDefinitions");
    private static final String OPTIMIZATION_REPORT_JSON = "codegen_optimization_report.json";
    private static final String TARGET = "target";
    private static final String SINGLE_FILE_PROJECTS = "single-file-projects";
    private static final String BUILD_PROJECTS = "build-projects";

    @Test(dataProvider = "SingleFileFunctionProjectPaths")
    public void testSingleProjectFunctionCasesWithDataProvider(Path singleFileProjectPath) {
        BCompileUtil.compileOptimized(getProjectPathInBCompileUtilCompatibleFormat(singleFileProjectPath));
        assertSingleFileJsonReportsAreSimilar(singleFileProjectPath);
    }

    @Test(dataProvider = "SingleFileTypeDefinitionProjectPaths")
    public void testSingleProjectTypeDefinitionCasesWithDataProvider(Path singleFileProjectPath) {
        BCompileUtil.compileOptimized(getProjectPathInBCompileUtilCompatibleFormat(singleFileProjectPath));
        assertSingleFileJsonReportsAreSimilar(singleFileProjectPath);
    }

    @Test(dataProvider = "BuildProjectFunctionPaths")
    public void testBuildProjectFunctionCasesWithDataProvider(Path buildProjectPath) {
        BCompileUtil.compileOptimized(getProjectPathInBCompileUtilCompatibleFormat(buildProjectPath));
        assertBuildProjectJsonReportsAreSimilar(buildProjectPath);
    }

    @Test(dataProvider = "BuildProjectTypeDefinitionPaths")
    public void testBuildProjectTypeDefinitionCasesWithDataProvider(Path buildProjectPath) {
        BCompileUtil.compileOptimized(getProjectPathInBCompileUtilCompatibleFormat(buildProjectPath));
        assertBuildProjectJsonReportsAreSimilar(buildProjectPath);
    }

    @DataProvider(name = "SingleFileFunctionProjectPaths")
    public Object[] getSingleFileFunctionProjectPaths() {
        File[] functionFiles = SINGLE_FILE_FUNCTION_TESTS_PATH.toFile().listFiles();
        Assert.assertNotNull(functionFiles);
        return Arrays.stream(functionFiles).map(file -> Path.of(file.getPath()).toAbsolutePath().normalize()).toArray();
    }

    @DataProvider(name = "SingleFileTypeDefinitionProjectPaths")
    public Object[] getSingleFileTypeDefinitionProjectPaths() {
        File[] typeDefinitionFiles = SINGLE_FILE_TYPE_DEFINITION_TESTS_PATH.toFile().listFiles();
        Assert.assertNotNull(typeDefinitionFiles);
        return Arrays.stream(typeDefinitionFiles).map(file -> Path.of(file.getPath()).toAbsolutePath().normalize())
                .toArray();
    }

    @DataProvider(name = "BuildProjectFunctionPaths")
    public Object[] getBuildProjectFunctionPaths() {
        File[] functionFiles = BUILD_PROJECT_FUNCTION_TESTS_PATH.toFile().listFiles();
        Assert.assertNotNull(functionFiles);
        return Arrays.stream(functionFiles).map(file -> Path.of(file.getPath()).toAbsolutePath().normalize()).toArray();
    }

    @DataProvider(name = "BuildProjectTypeDefinitionPaths")
    public Object[] getBuildProjectTypeDefinitionPaths() {
        File[] functionFiles = BUILD_PROJECT_TYPE_DEFINITION_TESTS_PATH.toFile().listFiles();
        Assert.assertNotNull(functionFiles);
        return Arrays.stream(functionFiles).map(file -> Path.of(file.getPath()).toAbsolutePath().normalize()).toArray();
    }

    private void assertSingleFileJsonReportsAreSimilar(Path singleFileProjectPath) {
        String jsonFileName = singleFileProjectPath.getFileName().toString().replace(".bal", ".json");
        Path actualJsonPath = singleFileProjectPath.getParent().resolve(OPTIMIZATION_REPORT_JSON);

        JsonObject expectedJsonObject =
                fileContentAsObject(EXPECTED_JSON_DIR_PATH.resolve(SINGLE_FILE_PROJECTS).resolve(jsonFileName));
        JsonObject actualJsonObject = fileContentAsObject(actualJsonPath);
        Assert.assertEquals(actualJsonObject, expectedJsonObject);
        actualJsonPath.toFile().delete();
    }

    private void assertBuildProjectJsonReportsAreSimilar(Path buildProjectPath) {
        String jsonFileName = buildProjectPath.getFileName().toString() + ".json";
        Path actualJsonPath = buildProjectPath.resolve(TARGET).resolve(OPTIMIZATION_REPORT_JSON);

        JsonObject expectedJsonObject =
                fileContentAsObject(EXPECTED_JSON_DIR_PATH.resolve(BUILD_PROJECTS).resolve(jsonFileName));
        JsonObject actualJsonObject = fileContentAsObject(actualJsonPath);
        Assert.assertEquals(actualJsonObject, expectedJsonObject);
        actualJsonPath.toFile().delete();
    }

    private String getProjectPathInBCompileUtilCompatibleFormat(Path projectAbsolutePath) {
        return projectAbsolutePath.toString().replace(TESTS_SOURCE_PATH.toString(), "").substring(1);
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
