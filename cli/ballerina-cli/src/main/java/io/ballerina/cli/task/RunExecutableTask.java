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
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MODULE_INIT_CLASS_NAME;
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

    /**
     * Create a task to run the executable. This requires {@link CreateExecutableTask} to be completed.
     *
     * @param args Arguments for the executable.
     * @param out output stream
     * @param err error stream
     */
    public RunExecutableTask(String[] args, PrintStream out, PrintStream err) {
        this.args = Lists.of(args);
        this.out = out;
        this.err = err;
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
            this.runGeneratedExecutable(project.currentPackage().getDefaultModule(), project);
        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().runningExecutableDuration = System.currentTimeMillis() - start;
        }
    }

    private void runGeneratedExecutable(Module executableModule, Project project) {
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        JarResolver jarResolver = jBallerinaBackend.jarResolver();

        String initClassName = JarResolver.getQualifiedClassName(
                executableModule.packageInstance().packageOrg().toString(),
                executableModule.packageInstance().packageName().toString(),
                executableModule.packageInstance().packageVersion().toString(),
                MODULE_INIT_CLASS_NAME);
        try {
            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.command"));
            commands.add("-XX:+HeapDumpOnOutOfMemoryError");
            commands.add("-XX:HeapDumpPath=" + System.getProperty(USER_DIR));
            // Sets classpath with executable thin jar and all dependency jar paths.
            commands.add("-cp");
            commands.add(getAllClassPaths(jarResolver));
            if (isInDebugMode()) {
                commands.add(getDebugArgs(err));
            }
            commands.add(initClassName);
            commands.addAll(args);
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            Process process = pb.start();
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new RuntimePanicException(exitValue);
            }
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("Error occurred while running the executable ", e.getCause());
        }
    }

    private String getAllClassPaths(JarResolver jarResolver) {

        StringJoiner cp = new StringJoiner(File.pathSeparator);
        jarResolver.getJarFilePathsRequiredForExecution().stream()
                .map(JarLibrary::path).map(Path::toString)
                .forEach(cp::add);
        return cp.toString();
    }
}

