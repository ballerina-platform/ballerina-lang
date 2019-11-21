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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains tests necessary to test OpenApi Generate Service command.
 */
public class OpenApiGenServiceCmdTest extends OpenAPICommandTest {
    private static final Path RES_DIR = OpenAPICommandTest.getResourceFolderPath();
    private OpenAPIBallerinaProject petProject;

    @BeforeTest(description = "This will create a new ballerina project for testing below scenarios.")
    public void setupBallerinaProject() throws IOException {
        super.setup();
        petProject = OpenAPICommandTest.createBalProject(tmpDir.toString());
    }

    @Test(description = "Test openapi gen-service for invalid ballerina project")
    public void testIsLocationBallerinaProject() {
        String[] args = {"petsModule:petService", "../petstore.yml"};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("Ballerina service generation should be done from the project root."));
    }

    @Test(description = "Test openapi gen-service with help option")
    public void testWithHelpOption() throws IOException {
        String[] args = {"-h"};

        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("ballerina-openapi-gen-service - Convert an OpenApi contract to a\n" +
                                          "       Ballerina mock service"));
    }

    @Test(description = "Test openapi gen-service without help option")
    public void testWithOutHelpOption() {
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("A module name and a service name is required in order to generate " +
                "the ballerina service for the provided OpenApi contract."));
    }

    @Test(description = "Test openapi gen-service only with module name option")
    public void testOnlyWithModuleNameOption() {
        String[] args = {"petsModule:"};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("A module name and a service name is required in order to " +
                "generate the ballerina service for the provided OpenApi contract"));
    }

    @Test(description = "Test openapi gen-service only with service name option ")
    public void testOnlyWithServiceNameOption() {
        String[] args = {":petService"};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("A module name is required in order to generate the ballerina " +
                "service for the provided OpenApi contract."));
    }

    @Test(description = "Test openapi gen-service with a space as module name")
    public void testBlankAsModuleName() {
        String[] args = {" :petService"};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("A module name is required in order to generate the ballerina " +
                "service for the provided OpenApi contract."));
    }

    @Test(description = "Test openapi gen-service with a space as service name")
    public void testBlankAsServiceName() {
        String[] args = {"petsModule: "};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("A service name is required in order to generate the ballerina" +
                " service for the provided OpenApi contract. "));
    }

    @Test(description = "Test openapi gen-service without openapi contract file")
    public void testWithoutOpenApiContract() {
        String[] args = {"petsModule:petService"};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, tmpDir.toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("An OpenApi definition file is required to generate the service."));
    }

    @Test(description = "Test openapi gen-service for invalid openapi contract")
    public void testInvalidOpenApiContract() {
        String[] args = {"petsModule:petService", "../petstore.yml"};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, petProject.getBalProjectPath().toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(output.contains("Could not resolve a valid OpenApi contract in "));
    }

    @Test(description = "Test openapi gen-service for successful service generation", enabled = false)
    public void testSuccessfulServiceGeneration() throws IOException {
        Path petstoreYaml = RES_DIR.resolve(Paths.get("petstore.yaml"));
        String[] args = {"petsModule:petService", petstoreYaml.toString()};
        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, petProject.getBalProjectPath().toString());
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

        if (Files.exists(petProject.getResourcePath().resolve(petstoreYaml.getFileName()))
                && Files.exists(petProject.getSrcPath().resolve("petsModule").resolve("petservice.bal"))
                && Files.exists(petProject.getSrcPath().resolve("petsModule").resolve("schema.bal"))) {

            Stream<String> serviceLines = Files.lines(petProject.getSrcPath()
                    .resolve("petsModule").resolve("petservice.bal"));
            String generatedService = serviceLines.collect(Collectors.joining("\n"));
            serviceLines.close();

            Stream<String> schemaLines = Files.lines(petProject.getSrcPath()
                    .resolve("petsModule").resolve("schema.bal"));
            String generatedSchema = schemaLines.collect(Collectors.joining("\n"));
            schemaLines.close();

            if (expectedServiceContent.trim().equals(generatedService.trim())
                    && expectedSchemaContent.trim().equals(generatedSchema.trim())) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
            }
        } else {
            Assert.fail("Service generation failed.");
        }
    }

    @Test(description = "Test openapi gen-service for successful service generation with inline request body type")
    public void testInlineRequestBodyServiceGen() throws IOException {
        Path inlineYaml = RES_DIR.resolve(Paths.get("inline-request-body.yaml"));
        createBalProjectModule(petProject, "inlineModule");
        String[] args = {"inlineModule:inlineservice", inlineYaml.toString()};

        OpenApiGenServiceCmd cmd = new OpenApiGenServiceCmd(printStream, petProject.getBalProjectPath().toString());
        new CommandLine(cmd).parseArgs(args);

        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }

        Path expectedServiceFile = RES_DIR.resolve(Paths.get("expected_gen",
                "inline-request-expected.bal"));

        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));

        if (Files.exists(petProject.getResourcePath().resolve(inlineYaml.getFileName()))
                && Files.exists(petProject.getSrcPath().resolve("inlineModule").resolve("inlineservice.bal"))) {

            Stream<String> serviceLines = Files.lines(petProject.getSrcPath()
                    .resolve("inlineModule").resolve("inlineservice.bal"));
            String generatedService = serviceLines.collect(Collectors.joining("\n"));
            serviceLines.close();

            Pattern pattern = Pattern.compile("\\bcontract\\b: \"(.*?)\"");
            Matcher matcher = pattern.matcher(generatedService);
            matcher.find();

            String contractPath = "contract: " + "\"" + matcher.group(1)  + "\"";
            expectedServiceContent = expectedServiceContent.replaceAll("\\bcontract\\b: \"(.*?)\"",
                    Matcher.quoteReplacement(contractPath));
            expectedServiceLines.close();

            if (expectedServiceContent.trim().equals(generatedService.trim())) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + inlineYaml.toString());
            }

        } else {
            Assert.fail("Service generation for inline request body type failed.");
        }
    }

}
