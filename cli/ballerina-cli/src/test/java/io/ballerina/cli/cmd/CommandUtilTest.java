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
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;

/**
 * Unit tests for @code{CommandUtil} class used in commands.
 *
 * @since 2.0.0
 */
public class CommandUtilTest {

    private static final Path COMMAND_UTIL_RESOURCE_DIR = Paths
            .get("src", "test", "resources", "test-resources", "command-util");

    @Test(description = "Test write new project Ballerina.toml from template package.json")
    public void testWriteBallerinaToml() throws IOException {
        // Read sample package.json
        PackageJson packageJson = null;
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

    @AfterMethod
    public void tearDown() throws IOException {
        Files.deleteIfExists(COMMAND_UTIL_RESOURCE_DIR.resolve(BALLERINA_TOML));
    }
}
