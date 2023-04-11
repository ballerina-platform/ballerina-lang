package io.ballerina.cli.cmd.sub;

import picocli.CommandLine;

/**
 * This class is here to demonstrate how to add a sub tool to the Ballerina CLI.
 * Remove during the actual implementation
 *
 * @since 2201.6.0
 */
@CommandLine.Command(name = "openapi", description = "OpenAPI tool for Ballerina")
public class ConcreteSubTool implements SubToolCommand {

    @Override
    public void execute() {

    }

    @Override
    public String getName() {
        return "concretsubtool";
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }
}
