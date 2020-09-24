/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.openapi.cmd;

import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.OpenApiMesseges;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
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
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.openapi.OpenApiMesseges.DEFINITION_EXISTS;
import static org.ballerinalang.openapi.OpenApiMesseges.GEN_SERVICE_MODULE_ARGS_REQUIRED;
import static org.ballerinalang.openapi.OpenApiMesseges.GEN_SERVICE_MODULE_REQUIRED;
import static org.ballerinalang.openapi.OpenApiMesseges.GEN_SERVICE_PROJECT_ROOT;
import static org.ballerinalang.openapi.OpenApiMesseges.GEN_SERVICE_SERVICE_NAME_REQUIRED;
import static org.ballerinalang.openapi.OpenApiMesseges.MODULE_DIRECTORY_EXCEPTION;
import static org.ballerinalang.openapi.OpenApiMesseges.MODULE_MD_EXCEPTION;
import static org.ballerinalang.openapi.OpenApiMesseges.RESOURCE_DIRECTORY_EXCEPTION;
import static org.ballerinalang.openapi.OpenApiMesseges.TESTS_DIRECTORY_EXCEPTION;

/**
 * Class to implement "openapi gen-service" command for ballerina.
 * Ex: ballerina openapi gen-service module:serviceName contract
 */

/**
 * This class will implement the "openapi" sub-command "gen-service" for Ballerina OpenApi tool.
 * <p>
 * Ex: ballerina openapi gen-service moduleName:serivceName [-c: copy-contract] [-o: outputFile]
 */
@CommandLine.Command(name = "gen-service")
public class OpenApiGenServiceCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi-gen-service";

    private PrintStream outStream;
    private String executionPath;

    @CommandLine.Parameters(index = "0", split = ":")
    private List<String> moduleArgs;

    @CommandLine.Parameters(index = "1..*")
    private List<String> argList;

    @CommandLine.Option(names = {"-s", "--skip-bind"}, description = "Skip copying of the used contract to project")
    boolean skipBind = false;

    @CommandLine.Option(names = {"-o", "--output"}, description = "where to write the generated " +
            "files (current dir by default)")
    private String output = "";

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--tags"}, description = "Tag that need to write service")
    private List<String> tags;

    @CommandLine.Option(names = {"--operations"}, description = "Operations that need to write service")
    private List<String> operations;


    public OpenApiGenServiceCmd() {
        this.outStream = System.err;
        this.executionPath = System.getProperty("user.dir");
    }

    public OpenApiGenServiceCmd(PrintStream outStream, String executionPath) {
        this.outStream = outStream;
        this.executionPath = executionPath;
    }

    @Override
    public void execute() {
        //User notification of using an experimental tool
        outStream.println(OpenApiMesseges.EXPERIMENTAL_FEATURE);

        CodeGenerator generator = new CodeGenerator();

        List<String> tag = new ArrayList<>();
        List<String> operation = new ArrayList<>();
        Filter filter = new Filter(tag, operation);
        tag.add("pet");
        operation.add("operation");
        //Check if cli help argument is present
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        //Check if a module name is present
        if (moduleArgs == null || moduleArgs.size() < 2) {
            throw LauncherUtils.createLauncherException(GEN_SERVICE_MODULE_ARGS_REQUIRED);
        } else {
            if (moduleArgs.get(0).trim().isEmpty()) {
                throw LauncherUtils.createLauncherException(GEN_SERVICE_MODULE_REQUIRED);
            } else if (moduleArgs.get(1).trim().isEmpty()) {
                throw LauncherUtils.createLauncherException(GEN_SERVICE_SERVICE_NAME_REQUIRED);
            }
        }

        //Check if an OpenApi definition is provided
        if (argList == null) {
            throw LauncherUtils.createLauncherException("An OpenApi definition file is required to generate the " +
                    "service. \nE.g: ballerina openapi gen-service " + moduleArgs.get(0) + ":"
                    + moduleArgs.get(1) + " <OpenApiContract>");
        }

        final Path projectRoot = ProjectDirs.findProjectRoot(Paths.get(executionPath));
        if (projectRoot == null) {
            throw LauncherUtils.createLauncherException(GEN_SERVICE_PROJECT_ROOT);
        }
        final Path sourceDirectory = projectRoot.resolve("src");
        final Path moduleDirectory = sourceDirectory.resolve(moduleArgs.get(0));
        final Path resourcesDirectory = Paths.get(moduleDirectory + "/resources");
        final Path testsDirectory = Paths.get(moduleDirectory + "/tests");
        final File openApiFile = new File(argList.get(0));
        final String openApiFilePath = openApiFile.getPath();
        Path resourcePath = Paths.get(resourcesDirectory + "/" + openApiFile.getName());
        Path relativeResourcePath = Paths.get("resources", openApiFile.getName());

        //Check provided OpenApi file is a valid and existing one
        if (Files.notExists(Paths.get(openApiFilePath))) {
            throw LauncherUtils.createLauncherException("Could not resolve a valid OpenApi" +
                    " contract in " + openApiFilePath);
        }

        if (Files.notExists(moduleDirectory)) {
            try {
                Files.createDirectory(moduleDirectory);
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException(MODULE_DIRECTORY_EXCEPTION + "\n"
                        + e.getLocalizedMessage());
            }
        }

        File moduleMd = new File(moduleDirectory + "/Module.md");
        if (!moduleMd.exists()) {
            try {
                boolean createMd = true;
                createMd = moduleMd.createNewFile();

                if (!createMd) {
                    throw LauncherUtils.createLauncherException(MODULE_MD_EXCEPTION);
                }
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException(MODULE_MD_EXCEPTION + "\n"
                        + e.getLocalizedMessage());
            }
        }

        // Check for tests folder in ballerina module root
        if (Files.notExists(testsDirectory)) {
            try {
                Files.createDirectory(testsDirectory);
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException(TESTS_DIRECTORY_EXCEPTION + "\n"
                        + e.getLocalizedMessage());
            }
        }

        if (skipBind) {
            try {
                resourcePath = Paths.get(openApiFile.getCanonicalPath());
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
            }
        } else {
            // Check for resources folder in ballerina module root
            if (Files.notExists(resourcesDirectory)) {
                try {
                    Files.createDirectory(resourcesDirectory);
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException(RESOURCE_DIRECTORY_EXCEPTION + "\n"
                            + e.getLocalizedMessage());
                }
            }

            // If OpenAPI contract by the same name doesn't exist in the resource folder continue to copy.
            // Else throw an exception.
            if (Files.notExists(resourcePath)) {
                try {
                    Files.copy(Paths.get(openApiFilePath), resourcePath);
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
                }
            } else {
                throw LauncherUtils.createLauncherException(DEFINITION_EXISTS
                        + " " + resourcesDirectory);
            }
        }

        if (output.isEmpty()) {
            output = executionPath;
        }

        //Set source package for the generated service
        generator.setSrcPackage(moduleArgs.get(0));

        //TODO Fix relative path compiler plugin issue
        //Set relative path for contract path which will be printed on the generated service bal file
        //Path absPath = Paths.get(resourcePath.toString());
        //Path basePath = Paths.get(output);
        //Path pathRelative = basePath.relativize(absPath);

        try {
            generator.generateService(executionPath, resourcePath.toString(), relativeResourcePath.toString(),
                    moduleArgs.get(1), output, filter);
        } catch (IOException | BallerinaOpenApiException e) {
            throw LauncherUtils.createLauncherException("Error occurred when generating service for openapi " +
                    "contract at " + argList.get(0) + ". " + e.getMessage() + ".");
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
