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
import org.ballerinalang.packerina.NativeDependencyResolverImpl;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.model.ExecutableJar;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copy native libraries to target/tmp.
 */
public class CopyNativeLibTask implements Task {
    private boolean skipTests;
    private PackageCache packageCache;
    private NativeDependencyResolverImpl nativeDependencyResolverImpl;

    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        packageCache = PackageCache.getInstance(context);
        skipTests = buildContext.skipTests();

        nativeDependencyResolverImpl = buildContext.get(buildContext.get(BuildContextField.JAR_RESOLVER));
        copyImportedJarsForModules(buildContext, buildContext.getModules());
    }

    private void copyImportedJarsForModules(BuildContext buildContext, List<BLangPackage> moduleBirMap) {
        // Iterate through the imports and copy dependencies.
        HashSet<PackageID> alreadyImportedSet = new HashSet<>();
        for (BLangPackage pkg : moduleBirMap) {
            PackageID packageID = pkg.packageID;
            BLangPackage bLangPackage = packageCache.get(packageID);
            if (bLangPackage == null || !buildContext.moduleDependencyPathMap.containsKey(packageID)) {
                continue;
            }
            // Copy native libs for modules
            ExecutableJar executableJar = buildContext.moduleDependencyPathMap.get(packageID);
            copyPlatformLibsForModules(packageID, executableJar);
            copyImportedLibs(bLangPackage.symbol.imports, executableJar.moduleLibs, buildContext, alreadyImportedSet);
            if (skipTests || !bLangPackage.hasTestablePackage()) {
                continue;
            }
            // Copy native libs imported by testable package
            for (BLangPackage testPkg : bLangPackage.getTestablePkgs()) {
                if (!buildContext.moduleDependencyPathMap.containsKey(testPkg.packageID)) {
                    continue;
                }
                copyImportedLibs(testPkg.symbol.imports,
                        buildContext.moduleDependencyPathMap.get(testPkg.packageID).testLibs,
                        buildContext, alreadyImportedSet);
            }
        }
    }

    private void copyPlatformLibsForModules(PackageID packageID, ExecutableJar executableJar) {
        executableJar.moduleLibs.addAll(nativeDependencyResolverImpl.nativeDependencies(packageID));
        executableJar.testLibs.addAll(nativeDependencyResolverImpl.nativeDependenciesForTests(packageID));
    }

    private void copyImportedLibs(List<BPackageSymbol> imports, Set<Path> moduleDependencySet,
                                  BuildContext buildContext, HashSet<PackageID> alreadyImportedSet) {
        for (BPackageSymbol importSymbol : imports) {
            PackageID pkgId = importSymbol.pkgID;
            ExecutableJar jar = buildContext.moduleDependencyPathMap.get(pkgId);
            if (!alreadyImportedSet.contains(pkgId)) {
                alreadyImportedSet.add(pkgId);
                if (jar == null) {
                    jar = new ExecutableJar();
                    buildContext.moduleDependencyPathMap.put(pkgId, jar);
                }
                jar.moduleLibs.addAll(nativeDependencyResolverImpl.nativeDependencies(pkgId));
                copyImportedLibs(importSymbol.imports, jar.moduleLibs, buildContext, alreadyImportedSet);
            }
            moduleDependencySet.addAll(jar.moduleLibs);
        }
    }
}
