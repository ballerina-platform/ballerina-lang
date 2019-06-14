package org.ballerinalang.openapi.cmd;

import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

@CommandLine.Command(name = "gen-service")
public class OpenApiGenServiceCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";

    private static final PrintStream outStream = System.err;

    @CommandLine.Parameters(index = "0", split = ":")
    private List<String> moduleArgs;

    @CommandLine.Parameters(index="1..*")
    private List<String> argList;

    @CommandLine.Option(names = {"-c", "--copy-contract"},
            description = "Do you want to copy the contract in to the project?", interactive = true, arity = "1")
    boolean isCopy;

    @CommandLine.Option(names = { "-o", "--output" }, description = "where to write the generated " +
            "files (current dir by default)")
    private String output = "";

    @CommandLine.Option(names = { "-h", "--help" }, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        CodeGenerator generator = new CodeGenerator();

        //Check if cli help argument is present
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        //Check if a module name is present
        if (moduleArgs.size() < 2) {
            throw LauncherUtils.createLauncherException("A module name is required to successfully " +
                    "generate the service from the provided OpenApi contract");
        }

        //Check if relevant arguments are present
        if (argList == null) {
            throw LauncherUtils.createLauncherException("An OpenApi definition file is required to " +
                    "generate the service. \nE.g: ballerina openapi gen-service " + moduleArgs.get(0) + ":"
                    + moduleArgs.get(1) + " <OpenApiContract>");
        }

        //TODO Accept user confirmation to copy the contract in to the ballerina porject.
        if(isCopy) {
            throw LauncherUtils.createLauncherException("User selected : " + isCopy);
        }

        //Set source package for the generated service
        generator.setSrcPackage(moduleArgs.get(0));

        try {
            generator.generate(GeneratorConstants.GenType.valueOf("GEN_SERVICE"), argList.get(0), output);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(
                    "Error occurred when generating service for openapi contract at " + argList.get(0)
                            + ". " + e.getMessage() + ".");
        }

    }

    @Override
    public String getName() {
        return CMD_NAME;
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
