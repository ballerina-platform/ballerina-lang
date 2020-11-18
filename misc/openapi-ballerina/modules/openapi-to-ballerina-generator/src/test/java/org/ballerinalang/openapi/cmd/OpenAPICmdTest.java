/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.cmd;

import org.ballerinalang.tool.BLauncherException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

/**
 * OpenAPI command test suit.
 */
public class OpenAPICmdTest extends OpenAPICommandTest {
    private static final Path RES_DIR = OpenAPICommandTest.getResourceFolderPath();
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    private OpenAPIBallerinaProject petProject;

    @BeforeTest(description = "This will create a new ballerina project for testing below scenarios.")
    public void setupBallerinaProject() throws IOException {
        super.setup();
        petProject = OpenAPICommandTest.createBalProject(tmpDir.toString());
    }

    @Test(description = "Test openapi command with help flag")
    public void testOpenAPICmdHelp() throws IOException {
        String[] args = {"-h"};
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream);
        new CommandLine(openApiCommand).parseArgs(args);
        openApiCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n       The Ballerina OpenAPI Tool"));
    }

    @Test(description = "Test openapi command without help flag")
    public void testOpenAPICmdHelpWithoutFlag() throws IOException {
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream);
        new CommandLine(openApiCommand);
        openApiCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n       The Ballerina OpenAPI Tool"));
    }

    @Test(description = "Test openapi gen-service without openapi contract file")
    public void testWithoutOpenApiContract() {
        String[] args = {"--input"};
        OpenApiCmd cmd = new OpenApiCmd(printStream);
        new CommandLine(cmd).parseArgs(args);
        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }
        Assert.assertTrue(output.contains("An OpenApi definition file is required to generate the service."));
    }

    @Test(description = "Test openapi command with --input flag", enabled = false)
    public void testOpenAPICmdInput() throws IOException {
        Path petstoreYaml = RES_DIR.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString()};
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream);
        new CommandLine(openApiCommand).parseArgs(args);
        openApiCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("Following files were created."));
    }

    @Test(description = "Test openapi gen-service for successful service generation", enabled = false)
    public void testSuccessfulServiceGeneration() throws IOException {
        Path petstoreYaml = RES_DIR.resolve(Paths.get("petstore.yaml"));
        String[] args = {"-i", petstoreYaml.toString(), "-o", resourcePath.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream);
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }
        Path expectedServiceFile = RES_DIR.resolve(Paths.get("expected_gen", "petstore_gen.bal"));
        Path expectedSchemaFile = RES_DIR.resolve(Paths.get("expected_gen", "petstore_schema.bal"));

        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();

        Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile);
        String expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining("\n"));
        expectedSchemaLines.close();

        if (Files.exists(resourcePath.resolve("petstoreClient.bal"))
                && Files.exists(resourcePath.resolve("petstoreService.bal"))
                && Files.exists(resourcePath.resolve("schema.bal"))) {

            Stream<String> schemaLines = Files.lines(resourcePath.resolve("schema.bal"));
            String generatedSchema = schemaLines.collect(Collectors.joining("\n"));
            schemaLines.close();

            if (expectedSchemaContent.trim().equals(generatedSchema.trim())) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
            }
        } else {
            Assert.fail("Service generation failed.");
        }
    }
}
