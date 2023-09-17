/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.task;

import io.ballerina.cli.launcher.RuntimePanicException;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.getProfileDebugArg;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static io.ballerina.cli.utils.DebugUtils.isInProfileDebugMode;
import static io.ballerina.cli.utils.FileUtils.getFileNameWithoutExtension;
import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_HOME;

/**
 * Task for running the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class RunProfilerTask implements Task {
    private final PrintStream err;
    private static final String JAVA_OPTS = "JAVA_OPTS";

    public RunProfilerTask(PrintStream errStream) {
        this.err = errStream;
    }

    private void initiateProfiler(Project project) {
        String profilerSource = Paths.get(System.getProperty(BALLERINA_HOME), "bre", "lib",
                "ballerina-profiler-1.0.jar").toString();
        ProjectKind projectKind = project.kind();
        Path sourcePath = Path.of(profilerSource);
        Path targetPath = getTargetProfilerPath(project);
        StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
        try {
            Files.copy(sourcePath, targetPath, copyOption);
            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.command"));
            commands.add("-jar");
            if (isInDebugMode()) {
                commands.add(getDebugArgs(err));
            }
            commands.add("Profiler.jar");
            // Sets classpath with executable thin jar and all dependency jar paths.
            commands.add("--file");
            commands.add(getPackageJarName(project, projectKind));
            commands.add("--target");
            commands.add(targetPath.toString());
            commands.add("--sourceroot");
            commands.add(getProjectPath(project).toString());
            if (isInProfileDebugMode()) {
                commands.add("--profilerDebug");
                commands.add(getProfileDebugArg(err));
            }
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            pb.environment().put(JAVA_OPTS, getAgentArgs());
            setWorkingDirectory(project, projectKind, pb);
            Process process = pb.start();
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new RuntimePanicException(exitValue);
            }
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("error occurred while running the profiler ", e);
        }
    }

    private static void setWorkingDirectory(Project project, ProjectKind projectKind, ProcessBuilder pb) {
        if (projectKind == ProjectKind.BUILD_PROJECT) {
            pb.directory(new File(project.targetDir() + "/bin"));
        } else {
            pb.directory(new File(System.getProperty(USER_DIR)));
        }
    }

    private static String getPackageJarName(Project project, ProjectKind kind) {
        if (kind == ProjectKind.SINGLE_FILE_PROJECT) {
            return getFileNameWithoutExtension(project.sourceRoot()) + BLANG_COMPILED_JAR_EXT;
        }
        return project.currentPackage().packageName() + BLANG_COMPILED_JAR_EXT;
    }

    @Override
    public void execute(Project project) {
        initiateProfiler(project);
    }

    private String getAgentArgs() {
        // add jacoco agent
        String jacocoArgLine = "-javaagent:" + Paths.get(System.getProperty(BALLERINA_HOME), "bre", "lib",
                "jacocoagent.jar") + "=destfile=" + Paths.get(System.getProperty(USER_DIR))
                        .resolve("build").resolve("jacoco").resolve("test.exec");
        return jacocoArgLine + " ";
    }

    private Path getTargetProfilerPath(Project project) {
        return getProjectPath(project).resolve("Profiler" + BLANG_COMPILED_JAR_EXT);
    }

    private Path getProjectPath(Project project) {
        // If the --output flag is not set, create the executable in the current directory
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return Paths.get(System.getProperty(USER_DIR));
        }
        return project.targetDir().resolve("bin");
    }
}
