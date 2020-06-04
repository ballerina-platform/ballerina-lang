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
import org.ballerinalang.bindgen.utils.BindgenUtils;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.bindgen.command.BindingsGenerator.setOutputPath;
import static org.ballerinalang.bindgen.utils.BindgenConstants.COMPONENT_IDENTIFIER;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;

/**
 * This class represents the "ballerina bindgen" command.
 *
 * @since 1.2.0
 */
@CommandLine.Command(
        name = "bindgen",
        description = "A CLI tool for generating Ballerina bindings for Java APIs.")
public class BindgenCommand implements BLauncherCmd {

    private PrintStream outStream;
    private PrintStream outError;
    private Path targetOutputPath = Paths.get(System.getProperty(USER_DIR));

    public BindgenCommand() {
        this(System.out, System.err);
    }

    public BindgenCommand(PrintStream out, PrintStream err) {
        this.outStream = out;
        this.outError = err;
        BindgenUtils.setOutStream(out);
        BindgenUtils.setErrStream(err);
    }

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-cp", "--classpath"},
            description = "One or more comma-delimited classpaths for obtaining the jar files required for\n" +
                    "generating the Ballerina bindings.")
    private String classPath;

    @CommandLine.Option(names = {"-mvn", "--maven"},
            description = "A maven dependency with colon delimited groupId, artifactId and version.")
    private String mavenDependency;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Location of the generated Ballerina bridge code."
    )
    private String outputPath;

    @CommandLine.Parameters
    private List<String> classNames;

    private static final String BINDGEN_CMD = "ballerina bindgen [(-cp|--classpath) <classpath>...]\n" +
            "                  [(-mvn|--maven) <groupId>:<artifactId>:<version>]\n" +
            "                  [(-o|--output) <output>]\n" +
            "                  (<class-name>...)";

    @Override
    public void execute() {
        outStream.println("\nNote: This is an experimental tool.");
        //Help flag check
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        if (classNames == null) {
            outError.println("\nOne or more class names should be specified to generate the Ballerina bindings.\n");
            outStream.println(BINDGEN_CMD);
            outStream.println("\nUse 'ballerina bindgen --help' for more information on the command.");
            return;
        }

        BindingsGenerator bindingsGenerator = new BindingsGenerator(outStream, outError);
        if (this.outputPath != null) {
            if (Paths.get(outputPath).isAbsolute()) {
                targetOutputPath = Paths.get(outputPath);
            } else {
                targetOutputPath = Paths.get(targetOutputPath.toString(), outputPath);
            }
            setOutputPath(outputPath);
        }

        if (!ProjectDirs.isProject(targetOutputPath)) {
            Path findRoot = ProjectDirs.findProjectRoot(targetOutputPath);
            if (findRoot != null) {
                outStream.println("\nBallerina project detected at: " + findRoot.toString());
                bindingsGenerator.setProjectRoot(findRoot);
            }
        } else {
            outStream.println("\nBallerina project detected at: " + targetOutputPath.toString());
            bindingsGenerator.setProjectRoot(targetOutputPath);
        }

        String splitCommaRegex = "\\s*,\\s*";
        if (this.classPath != null) {
            String[] dependencyList = this.classPath.split(splitCommaRegex);
            bindingsGenerator.setDependentJars(dependencyList);
        }

        String splitColonRegex = "\\s*:\\s*";
        if (this.mavenDependency != null) {
            String[] mvnDependency = this.mavenDependency.split(splitColonRegex);
            if (mvnDependency.length != 3) {
                outError.println("\nError in the maven dependency provided.\n");
                outStream.println(BINDGEN_CMD);
                outStream.println("\nUse 'ballerina bindgen --help' for more information on the command.");
                return;
            }
            bindingsGenerator.setMvnGroupId(mvnDependency[0]);
            bindingsGenerator.setMvnArtifactId(mvnDependency[1]);
            bindingsGenerator.setMvnVersion(mvnDependency[2]);
        }

        bindingsGenerator.setClassNames(this.classNames);
        try {
            bindingsGenerator.generateJavaBindings();
        } catch (BindgenException e) {
            outError.println("\nError while generating Ballerina bindings:\n" + e.getMessage());
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
        out.append("Ballerina bindings could be generated for Java classes residing inside Java libraries \n");
        out.append("or standard Java classes. Here, the Java classes will be mapped onto Ballerina");
        out.append("objects, making the developer experience of Ballerina Java interoperability seamless. \n");
        out.append("\n");
        out.append("The directly dependent Java classes and other required resources will be automatically \n");
        out.append("generated apart from the specified Java classes. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina " + COMPONENT_IDENTIFIER + " java.utils.ArrayDeque\n");
        out.append("  ballerina " + COMPONENT_IDENTIFIER + " -cp ./libs/snakeyaml-1.25.jar,./libs/pdfbox-1.8.10.jar " +
                "  -o ./src/sample org.yaml.snakeyaml.Yaml org.apache.pdfbox.pdmodel.PDDocument java.io.File\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
