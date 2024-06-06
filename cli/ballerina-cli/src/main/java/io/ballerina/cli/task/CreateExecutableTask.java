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

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.BuildUtils;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.cli.utils.GraalVMCompatibilityUtils;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.model.Target;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.FileUtils.getFileNameWithoutExtension;
import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectConstants.BYTECODE_OPTIMIZED_JAR_SUFFIX;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;

/**
 * Task for creating the executable jar file.
 *
 * @since 2.0.0
 */
public class CreateExecutableTask implements Task {
    private final transient PrintStream out;
    private Path output;
    private Path currentDir;
    private Target target;
    private final boolean isHideTaskOutput;

    public CreateExecutableTask(PrintStream out, String output, Target target, boolean isHideTaskOutput) {
        this.out = out;
        this.target = target;
        this.isHideTaskOutput = isHideTaskOutput;
        if (output != null) {
            this.output = Paths.get(output);
        }
    }

    @Override
    public void execute(Project project) {
        if (!isHideTaskOutput) {
            this.out.println();
            if (!project.buildOptions().nativeImage()) {
                this.out.println("Generating executable");
            }
        }

        this.currentDir = Paths.get(System.getProperty(USER_DIR));
        if (target == null) {
            target = getTarget(project);
        }
        Path executablePath = getExecutablePath(project, target);
        try {
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_17);
            long start = 0;
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }
            EmitResult emitResult;
            if (project.buildOptions().nativeImage() && project.buildOptions().cloud().isEmpty()) {
                String warnings = GraalVMCompatibilityUtils.getAllWarnings(
                        project.currentPackage(), jBallerinaBackend.targetPlatform().code(), false);
                if (!warnings.isEmpty()) {
                    out.println(warnings);
                }
                emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.GRAAL_EXEC, executablePath);
            } else if (project.buildOptions().optimizeCodegen()) {
                emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.OPTIMIZE_CODEGEN, executablePath);
            } else {
                emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, executablePath);
            }

            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().emitArtifactDuration = System.currentTimeMillis() - start;
                BuildTime.getInstance().compile = false;
            }

            // Print warnings for conflicted jars
            if (!jBallerinaBackend.conflictedJars().isEmpty()) {
                out.println("\twarning: Detected conflicting jar files:");
                for (JBallerinaBackend.JarConflict conflict : jBallerinaBackend.conflictedJars()) {
                    out.println(conflict.getWarning(project.buildOptions().listConflictedClasses()));
                }
            }

            // Print diagnostics found during emit executable
            if (!emitResult.diagnostics().diagnostics().isEmpty() && !isHideTaskOutput) {
                emitResult.diagnostics().diagnostics().forEach(d -> out.println("\n" + d.toString()));
            }

        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }

        if (!project.buildOptions().nativeImage() && !isHideTaskOutput && !project.buildOptions().optimizeCodegen()) {
            Path relativePathToExecutable = currentDir.relativize(executablePath);

            if (project.buildOptions().getTargetPath() != null) {
                this.out.println("\t" + relativePathToExecutable);
            } else {
                if (relativePathToExecutable.toString().contains("..") ||
                        relativePathToExecutable.toString().contains("." + File.separator)) {
                    this.out.println("\t" + executablePath);
                } else {
                    this.out.println("\t" + relativePathToExecutable);
                }
            }
        }

        if (project.buildOptions().optimizeCodegen()) {
            Path relativePathToExecutable = currentDir.relativize(executablePath);
            String relativePathToExecutableString =
                    relativePathToExecutable.toString().replace(BLANG_COMPILED_JAR_EXT, BYTECODE_OPTIMIZED_JAR_SUFFIX);
            String executablePathString =
                    executablePath.toString().replace(BLANG_COMPILED_JAR_EXT, BYTECODE_OPTIMIZED_JAR_SUFFIX);

            if (project.buildOptions().getTargetPath() != null) {
                this.out.println("\t" + relativePathToExecutableString);
            } else {
                if (relativePathToExecutableString.contains("..")
                        || relativePathToExecutableString.contains("." + File.separator)) {
                    this.out.println("\t" + executablePathString);
                } else {
                    this.out.println("\t" + relativePathToExecutableString);
                }
            }
        }

        // notify plugin
        // todo following call has to be refactored after introducing new plugin architecture
        BuildUtils.notifyPlugins(project, target);
    }

    private Target getTarget(Project project) {
        Target target;
        try {
            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                target = new Target(project.targetDir());
            } else {
                target = new Target(Files.createTempDirectory("ballerina-cache" + System.nanoTime()));
                target.setOutputPath(getExecutablePath(project));
            }
        } catch (IOException e) {
            throw createLauncherException("unable to resolve target path:" + e.getMessage());
        } catch (ProjectException e) {
            throw createLauncherException("unable to create executable:" + e.getMessage());
        }
        return target;
    }
    private Path getExecutablePath(Project project, Target target) {
        try {
            return target.getExecutablePath(project.currentPackage()).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw createLauncherException(e.getMessage());
        }
    }

    private Path getExecutablePath(Project project) {

        Path fileName = project.sourceRoot().getFileName();

        // If the --output flag is not set, create the executable in the current directory
        if (this.output == null) {
            return this.currentDir.resolve(getFileNameWithoutExtension(fileName) + BLANG_COMPILED_JAR_EXT);
        }

        if (!this.output.isAbsolute()) {
            this.output = currentDir.resolve(this.output);
        }

        // If the --output is a directory create the executable in the given directory
        if (Files.isDirectory(this.output)) {
            return output.resolve(getFileNameWithoutExtension(fileName) + BLANG_COMPILED_JAR_EXT);
        }

        // If the --output does not have an extension, append the .jar extension
        if (!FileUtils.hasExtension(this.output)) {
            return Paths.get(this.output.toString() + BLANG_COMPILED_JAR_EXT);
        }

        return this.output;
    }
}
