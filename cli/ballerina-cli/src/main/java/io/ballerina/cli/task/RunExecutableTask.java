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

import io.ballerina.cli.launcher.RuntimePanicException;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.model.Target;
import org.wso2.ballerinalang.util.Lists;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.USER_DIR;

/**
 * Task for running the executable.
 *
 * @since 2.0.0
 */
public class RunExecutableTask implements Task {

    private final List<String> args;
    private final transient PrintStream out;
    private final transient PrintStream err;
    private final Target target;
    private Process process;

    /**
     * Create a task to run the executable. This requires {@link CreateExecutableTask} to be completed.
     *
     * @param args Arguments for the executable.
     * @param out output stream
     * @param err error stream
     */
    public RunExecutableTask(String[] args, PrintStream out, PrintStream err, Target target) {
        this.args = Lists.of(args);
        this.out = out;
        this.err = err;
        this.target = target;
    }

    @Override
    public void execute(Project project) {
        long start = 0;
        if (project.buildOptions().dumpBuildTime()) {
            start = System.currentTimeMillis();
        }

        out.println();
        out.println("Running executable");
        out.println();

        try {
            this.runGeneratedExecutable(project);
        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().runningExecutableDuration = System.currentTimeMillis() - start;
        }
    }

    private void runGeneratedExecutable(Project project) {
        try {
            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.command"));
            if (isInDebugMode()) {
                commands.add(getDebugArgs(err));
            }
            commands.add("-XX:+HeapDumpOnOutOfMemoryError");
            commands.add("-XX:HeapDumpPath=" + System.getProperty(USER_DIR));
            // Sets classpath with executable thin jar and all dependency jar paths.
            commands.add("-jar");
            commands.add(this.target.getExecutablePath(project.currentPackage()).toAbsolutePath()
                    .normalize().toString());
            commands.addAll(args);
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            process = pb.start();
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new RuntimePanicException(exitValue);
            }
        } catch (IOException e) {
            throw createLauncherException("Error occurred while running the executable ", e.getCause());
        } catch (InterruptedException e) {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    public void killProcess() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}
