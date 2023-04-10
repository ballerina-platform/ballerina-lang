package io.ballerina.cli.cmd.sub;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "openapi", description = "OpenAPI tool for Ballerina")
public class OpenAPITool implements SubToolCommand {
    @Override
    public void execute(List<String> args) {
        System.out.println("OpenAPI tool is executing");
    }

    @Override
    public String getName() {
        return "openapi";
    }
}
