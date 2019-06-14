package org.ballerinalang.openapi.cmd;

import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.utils.GeneratorConstants;
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

    @CommandLine.Option(names = { "-o", "--output" }, description = "where to write the generated " +
            "files (current dir by default)")
    private String output = "";

    @CommandLine.Option(names = { "-h", "--help" }, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        CodeGenerator generator = new CodeGenerator();

        //Help flag check
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        try {
            generator.generate(GeneratorConstants.GenType.valueOf("GEN_CLIENT"), argList.get(0), output);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(
                    "Error occurred when generating service for openapi contract at " + argList.get(0)
                            + ". " + e.getMessage() + ".");
        }


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

}
