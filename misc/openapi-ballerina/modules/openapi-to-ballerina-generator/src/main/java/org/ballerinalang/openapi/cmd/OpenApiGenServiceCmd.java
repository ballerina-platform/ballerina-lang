package org.ballerinalang.openapi.cmd;

import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class to implement "openapi gen-service" command for ballerina.
 * Ex: ballerina openapi gen-service module:serviceName contract
 */

/**
 * This class will implement the "openapi" sub-command "gen-service" for Ballerina OpenApi tool.
 *
 * Ex: ballerina openapi gen-service moduleName:serivceName [-c: copy-contract] [-o: outputFile]
 */
@CommandLine.Command(name = "gen-service")
public class OpenApiGenServiceCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";

    private static final PrintStream outStream = System.err;

    @CommandLine.Parameters(index = "0", split = ":")
    private List<String> moduleArgs;

    @CommandLine.Parameters(index = "1..*")
    private List<String> argList;

    @CommandLine.Option(names = {"-c", "--skip-bind"},
            description = "Do you want to copy the contract in to the project?")
    boolean skipBind = false;

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
                    "generate the service from the provided OpenApi contract. " +
                    "\nE.g ballerina openapi gen-service <modulename>:<servicename>");
        }

        //Check if relevant arguments are present
        if (argList == null) {
            throw LauncherUtils.createLauncherException("An OpenApi definition file is required to " +
                    "generate the service. \nE.g: ballerina openapi gen-service " + moduleArgs.get(0) + ":"
                    + moduleArgs.get(1) + " <OpenApiContract>");
        }

        final Path projectRoot = ProjectDirs.findProjectRoot(Paths.get(System.getProperty("user.dir")));
        if (projectRoot == null) {
            // TODO: Better throw a meaningful exception
            return;
        }
        final Path sourceDirectory = projectRoot.resolve("src");
        final Path moduleDirectory = sourceDirectory.resolve(moduleArgs.get(0));
        final Path resourcesDirectory = Paths.get(moduleDirectory + "/resources");
        final File openApiFile = new File(argList.get(0));
        final String openApiFilePath = openApiFile.getPath();
        Path resourcePath = Paths.get(resourcesDirectory + "/" + openApiFile.getName());

        //TODO Accept user confirmation to copy the contract in to the ballerina porject.
        if (!skipBind) {
            //Check if OpenApi contract file exists
            if (Files.notExists(Paths.get(openApiFilePath))) {
                throw LauncherUtils.createLauncherException("Could not resolve a valid OpenApi" +
                        " contract in " + openApiFilePath);
            }

            if (Files.notExists(sourceDirectory)) {
                try {
                    Files.createDirectory(sourceDirectory);
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
                }
            }

            if (Files.notExists(moduleDirectory)) {
                try {
                    Files.createDirectory(moduleDirectory);
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
                }
            }

            //Check for resources folder in ballerina project root
            if (Files.notExists(resourcesDirectory)) {
                try {
                    Files.createDirectory(resourcesDirectory);
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
                }
            }

            //If file
            if (Files.notExists(resourcePath)) {
                try {
                    Files.copy(Paths.get(openApiFilePath), resourcePath);
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
                }
            } else {
                throw LauncherUtils.createLauncherException("There is already an OpenApi contract in the location "
                        + resourcesDirectory);
            }

        }

        //Set source package for the generated service
        generator.setSrcPackage(moduleArgs.get(0));

        try {
            generator.generate(GeneratorConstants.GenType.valueOf("GEN_SERVICE"),
                    resourcePath.toString(), moduleArgs.get(1), output);
        } catch (IOException | BallerinaOpenApiException e) {
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
