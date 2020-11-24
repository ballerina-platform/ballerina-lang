/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.cli.task;

import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;
import org.ballerinalang.compiler.plugins.CompilerPlugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;

import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for creating the executable jar file.
 *
 * @since 2.0.0
 */
public class CreateExecutableTask implements Task {
    private final transient PrintStream out;
    private Path output;

    public CreateExecutableTask(PrintStream out, String output) {
        this.out = out;
        if (output != null) {
            this.output = Paths.get(output);
        }
    }

    @Override
    public void execute(Project project) {
        this.out.println();
        this.out.println("Generating executable");

        Target target;
        Path currentDir = Paths.get(System.getProperty(USER_DIR));
        try {
            target = new Target(project.sourceRoot());
            if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                Path outputPath;
                DocumentId documentId = project.currentPackage().getDefaultModule().documentIds().iterator().next();
                String documentName = project.currentPackage().getDefaultModule().document(documentId).name();
                String executableName = FileUtils.geFileNameWithoutExtension(Paths.get(documentName));
                if (executableName == null) {
                    throw createLauncherException("unable to determine executable name");
                }
                if (output != null) {
                    if (Files.isDirectory(output)) {
                        outputPath = output.resolve(executableName + BLANG_COMPILED_JAR_EXT);
                    } else {
                        if (!FileUtils.hasExtension(output)) {
                            outputPath = Paths.get(output.toString() + BLANG_COMPILED_JAR_EXT);
                        } else {
                            outputPath = output;
                        }
                    }
                    if (!outputPath.isAbsolute()) {
                        outputPath = currentDir.resolve(outputPath);
                    }
                } else {
                    outputPath = currentDir.resolve(executableName + BLANG_COMPILED_JAR_EXT);
                }
                target.setOutputPath(outputPath);
            }
        } catch (IOException e) {
            throw createLauncherException("unable to set executable path: " + e.getMessage());
        }

        Path executablePath;
        try {
            executablePath = target.getExecutablePath(project.currentPackage()).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw createLauncherException(e.getMessage());
        }

        try {
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JdkVersion.JAVA_11);
            jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, executablePath);
        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }

        // notify plugin
        // todo following call has to be refactored after introducing new plugin architecture
        notifyPlugins(project, target);

        // Print the path of the executable
        Path relativePathToExecutable = currentDir.relativize(executablePath);
        if (relativePathToExecutable.toString().contains("..") ||
                relativePathToExecutable.toString().contains("." + File.separator)) {
            this.out.println("\t" + executablePath.toString());
        } else {
            this.out.println("\t" + relativePathToExecutable.toString());
        }
    }

    private void notifyPlugins(Project project, Target target) {
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        for (CompilerPlugin plugin : processorServiceLoader) {
            plugin.codeGenerated(project, target);
        }
    }
}
