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
import io.ballerina.projects.internal.model.Target;

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
    private static final String CURRENT_DIR_KEY = "current.dir";
    private static final Path TARGET_OUTPUT_PATH = Paths.get(System.getProperty(USER_DIR));

    public RunProfilerTask(PrintStream errStream) {
        this.err = errStream;
    }

    private void initiateProfiler(Project project) {
        String profilerSource = Paths.get(System.getProperty(BALLERINA_HOME), "bre", "lib",
                "ballerina-profiler-1.0.jar").toString();
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
            commands.add(getTargetFilePath(project));
            if (isInProfileDebugMode()) {
                commands.add("--profiler-debug");
                commands.add(getProfileDebugArg(err));
            }
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            pb.environment().put(JAVA_OPTS, getAgentArgs());
            pb.environment().put(BALLERINA_HOME, System.getProperty(BALLERINA_HOME));
            pb.environment().put(CURRENT_DIR_KEY, System.getProperty(USER_DIR));
            pb.environment().put("java.command", System.getProperty("java.command"));
            pb.directory(new File(getProfilerPath(project).toUri()));
            Process process = pb.start();
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new RuntimePanicException(exitValue);
            }
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("error occurred while running the profiler ", e);
        } finally {
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException e) {
                err.println("error occurred while deleting the profiler.jar file");
            }
        }
    }

    @Override
    public void execute(Project project) {
        initiateProfiler(project);
    }

    private String getAgentArgs() {
        // add jacoco agent
        String jacocoArgLine = "-javaagent:" + Paths.get(System.getProperty(BALLERINA_HOME), "bre", "lib",
                "jacocoagent.jar") + "=destfile=" + TARGET_OUTPUT_PATH.resolve("build").resolve("jacoco")
                .resolve("test.exec");
        return jacocoArgLine + " ";
    }

    private Path getTargetProfilerPath(Project project) {
        return getProfilerPath(project).resolve("Profiler" + BLANG_COMPILED_JAR_EXT);
    }

    private Path getProfilerPath(Project project) {
        try {
            Target target = new Target(getTargetPath(project));
            return target.getProfilerPath();
        } catch (IOException e) {
            throw createLauncherException("error while creating profiler directory: ", e);
        }
    }

    private Path getTargetPath(Project project) {
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return TARGET_OUTPUT_PATH;
        }
        return project.targetDir();
    }

    private String getTargetFilePath(Project project) {
        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return Path.of(TARGET_OUTPUT_PATH.resolve(getFileNameWithoutExtension(project.sourceRoot()) +
                    BLANG_COMPILED_JAR_EXT).toUri()).toString();
        }
        return Path.of(project.targetDir().resolve("bin").resolve(project.currentPackage().packageName() +
                BLANG_COMPILED_JAR_EXT).toUri()).toString();
    }
}
