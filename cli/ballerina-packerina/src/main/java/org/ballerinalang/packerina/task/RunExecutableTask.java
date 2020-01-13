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

import org.ballerinalang.packerina.OsUtils;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.model.ExecutableJar;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.Lists;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.jvm.runtime.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.jvm.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType.SINGLE_BAL_FILE;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.JAVA_MAIN;

/**
 * Task for running the executable.
 */
public class RunExecutableTask implements Task {

    private final String[] args;
    private Path executablePath;
    private boolean isInDebugMode = false;
    private static final String DEBUG_ARGS_JAVA8 = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=";

    /**
     * Create a task to run the executable. This requires {@link CreateExecutableTask} to be completed.
     *
     * @param args          Arguments for the executable.
     * @param isInDebugMode Flag to notify whether the executable jar should be run in the debug mode.
     */
    public RunExecutableTask(String[] args, boolean isInDebugMode) {
        this(null, args);
        this.isInDebugMode = isInDebugMode;
    }

    /**
     * Create a task to run an executable from a given path.
     *
     * @param executablePath The path to the executable.
     * @param args           Arguments for the executable.
     */
    public RunExecutableTask(Path executablePath, String[] args) {
        this.executablePath = executablePath;
        this.args = args;
    }

    @Override
    public void execute(BuildContext buildContext) {
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        BLangPackage executableModule = null;
        // set executable path from an executable built on the go
        if (null == this.executablePath) {
            for (BLangPackage module : buildContext.getModules()) {
                if (module.symbol.entryPointExists) {
                    this.executablePath = buildContext.getExecutablePathFromTarget(module.packageID);
                    executableModule = module;
                    break;
                }
            }

            // check if executable found with entry points.
            if (null == this.executablePath) {
                switch (buildContext.getSourceType()) {
                    case SINGLE_BAL_FILE:
                        SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                        throw createLauncherException("no entry points found in '" + singleFileContext.getBalFile() +
                                "'.");
                    case SINGLE_MODULE:
                        SingleModuleContext singleModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                        throw createLauncherException("no entry points found in '" +
                                singleModuleContext.getModuleName() + "'.");
                    default:
                        throw createLauncherException("unknown source type found when running executable.");
                }
            }
        }

        if (!this.executablePath.isAbsolute()) {
            this.executablePath = sourceRootPath.resolve(executablePath);
        }

        // clean up the path and get absolute path
        this.executablePath = this.executablePath.toAbsolutePath().normalize();

        // if the executable does not exist
        if (Files.notExists(this.executablePath)) {
            throw createLauncherException("cannot run '" + this.executablePath.toAbsolutePath().toString() +
                    "' as it does not exist.");
        }

        // if the executable is not a file and not an extension with .jar
        if (!(Files.isRegularFile(this.executablePath) &&
                this.executablePath.toString().endsWith(BLANG_COMPILED_JAR_EXT))) {

            throw createLauncherException("cannot run '" + this.executablePath.toAbsolutePath().toString() +
                    "' as it is not an executable with .jar extension.");
        }

        // set the source root path relative to the source path i.e. set the parent directory of the source path
        System.setProperty(ProjectDirConstants.BALLERINA_SOURCE_ROOT, sourceRootPath.toString());
        if (buildContext.getSourceType() == SINGLE_BAL_FILE) {
            this.runGeneratedExecutableWithSameClassLoader(executableModule, buildContext);
            return;
        }
        this.runGeneratedExecutable(executableModule, buildContext);
    }

    /**
     * Run an executable that is generated from 'run' command.
     *
     * @param executableModule The module to run.
     */
    private void runGeneratedExecutable(BLangPackage executableModule, BuildContext buildContext) {

        ExecutableJar executableJar = buildContext.moduleDependencyPathMap.get(executableModule.packageID);
        String initClassName = BFileUtil.getQualifiedClassName(executableModule.packageID.orgName.value,
                executableModule.packageID.name.value, MODULE_INIT_CLASS_NAME);
        try {
            List<String> commands = new ArrayList<>();
            commands.add("java");
            commands.add("-cp");
            commands.add(getClassPath(executableJar));
            if (isInDebugMode) {
                commands.add(String.format("%s,address=%s", DEBUG_ARGS_JAVA8,
                        System.getProperty(SYSTEM_PROP_BAL_DEBUG)));
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

    /**
     * Run an executable that is generated from 'run bal' command.
     *
     * @param executableModule The module to run.
     */
    private void runGeneratedExecutableWithSameClassLoader(BLangPackage executableModule, BuildContext buildContext) {

        ExecutableJar executableJar = buildContext.moduleDependencyPathMap.get(executableModule.packageID);
        String initClassName = BFileUtil.getQualifiedClassName(executableModule.packageID.orgName.value,
                                                               executableModule.packageID.name.value,
                                                               MODULE_INIT_CLASS_NAME);
        try {
            URL[] urls = new URL[executableJar.platformLibs.size() + 1];
            urls[0] = executableJar.moduleJar.toUri().toURL();
            int i = 1;
            for (Path platformLib : executableJar.platformLibs) {
                urls[i++] = platformLib.toUri().toURL();
            }
            URLClassLoader classLoader = new URLClassLoader(urls);
            Class<?> initClazz = classLoader.loadClass(initClassName);
            Method mainMethod = initClazz.getDeclaredMethod(JAVA_MAIN, String[].class);
            mainMethod.invoke(null, (Object) this.args);
            if (!initClazz.getField("serviceEPAvailable").getBoolean(initClazz)) {
                Runtime.getRuntime().exit(0);
            }
        } catch (MalformedURLException e) {
            throw createLauncherException("loading jar file failed with given source path " + this.executablePath);
        } catch (ClassNotFoundException e) {
            throw createLauncherException("module init class with name " + initClassName + " cannot be found ");
        } catch (NoSuchMethodException e) {
            throw createLauncherException("main method cannot be found for init class " + initClassName);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw createLauncherException("invoking main method failed due to " + e.getMessage());
        } catch (InvocationTargetException | NoSuchFieldException e) {
            throw createLauncherException("invoking main method failed due to ", e.getCause());
        }
    }

    private static String getClassPath(ExecutableJar jar) {
        String seperator = ":";
        if (OsUtils.isWindows()) {
            seperator = ";";
        }
        StringBuilder classPath = new StringBuilder(jar.moduleJar.toString());
        for (Path path : jar.platformLibs) {
            classPath.append(seperator).append(path);
        }
        return classPath.toString();
    }
}
