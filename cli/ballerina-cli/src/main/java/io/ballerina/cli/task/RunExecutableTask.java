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
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;

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

    private static final String KEY_SCOPE = "scope";
    private static final String KEY_PATH = "path";
    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_ARTIFACT_ID = "artifactId";
    private static final String KEY_VERSION = "version";
    private static final String PLATFORM_LIBS_DIR = "platform-libs";

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

            Path executablePath = this.target.getExecutablePath(project.currentPackage())
                    .toAbsolutePath().normalize();
            List<String> providedDepPaths = getProvidedPlatformDepPaths(project);

            if (providedDepPaths.isEmpty()) {
                // No provided deps, use -jar as before.
                commands.add("-jar");
                commands.add(executablePath.toString());
            } else {
                // Provided deps must be on the classpath at runtime but are not bundled in the executable.
                // Since -jar ignores -cp, switch to -cp <classpath> <MainClass> mode.
                StringJoiner classPath = new StringJoiner(File.pathSeparator);
                providedDepPaths.forEach(classPath::add);
                classPath.add(executablePath.toString());
                commands.add("-cp");
                commands.add(classPath.toString());
                commands.add(getMainClass(executablePath));
            }
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

    private List<String> getProvidedPlatformDepPaths(Project project) {
        List<String> paths = new ArrayList<>();
        String targetRepo = project.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(PLATFORM_LIBS_DIR).toAbsolutePath().toString();
        Map<String, PackageManifest.Platform> platforms =
                project.currentPackage().manifest().platforms();
        for (PackageManifest.Platform platform : platforms.values()) {
            if (platform == null || platform.dependencies().isEmpty()) {
                continue;
            }
            for (Map<String, Object> dependency : platform.dependencies()) {
                String scopeValue = (String) dependency.get(KEY_SCOPE);
                if (!PlatformLibraryScope.PROVIDED.getStringValue().equals(scopeValue)) {
                    continue;
                }
                String depFilePath = (String) dependency.get(KEY_PATH);
                if (depFilePath != null && !depFilePath.isEmpty()) {
                    // Path-based dependency: resolve relative paths against the project source root.
                    Path jarPath = Path.of(depFilePath);
                    if (!jarPath.isAbsolute()) {
                        jarPath = project.sourceRoot().resolve(jarPath);
                    }
                    paths.add(jarPath.toAbsolutePath().normalize().toString());
                } else {
                    // Maven dependency (groupId + artifactId + version): construct path from target/platform-libs.
                    String groupId = (String) dependency.get(KEY_GROUP_ID);
                    String artifactId = (String) dependency.get(KEY_ARTIFACT_ID);
                    String version = (String) dependency.get(KEY_VERSION);
                    if (groupId != null && artifactId != null && version != null) {
                        String jarPath = targetRepo + File.separator
                                + groupId.replace('.', File.separatorChar)
                                + File.separator + artifactId
                                + File.separator + version
                                + File.separator + artifactId + "-" + version + ".jar";
                        paths.add(jarPath);
                    }
                }
            }
        }
        return paths;
    }

    private String getMainClass(Path executablePath) throws IOException {
        try (JarInputStream jarStream = new JarInputStream(Files.newInputStream(executablePath))) {
            return (String) jarStream.getManifest().getMainAttributes().get(Attributes.Name.MAIN_CLASS);
        }
    }

    public void killProcess() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}
