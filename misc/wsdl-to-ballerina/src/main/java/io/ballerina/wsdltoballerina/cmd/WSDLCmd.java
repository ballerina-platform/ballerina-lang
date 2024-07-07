/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.wsdltoballerina.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.wsdltoballerina.GeneratedSourceFile;
import io.ballerina.wsdltoballerina.WSDLToBallerina;
import io.ballerina.wsdltoballerina.WSDLToBallerinaResponse;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@CommandLine.Command(
        name = "wsdl",
        description = "Generate the Ballerina sources for a given WSDL definition."
)
public class WSDLCmd implements BLauncherCmd {
    private static final String CMD_NAME = "wsdl";
    private PrintStream outStream;
    private boolean exitWhenFinish;

    @CommandLine.Option(names = {"-i", "--input"}, description = "WSDL relative file path")
    public String inputPath;

    @CommandLine.Option(
            names = {"--operations"},
            description = "Comma separated operation names that need to be generated"
    )
    public String operations;

    public WSDLCmd() {
        this.outStream = System.err;
        this.exitWhenFinish = true;
    }

    @Override
    public void execute() {
        if (inputPath == null || inputPath.isBlank()) {
            outStream.println(ErrorMessages.MISSING_WSDL_PATH);
            exitError(this.exitWhenFinish);
            return;
        }
        String fileName = inputPath;
        List<String> operation = new ArrayList<>();
        if (operations != null) {
            String[] ids = operations.split(",");
            List<String> normalizedOperationIds = Arrays.stream(ids).toList();
            operation.addAll(normalizedOperationIds);
        }

        try {
            wsdlToBallerina(fileName, operation);
        } catch (IOException e) {
            outStream.println(e.getLocalizedMessage());
            exitError(this.exitWhenFinish);
        }

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
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

    private void wsdlToBallerina(String fileName, List<String> operations) throws IOException {
        final File wsdlFile = new File(fileName);

        Path wsdlFilePath = Paths.get(wsdlFile.getCanonicalPath());
        String fileContent = Files.readString(wsdlFilePath);

        WSDLToBallerina wsdlToBallerina = new WSDLToBallerina();
        WSDLToBallerinaResponse wsdlToBallerinaResponse = wsdlToBallerina.generateFromWSDL(fileContent, operations);

        GeneratedSourceFile generatedTypes = wsdlToBallerinaResponse.getTypesSource();

        File typesFile = new File(generatedTypes.getFileName());
        Path typesFilePath = Paths.get(typesFile.getCanonicalPath());

        if (typesFile.exists()) {
            String userInput = System.console().readLine("There is already a/an " + typesFile.getName() +
                    " in the location. Do you want to override the file? [y/N] ");
            if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                outStream.println("Not overwriting the file.");
                return;
            }
        }
        try (FileWriter writer = new FileWriter(typesFilePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(generatedTypes.getContent());
        }
    }

    private static void exitError(boolean exit) {
        if (exit) {
            Runtime.getRuntime().exit(1);
        }
    }
}
