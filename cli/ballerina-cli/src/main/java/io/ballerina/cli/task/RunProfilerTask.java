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
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_HOME;

/**
 * Task for running the jBallerina profiler.
 *
 * @since 2201.8.0
 */
public class RunProfilerTask implements Task {

    private final String[] args;
    private final PrintStream err;
    private static final String JAVA_OPTS = "JAVA_OPTS";

    public RunProfilerTask(PrintStream errStream, String[] args) {
        this.err = errStream;
        this.args = args;
    }


    private void initiateProfiler(Project project, String[] args) {
        String profilerArguments = String.join(" ", args);
        String profilerSource = Paths.get(System.getProperty(BALLERINA_HOME), "bre", "lib",
                "ballerina-profiler-1.0.jar").toString();
        Path sourcePath = Path.of(profilerSource);
        Path targetPath = Path.of(project.targetDir() + "/bin/Profiler.jar");
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
            commands.add(project.currentPackage().packageName() + ".jar");
            commands.add("--target");
            commands.add(targetPath.toString());
            if (args.length != 0) {
                commands.add("--args");
                commands.add("[" + profilerArguments + "]");
            }
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            pb.directory(new File(project.targetDir() + "/bin"));
            pb.environment().put(JAVA_OPTS, getAgentArgs());
            Process process = pb.start();
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new RuntimePanicException(exitValue);
            }
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("Error occurred while running the profiler ", e.getCause());
        }
    }

    @Override
    public void execute(Project project) {
        initiateProfiler(project, this.args);
    }

    private String getAgentArgs() {
        // add jacoco agent
        String jacocoArgLine = "-javaagent:" + Paths.get(System.getProperty(BALLERINA_HOME), "bre", "lib",
                "jacocoagent.jar") + "=destfile=" + Paths.get(System.getProperty("user.dir"))
                        .resolve("build").resolve("jacoco").resolve("test.exec");
        return jacocoArgLine + " ";
    }
}
