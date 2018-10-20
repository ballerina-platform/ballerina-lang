/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.cmd;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * doc command for ballerina which generates documentation for Ballerina modules.
 */
@CommandLine.Command(name = "doc", description = "generate Ballerina API documentation")
public class BallerinaDocCmd implements BLauncherCmd {
    private final PrintStream out = System.out;

    @CommandLine.Parameters(description = "either the path to the directories where Ballerina source files reside or"
            + " a path to a Ballerina file which does not belong to a module")
    private List<String> argList;

    @CommandLine.Option(names = {"--output", "-o"},
            description = "path to the output directory where the API documentation will be written to")
    private String outputDir;

    @CommandLine.Option(names = {"--template", "-t"},
            description = "path to a custom templates directory to be used for API documentation generation")
    private String templatesDir;

    @CommandLine.Option(names = {"--exclude"}, description = "a comma separated list of module names to be "
            + "filtered from the documentation")
    private String packageFilter;

    @CommandLine.Option(names = {"--native", "-n"}, description = "read the source as native ballerina code")
    private boolean nativeSource;

    @CommandLine.Option(names = "-e", description = "Ballerina environment parameters")
    private Map<String, String> runtimeParams = new HashMap<>();

    @CommandLine.Option(names = {"--config", "-c"}, description = "path to the Docerina configuration file")
    private String configFilePath;

    @CommandLine.Option(names = {"--verbose", "-v"}, description = "enable debug level logs")
    private boolean debugEnabled;

    @CommandLine.Option(names = {"--sourceroot"},
            description = "path to the directory containing source files and modules")
    private String sourceRoot;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"}, hidden = true)
    private boolean offline;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo("doc");
            out.println(commandUsageInfo);
            return;
        }

        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        if (argList == null || argList.isEmpty()) {
            SourceDirectory srcDirectory = new FileSystemProjectDirectory(sourceRootPath);
            argList = srcDirectory.getSourcePackageNames();
        }

        if (debugEnabled) {
            System.setProperty(BallerinaDocConstants.ENABLE_DEBUG_LOGS, "true");
        }

        if (templatesDir != null) {
            System.setProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, templatesDir);
        }

        try {
            ConfigRegistry.getInstance().initRegistry(runtimeParams, configFilePath, null);
        } catch (IOException e) {
            throw new RuntimeException("failed to read the specified configuration file: " + configFilePath, e);
        }

        String[] sources = argList.toArray(new String[argList.size()]);
        BallerinaDocGenerator.generateApiDocs(sourceRootPath.toString(), outputDir, packageFilter, nativeSource,
                offline, sources);
    }

    @Override
    public String getName() {
        return "doc";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generates the API documentation of give Ballerina programs." + System.lineSeparator());
        out.append(System.lineSeparator());
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder
                .append("ballerina doc <sourcepath>... [-t templatesdir] [-o outputdir] [-n] [-e excludedmodules] [-v]"
                        + System.lineSeparator())
                .append("  sourcepath:" + System.lineSeparator())
                .append("  Paths to the directories where Ballerina source files reside or a path to"
                        + System.lineSeparator())
                .append("  a Ballerina file which does not belong to a module" + System.lineSeparator());
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {
    }
}
