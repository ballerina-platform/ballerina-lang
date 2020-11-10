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
package org.ballerinalang.packerina.cmd;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.ballerina.runtime.util.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.packerina.cmd.Constants.MODULE_NAME_REGEX;
import static org.ballerinalang.packerina.cmd.Constants.PULL_COMMAND;

/**
 * This class represents the "ballerina pull" command.
 *
 * @since 0.964
 */
@CommandLine.Command(name = PULL_COMMAND,
                description = "download the module source and binaries from a remote repository")
public class PullCommand implements BLauncherCmd {
    private static PrintStream outStream = System.err;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PULL_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            throw LauncherUtils.createUsageExceptionWithHelp("no module given");
        }

        if (argList.size() > 1) {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        String resourceName = argList.get(0);
        String orgName;
        String packageName;
        String moduleName;
        String version;

        // Get org-name
        if (!validModuleName(resourceName)) {
            CommandUtil.printError(outStream,
                    "invalid module name. Provide the module name with the org name ",
                    "ballerina pull {<org-name>/<module-name> | <org-name>/<module-name>:<version>}",
                    false);
            Runtime.getRuntime().exit(1);
            return;
        }

        // Get org-name
        String[] moduleInfo = resourceName.split("/");
        orgName = moduleInfo[0];
        packageName = moduleInfo[1];

        // Get module name
        String[] packageInfo = packageName.split(":");
        if (packageInfo.length == 2) {
            moduleName = packageInfo[0];
            version = packageInfo[1];
        } else {
            moduleName = packageName;
            version = Names.EMPTY.getValue();
        }

        URI baseURI = URI.create(RepoUtils.getRemoteRepoURL());
        String ballerinaHome = System.getProperty(ProjectDirConstants.BALLERINA_HOME);
        Repo remoteRepo = new RemoteRepo(baseURI, new HashMap<>(), false, Paths.get(ballerinaHome));

        PackageID moduleID = new PackageID(new Name(orgName), new Name(moduleName), new Name(version));

        Patten patten = remoteRepo.calculate(moduleID);
        if (patten != Patten.NULL) {
            Converter converter = remoteRepo.getConverterInstance();
            List<CompilerInput> compilerInputs = patten.convertToSources(converter, moduleID)
                                                       .collect(Collectors.toList());
            if (compilerInputs.size() == 0) {
                // Exit status, zero for OK, non-zero for error
                Runtime.getRuntime().exit(1);
            }
        } else {
            outStream.println("couldn't find module " + patten);
            // Exit status, zero for OK, non-zero for error
            Runtime.getRuntime().exit(1);
        }
        // Exit status, zero for OK, non-zero for error
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return PULL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("download modules to the user repository \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina pull <module-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private String getPullCommandRegex() {
        return MODULE_NAME_REGEX;
    }

    public boolean validModuleName(String str) {
        return Pattern.matches(getPullCommandRegex(), str);
    }
}
