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

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.ballerinalang.util.JBallerinaInMemoryClassLoader;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for executing tests.
 */
public class RunTestsTask implements Task {
    private Path configPath = null;
    
    public RunTestsTask() {}
    
    public RunTestsTask(Path configPath) {
        if (null != configPath) {
            this.configPath = configPath;
        }
    }
    
    @Override
    public void execute(BuildContext buildContext) {
        // load configurations
        if (null != this.configPath) {
            Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
            loadConfigurations(sourceRootPath, this.configPath);
        }
    
        Map<BLangPackage, JBallerinaInMemoryClassLoader> programFileMap = new HashMap<>();
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        moduleBirMap.stream()
            .forEach(bLangPackage -> {
                // todo following is some legacy logic check if we need to do this.
                // if (bLangPackage.containsTestablePkg()) {
                // } else {
                    // In this package there are no tests to be executed. But we need to say to the users that
                    // there are no tests found in the package to be executed as :
                    // Running tests
                    //     <org-name>/<package-name>:<version>
                    //         No tests found
                // }
                Path jarPath = buildContext.getJarPathFromTargetCache(bLangPackage.packageID);
                JBallerinaInMemoryClassLoader classLoader = new JBallerinaInMemoryClassLoader(jarPath,
                        Paths.get(System.getProperty("ballerina.home"), "bre", "lib").toFile());
                programFileMap.put(bLangPackage, classLoader);
            });
        // Create a class loader to

        if (programFileMap.size() > 0) {
            TesterinaUtils.executeTests(sourceRootPath, programFileMap, buildContext.out(), buildContext.err());
        }
    }
    
    /**
     * Initializes the {@link ConfigRegistry} and loads {@link LogManager} configs.
     *
     * @param sourceRootPath source directory
     * @param configFilePath config file path
     */
    public static void loadConfigurations(Path sourceRootPath, Path configFilePath) {
        Path ballerinaConfPath = sourceRootPath.resolve("ballerina.conf");
        try {
            ConfigRegistry.getInstance().initRegistry(new LinkedHashMap<>(), configFilePath.toAbsolutePath().toString(),
                    ballerinaConfPath);
            ((BLogManager) LogManager.getLogManager()).loadUserProvidedLogConfiguration();
        } catch (IOException e) {
            throw createLauncherException("failed to read the specified configuration file: " +
                                          ballerinaConfPath.toString());
        } catch (RuntimeException e) {
            throw createLauncherException(e.getMessage());
        }
    }
}
