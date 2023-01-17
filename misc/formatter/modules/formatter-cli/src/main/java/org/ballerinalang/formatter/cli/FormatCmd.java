/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.cli;

import io.ballerina.cli.BLauncherCmd;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class to implement "format" command for ballerina.
 * Ex: bal format [ballerinaFile | ModuleName] [-d | --dry-run]
 */
@CommandLine.Command(name = "format", description = "format given Ballerina source file")
public class FormatCmd implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-d", "--dry-run"})
    private boolean dryRun;

    @CommandLine.Option(names = "--module")
    private String moduleName;

    @CommandLine.Option(names = "--file")
    private String fileName;

    @Override
    public void execute() {
        // Get source root path.
        Path sourceRootPath = Paths.get(System.getProperty(USER_DIR));
        FormatUtil.execute(argList, helpFlag, moduleName, fileName, dryRun, sourceRootPath);
    }

    @Override
    public String getName() {
        return FormatUtil.CMD_NAME;
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
