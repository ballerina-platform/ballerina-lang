/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.task;

import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

import static io.ballerina.runtime.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType.SINGLE_BAL_FILE;
import static org.ballerinalang.packerina.utils.DebugUtils.getDebugArgs;
import static org.ballerinalang.packerina.utils.DebugUtils.isInDebugMode;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Task for running the executable.
 */
public class RunExecutableTask implements Task {

    private final String[] args;
    private Path executableJarPath;

    /**
     * Create a task to run the executable. This requires {@link CreateExecutableTask} to be completed.
     *
     * @param args Arguments for the executable.
     */
    public RunExecutableTask(String[] args) {
        this.args = args;
        this.executableJarPath = null;
    }

    @Override
    public void execute(BuildContext buildContext) {
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);

        BLangPackage executableModule = null;
        for (BLangPackage module : buildContext.getModules()) {
            if (module.symbol.entryPointExists) {
                executableModule = module;
                this.executableJarPath = buildContext.getJarPathFromTargetCache(executableModule.packageID);
                break;
            }
        }

        // If any entry point is not found.
        if (executableModule == null) {
            switch (buildContext.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    throw createLauncherException(String.format("no entry points found in '%s'.",
                            singleFileContext.getBalFile()));
                case SINGLE_MODULE:
                    SingleModuleContext singleModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    throw createLauncherException(String.format("no entry points found in '%s'.",
                            singleModuleContext.getModuleName()));
                default:
                    throw createLauncherException("unknown source type found when running executable.");
            }
        }

        // if the executable does not exist.
        if (Files.notExists(this.executableJarPath)) {
            throw createLauncherException(String.format("cannot run '%s' as it does not exist.",
                    executableJarPath.toAbsolutePath().toString()));
        }

        // if the executable is not a file and not an extension with .jar.
        if (!(Files.isRegularFile(this.executableJarPath) &&
                this.executableJarPath.toString().endsWith(BLANG_COMPILED_JAR_EXT))) {
            throw createLauncherException(String.format("cannot run '%s' as it is not an executable with .jar " +
                    "extension.", this.executableJarPath.toAbsolutePath().toString()));
        }

        // set the source root path relative to the source path i.e. set the parent directory of the source path
        System.setProperty(ProjectDirConstants.BALLERINA_SOURCE_ROOT, sourceRootPath.toString());
        this.runGeneratedExecutable(executableModule, buildContext);
    }

    /**
     * Run an executable that is generated from 'run' command.
     *
     * @param executableModule The module to run.
     */
    private void runGeneratedExecutable(BLangPackage executableModule, BuildContext buildContext) {

        String initClassName = BFileUtil.getQualifiedClassName(executableModule.packageID.orgName.value,
                executableModule.packageID.name.value, executableModule.packageID.version.value,
                                                               MODULE_INIT_CLASS_NAME);
        try {
            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.command"));
            // Sets classpath with executable thin jar and all dependency jar paths.
            commands.add("-cp");
            commands.add(getAllClassPaths(executableModule, buildContext));
            if (isInDebugMode()) {
                commands.add(getDebugArgs(buildContext));
            }
            commands.add(initClassName);
            commands.addAll(Lists.of(args));
            ProcessBuilder pb = new ProcessBuilder(commands).inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw createLauncherException("Error occurred while running the executable ", e.getCause());
        }
    }

    private String getAllClassPaths(BLangPackage executableModule, BuildContext buildContext) {
        JarResolver jarResolver = buildContext.get(BuildContextField.JAR_RESOLVER);
        StringJoiner cp = new StringJoiner(File.pathSeparator);
        // Adds executable thin jar path.
        cp.add(this.executableJarPath.toString());
        // Adds all the dependency paths for modules.
        HashSet<Path> dependencySet = new HashSet<>(jarResolver.allDependencies(executableModule));
        dependencySet.forEach(path -> cp.add(path.toString()));
        // Adds bre/lib/* to the class-path since we need to have ballerina runtime related dependencies
        // when running single bal files
        if (buildContext.getSourceType().equals(SINGLE_BAL_FILE)) {
            cp.add(buildContext.get(BuildContextField.HOME_REPO).toString() + "/bre/lib/*");
        }
        return cp.toString();
    }
}
