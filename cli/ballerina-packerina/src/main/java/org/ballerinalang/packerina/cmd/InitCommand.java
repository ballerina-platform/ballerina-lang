/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.packerina.init.InitHandler;
import org.ballerinalang.packerina.init.models.SrcFile;
import org.ballerinalang.packerina.toml.model.Manifest;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Init command for creating a ballerina project.
 */
@Parameters(commandNames = "init", commandDescription = "initialize ballerina project")
public class InitCommand implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";
    private static PrintStream outStream = System.err;
    private JCommander parentCmdParser;
    
    @Parameter(names = {"--yes", "-y"})
    private boolean yesFlag;
    
    @Parameter(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;
    
    @Override
    public void execute() {
        PrintStream out = System.out;
    
        // Get source root path.
        Path projectPath = Paths.get(System.getProperty(USER_DIR));
        Scanner scanner = new Scanner(System.in);
        try {
            Manifest manifest = new Manifest();
            List<SrcFile> sourceFiles = null;
        
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "init");
                outStream.println(commandUsageInfo);
                return;
            }
        
            if (!yesFlag) {
                manifest.setName("home");
                manifest.setVersion("1.0.0");
            } else {
                sourceFiles = new ArrayList<>();
        
                // Get org name.
                out.print("Organization name: (home)");
                String orgName = scanner.next().trim();
                manifest.setName(orgName.isEmpty() ? "home" : orgName);
            
                out.print("Version: (1.0.0)");
                // TODO: Validation with semver
                String version = scanner.next().trim();
                manifest.setVersion(version.isEmpty() ? "1.0.0" : version);
            
                out.print("Ballerina source [package/p, main/m, skip/(empty)]");
                String srcInput = scanner.next().trim();
                while (srcInput.equalsIgnoreCase("package") ||
                       srcInput.equalsIgnoreCase("p") ||
                       srcInput.equalsIgnoreCase("main") ||
                       srcInput.equalsIgnoreCase("m")) {
                
                    if (srcInput.equalsIgnoreCase("package") || srcInput.equalsIgnoreCase("p")) {
                        out.print("Package Name: (my.package)");
                        String packageName = scanner.next().trim();
                        packageName = packageName.isEmpty() ? "my.package" : packageName;
                        SrcFile srcFile = new SrcFile(packageName, SrcFile.SrcFileType.SERVICE);
                        sourceFiles.add(srcFile);
                    } else if (srcInput.equalsIgnoreCase("main") || srcInput.equalsIgnoreCase("m")) {
                        out.print("Main function: (main)");
                        String mainBal = scanner.next().trim();
                        mainBal = mainBal.isEmpty() ? "main" : mainBal;
                        SrcFile srcFile = new SrcFile(mainBal, SrcFile.SrcFileType.MAIN);
                        sourceFiles.add(srcFile);
                    }
                
                    srcInput = scanner.next();
                }
            }
        
            out.print("Proceed to create project? (yes) n");
            String createProject = scanner.next().trim();
            if (!createProject.equalsIgnoreCase("n")) {
                InitHandler.initialize(System.out, projectPath, manifest, sourceFiles);
            } else {
                out.println("Aborting");
            }
        } catch (IOException e) {
            out.println("Error occurred while creating project: " + e.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "init";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Initializes a Ballerina Project. \n");
        out.append("\n");
        out.append("By default, it will ask for the project details. \n");
        out.append("\n");
        out.append("Use --yes or -y to create a project without having to input anything.\n");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina build <balfile | packagename> [-o output] \n");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    
    }
}
