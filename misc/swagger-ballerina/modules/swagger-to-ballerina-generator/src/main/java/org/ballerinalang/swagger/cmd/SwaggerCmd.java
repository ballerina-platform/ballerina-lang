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
package org.ballerinalang.swagger.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ballerinalang.ballerina.swagger.convertor.service.SwaggerConverterUtils;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.swagger.CodeGenerator;
import org.ballerinalang.swagger.utils.GeneratorConstants.GenType;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Class to implement "swagger" command for ballerina.
 * Ex: ballerina swagger (mock | client) (swaggerFile | balFile) -p(package name) -o(output directory name)
 */
@Parameters(commandNames = "swagger",
            commandDescription = "Generate client/service using swagger definition or "
                    + "export swagger file for a ballerina service")
public class SwaggerCmd implements BLauncherCmd {
    private static final String client = "CLIENT";
    private static final String mock = "MOCK";
    private static final String export = "EXPORT";

    private static final PrintStream outStream = System.err;
    private JCommander parentCmdParser;

    @Parameter(arity = 1, description = "<action> <swagger spec| ballerina file>. action : mock|client|export")
    private List<String> argList;

    @Parameter(names = { "-o", "--output" },
               description = "where to write the generated files (current dir by default)")
    private String output = "";

    @Parameter(names = { "-p", "--package" }, description = "package name to be used in the generated source files")
    private String srcPackage;

    @Parameter(names = { "-s", "--service" }, description = "Name of the service to export. "
            + "This will extract a service with the specified name in the .bal file")
    private String serviceName;

    @Parameter(names = { "-h", "--help" }, hidden = true)
    private boolean helpFlag;

    @Parameter(names = "--debug", hidden = true)
    private String debugPort;

    @Parameter(names = "--java.debug", hidden = true)
    private String javaDebugPort;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "swagger");
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() < 2) {
            throw LauncherUtils.createUsageException("action and a input file should be provided. "
                    + "Ex: ballerina swagger client swagger_file");
        }
        String action = argList.get(0).toUpperCase(Locale.ENGLISH);
        StringBuilder msg = new StringBuilder("successfully ");

        switch (action) {
            case mock:
                generateFromSwagger(mock);
                msg.append("generated ballerina mock service");
                break;
            case client:
                generateFromSwagger(client);
                msg.append("generated ballerina client");
                break;
            case export:
                exportFromBal();
                break;
            default:
                throw LauncherUtils.createUsageException(
                        "Only following actions(mock, client) are " + "supported in swagger command");
        }
        msg.append(" for input file - " + argList.get(1));
        outStream.println(msg.toString());
    }

    @Override
    public String getName() {
        return "swagger";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generates ballerina mock service, client for a given swagger definition ");
        out.append(System.lineSeparator());
        out.append("or exports swagger definition of a ballerina service");
        out.append(System.lineSeparator());
        out.append(System.lineSeparator());
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("  ballerina swagger <mock | client> <swagger file> -p<package name> "
                + "-o[<output directory name>]\n");
        stringBuilder.append("  ballerina swagger export <ballerina service file> -o[<output directory name>] "
                + "-s<service name>\n");
        stringBuilder.append("\tmock      : generates a ballerina mock service\n");
        stringBuilder.append("\tclient    : generates a ballerina client\n");
        stringBuilder.append("\texport    : exports swagger definition of a ballerina service\n");
    }

    private void generateFromSwagger(String targetLanguage) {
        CodeGenerator generator = new CodeGenerator();
        generator.setSrcPackage(srcPackage);

        try {
            generator.generate(GenType.valueOf(targetLanguage), argList.get(1), output);
        } catch (Exception e) {
            String causeMessage = "";
            Throwable rootCause = ExceptionUtils.getRootCause(e);

            if (rootCause != null) {
                causeMessage = rootCause.getMessage();
            }
            throw LauncherUtils.createUsageException(
                    "Error occurred when generating " + targetLanguage + " for " + "swagger file at " + argList.get(1)
                            + ". " + e.getMessage() + ". " + causeMessage);
        }
    }

    private void exportFromBal() {
        Path outPath = Paths.get(output);
        Path servicePath = Paths.get(argList.get(1));

        try {
            SwaggerConverterUtils.generateOAS3Definitions(servicePath, outPath, serviceName);
        } catch (IOException e) {
            String causeMessage = "";
            Throwable rootCause = ExceptionUtils.getRootCause(e);

            if (rootCause != null) {
                causeMessage = rootCause.getMessage();
            }
            throw LauncherUtils.createUsageException(
                    "Error occurred when exporting swagger file for service file at " + argList.get(1)
                            + ". " + e.getMessage() + ". " + causeMessage);
        }
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {

    }
}
