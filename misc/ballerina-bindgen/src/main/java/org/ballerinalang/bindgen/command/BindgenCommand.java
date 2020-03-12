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
import java.util.List;

import static org.ballerinalang.bindgen.utils.BindgenConstants.COMPONENT_IDENTIFIER;

/**
 * This class represents the "ballerina bindgen" command.
 *
 * @since 1.20
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

    @CommandLine.Option(names = {"-cp", "--classpath"},
            description = "One or more comma-delimited classpaths for obtaining the jar files required for\n" +
                    "generating the Ballerina bindings.")
    private String classPath;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Location of the generated Ballerina bridge code."
    )
    private String outputPath;

    @CommandLine.Parameters
    private List<String> classNames;

    private static final String BINDGEN_CMD = "ballerina bindgen [(-cp|--classpath) <classpath>...]\n" +
            "                         [(-o|--output) <output>]\n" +
            "                         (<class-name>...|<package-name>...)";

    @Override
    public void execute() {

        //Help flag check
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        if (classNames == null) {
            outError.println("One or more class names for bindings generation should be specified.");
            outStream.println(BINDGEN_CMD);
            return;
        }

        BindingsGenerator bindingsGenerator = new BindingsGenerator();
        if (this.outputPath != null) {
            bindingsGenerator.setOutputPath(outputPath);
        }

        String splitCommaRegex = "\\s*,\\s*";
        if (this.classPath != null) {
            String[] dependencyList = this.classPath.split(splitCommaRegex);
            bindingsGenerator.setDependentJars(dependencyList);
        }

        bindingsGenerator.setClassNames(this.classNames);
        try {
            bindingsGenerator.generateJavaBindings();
        } catch (BindgenException e) {
            outError.println("Error while generating Ballerina bindings: " + e);
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
