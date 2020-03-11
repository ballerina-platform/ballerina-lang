/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.command;

import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.tool.BLauncherCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.bindgen.utils.BindgenConstants.COMPONENT_IDENTIFIER;

/**
 * Class to implement "bindgen" command for Ballerina.
 * Ex: ballerina bindgen --jar (jar-file-path) -pn=(package-name)... -a=(alias-for-package)... [--output (path)]
 * ballerina bindgen --mvn=(groupId:artifactId:version) [--output (path)]
 * ballerina bindgen -cn|--class-name=(class-name)... [--output (path)]
 */
@CommandLine.Command(
        name = "bindgen",
        description = "A CLI tool for generating Ballerina bindings for Java APIs.")
public class BindgenCommand implements BLauncherCmd {

    private static final Logger LOG = LoggerFactory.getLogger(BindgenCommand.class);
    private static final PrintStream outStream = System.out;
    private static final PrintStream outError = System.err;

    private CommandLine parentCmdParser;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--jar"},
            description = "Path to the jar file from which the interop code is to be generated.")
    private String jarPath;

    @CommandLine.Option(names = {"-pn", "--package-name"},
            description = "Comma-delimited FQNs of packages for which the bridge code is to be generated.")
    private String packages;

    @CommandLine.Option(names = {"-a", "--alias"},
            description = "Comma-delimited aliases for the package names."
    )
    private String aliases;

    @CommandLine.Option(names = {"-cn", "--class-name"},
            description = "Comma-delimited FQNs of Java classes for which the bridge code is to be generated.")
    private String classes;

    @CommandLine.Option(names = {"-d", "--dependency"},
            description = "Direct and transitive dependencies required for loading the jar file."
    )
    private String dependencies;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Location for generated jBallerina bridge code."
    )
    private String outputPath;

    private static final String BINDGEN_CMD = "ballerina bindgen [--jar <path-to-jar>] [-o|--output <output-path>]";

    private String[] packageList;

    @Override
    public void execute() {

        //Help flag check
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        if (jarPath == null && classes == null) {
            outError.println("Invalid arguments provided.");
            outError.println("USAGE:\n\t" + BINDGEN_CMD);
            return;
        }

        BindingsGenerator bindingsGenerator = new BindingsGenerator();
        if (this.outputPath != null) {
            bindingsGenerator.setOutputPath(outputPath);
        }

        String splitCommaRegex = "\\s*,\\s*";
        if (this.dependencies != null) {
            String[] dependencyList = this.dependencies.split(splitCommaRegex);
            bindingsGenerator.setDependentJars(dependencyList);
        }
        if (this.packages != null) {
            this.packageList = this.packages.split(splitCommaRegex);
            bindingsGenerator.setPackageNames(this.packageList);
        }
        if (this.classes != null) {
            String[] classList = this.classes.split(splitCommaRegex);
            bindingsGenerator.setClassNames(classList);
        }
        if (this.aliases != null) {
            String[] aliasList = this.aliases.split(splitCommaRegex);
            if (aliasList.length != this.packageList.length) {
                outError.println("Number of aliases provided does not match with the number of packages.");
                return;
            }
            bindingsGenerator.setPackageNames(aliasList);
        }
        try {
            if (this.jarPath != null) {
                bindingsGenerator.bindingsFromJar(this.jarPath);
            } else {
                List<String> classList = Arrays.asList(this.classes.split(splitCommaRegex));
                bindingsGenerator.stdJavaBindings(new HashSet<>(classList));
            }
        } catch (BindgenException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public String getName() {

        return COMPONENT_IDENTIFIER;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

        out.append("A CLI tool for generating Ballerina bridge code for Java APIs. \n");
        out.append("\n");
        out.append("Ballerina bindings could be generated for jars and standard Java classes. \n");
        out.append("\n");
        out.append("A jar file could be pointed using the --jar option. \n");
        out.append("\n");
        out.append("If the bridge code is to be generated for standard Java classes, the \n");
        out.append("--class-name option could be used. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {

        out.append("  ballerina " + COMPONENT_IDENTIFIER + " --jar /Users/mike/snakeyaml-1.25.jar\n");
        out.append("  ballerina " + COMPONENT_IDENTIFIER + " --class-name java.util.Collection,java.util.HashSet\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

        this.parentCmdParser = parentCmdParser;
    }
}
