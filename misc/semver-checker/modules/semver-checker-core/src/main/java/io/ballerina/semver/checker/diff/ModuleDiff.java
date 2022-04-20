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
import io.ballerina.semver.checker.comparator.FunctionComparator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.semver.checker.diff.NodeDiff.getUnifiedCompatibility;

public class ModuleDiff extends Diff implements IModuleDiff {

    // Todo: Implement diff objects for other top-level constructs
    private final Map<String, FunctionDiff> functionDiffs = new HashMap<>();

    @Override
    public void functionAdded(FunctionDefinitionNode function) {
        FunctionDiff moduleDiff = new FunctionDiff(function, null);
        moduleDiff.setType(DiffType.NEW);
        addFunctionDiff(function.functionName().text(), moduleDiff);
    }

    @Override
    public void functionRemoved(FunctionDefinitionNode function) {
        FunctionDiff moduleDiff = new FunctionDiff(null, function);
        moduleDiff.setType(DiffType.REMOVED);
        addFunctionDiff(function.functionName().text(), moduleDiff);
    }

    @Override
    public void functionChanged(FunctionDefinitionNode newFunction, FunctionDefinitionNode oldFunction) {
        Optional<FunctionDiff> functionDiff = new FunctionComparator(newFunction, oldFunction).computeDiff();
        functionDiff.ifPresent(diff -> addFunctionDiff(newFunction.functionName().text(), diff));
    }

    public void addFunctionDiff(String functionName, FunctionDiff functionDiff) {
        functionDiffs.put(functionName, functionDiff);
    }

    @Override
    public CompatibilityLevel getCompatibilityLevel() {
        CompatibilityLevel funcCompatibility = functionDiffs.values().stream()
                .map(FunctionDiff::getCompatibilityLevel)
                .max(Comparator.comparingInt(CompatibilityLevel::getRank))
                .orElse(CompatibilityLevel.UNKNOWN);

        // Todo: add other top-level definitions compatibilities

        return getUnifiedCompatibility(funcCompatibility);
    }
}
