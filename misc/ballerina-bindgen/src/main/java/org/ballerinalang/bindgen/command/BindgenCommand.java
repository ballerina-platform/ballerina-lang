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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.ProjectLoader;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.utils.BindgenUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.bindgen.utils.BindgenConstants.COMPONENT_IDENTIFIER;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;

/**
 * This class represents the `bal bindgen` command.
 *
 * @since 1.2.0
 */
@CommandLine.Command(
        name = "bindgen",
        description = "A CLI tool for generating Ballerina bindings for Java APIs.")
public class BindgenCommand implements BLauncherCmd {

    private PrintStream outStream;
    private PrintStream outError;
    private boolean exitWhenFinish;
    private Path targetOutputPath = Paths.get(System.getProperty(USER_DIR));

    public BindgenCommand() {
        this(System.out, System.err);
    }

    public BindgenCommand(PrintStream out, PrintStream err, boolean exitWhenFinish) {
        this.outStream = out;
        this.outError = err;
        this.exitWhenFinish = exitWhenFinish;
        BindgenUtils.setOutStream(out);
        BindgenUtils.setErrStream(err);
    }

    public BindgenCommand(PrintStream out, PrintStream err) {
        this.outStream = out;
        this.outError = err;
        this.exitWhenFinish = true;
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
            description = "Generate all bindings inside the specified directory. This option could be " +
                    "used to generate mappings inside a single module."
    )
    private String outputPath;

    @CommandLine.Option(names = {"--public"},
            description = "Set the visibility modifier of Ballerina bindings to public."
    )
    private boolean publicFlag;

    @CommandLine.Parameters
    private List<String> classNames;

    private static final String BINDGEN_CMD = "bal bindgen [(-cp|--classpath) <classpath>...]\n" +
            "                  [(-mvn|--maven) <groupId>:<artifactId>:<version>]\n" +
            "                  [(-o|--output) <output-path>]\n" +
            "                  [--public]\n" +
            "                  (<class-name>...)";

    @Override
    public void execute() {
        //Help flag check
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            exitWithCode(0, this.exitWhenFinish);
            return;
        }

        if (classNames == null) {
            setOutError("one or more class names are required");
            exitWithCode(1, this.exitWhenFinish);
            return;
        }

        BindingsGenerator bindingsGenerator = new BindingsGenerator(outStream, outError);
        if (this.outputPath != null) {
            if (Paths.get(outputPath).isAbsolute()) {
                targetOutputPath = Paths.get(outputPath).normalize();
            } else {
                targetOutputPath = Paths.get(targetOutputPath.toString(), outputPath).normalize();
            }
            bindingsGenerator.setOutputPath(targetOutputPath.toString());
        } else {
            bindingsGenerator.setModulesFlag(true);
            bindingsGenerator.setPublic();
        }

        if (publicFlag) {
            bindingsGenerator.setPublic();
        }

        Project project = null;
        if (!ProjectDirs.isProject(targetOutputPath)) {
            Path projectDir = ProjectDirs.findProjectRoot(targetOutputPath);
            if (projectDir != null) {
                try {
                    project = ProjectLoader.loadProject(projectDir);
                } catch (ProjectException e) {
                    setOutError("unable to load the Ballerina package [" + projectDir + "]: " + e.getMessage());
                    exitWithCode(1, this.exitWhenFinish);
                    return;
                }
                outStream.println("\nBallerina package detected at: " + projectDir.toString());
                bindingsGenerator.setProject(project);
            }
        } else {
            try {
                project = ProjectLoader.loadProject(targetOutputPath);
            } catch (ProjectException e) {
                setOutError("unable to load the Ballerina package [" + targetOutputPath + "]: " + e.getMessage());
                exitWithCode(1, this.exitWhenFinish);
                return;
            }
            outStream.println("\nBallerina package detected at: " + targetOutputPath.toString());
            bindingsGenerator.setProject(project);
        }

        if (this.outputPath == null && project == null) {
            setOutError("unable to detect a Ballerina package: bindings should either be generated inside a valid " +
                    "Ballerina package or the `[(-o|--output) <output-path>]` option should be used to generate " +
                    "the bindings inside a specific directory");
            exitWithCode(1, this.exitWhenFinish);
            return;
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
                setOutError("invalid maven dependency provided");
                exitWithCode(1, this.exitWhenFinish);
                return;
            }
            bindingsGenerator.setMvnGroupId(mvnDependency[0]);
            bindingsGenerator.setMvnArtifactId(mvnDependency[1]);
            bindingsGenerator.setMvnVersion(mvnDependency[2]);
        }

        bindingsGenerator.setClassNames(this.classNames);
        try {
            bindingsGenerator.generateJavaBindings();
            exitWithCode(0, this.exitWhenFinish);
        } catch (BindgenException e) {
            outError.println("\nFailed to generate the Ballerina bindings.\n" + e.getMessage());
            exitWithCode(1, this.exitWhenFinish);
        }
    }

    private void setOutError(String errorValue) {
        outError.println("\nerror: " + errorValue + "\n");
        outStream.println(BINDGEN_CMD);
        outStream.println("\nUse 'bal bindgen --help' for more information on the command.");
    }

    public void exitWithCode(int exit, boolean exitWhenFinish) {
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(exit);
        }
    }

    @Override
    public String getName() {
        return COMPONENT_IDENTIFIER;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generate Ballerina bridge code for Java APIs.\n");
        out.append("\n");
        out.append("Ballerina bindings could be generated for Java classes residing inside Java libraries\n");
        out.append("or for standard Java classes. The Java classes will be mapped to the Ballerina\n");
        out.append("classes providing a seamless Java interoperability developer experience to Ballerina users.\n");
        out.append("\n");
        out.append("In addition to the user-specified Java classes, partial implementations of directly\n");
        out.append("-dependent Java classes will also be generated by the tool. By default, the bindings for\n");
        out.append("each Java package will be mapped to a separate Ballerina module.\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  $ bal " + COMPONENT_IDENTIFIER + " java.utils.ArrayDeque\n");
        out.append("  $ bal " + COMPONENT_IDENTIFIER + " -cp ./libs/snakeyaml-1.25.jar,./libs/pdfbox-1.8.10.jar " +
                "-o ./modules/sample\n");
        out.append("  org.yaml.snakeyaml.Yaml org.apache.pdfbox.pdmodel.PDDocument java.io.File\n");
        out.append("  $ bal " + COMPONENT_IDENTIFIER + " -mvn org.yaml:snakeyaml:1.25 org.yaml.snakeyaml.Yaml\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
