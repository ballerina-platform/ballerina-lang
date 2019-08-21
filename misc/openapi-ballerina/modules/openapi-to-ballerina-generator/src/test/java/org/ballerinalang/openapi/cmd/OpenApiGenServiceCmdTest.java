package org.ballerinalang.openapi.cmd;

import org.ballerinalang.tool.BLauncherException;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;

/**
 * Open Api gen service test.
 */
public class OpenApiGenServiceCmdTest extends OpenAPICommandTest {

    @Test(description = "Test openapi gen service command with help flag")
    public void testOpenAPIGenServiceCmdWithHelp() throws IOException {
        String[] args = {"-h"};
        OpenApiGenServiceCmd openApiGenServiceCommand = new OpenApiGenServiceCmd(printStream, this.tmpDir.toString());
        new CommandLine(openApiGenServiceCommand).parseArgs(args);
        openApiGenServiceCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n" +
                "    Ballerina OpenApi - Gen Service is a tool which will convert an" +
                " OpenApi contract to a Ballerina mock service.\n" +
                "\n    Note: This is an Experimental tool ship under " +
                "ballerina hence this will only support limited set of functionality."));
    }

    @Test(description = "Test openapi gen service command without help flag")
    public void testOpenAPIGenServiceCmdWithoutHelp() throws IOException {
        OpenApiGenServiceCmd openApiGenServiceCommand = new OpenApiGenServiceCmd(printStream, this.tmpDir.toString());
        new CommandLine(openApiGenServiceCommand);

        String exception = "";

        try {
            openApiGenServiceCommand.execute();
        } catch (BLauncherException e) {
            exception = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(exception.contains("error: " + "A module name and a service name is required to " +
                "generate the service from the provided OpenAPI file. " +
                "\n E.g. ballerina openapi gen-service " +
                "<module_name>:<service_name> <OpenAPI_file>"));
    }

    @Test(description = "Test openapi gen service command without providing the openapi definition")
    public void testOpenApiGenServiceWithoutOpenApiDefinition() throws IOException {
        OpenApiGenServiceCmd openApiGenServiceCommand = new OpenApiGenServiceCmd(printStream, this.tmpDir.toString());
        new CommandLine(openApiGenServiceCommand).parseArgs("testModule:testServiceName");

        String exception = "";

        try {
            openApiGenServiceCommand.execute();
        } catch (BLauncherException e) {
            exception = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(exception.contains("An OpenApi definition file is required to generate the " +
                "service. \nE.g: ballerina openapi gen-service testModule:testServiceName <OpenApiContract>"));
    }

    @Test(description = "Test openapi gen service command with invalid ballerina project")
    public void testBallerinaProjectExist() throws IOException {
        String[] args = {"testModule:testServiceName", "../petstore.yml"};
        OpenApiGenServiceCmd openApiGenServiceCommand = new OpenApiGenServiceCmd(printStream, this.tmpDir.toString());
        new CommandLine(openApiGenServiceCommand).parseArgs(args);

        String exception = "";

        try {
            openApiGenServiceCommand.execute();
        } catch (BLauncherException e) {
            exception = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(exception.contains("Ballerina service generation should be done from the " +
                "project root. \nIf you like to start with a new project " +
                "use `ballerina new` command to create a new project."));
    }


}
