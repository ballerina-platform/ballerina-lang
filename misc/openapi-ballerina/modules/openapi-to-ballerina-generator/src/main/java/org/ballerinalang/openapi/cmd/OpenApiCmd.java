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

import org.ballerinalang.ballerina.openapi.convertor.service.OpenApiConverterUtils;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.openapi.CodeGenerator;
import org.ballerinalang.openapi.utils.GeneratorConstants.GenType;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Class to implement "openapi" command for ballerina.
 * Ex: ballerina swagger (gen-service | gen-client) (openApiFile | balFile) -m(module name) -o(output directory name)
 */
@CommandLine.Command(name = "openapi",
            description = "generate client/service using OpenApi definition or exports openapi file for a Ballerina"
                    + " Service")
public class OpenApiCmd implements BLauncherCmd {
    private static final String genclient = "GEN-CLIENT";
    private static final String genservice = "GEN-SERVICE";
    private static final String export = "EXPORT";
    private static final String CMD_NAME = "openapi";

    private static final PrintStream outStream = System.err;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = { "-o", "--output" },
               description = "where to write the generated files (current dir by default)")
    private String output = "";

    @CommandLine.Option(names = { "-m", "--module" },
            description = "module name to be used in the generated source files")
    private String srcPackage;

    @CommandLine.Option(names = { "-s", "--service" }, description = "Name of the service to export. "
            + "This will extract a service with the specified name in the .bal file")
    private String serviceName;

    @CommandLine.Option(names = { "-h", "--help" }, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CMD_NAME);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() < 2) {
            throw LauncherUtils.createUsageExceptionWithHelp("action and a input file should be provided. "
                    + "Ex: ballerina openapi gen-client openapi_file");
        }
        String action = argList.get(0).toUpperCase(Locale.ENGLISH);
        StringBuilder msg = new StringBuilder("successfully ");

        switch (action) {
            case genservice:
                generateFromOpenApi("GEN_SERVICE");
                msg.append("generated ballerina genservice service");
                break;
            case genclient:
                generateFromOpenApi("GEN_CLIENT");
                msg.append("generated ballerina client");
                break;
            case export:
                exportFromBal();
                msg.append("generated openapi definition");
                break;
            default:
                throw LauncherUtils.createLauncherException(
                        "Only following actions(genservice, gen-client) are " + "supported in openapi command");
        }
        msg.append(" for input file - " + argList.get(1));
        outStream.println(msg.toString());
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

    private void generateFromOpenApi(String targetLanguage) {
        CodeGenerator generator = new CodeGenerator();
        generator.setSrcPackage(srcPackage);

        try {
            generator.generate(GenType.valueOf(targetLanguage), argList.get(1), output);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(
                    "Error occurred when generating " + targetLanguage + " for " + "openapi file at " + argList.get(1)
                            + ". " + e.getMessage() + ".");
        }
    }

    private void exportFromBal() {
        Path outPath = Paths.get(output);
        Path servicePath = Paths.get(argList.get(1));

        try {
            OpenApiConverterUtils.generateOAS3Definitions(servicePath, outPath, serviceName);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(
                    "Error occurred when exporting openapi file for service file at " + argList.get(1)
                            + ". " + e.getMessage() + ".");
        }
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
