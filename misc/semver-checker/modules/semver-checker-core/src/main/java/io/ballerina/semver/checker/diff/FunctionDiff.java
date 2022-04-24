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

import static io.ballerina.semver.checker.util.DiffUtils.stringifyDiff;
import static io.ballerina.semver.checker.util.PackageUtils.QUALIFIER_PUBLIC;

/**
 * Represents the diff in between two versions of a Ballerina function definition.
 *
 * @since 2201.2.0
 */
public class FunctionDiff extends NodeDiffImpl<FunctionDefinitionNode> {

    public FunctionDiff(FunctionDefinitionNode newNode, FunctionDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public void computeCompatibilityLevel() {
        if (newNode != null && oldNode == null) {
            // if the function is newly added
            compatibilityLevel = isPrivateFunction() ? CompatibilityLevel.PATCH : CompatibilityLevel.MINOR;
        } else if (newNode == null && oldNode != null) {
            // if the function is removed
            compatibilityLevel = isPrivateFunction() ? CompatibilityLevel.PATCH : CompatibilityLevel.MAJOR;
        } else {
            // if the function is modified, checks if function definition is non-public and if so all the
            // children-level incompatibilities can be discarded.
            if (isPrivateFunction()) {
                compatibilityLevel = CompatibilityLevel.PATCH;
            } else {
                super.computeCompatibilityLevel();
            }
        }
    }

    private boolean isPrivateFunction() {
        boolean isNewPrivate = newNode != null && newNode.qualifierList().stream().noneMatch(qualifier ->
                qualifier.text().equals(QUALIFIER_PUBLIC));
        boolean isOldPrivate = oldNode != null && oldNode.qualifierList().stream().noneMatch(qualifier ->
                qualifier.text().equals(QUALIFIER_PUBLIC));

        return (isNewPrivate && isOldPrivate) || (newNode == null && isOldPrivate) || (oldNode == null && isNewPrivate);
    }

    @Override
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stringifyDiff(this));
        if (diffType == DiffType.MODIFIED && childDiffs != null) {
            childDiffs.forEach(diff -> sb.append(diff.getAsString()));
        }
        return sb.toString();
    }
}
