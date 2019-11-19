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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.testerina.util.TestarinaClassLoader;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Task for executing tests.
 */
public class RunTestsTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);

        Map<BLangPackage, TestarinaClassLoader> programFileMap = new HashMap<>();
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        for (BLangPackage bLangPackage : moduleBirMap) {
            // todo following is some legacy logic check if we need to do this.
            // if (bLangPackage.containsTestablePkg()) {
            // } else {
            // In this package there are no tests to be executed. But we need to say to the users that
            // there are no tests found in the package to be executed as :
            // Running tests
            //     <org-name>/<package-name>:<version>
            //         No tests found
            // }
            Path jarPath = buildContext.getTestJarPathFromTargetCache(bLangPackage.packageID);
            Path modulejarPath = buildContext.getJarPathFromTargetCache(bLangPackage.packageID).getFileName();
            // subsitute test jar if module jar if tests not exists
            if (Files.notExists(jarPath)) {
                jarPath = modulejarPath;
            }
            // find the set of dependency jar paths for running test for this module
            HashSet<Path> dependencyJarPaths = findDependencyJarPaths(bLangPackage, buildContext);
            // Create a class loader to run tests.
            TestarinaClassLoader classLoader = new TestarinaClassLoader(jarPath, dependencyJarPaths);
            programFileMap.put(bLangPackage, classLoader);
        }
        if (programFileMap.size() > 0) {
            TesterinaUtils.executeTests(sourceRootPath, programFileMap, buildContext.out(), buildContext.err());
        }
    }

    private HashSet<Path> findDependencyJarPaths(BLangPackage bLangPackage, BuildContext buildContext) {

        HashSet<Path> moduleDependencies = buildContext.moduleDependencyPathMap.get(bLangPackage.packageID).
                platformLibs;
        // create a new set so that original set is not affected with test dependencies
        HashSet<Path> dependencyJarPaths = new HashSet<>(moduleDependencies);

        if (!bLangPackage.containsTestablePkg()) {
            return dependencyJarPaths;
        }
        // add test dependency jars also to the dependency set, if it exists
        BLangTestablePackage testablePackage = bLangPackage.getTestablePkg();
        for (BLangImportPackage importPackage : testablePackage.imports) {
            PackageID importPkgId = importPackage.symbol.pkgID;
            if (!buildContext.moduleDependencyPathMap.containsKey(importPkgId)) {
                continue;
            }

            // add imported modules dependency jar paths
            HashSet<Path> testDependencies = buildContext.moduleDependencyPathMap.get(importPkgId).platformLibs;
            dependencyJarPaths.addAll(testDependencies);

            // add imported modules jar path
            Path jarPath = buildContext.getTestJarPathFromTargetCache(importPkgId);
            Path moduleJarPath = buildContext.getJarPathFromTargetCache(importPkgId).getFileName();
            if (Files.exists(jarPath)) {
                dependencyJarPaths.add(jarPath);
            } else {
                dependencyJarPaths.add(moduleJarPath);
            }
        }
        return dependencyJarPaths;
    }
}
