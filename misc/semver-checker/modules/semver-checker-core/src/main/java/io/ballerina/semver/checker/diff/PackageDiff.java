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
import io.ballerina.projects.Package;
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

    private final Package newPackage;
    private final Package oldPackage;
    private final List<ModuleDiff> moduleDiffs = new ArrayList<>();

    public PackageDiff(Package newPackage, Package oldPackage) {
        this.newPackage = newPackage;
        this.oldPackage = oldPackage;
    }

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

    private String getPackageName() {
        switch (diffType) {
            case NEW:
                return newPackage.packageName().value();
            case REMOVED:
                return oldPackage.packageName().value();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (newPackage != null) {
                    return newPackage.packageName().value();
                } else if (oldPackage != null) {
                    return oldPackage.packageName().value();
                } else {
                    return "unknown";
                }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("compatibility: ").append(compatibilityLevel.toString()).append(", ");

        switch (diffType) {
            case NEW:
                sb.append("description: package '").append(getPackageName()).append("' is added")
                        .append(System.lineSeparator());
                break;
            case REMOVED:
                sb.append("description: package '").append(getPackageName()).append("' is removed")
                        .append(System.lineSeparator());
                break;
            case MODIFIED:
                sb.append("description: package '").append(getPackageName()).append("' is modified with " +
                        "the following changes").append(System.lineSeparator());
                if (childDiffs != null) {
                    childDiffs.forEach(diff -> sb.append(diff.toString()));
                }
            case UNKNOWN:
        }

        return sb.toString();
    }

    public static class Modifier implements DiffModifier {

        private final PackageDiff packageDiff;

        public Modifier(Package newPackage, Package oldPackage) {
            packageDiff = new PackageDiff(newPackage, oldPackage);
        }

        @Override
        public Optional<PackageDiff> modify() {
            if (!packageDiff.getChildDiffs().isEmpty()) {
                packageDiff.computeCompatibilityLevel();
                packageDiff.setType(DiffType.MODIFIED);
                return Optional.of(packageDiff);
            }

            return Optional.empty();
        }

        public void moduleAdded(Module module) {
            ModuleDiff moduleDiff = new ModuleDiff(module, null);
            moduleDiff.setType(DiffType.NEW);
            // Todo: decide whether this should be backward incompatible
            moduleDiff.setCompatibilityLevel(CompatibilityLevel.MINOR);
            packageDiff.addModuleDiff(moduleDiff);
        }

        public void moduleRemoved(Module module) {
            ModuleDiff moduleDiff = new ModuleDiff(null, module);
            moduleDiff.setType(DiffType.REMOVED);
            // Todo: decide whether this should be backward compatible
            moduleDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
            packageDiff.addModuleDiff(moduleDiff);
        }

        public void moduleChanged(Module newModule, Module oldModule) {
            Optional<ModuleDiff> moduleDiff = new ModuleComparator(newModule, oldModule).computeDiff();
            moduleDiff.ifPresent(packageDiff::addModuleDiff);
        }
    }
}
