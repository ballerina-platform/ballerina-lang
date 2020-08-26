/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.openapi.cmd;

import org.ballerinalang.ballerina.openapi.convertor.OpenApiConverterException;
import org.ballerinalang.ballerina.openapi.convertor.service.OpenApiConverterUtils;
import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.OpenApiMesseges;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

/**
 * Main class to implement "openapi" command for ballerina.
 * This class will accept sub-commands and execute the relevant sub-command class as given to the sub-commands
 * parameter.
 *
 * Command usage will change according to the sub-command.
 */
@CommandLine.Command(
        name = "openapi",
        description = "Generates Ballerina service/client for OpenApi contract and OpenApi contract for Ballerina" +
                "Service.",
        subcommands = {
//                OpenApiGenContractCmd.class,
//                OpenApiGenClientCmd.class,
//                OpenApiGenServiceCmd.class
        }
)
public class OpenApiCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";
    private PrintStream outStream;
    private Path executionPath = Paths.get(System.getProperty(USER_DIR));
    private Path targetOutputPath;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Generating the client and service both files")
    private boolean inputPath;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Location of the generated Ballerina service, " +
            "client and model files.")
    private String outputPath;

    @CommandLine.Option(names = {"--mode"}, description = "Generate only service file or client file")
    private String mode;

    @CommandLine.Option(names = {"-s", "--service"}, description = "Service name that need to documented as openapi " +
            "contract")
    private String service;

    @CommandLine.Option(names = {"-m", "--module"}, description = "Module name which service used to documented")
    private  String module;

    @CommandLine.Parameters
    private List<String> argList;

    public OpenApiCmd() {
        this.outStream = System.err;
        this.executionPath = Paths.get(System.getProperty("user.dir"));
    }

    public OpenApiCmd(PrintStream outStream) {
        this.outStream = outStream;
        this.executionPath = Paths.get(System.getProperty("user.dir"));
    }

    @Override
    public void execute() {
        //User notification of using an experimental tool
        outStream.println(OpenApiMesseges.EXPERIMENTAL_FEATURE);
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }
        //Check if cli input argument is present
        if (inputPath) {
            //Check if an OpenApi definition is provided
            if (argList == null) {
                throw LauncherUtils.createLauncherException(OpenApiMesseges.MESSAGE_FOR_MISSING_INPUT);
            }
            // If given input is yaml contract, it generates service file and client stub
            // else if given ballerina service file it generates openapi contract file
            // else it generates error message to enter correct input file
            String fileName = argList.get(0);
            if(fileName.endsWith(".yaml")) {
                openApiToBallerina(fileName);
            } else if ((fileName.endsWith(".bal")) && (service != null)) {
                ballerinaToOpenApi(fileName);
            } else {
                throw LauncherUtils.createLauncherException(OpenApiMesseges.MESSAGE_FOR_MISSING_INPUT);
            }
        } else {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }
    }

    /**
     *  This util method to generate openApi contract based on the given service ballerina file.
     * @param fileName  input resource file
     */
    private void ballerinaToOpenApi(String fileName) {
        final File balFile = new File(fileName);
        String serviceName = service;
        Path resourcePath = getResourcePath(balFile);
        getTargetOutputPath();
        //ballerina openapi -i service.bal --serviceName serviceName --module exampleModul -o ./
        // Check service name it is mandatory
        if (module != null) {
            if (!checkModuleExist(module)) {
                throw LauncherUtils.createLauncherException(OpenApiMesseges.MESSAGE_FOR_INVALID_MODULE);
            }
            try {
                OpenApiConverterUtils.generateOAS3DefinitionFromModule(module, serviceName,
                        targetOutputPath);
            } catch (Exception e) {
                throw LauncherUtils.createLauncherException("Error occurred when exporting openapi file. " +
                        "\n" + e.getMessage());
            }
        } else {
            try {
                OpenApiConverterUtils.generateOAS3Definitions(resourcePath, targetOutputPath, serviceName);
            } catch (IOException | OpenApiConverterException e){
                throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
            }
        }
    }

    /**
     * A util method for generating service and client stub using given contract file.
     * @param fileName input resource file
     */
    private void openApiToBallerina(String fileName) {
        CodeGenerator generator = new CodeGenerator();
        final File openApiFile = new File(fileName);
        String serviceName = openApiFile.getName();
        Path resourcePath = getResourcePath(openApiFile);
        getTargetOutputPath();
        switch (mode) {
            case "service":
                generateServiceFile(generator, serviceName, resourcePath);
                break;
            case "client":
                generatesClientFile(generator, serviceName, resourcePath);
                break;
            default:
                generateBothFiles(generator, serviceName, resourcePath);
                break;
        }
    }

    /**
     * A util to take the resource Path.
     * @param resourceFile
     * @return path of given resource file
     */
    private Path getResourcePath(File resourceFile) {
        Path resourcePath = null;
        try {
            resourcePath = Paths.get(resourceFile.getCanonicalPath());
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
        }
        return resourcePath;
    }

    /**
     * A util to get the output Path.
     */
    private void getTargetOutputPath() {
        targetOutputPath = executionPath;
        if (this.outputPath != null) {
            if (Paths.get(outputPath).isAbsolute()) {
                targetOutputPath = Paths.get(outputPath);
            } else {
                targetOutputPath = Paths.get(targetOutputPath.toString(), outputPath);
            }
        }
    }

    /**
     * A Util to Client generation.
     * @param generator     generator object
     * @param serviceName   serviceName uses to name the generated file
     * @param resourcePath  resource Path
     */
    private void generatesClientFile(CodeGenerator generator, String serviceName, Path resourcePath) {
        String clientName = serviceName.split("\\.")[0] + "Client";
        try {
            generator.generateClient(executionPath.toString(), resourcePath.toString(), clientName,
                    targetOutputPath.toString());
        } catch (IOException | BallerinaOpenApiException e) {
            if (e.getLocalizedMessage() != null) {
                throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
            } else {
                throw LauncherUtils.createLauncherException(OpenApiMesseges.OPENAPI_CLIENT_EXCEPTION);
            }
        }
    }

    /**
     * A utile to generate service file.
     * @param generator     generator object
     * @param serviceName   service name uses for naming the generated file
     * @param resourcePath  resource Path
     */
    private void generateServiceFile(CodeGenerator generator, String serviceName, Path resourcePath) {
        try {
            assert resourcePath != null;
            Path relativeResourcePath = Paths.get(executionPath.toString(), serviceName);
            generator.generateService(executionPath.toString(), resourcePath.toString(),
                    relativeResourcePath.toString(), serviceName.split("\\.")[0] , targetOutputPath.toString());
        } catch (IOException | BallerinaOpenApiException e) {
            throw LauncherUtils.createLauncherException("Error occurred when generating service for openapi " +
                    "contract at " + argList.get(0) + ". " + e.getMessage() + ".");
        }
    }

    /**
     * A util method to generate both service and client stub files based on the given yaml contract file.
     * @param generator         generator object
     * @param serviceName       service name  use for naming the files
     * @param resourcePath      resource path
     */
    private void generateBothFiles(CodeGenerator generator, String serviceName, Path resourcePath) {
        try {
            assert resourcePath != null;
            Path relativeResourcePath = Paths.get(executionPath.toString(), serviceName);
            generator.generateBothFiles(
                    GeneratorConstants.GenType.GEN_BOTH, executionPath.toString(), resourcePath.toString(),
                    relativeResourcePath.toString(), serviceName.split("\\.")[0] , targetOutputPath.toString());
        } catch (IOException | BallerinaOpenApiException e) {
            throw LauncherUtils.createLauncherException("Error occurred when generating service for openapi " +
                    "contract at " + argList.get(0) + ". " + e.getMessage() + ".");
        }
    }

    /**
     * A util method to check a given module name actually exists in the current command location.
     * @param moduleName - module name to be checked
     * @return true if module exists.
     */
    private boolean checkModuleExist(String moduleName) {
        Path userLocation = Paths.get(System.getProperty("user.dir"));
        Path moduleLocation = userLocation.resolve("src").resolve(moduleName);
        return Files.exists(moduleLocation);
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
