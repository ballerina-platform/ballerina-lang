/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.packerina.utils;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

/**
 * Utilities related to test files.
 */
public class TestFileUtils {

    /**
     * Updates paths of dependency JARs for test execution.
     *
     * @param importPackageSymbols  list of import package symbols
     * @param buildContext build context
     * @param dependencyJarPaths paths of dependency jars
     */
    public static void updateDependencyJarPaths(List<BPackageSymbol> importPackageSymbols, BuildContext buildContext,
                                          HashSet<Path> dependencyJarPaths) {
        for (BPackageSymbol importPackageSymbol : importPackageSymbols) {
            PackageID importPkgId = importPackageSymbol.pkgID;
            if (!buildContext.moduleDependencyPathMap.containsKey(importPkgId)) {
                continue;
            }
            // Add the dependent JAR paths of the imported modules 
            HashSet<Path> testDependencies = buildContext.moduleDependencyPathMap.get(importPkgId).platformLibs;
            dependencyJarPaths.addAll(testDependencies);

            // Add the JAR path of the imported module
            Path testJarPath = buildContext.getTestJarPathFromTargetCache(importPkgId);
            Path moduleJarPath = buildContext.getJarPathFromTargetCache(importPkgId);
            if (Files.exists(testJarPath)) {
                dependencyJarPaths.add(testJarPath);
            } else if (Files.exists(moduleJarPath)) {
                dependencyJarPaths.add(moduleJarPath);
            }
            updateDependencyJarPaths(importPackageSymbol.imports, buildContext, dependencyJarPaths);
        }
    }
}
