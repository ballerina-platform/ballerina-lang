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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class represents the "ballerina pull" command.
 *
 * @since 0.964
 */
@Parameters(commandNames = "pull", commandDescription = "downloads the package source and binaries from a " +
        "remote repository,")
public class PullCommand implements BLauncherCmd {
    private static PrintStream outStream = System.err;
    private JCommander parentCmdParser;
    @Parameter(arity = 1)
    private List<String> argList;

    @Parameter(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @Parameter(names = "--java.debug", hidden = true, description = "remote java debugging port")
    private String javaDebugPort;

    @Parameter(names = "--debug", hidden = true)
    private String debugPort;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "pull");
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            throw new BLangCompilerException("no package given");
        }

        if (argList.size() > 1) {
            throw new BLangCompilerException("too many arguments");
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        String resourceName = argList.get(0);
        String orgName;
        String packageName;
        String version;

        // Get org-name
        int orgNameIndex = resourceName.indexOf("/");
        if (orgNameIndex != -1) {
            orgName = resourceName.substring(0, orgNameIndex);
            if (orgName.equals("ballerina")) {
                throw new BLangCompilerException("`Ballerina` is the builtin organization and its packages are " +
                                                         "included in the runtime.");
            }
        } else {
            throw new BLangCompilerException("no package-name provided");
        }

        // Get package name
        int packageNameIndex = resourceName.indexOf(":");
        if (packageNameIndex != -1) { // version is provided
            packageName = resourceName.substring(orgNameIndex + 1, packageNameIndex);
            version = resourceName.substring(packageNameIndex + 1, resourceName.length());
        } else {
            packageName = resourceName.substring(orgNameIndex + 1, resourceName.length());
            version = "*";
        }

        URI baseURI = URI.create(RepoUtils.getRemoteRepoURL());
        Repo remoteRepo = new RemoteRepo(baseURI);

        PackageID packageID = new PackageID(new Name(orgName), new Name(packageName), new Name(version));

        Patten patten = remoteRepo.calculate(packageID);
        if (patten != Patten.NULL) {
            Converter converter = remoteRepo.getConverterInstance();
            patten.convertToSources(converter, packageID).collect(Collectors.toList());

        } else {
            outStream.println("couldn't find package " + patten);
        }
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return "pull";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("download packages to the user repository \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina pull <package-name> \n");
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    }
}
