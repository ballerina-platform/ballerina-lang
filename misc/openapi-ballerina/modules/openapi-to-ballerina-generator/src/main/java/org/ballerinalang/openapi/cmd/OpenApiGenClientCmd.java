package org.ballerinalang.openapi.cmd;

import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.openapi.CodeGenerator;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

@CommandLine.Command(name = "gen-client")
public class OpenApiGenClientCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";

    private static final PrintStream outStream = System.err;

    @CommandLine.Parameters(index = "0", split = ":")
    private List<String> moduleArgs;

    @CommandLine.Parameters(index="1..*")
    private List<String> argList;

    @Override
    public void execute() {
        CodeGenerator generator = new CodeGenerator();



    }

    @Override
    public String getName() {
        return null;
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

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {

    }
}
