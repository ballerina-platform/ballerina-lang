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
                "    Ballerina openapi - Generates Ballerina code for a provided\n" +
                "    OpenApi definition or exports the OpenApi definition of a\n" +
                "    Ballerina service."));
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

        Assert.assertTrue(exception.contains("error: A module name is required to successfully generate the service"));
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

        Assert.assertTrue(exception.contains("An OpenApi definition file is required to generate the service."));
    }

    @Test(description = "Test openapi gen service command with invalid open api definition")
    public void testOpenApiContractExist() throws IOException {
        String[] args = {"testModule:testServiceName", "../petstore.yml"};
        OpenApiGenServiceCmd openApiGenServiceCommand = new OpenApiGenServiceCmd(printStream, this.tmpDir.toString());
        new CommandLine(openApiGenServiceCommand).parseArgs(args);

        String exception = "";

        try {
            openApiGenServiceCommand.execute();
        } catch (BLauncherException e) {
            exception = e.getDetailedMessages().get(0);
        }

        Assert.assertTrue(exception.contains("An OpenApi definition file is required to generate the service."));
    }


}
