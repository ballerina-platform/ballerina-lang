package org.ballerinalang.openapi.cmd;

import org.ballerinalang.ballerina.openapi.convertor.service.OpenApiConverterUtils;
import org.ballerinalang.openapi.OpenApiMesseges;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * This class will implement the "openapi" sub-command "gen-contract" for Ballerina OpenApi tool.
 *
 * Ex: ballerina openapi gen-contract [moduleName]:serviceName [-i: ballerinaFile] [-o: contractFile] [-s: skip-bind]
 */
@CommandLine.Command(name = "gen-contract")
public class OpenApiGenContractCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";

    private static final PrintStream outStream = System.err;

    @CommandLine.Parameters(index = "0", split = ":")
    private List<String> moduleArgs;

    @CommandLine.Parameters(index = "1..*")
    private List<String> argList;

    @CommandLine.Option(names = { "-i", "--ballerina-file" }, description = "The ballerina file " +
            "which consists the service which needs to be exported.")
    private String balFile;

    @CommandLine.Option(names = { "-o", "--contract-location" }, description = "The location which the exported " +
            "OpenApi contract should be saved.")
    private String exportLocation = "";

    @CommandLine.Option(names = { "-s", "--skip-bind" }, description = "This will skip binding the generated " +
            "contract to the relevant ballerina file if specified.")
    private boolean skipBind;

    @CommandLine.Option(names = { "-h", "--help" }, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {

        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        if (moduleArgs == null) {
            throw LauncherUtils.createLauncherException(OpenApiMesseges.CONTRACT_SERVICE_MANDATORY);
        } else if (moduleArgs.size() == 1 && balFile == null) {
            throw LauncherUtils.createLauncherException(OpenApiMesseges.CONTRACT_BALLERINA_DOC_MANDATORY);
        }

        //When module and service name is available
        if (moduleArgs.size() == 2) {
            String module = moduleArgs.get(0);
            String service = moduleArgs.get(1);

            genOASfromModule(module, service);
        }

        //When service name and a ballerina document is available
        if (moduleArgs.size() == 1 && balFile != null) {
            String service = moduleArgs.get(0);
            Path servicePath = Paths.get(balFile);

            genOASfromFile(service, servicePath);
        }

    }

    /**
     * This method will generate the OAS Contract if a module and a service name is provided.
     * @param module - Module Name
     * @param serviceName - Service Name
     */
    private void genOASfromModule(String module, String serviceName) {
        Path outPutPath = Paths.get(exportLocation);

        if (!checkModuleExist(module)) {
            throw LauncherUtils.createLauncherException("The module provided is not found in the current location.");
        }

        try {
            OpenApiConverterUtils.generateOAS3DefinitionFromModule(module, serviceName, outPutPath);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException("Error occurred when exporting openapi file. " +
                    "\n" + e.getMessage());
        }
    }

    /**
     * This method will generate the OAS Contract if a file and a service name is provided.
     *
     * @param serviceName - Service Name
     */
    private void genOASfromFile(String serviceName, Path servicePath) {
        Path outPath = Paths.get(exportLocation);

        try {
            OpenApiConverterUtils.generateOAS3Definitions(servicePath, outPath, serviceName);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(
                    "Error occurred when exporting openapi file for service file at " + servicePath.toString()
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


    /**
     * A util method to check a given module name actually exists in the current command location.
     * @param moduleName - module name to be checked
     * @return true if module exists.
     */
    private boolean checkModuleExist(String moduleName) {
        Path userLocation = Paths.get(System.getProperty("user.dir"));
        Path moduleLocation = userLocation.resolve(moduleName);

        return Files.exists(moduleLocation);
    }

}
