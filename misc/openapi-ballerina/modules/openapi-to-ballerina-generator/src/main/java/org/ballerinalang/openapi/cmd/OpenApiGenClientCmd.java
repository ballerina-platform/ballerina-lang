package org.ballerinalang.openapi.cmd;

import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.OpenApiMesseges;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * This class will implement the "openapi" sub-command "gen-client" for Ballerina OpenApi tool.
 *
 * Ex: ballerina openapi (gen-client) [moduleName]:clientName -o[output directory name]
 */
@CommandLine.Command(name = "gen-client")
public class OpenApiGenClientCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";

    private static final PrintStream outStream = System.err;

    @CommandLine.Parameters(index = "0", split = ":")
    private List<String> moduleArgs;

    @CommandLine.Parameters(index = "1..*")
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

        if (moduleArgs == null) {
            throw LauncherUtils.createLauncherException(OpenApiMesseges.CLIENT_MANDATORY);
        }

        //Check if relevant arguments are present
        if (argList == null) {
            throw LauncherUtils.createLauncherException(OpenApiMesseges.OPENAPI_FILE_MANDATORY);
        }

        if (moduleArgs.size() > 2) {
            generator.setSrcPackage(moduleArgs.get(0));
        }

        try {
            generator.generate(GeneratorConstants.GenType.valueOf("GEN_CLIENT"), argList.get(0), output);
        } catch (IOException | BallerinaOpenApiException e) {
            throw LauncherUtils.createLauncherException(OpenApiMesseges.OPENAPI_CLIENT_EXCEPTION);
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
