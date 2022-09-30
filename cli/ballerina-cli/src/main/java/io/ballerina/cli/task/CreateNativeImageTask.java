/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.util.Optional;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.FileUtils.getFileNameWithoutExtension;
import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;

/**
 * Task for creating the native image file.
 *
 * @since 2.0.0
 */
public class CreateNativeImageTask implements Task {
    private final transient PrintStream out;
    private Path output;
    private Path currentDir;

    public CreateNativeImageTask(PrintStream out, String output) {
        this.out = out;
        if (output != null) {
            this.output = Paths.get(output);
        }
    }

    @Override
    public void execute(Project project) {
        this.out.println();
        this.out.println("Generating executable with native-image");
        this.currentDir = Paths.get(System.getProperty(USER_DIR));

        Path nativeDirectoryPath;
        String nativeImageName;
        Target target;

        try {
            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                target = new Target(project.targetDir());
            } else {
                target = new Target(Files.createTempDirectory("ballerina-cache" + System.nanoTime()));
                target.setOutputPath(getExecutablePath(project));
            }
        } catch (IOException e) {
            throw createLauncherException("unable to resolve target path : " + e.getMessage());
        } catch (ProjectException e) {
            throw createLauncherException("unable to create executable : " + e.getMessage());
        }

        Path executablePath;
        try {
            executablePath = target.getExecutablePath(project.currentPackage()).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw createLauncherException("unable to resolve executable path : " + e.getMessage());
        }

        if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            Path sourceRootParent = Optional.of(project.sourceRoot().toAbsolutePath().getParent()).orElseThrow();
            nativeDirectoryPath = sourceRootParent.resolve("native");
            nativeImageName = Optional.of(sourceRootParent.getFileName()).orElseThrow().toString();
        } else {
            nativeDirectoryPath = target.path().toAbsolutePath().resolve("native");
            nativeImageName = project.currentPackage().packageName().toString();
        }

        try {
            String[] command = {
                    "native-image",
                    "-jar",
                    executablePath.toString(),
                    "-H:Name=" + nativeImageName,
                    "-H:Path=" + nativeDirectoryPath,
                    "--no-fallback"};

            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            builder.inheritIO();
            Process process = builder.start();

            if (process.waitFor() != 0) {
                out.println("\n\t GraalVM image generation failed.");
            } else {
                out.println("\n" + "GraalVM image generated");
                out.println("\t" + nativeDirectoryPath + File.separator + nativeImageName);
                out.println();
            }

        } catch (IOException e) {
            throw createLauncherException("unable to create native directory:" + e.getMessage());
        } catch (InterruptedException e) {
            throw createLauncherException("unable to create native image:" + e.getMessage());
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
