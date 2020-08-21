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

import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.OpenApiMesseges;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
                "Service."
//        subcommands = {
//                OpenApiGenContractCmd.class,
//                OpenApiGenClientCmd.class,
//                OpenApiGenServiceCmd.class
//        }
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
        //Check if cli help argument is present
//        if (argList == null) {
//            helpFlag = true;
//        }
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }
        //Check if cli input argument is present
        if (inputPath) {
            //Check if an OpenApi definition is provided
            if (argList == null) {
                throw LauncherUtils.createLauncherException("An OpenApi definition file is required to generate the " +
                        "service. \nE.g: ballerina openapi --input <OpenApiContract> -o /.");
            }
            //Generate Service file
            CodeGenerator generator = new CodeGenerator();

            final File openApiFile = new File(argList.get(0));
            String serviceName = openApiFile.getName();
            Path resourcePath = null;
            try {
                resourcePath = Paths.get(openApiFile.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (this.outputPath != null) {
                if (Paths.get(outputPath).isAbsolute()) {
                    targetOutputPath = Paths.get(outputPath);
                } else {
                    targetOutputPath = Paths.get(targetOutputPath.toString(), outputPath);
                }
            } else {
                targetOutputPath = executionPath;
            }
            try {
                assert resourcePath != null;
                generator.generateService(executionPath.toString(), resourcePath.toString(),
                        targetOutputPath.toString(), serviceName.split("\\.")[0] , targetOutputPath.toString());
            } catch (IOException | BallerinaOpenApiException e) {
                throw LauncherUtils.createLauncherException("Error occurred when generating service for openapi " +
                        "contract at " + argList.get(0) + ". " + e.getMessage() + ".");
            }
            //Generates Client file
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
