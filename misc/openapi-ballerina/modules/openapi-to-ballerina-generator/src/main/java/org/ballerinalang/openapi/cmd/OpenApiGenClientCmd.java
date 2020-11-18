/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.openapi.cmd;

import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.OpenApiMesseges;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will implement the "openapi" sub-command "gen-client" for Ballerina OpenApi tool.
 *
 * Ex: ballerina openapi (gen-client) [moduleName]:clientName -o[output directory name]
 */
@CommandLine.Command(name = "gen-client")
public class OpenApiGenClientCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi-gen-client";

    private PrintStream outStream = System.err;
    private String executionPath = System.getProperty("user.dir");

    @CommandLine.Parameters(index = "0", split = ":")
    private List<String> moduleArgs;

    @CommandLine.Parameters(index = "1..*")
    private List<String> argList;

    @CommandLine.Option(names = {"-o", "--output"}, description = "where to write the generated " +
            "files (current dir by default)")
    private String output = "";

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--tags"}, description = "Tag that need to write service")
    private List<String> tags;

    @CommandLine.Option(names = {"--operations"}, description = "Operations that need to write service")
    private List<String> operations;

    @CommandLine.Option(names = {"--service-name"}, description = "Service name for generated files")
    private String generatedServiceName;

    public OpenApiGenClientCmd() {
        this.outStream = System.err;
    }

    public OpenApiGenClientCmd(PrintStream outStream, String executionPath) {
        this.outStream = outStream;
        this.executionPath = executionPath;
    }

    @Override
    public void execute() {
        List<String> tag = new ArrayList<>();
        List<String> operation = new ArrayList<>();
        tag.add("pet");
        operation.add("operation");
        Filter filter = new Filter(tag, operation);

        //User notification of using an experimental tool
        outStream.println(OpenApiMesseges.EXPERIMENTAL_FEATURE);

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

        if (moduleArgs.size() >= 2) {
            generator.setSrcPackage(moduleArgs.get(0));
        }

        try {
            generator.generateClient(executionPath, argList.get(0), moduleArgs.get(1), output, filter);
        } catch (IOException | BallerinaOpenApiException e) {
            if (e.getLocalizedMessage() != null) {
                throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
            } else {
                throw LauncherUtils.createLauncherException(OpenApiMesseges.OPENAPI_CLIENT_EXCEPTION);
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
    public void printUsage(StringBuilder out) {

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }

}
