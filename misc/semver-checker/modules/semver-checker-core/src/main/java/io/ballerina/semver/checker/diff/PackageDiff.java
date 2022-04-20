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

package io.ballerina.semver.checker.diff;

import io.ballerina.projects.Module;
import io.ballerina.semver.checker.comparator.ModuleComparator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PackageDiff extends Diff implements IPackageDiff {

    private final Map<String, ModuleDiff> moduleDiffs = new HashMap<>();

    @Override
    public void moduleAdded(Module module) {
        ModuleDiff moduleDiff = new ModuleDiff();
        moduleDiff.setType(DiffType.NEW);
        addModuleDiff(module.moduleName().toString(), moduleDiff);
    }

    @Override
    public void moduleRemoved(Module module) {
        ModuleDiff moduleDiff = new ModuleDiff();
        moduleDiff.setType(DiffType.REMOVED);
        addModuleDiff(module.moduleName().toString(), moduleDiff);
    }

    @Override
    public void moduleChanged(Module newModule, Module oldModule) {
        Optional<ModuleDiff> moduleDiff = new ModuleComparator(newModule, oldModule).computeDiff();
        moduleDiff.ifPresent(diff -> addModuleDiff(newModule.moduleName().toString(), diff));
    }

    @Override
    public DiffType getType() {
        return moduleDiffs.isEmpty() ? DiffType.MODIFIED : DiffType.UNKNOWN;
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        return moduleDiffs.values().stream()
                .map(Diff::getCompatibilityLevel)
                .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                .orElse(CompatibilityLevel.UNKNOWN);
    }

    private void addModuleDiff(String moduleName, ModuleDiff moduleDiff) {
        moduleDiffs.put(moduleName, moduleDiff);
    }
}
