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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Represents all the source code changes within a single Ballerina package.
 *
 * @since 2201.2.0
 */
public class PackageDiff extends Diff {

    private final List<ModuleDiff> moduleDiffs = new ArrayList<>();

    public List<ModuleDiff> getModuleDiffs() {
        return Collections.unmodifiableList(moduleDiffs);
    }

    @Override
    public DiffType getType() {
        return childDiffs.isEmpty() ? DiffType.MODIFIED : DiffType.UNKNOWN;
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        return childDiffs.stream()
                .map(IDiff::getCompatibilityLevel)
                .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                .orElse(CompatibilityLevel.UNKNOWN);
    }

    public void addModuleDiff(ModuleDiff moduleDiff) {
        childDiffs.add(moduleDiff);
        moduleDiffs.add(moduleDiff);
    }

    public static class Modifier implements DiffModifier {

        private final PackageDiff packageDiff;

        public Modifier() {
            packageDiff = new PackageDiff();
        }

        @Override
        public PackageDiff modify() {
            return packageDiff;
        }

        public void moduleAdded(Module module) {
            ModuleDiff moduleDiff = new ModuleDiff();
            moduleDiff.setType(DiffType.NEW);
            packageDiff.addModuleDiff(moduleDiff);
        }

        public void moduleRemoved(Module module) {
            ModuleDiff moduleDiff = new ModuleDiff();
            moduleDiff.setType(DiffType.REMOVED);
            packageDiff.addModuleDiff(moduleDiff);
        }

        public void moduleChanged(Module newModule, Module oldModule) {
            Optional<ModuleDiff> moduleDiff = new ModuleComparator(newModule, oldModule).computeDiff();
            moduleDiff.ifPresent(packageDiff::addModuleDiff);
        }
    }
}
