/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.cmd;

import com.google.gson.Gson;
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.PackageJson;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.cmd.CommandOutputUtils.readFileAsString;
import static io.ballerina.cli.cmd.CommandUtil.writeBallerinaToml;
import static io.ballerina.cli.cmd.CommandUtil.writeDependenciesToml;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;

/**
 * Unit tests for @code{CommandUtil} class used in commands.
 *
 * @since 2201.0.1
 */
public class CommandUtilTest {

    private static final Path COMMAND_UTIL_RESOURCE_DIR = Paths
            .get("src", "test", "resources", "test-resources", "command-util");

    @Test(description = "Test write new project Ballerina.toml from template package.json")
    public void testWriteBallerinaToml() throws IOException {
        // Read sample package.json
        PackageJson packageJson;
        try (InputStream inputStream = new FileInputStream(
                String.valueOf(COMMAND_UTIL_RESOURCE_DIR.resolve("sample-package.json")))) {
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            packageJson = new Gson().fromJson(fileReader, PackageJson.class);
        }

        // Create empty Ballerina.toml
        Path ballerinaTomlPath = Files.createFile(COMMAND_UTIL_RESOURCE_DIR.resolve(BALLERINA_TOML));

        // Test writeBallerinaToml method
        writeBallerinaToml(ballerinaTomlPath, packageJson, "gsheet_new_row_to_github_new_issue", "any");
        Assert.assertEquals(readFileAsString(COMMAND_UTIL_RESOURCE_DIR.resolve(BALLERINA_TOML)),
                readFileAsString(COMMAND_UTIL_RESOURCE_DIR.resolve("expected-ballerina.toml")));
    }

    @Test(description = "Test write new project Dependencies.toml from template dependency-graph.json")
    public void testWriteDependenciesToml() throws IOException {
        // Read sample package.json
        PackageJson templatePackageJson;
        try (InputStream inputStream = new FileInputStream(
                String.valueOf(COMMAND_UTIL_RESOURCE_DIR.resolve("test-write-deps-package.json")))) {
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            templatePackageJson = new Gson().fromJson(fileReader, PackageJson.class);
        }

        // Read sample dependency-graph.json
        DependencyGraphJson templateDependencyGraphJson;
        try (InputStream inputStream = new FileInputStream(
                String.valueOf(COMMAND_UTIL_RESOURCE_DIR.resolve("test-write-deps-dependency-graph.json")))) {
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            templateDependencyGraphJson = new Gson().fromJson(fileReader, DependencyGraphJson.class);
        }

        // Create empty Dependencies.toml
        Path projectPath = COMMAND_UTIL_RESOURCE_DIR.resolve("hello_template_project");
        Files.createFile(projectPath.resolve(DEPENDENCIES_TOML));

        // Test writeBallerinaToml method
        writeDependenciesToml(projectPath, templateDependencyGraphJson, templatePackageJson);
        String expected = readFileAsString(
                projectPath.resolve("test-write-deps-expected-dependencies.toml"));
        Assert.assertEquals(readFileAsString(projectPath.resolve(DEPENDENCIES_TOML)),
                expected);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        Files.deleteIfExists(COMMAND_UTIL_RESOURCE_DIR.resolve(BALLERINA_TOML));
        Files.deleteIfExists(COMMAND_UTIL_RESOURCE_DIR.resolve(DEPENDENCIES_TOML));
    }
}
