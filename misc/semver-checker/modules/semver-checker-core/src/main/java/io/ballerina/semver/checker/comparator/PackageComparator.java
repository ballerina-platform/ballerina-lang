/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.comparator;

import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.PackageDiff;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Comparator implementation for Ballerina packages.
 *
 * @since 2201.2.0
 */
public class PackageComparator implements Comparator {

    private final Package newPackage;
    private final Package oldPackage;

    public PackageComparator(Package newPackage, Package oldPackage) {
        this.newPackage = newPackage;
        this.oldPackage = oldPackage;
    }

    @Override
    public Optional<PackageDiff> computeDiff() {
        Map<String, Module> oldModules = new HashMap<>();
        Map<String, Module> newModules = new HashMap<>();
        newPackage.modules().forEach(module -> newModules.put(module.moduleName().toString(), module));
        oldPackage.modules().forEach(module -> oldModules.put(module.moduleName().toString(), module));

        DiffExtractor<Module> moduleDiffExtractor = new DiffExtractor<>(newModules, oldModules);
        PackageDiff.Builder pkgDiffBuilder = new PackageDiff.Builder(newPackage, oldPackage);
        moduleDiffExtractor.getAdditions().forEach((name, module) -> pkgDiffBuilder.withModuleAdded(module));
        moduleDiffExtractor.getRemovals().forEach((name, module) -> pkgDiffBuilder.withModuleRemoved(module));
        moduleDiffExtractor.getCommons().forEach((name, modules) -> pkgDiffBuilder.withModuleChanged(modules.getKey(),
                modules.getValue()));

        return pkgDiffBuilder.build();
    }
}
