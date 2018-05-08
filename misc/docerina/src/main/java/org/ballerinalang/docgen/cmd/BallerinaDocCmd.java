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

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * doc command for ballerina which generates documentation for Ballerina packages.
 */
@Parameters(commandNames = "doc", commandDescription = "generate Ballerina API documentation")
public class BallerinaDocCmd implements BLauncherCmd {
    private final PrintStream out = System.out;

    private JCommander parentCmdParser;

    @Parameter(arity = 1, description = "either the path to the directories where Ballerina source files reside or a "
            + "path to a Ballerina file which does not belong to a package")
    private List<String> argList;

    @Parameter(names = { "--output", "-o" },
            description = "path to the output directory where the API documentation will be written to", hidden = false)
    private String outputDir;

    @Parameter(names = { "--template", "-t" },
            description = "path to a custom templates directory to be used for API documentation generation",
            hidden = false)
    private String templatesDir;

    @Parameter(names = { "--exclude", "-e" },
            description = "a comma separated list of package names to be filtered from the documentation",
            hidden = false)
    private String packageFilter;

    @Parameter(names = { "--native", "-n" },
            description = "read the source as native ballerina code", hidden = false)
    private boolean nativeSource;

    @DynamicParameter(names = "-e", description = "Ballerina environment parameters")
    private Map<String, String> runtimeParams = new HashMap<>();

    @Parameter(names = {"--config", "-c"}, description = "path to the docerina configuration file")
    private String configFilePath;

    @Parameter(names = { "--verbose", "-v" },
            description = "enable debug level logs", hidden = false)
    private boolean debugEnabled;

    @Parameter(names = {"--sourceroot"}, description = "path to the directory containing source files and packages")
    private String sourceRoot;

    @Parameter(names = { "--help", "-h" }, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "doc");
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
                sources);
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
                .append("ballerina doc <sourcepath>... [-t templatesdir] [-o outputdir] [-n] [-e excludedpackages] [-v]"
                        + System.lineSeparator())
                .append("  sourcepath:" + System.lineSeparator())
                .append("  Paths to the directories where Ballerina source files reside or a path to"
                        + System.lineSeparator())
                .append("  a Ballerina file which does not belong to a package" + System.lineSeparator());
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    }
}
