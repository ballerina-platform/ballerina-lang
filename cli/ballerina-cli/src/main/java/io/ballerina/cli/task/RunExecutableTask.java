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

import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.model.Target;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static io.ballerina.cli.utils.DebugUtils.getDebugArgs;
import static io.ballerina.cli.utils.DebugUtils.isInDebugMode;
import static io.ballerina.projects.util.ProjectUtils.getBalHomePath;
import static io.ballerina.projects.util.ProjectUtils.getBallerinaRTJarPath;
import static io.ballerina.projects.utils.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static org.ballerinalang.jvm.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for running the executable.
 *
 * @since 2.0.0
 */
public class RunExecutableTask implements Task {

    private final List<String> args;
    private Path executableJarPath;
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
        this.executableJarPath = null;
    }

    @Override
    public void execute(Project project) {

        out.println();
        out.println("Running executable");
        out.println();

        Path sourceRootPath = project.sourceRoot();
        if (project.currentPackage().getDefaultModule().getCompilation().entryPointExists()) {
            Target target;
            try {
                target = new Target(sourceRootPath);
                //TODO: move this to a util
                this.executableJarPath = target.getJarCachePath().
                        resolve(project.currentPackage().packageName().toString() + BLANG_COMPILED_JAR_EXT);
            } catch (IOException e) {
                throw new RuntimeException("error creating the target directory", e);
            }
        } else {
            throw new RuntimeException("no entrypoint found in package: " + project.currentPackage().packageName());
        }

        // if the executable does not exist.
        if (Files.notExists(this.executableJarPath)) {
            throw createLauncherException(String.format("cannot run '%s' as it does not exist.",
                    executableJarPath.toAbsolutePath().toString()));
        }

        // if the executable is not a file and not an extension with .jar.
        if (!(Files.isRegularFile(this.executableJarPath) &&
                this.executableJarPath.toString().endsWith(BLANG_COMPILED_JAR_EXT))) {
            throw new RuntimeException(String.format("cannot run '%s' as it is not an executable with .jar " +
                    "extension.", this.executableJarPath.toAbsolutePath().toString()));
        }
        this.runGeneratedExecutable(project.currentPackage().getDefaultModule(), project);
    }

    private void runGeneratedExecutable(Module executableModule, Project project) {

        String initClassName = BFileUtil.getQualifiedClassName(
                executableModule.packageInstance().packageOrg().toString(),
                executableModule.packageInstance().packageName().toString(),
                executableModule.packageInstance().packageVersion().toString(),
                MODULE_INIT_CLASS_NAME);
        try {
            List<String> commands = new ArrayList<>();
            commands.add("java");
            // Sets classpath with executable thin jar and all dependency jar paths.
            commands.add("-cp");
            commands.add(getAllClassPaths(executableModule, project));
            if (isInDebugMode()) {
                commands.add(getDebugArgs(err));
            }
            commands.add(initClassName);
            commands.addAll(args);
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error occurred while running the executable ", e.getCause());
        }
    }

    private String getAllClassPaths(Module executableModule, Project project) {

        StringJoiner cp = new StringJoiner(File.pathSeparator);
        // Adds executable thin jar path.
        cp.add(this.executableJarPath.toString());
        // TODO: Adds all the dependency paths for modules once the JarResolves is implemented.

        // Adds bre/lib/* to the class-path since we need to have ballerina runtime related dependencies
        // when running single bal files
        if (project instanceof SingleFileProject) {
            cp.add(getBalHomePath() + "/bre/lib/*");
        } else {
            cp.add(getBallerinaRTJarPath().toString());
        }
        return cp.toString();
    }
}

