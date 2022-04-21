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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static io.ballerina.semver.checker.diff.NodeDiff.getUnifiedCompatibility;

/**
 * Represents all the source code changes within a single Ballerina module.
 *
 * @since 2201.2.0
 */
public class ModuleDiff extends Diff {

    private final Module newModule;
    private final Module oldModule;
    // Todo: Implement diff objects for other top-level constructs
    private final List<FunctionDiff> functionDiffs = new ArrayList<>();

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
        return Collections.unmodifiableList(functionDiffs);
    }

    public void addFunctionDiff(FunctionDiff functionDiff) {
        functionDiffs.add(functionDiff);
        childDiffs.add(functionDiff);
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        CompatibilityLevel funcCompatibility = functionDiffs.stream()
                .map(FunctionDiff::getCompatibilityLevel)
                .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                .orElse(CompatibilityLevel.UNKNOWN);

        // Todo: add other top-level definitions compatibilities

        return getUnifiedCompatibility(funcCompatibility);
    }

    public static class Modifier implements DiffModifier {

        private final ModuleDiff moduleDiff;

        public Modifier(Module newModule, Module oldModule) {
            moduleDiff = new ModuleDiff(newModule, oldModule);
        }

        @Override
        public ModuleDiff modify() {
            return moduleDiff;
        }

        public void functionAdded(FunctionDefinitionNode function) {
            FunctionDiff functionDiff = new FunctionDiff(function, null);
            functionDiff.setType(DiffType.NEW);
            moduleDiff.addFunctionDiff(functionDiff);
            moduleDiff.setType(DiffType.MODIFIED);
        }

        public void functionRemoved(FunctionDefinitionNode function) {
            FunctionDiff functionDiff = new FunctionDiff(null, function);
            functionDiff.setType(DiffType.REMOVED);
            moduleDiff.addFunctionDiff(functionDiff);
            moduleDiff.setType(DiffType.MODIFIED);
        }

        public void functionChanged(FunctionDefinitionNode newFunction, FunctionDefinitionNode oldFunction) {
            Optional<FunctionDiff> functionDiff = new FunctionComparator(newFunction, oldFunction).computeDiff();
            functionDiff.ifPresent(diff -> {
                moduleDiff.setType(DiffType.MODIFIED);
                moduleDiff.addFunctionDiff(diff);
            });
        }
    }
}
