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
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

/**
 * Main class to implement "openapi" command for ballerina. Commands for Client Stub, Service file and OpenApi contract
 * generation.
 */
@CommandLine.Command(
        name = "openapi",
        description = "Generates Ballerina service/client for OpenApi contract and OpenApi contract for Ballerina" +
                "Service."
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

    @CommandLine.Option(names = {"--mode"}, description = "Generate only service file or client file according to the" +
            " given mode type")
    private String mode;

    @CommandLine.Option(names = {"-s", "--service"}, description = "Service name that need to documented as openapi " +
            "contract")
    private String service;

    @CommandLine.Option(names = {"-m", "--module"}, description = "Module name which service used to documented")
    private  String module;

    @CommandLine.Option(names = {"--tags"}, description = "Tag that need to write service")
    private String tags;

    @CommandLine.Option(names = {"--operations"}, description = "Operations that need to write service")
    private String operations;

    @CommandLine.Option(names = {"--service-name"}, description = "Service name for generated files")
    private String generatedServiceName;

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
            if (fileName.endsWith(".yaml") || fileName.endsWith(".json")) {
                List<String> tag = new ArrayList<>();
                List<String> operation = new ArrayList<>();
                if (tags != null) {
                     tag.addAll(Arrays.asList(tags.split(",")));
                }
                if (operations != null) {
                    operation.addAll(Arrays.asList(operations.split(",")));
                }
                Filter filter = new Filter(tag, operation);
                openApiToBallerina(fileName, filter);
            } else if (fileName.endsWith(".bal")) {
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
     * This util method to generate openApi contract based on the given service ballerina file.
     * @param fileName  input resource file
     */
    private void ballerinaToOpenApi(String fileName) {
        final File balFile = new File(fileName);
        String serviceName = service;
        getTargetOutputPath();
        Path resourcePath = getResourcePath(balFile, this.targetOutputPath.toString());
        //ballerina openapi -i service.bal --serviceName serviceName --module exampleModul -o ./
        // Check service name it is mandatory
        if (module != null && service != null) {
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
        } else if (serviceName != null) {
            try {
                OpenApiConverterUtils.generateOAS3Definitions(resourcePath, targetOutputPath, serviceName);
            } catch (IOException | OpenApiConverterException e) {
                throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
            }
        } else {
            try {
                OpenApiConverterUtils.generateOAS3DefinitionsAllService(resourcePath, targetOutputPath);
            } catch (IOException | OpenApiConverterException | CompilationFailedException e) {
                throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
            }
        }
    }

    /**
     * A util method for generating service and client stub using given contract file.
     * @param fileName input resource file
     */
    private void openApiToBallerina(String fileName, Filter filter) {
        CodeGenerator generator = new CodeGenerator();
        final File openApiFile = new File(fileName);
        String serviceName;
        if (generatedServiceName != null) {
            serviceName = generatedServiceName;
        } else {
            serviceName = openApiFile.getName().split("\\.")[0];
        }
        getTargetOutputPath();
        Path resourcePath = getResourcePath(openApiFile, this.targetOutputPath.toString());
        if (mode != null) {
            switch (mode) {
                case "service":
                    generateServiceFile(generator, serviceName, resourcePath, filter);
                    break;
                case "client":
                    generatesClientFile(generator, serviceName, resourcePath, filter);
                    break;
                default:
                    break;
            }
        } else {
            generateBothFiles(generator, serviceName, resourcePath, filter);
        }
    }

    /**
     * A util to take the resource Path.
     * @param resourceFile      resource file path
     * @return path of given resource file
     */
    private Path getResourcePath(File resourceFile, String targetOutputPath) {
        Path resourcePath = null;
        Path relativePath = null;
        try {
            resourcePath = Paths.get(resourceFile.getCanonicalPath());
            relativePath = Paths.get(new File(targetOutputPath).toURI().
                    relativize(new File(resourcePath.toString()).toURI()).getPath());
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
        }
        return relativePath;
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
     * @param generator         generator object
     * @param clientName        file name uses to name the generated file
     * @param resourcePath      resource Path
     */
    private void generatesClientFile(CodeGenerator generator, String clientName, Path resourcePath, Filter filter) {

        try {
            generator.generateClient(executionPath.toString(), resourcePath.toString(), clientName,
                    targetOutputPath.toString(), filter);
        } catch (IOException | BallerinaOpenApiException e) {
            if (e.getLocalizedMessage() != null) {
                throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
            } else {
                throw LauncherUtils.createLauncherException(OpenApiMesseges.OPENAPI_CLIENT_EXCEPTION);
            }
        }
    }

    /**
     * A util to generate service file.
     * @param generator     generator object
     * @param serviceName   service name uses for naming the generated file
     * @param resourcePath  resource Path
     */
    private void generateServiceFile(CodeGenerator generator, String serviceName, Path resourcePath, Filter filter) {

        try {
            assert resourcePath != null;
            generator.generateService(executionPath.toString(), resourcePath.toString(),
                    resourcePath.toString(), serviceName, targetOutputPath.toString(), filter);
        } catch (IOException | BallerinaOpenApiException e) {
            throw LauncherUtils.createLauncherException("Error occurred when generating service for openapi " +
                    "contract at " + argList.get(0) + ". " + e.getMessage() + ".");
        }
    }

    /**
     * A util method to generate both service and client stub files based on the given yaml contract file.
     * @param generator         generator object
     * @param fileName          service name  use for naming the files
     * @param resourcePath      resource path
     */
    private void generateBothFiles(CodeGenerator generator, String fileName, Path resourcePath, Filter filter) {

        try {
            assert resourcePath != null;
            Path relativeResourcePath =
                    Paths.get(new File(executionPath.toString()).toURI().relativize(
                            new File(resourcePath.toString()).toURI()).getPath());
            generator.generateBothFiles(
                    GeneratorConstants.GenType.GEN_BOTH, relativeResourcePath.toString(),
                    resourcePath.toString(), fileName, targetOutputPath.toString(), filter);
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
