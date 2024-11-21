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

package org.ballerinalang.langserver.cmd;

import com.google.gson.JsonObject;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.launcher.LauncherUtils;
import org.ballerinalang.langserver.launchers.stdio.Main;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Command to generate API specifications for the active LS APIs.
 *
 * @since 2201.12.0
 */
@CommandLine.Command(name = "language-server-spec", description = "Generate specifications for the LS APIs")
public class LangServerSpecCmd implements BLauncherCmd {

    private static final String CMD_NAME = "language-server-spec";
    private static final String FILE_NAME = "language-server-spec.json";
    private static final String BALLERINA_HOME;
    private final PrintStream outputStream;

    static {
        BALLERINA_HOME = System.getProperty("ballerina.home");
    }

    public LangServerSpecCmd() {
        outputStream = System.out;
    }

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        try {
            System.setProperty("ballerina.home", BALLERINA_HOME);
            List<JsonObject> output = Main.generateApiDoc();
            String jsonString = output.toString();
            Files.writeString(Paths.get(FILE_NAME), jsonString);
            outputStream.println("LS API specifications are successfully dumped to " + FILE_NAME);
        } catch (Throwable e) {
            throw LauncherUtils.createLauncherException("Could not generate LS API specifications");
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
