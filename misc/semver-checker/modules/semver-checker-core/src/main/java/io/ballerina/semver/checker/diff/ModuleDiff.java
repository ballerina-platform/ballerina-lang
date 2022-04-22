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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.projects.Module;
import io.ballerina.semver.checker.comparator.FunctionComparator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents all the source code changes within a single Ballerina module.
 *
 * @since 2201.2.0
 */
public class ModuleDiff extends DiffImpl {

    private final Module newModule;
    private final Module oldModule;

    public ModuleDiff(Module newModule, Module oldModule) {
        this.newModule = newModule;
        this.oldModule = oldModule;
    }

    public Module getNewModule() {
        return newModule;
    }

    public Module getOldModule() {
        return oldModule;
    }

    public List<FunctionDiff> getFunctionDiffs() {
        return childDiffs.stream().filter(iDiff -> iDiff instanceof FunctionDiff)
                .map(iDiff -> (FunctionDiff) iDiff)
                .collect(Collectors.toUnmodifiableList());
    }

    public void addFunctionDiff(FunctionDiff functionDiff) {
        childDiffs.add(functionDiff);
    }

    private String getModuleName() {
        switch (diffType) {
            case NEW:
                return newModule.moduleName().moduleNamePart();
            case REMOVED:
                return oldModule.moduleName().moduleNamePart();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (newModule != null) {
                    return newModule.moduleName().moduleNamePart();
                } else if (oldModule != null) {
                    return oldModule.moduleName().moduleNamePart();
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
                sb.append("description: module '").append(getModuleName()).append("' is added")
                        .append(System.lineSeparator());
                break;
            case REMOVED:
                sb.append("description: module '").append(getModuleName()).append("' is removed")
                        .append(System.lineSeparator());
                break;
            case MODIFIED:
                sb.append("description: module '").append(getModuleName()).append("' is modified with " +
                        "the following changes").append(System.lineSeparator());
                if (childDiffs != null) {
                    childDiffs.forEach(diff -> sb.append(diff.toString()));
                }
            case UNKNOWN:
        }

        return sb.toString();
    }

    public static class Modifier implements DiffModifier {

        private final ModuleDiff moduleDiff;

        public Modifier(Module newModule, Module oldModule) {
            moduleDiff = new ModuleDiff(newModule, oldModule);
        }

        @Override
        public Optional<ModuleDiff> modify() {
            if (!moduleDiff.getChildDiffs().isEmpty()) {
                moduleDiff.computeCompatibilityLevel();
                moduleDiff.setType(DiffType.MODIFIED);
                return Optional.of(moduleDiff);
            }

            return Optional.empty();
        }

        public void functionAdded(FunctionDefinitionNode function) {
            FunctionDiff functionDiff = new FunctionDiff(function, null);
            functionDiff.computeCompatibilityLevel();
            moduleDiff.addFunctionDiff(functionDiff);
            moduleDiff.setType(DiffType.MODIFIED);
        }

        public void functionRemoved(FunctionDefinitionNode function) {
            FunctionDiff functionDiff = new FunctionDiff(null, function);
            functionDiff.computeCompatibilityLevel();
            moduleDiff.addFunctionDiff(functionDiff);
            moduleDiff.setType(DiffType.MODIFIED);
        }

        public void functionChanged(FunctionDefinitionNode newFunction, FunctionDefinitionNode oldFunction) {
            Optional<FunctionDiff> functionDiff = new FunctionComparator(newFunction, oldFunction).computeDiff();
            functionDiff.ifPresent(diff -> {
                diff.computeCompatibilityLevel();
                moduleDiff.setType(DiffType.MODIFIED);
                moduleDiff.addFunctionDiff(diff);
            });
        }
    }
}
