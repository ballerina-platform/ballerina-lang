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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.tool.util.BFileUtil;
import org.ballerinalang.util.BootstrapRunner;
import org.ballerinalang.util.JBallerinaInMemoryClassLoader;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.observability.ObservabilityConstants;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.LogManager;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.ballerinalang.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.JAVA_MAIN;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.MAIN_CLASS_MANIFEST_ENTRY;

/**
 * Task for running the executable.
 */
public class RunExecutableTask implements Task {
    
    private final String[] args;
    private final Map<String, String> runtimeParams;
    private final String configFilePath;
    private final boolean observeFlag;
    private Path executablePath;
    private boolean isGeneratedExecutable = false;
    
    /**
     * Create a task to run the executable. This requires {@link CreateExecutableTask} to be completed.
     *
     * @param args           Arguments for the executable.
     * @param runtimeParams  run time parameters
     * @param configFilePath config file path
     * @param observeFlag    to indicate whether observability is enabled
     */
    public RunExecutableTask(String[] args, Map<String, String> runtimeParams, String configFilePath,
                             boolean observeFlag) {
        this(null, args, runtimeParams, configFilePath, observeFlag);
        this.isGeneratedExecutable = true;
    }
    
    /**
     * Create a task to run an executable from a given path.
     *
     * @param executablePath The path to the executable.
     * @param args           Arguments for the executable.
     * @param runtimeParams  run time parameters
     * @param configFilePath config file path
     * @param observeFlag    to indicate whether observability is enabled
     */
    public RunExecutableTask(Path executablePath, String[] args, Map<String, String> runtimeParams,
                             String configFilePath, boolean observeFlag) {
        this.executablePath = executablePath;
        this.args = args;
        this.runtimeParams = runtimeParams;
        this.configFilePath = configFilePath;
        this.observeFlag = observeFlag;
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
                        throw new BLangCompilerException("no entry points found in '" +
                                                         singleFileContext.getBalFile() + "'");
                    case SINGLE_MODULE:
                        SingleModuleContext singleModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                        throw new BLangCompilerException("no entry points found in '" +
                                                         singleModuleContext.getModuleName() + "'");
                    default:
                        throw new BLangCompilerException("cannot run given source.");
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
            throw new BLangCompilerException("cannot run '" + this.executablePath.toAbsolutePath().toString() +
                                             "' as it does not exist.");
        }
    
        // if the executable is not a file and not an extension with .jar
        if (!(Files.isRegularFile(this.executablePath) &&
            this.executablePath.toString().endsWith(BLANG_COMPILED_JAR_EXT))) {
            
            throw new BLangCompilerException("cannot run '" + this.executablePath.toAbsolutePath().toString() +
                                             "' as it is not an executable with .jar extension.");
        }
    
        // set the source root path relative to the source path i.e. set the parent directory of the source path
        System.setProperty(ProjectDirConstants.BALLERINA_SOURCE_ROOT, sourceRootPath.toString());
        
        // load configurations from config file
        loadConfigurations(sourceRootPath, this.runtimeParams, this.configFilePath, this.observeFlag);
        
        if (this.isGeneratedExecutable) {
            this.runGeneratedExecutable(executableModule);
        } else {
            this.runExecutable();
        }
    }
    
    /**
     * Run an executable that is generated from 'run' command.
     *
     * @param executableModule The module to run.
     */
    private void runGeneratedExecutable(BLangPackage executableModule) {
        String balHome = Objects.requireNonNull(System.getProperty("ballerina.home"),
                "ballerina.home is not set");
        JBallerinaInMemoryClassLoader classLoader;
        try {
            Path targetDirectory = Files.createTempDirectory("ballerina-compile").toAbsolutePath();
            classLoader = BootstrapRunner.createClassLoaders(executableModule,
                    Paths.get(balHome).resolve("bir-cache"),
                    targetDirectory,  Optional.empty(), false);
        } catch (IOException e) {
            throw new BLangCompilerException("error invoking jballerina backend", e);
        }
    
        String initClassName = BFileUtil.getQualifiedClassName(executableModule.packageID.orgName.value,
                executableModule.packageID.name.value,
                MODULE_INIT_CLASS_NAME);
        
        try {
            Class<?> initClazz = classLoader.loadClass(initClassName);
            Method mainMethod = initClazz.getDeclaredMethod(JAVA_MAIN, String[].class);
            mainMethod.invoke(null, (Object) this.args);
            if (!initClazz.getField("serviceEPAvailable").getBoolean(initClazz)) {
                Runtime.getRuntime().exit(0);
            }
        } catch (NoSuchMethodException e) {
            throw createLauncherException("main method cannot be found for init class " + initClassName);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw createLauncherException("invoking main method failed due to " + e.getMessage());
        } catch (InvocationTargetException | NoSuchFieldException e) {
            throw createLauncherException("invoking main method failed due to " + e.getCause());
        }
        
    }
    
    /**
     * Run a given executable .jar file.
     */
    private void runExecutable() {
        String initClassName = null;
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] { this.executablePath.toUri().toURL() },
                    ClassLoader.getSystemClassLoader());
            
            initClassName = getModuleInitClassName(this.executablePath);
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
            throw createLauncherException("invoking main method failed due to " + e.getCause());
        }
    }
    
    /**
     * Get the clazz name with the main method. The name of the clazz is found through the Manifest file in the .jar.
     *
     * @param executablePath The path to the executable .jar file.
     * @return The name of the clazz
     */
    private static String getModuleInitClassName(Path executablePath) {
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream((executablePath.toString())))) {
            Manifest mf = jarStream.getManifest();
            Attributes attributes = mf.getMainAttributes();
            String initClassName = attributes.getValue(MAIN_CLASS_MANIFEST_ENTRY);
            if (initClassName == null) {
                throw createLauncherException("Main-class manifest entry cannot be found in the jar.");
            }
            return initClassName.replaceAll("/", ".");
        } catch (IOException e) {
            throw createLauncherException("error while getting init class name from manifest due to " + e.getMessage());
        }
    }
    
    /**
     * Initializes the {@link ConfigRegistry} and loads {@link LogManager} configs.
     *
     * @param sourceRootPath source directory
     * @param runtimeParams  run time parameters
     * @param configFilePath config file path
     * @param observeFlag    to indicate whether observability is enabled
     */
    public static void loadConfigurations(Path sourceRootPath, Map<String, String> runtimeParams,
                                          String configFilePath, boolean observeFlag) {
        Path ballerinaConfPath = sourceRootPath.resolve("ballerina.conf");
        try {
            ConfigRegistry.getInstance().initRegistry(runtimeParams, configFilePath, ballerinaConfPath);
            ((BLogManager) LogManager.getLogManager()).loadUserProvidedLogConfiguration();
            
            if (observeFlag) {
                ConfigRegistry.getInstance()
                        .addConfiguration(ObservabilityConstants.CONFIG_METRICS_ENABLED, Boolean.TRUE);
                ConfigRegistry.getInstance()
                        .addConfiguration(ObservabilityConstants.CONFIG_TRACING_ENABLED, Boolean.TRUE);
            }
            
        } catch (IOException e) {
            throw new BLangRuntimeException(
                    "failed to read the specified configuration file: " + ballerinaConfPath.toString(), e);
        } catch (RuntimeException e) {
            throw new BLangRuntimeException(e.getMessage(), e);
        }
    }
}
