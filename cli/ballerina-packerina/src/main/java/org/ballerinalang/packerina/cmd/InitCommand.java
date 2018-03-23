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
import org.ballerinalang.toml.model.Manifest;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Init command for creating a ballerina project.
 */
@Parameters(commandNames = "init", commandDescription = "initialize ballerina project")
public class InitCommand implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";
    private static PrintStream outStream = System.err;
    private JCommander parentCmdParser;
    
    @Parameter(names = {"--interactive", "-i"})
    private boolean interactiveFlag;
    
    @Parameter(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;
    
    @Override
    public void execute() {
        PrintStream out = System.out;
    
        // Get source root path.
        Path projectPath = Paths.get(System.getProperty(USER_DIR));
        Scanner scanner = new Scanner(System.in, Charset.defaultCharset().name());
        try {
            Manifest manifest = null;
            List<SrcFile> sourceFiles = null;
        
            if (helpFlag) {
                String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "init");
                outStream.println(commandUsageInfo);
                return;
            }
        
            if (interactiveFlag) {
                sourceFiles = new ArrayList<>();
    
                // Check if Ballerina.toml file needs to be created.
                out.print("Create Ballerina.toml: (yes/y) ");
                String createToml = scanner.nextLine().trim();
    
                if (createToml.equalsIgnoreCase("yes") || createToml.equalsIgnoreCase("y") ||
                    createToml.isEmpty()) {
                    manifest = new Manifest();
                    
                    // Get org name.
                    out.print("--Organization name: (home) ");
                    String orgName = scanner.nextLine().trim();
                    manifest.setName(orgName.isEmpty() ? "home" : orgName);
    
                    String version;
                    do {
                        out.print("--Version: (1.0.0) ");
                        version = scanner.nextLine().trim();
                        version = version.isEmpty() ? "1.0.0" : version;
                    } while (!validateVersion(out, version));
                    
                    manifest.setVersion(version);
                }
    
                String srcInput;
                boolean validInput = false;
                do  {
                    out.print("Ballerina source [package/p, main/m, (empty to finish)]: ");
                    srcInput = scanner.nextLine().trim();
                    
                    if (srcInput.equalsIgnoreCase("package") || srcInput.equalsIgnoreCase("p")) {
                        out.print("--Package Name: (my_package) ");
                        String packageName = scanner.nextLine().trim();
                        packageName = packageName.isEmpty() ? "my_package" : packageName;
                        SrcFile srcFile = new SrcFile(packageName, SrcFile.SrcFileType.SERVICE);
                        sourceFiles.add(srcFile);
                    } else if (srcInput.equalsIgnoreCase("main") || srcInput.equalsIgnoreCase("m")) {
                        out.print("--Main function: (main) ");
                        String mainBal = scanner.nextLine().trim();
                        mainBal = mainBal.isEmpty() ? "main" : mainBal;
                        SrcFile srcFile = new SrcFile(mainBal, SrcFile.SrcFileType.MAIN);
                        sourceFiles.add(srcFile);
                    } else if (srcInput.equalsIgnoreCase("")) {
                        validInput = true;
                    } else {
                        out.println("--Invalid input");
                    }
                } while (!validInput);
    
                out.print("\n");
            }
            
            InitHandler.initialize(projectPath, manifest, sourceFiles);
            out.println("Ballerina project initialized");

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
        out.append("Use --interactive or -i to create a ballerina project in interactive mode.\n");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina init [-i] \n");
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
    
    /**
     * Validates the version is a semver version.
     * @param versionAsString The version.
     * @return True if valid version, else false.
     */
    private boolean validateVersion(PrintStream out, String versionAsString) {
        String semverRegex = "((?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*))";
        Pattern pattern = Pattern.compile(semverRegex);
        Matcher matcher = pattern.matcher(versionAsString);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        if (count != 1) {
            out.println("--Invalid version: \"" + versionAsString + "\"");
        }
        return count == 1;
    }
}
