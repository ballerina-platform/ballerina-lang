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

package org.ballerinalang.packerina.task;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.testerina.util.TestarinaClassLoader;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.packerina.utils.TestFileUtils.updateDependencyJarPaths;

/**
 * Task for listing groups defined in tests.
 *
 * @since 1.1.2
 */
public class ListTestGroupsTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);

        Map<BLangPackage, TestarinaClassLoader> programFileMap = new HashMap<>();
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Only tests in packages are executed so that the default packages (i.e. single BAL files),
        // which have the package name as "." are ignored. This is to be consistent with the "ballerina test"
        // command, which only executes tests in packages.
        for (BLangPackage bLangPackage : moduleBirMap) {
            PackageID packageID = bLangPackage.packageID;

            if (!buildContext.moduleDependencyPathMap.containsKey(packageID)) {
                continue;
            }

            // todo following is some legacy logic check if we need to do this.
            // if (bLangPackage.containsTestablePkg()) {
            // } else {
            // In this package there are no tests to be executed. However, we need to say to the users that
            // there are no tests found in the package to be executed as :
            // Running tests
            //     <org-name>/<package-name>:<version>
            //         No tests found
            // }
            Path jarPath = buildContext.getTestJarPathFromTargetCache(packageID);
            Path modulejarPath = buildContext.getJarPathFromTargetCache(packageID);
            // Substitute test JAR if the module JAR if tests not exists
            if (Files.notExists(jarPath)) {
                jarPath = modulejarPath;
            }

            HashSet<Path> moduleDependencies = buildContext.moduleDependencyPathMap.get(packageID).platformLibs;
            // Create a new set so that the original set is not affected with the test dependencies.
            HashSet<Path> dependencyJarPaths = new HashSet<>(moduleDependencies);

            if (bLangPackage.containsTestablePkg()) {
                for (BLangTestablePackage testablePackage : bLangPackage.getTestablePkgs()) {
                    // Find the set of dependency JAR paths for running the test for this module and update
                    updateDependencyJarPaths(testablePackage.symbol.imports, buildContext, dependencyJarPaths);
                }
            }

            // Create a class loader to run the tests.
            TestarinaClassLoader classLoader = new TestarinaClassLoader(jarPath, dependencyJarPaths);
            programFileMap.put(bLangPackage, classLoader);
        }
        if (programFileMap.size() > 0) {
            TesterinaUtils.listTestGroups(sourceRootPath, programFileMap, buildContext.out(), buildContext.err());
        }
    }
}
