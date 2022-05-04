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

import java.util.Collection;
import java.util.Optional;

import static io.ballerina.semver.checker.util.PackageUtils.QUALIFIER_PUBLIC;

/**
 * Represents the diff in between two versions of a Ballerina function definition.
 *
 * @since 2201.2.0
 */
public class FunctionDiff extends NodeDiffImpl<FunctionDefinitionNode> {

    private FunctionDiff(FunctionDefinitionNode newNode, FunctionDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public void computeVersionImpact() {
        if (newNode != null && oldNode == null) {
            // if the function is newly added
            versionImpact = isPrivateFunction() ? SemverImpact.PATCH : SemverImpact.MINOR;
        } else if (newNode == null && oldNode != null) {
            // if the function is removed
            versionImpact = isPrivateFunction() ? SemverImpact.PATCH : SemverImpact.MAJOR;
        } else {
            // if the function is modified, checks if function definition is non-public and if so all the
            // children-level incompatibilities can be discarded.
            if (isPrivateFunction()) {
                versionImpact = SemverImpact.PATCH;
            } else {
                super.computeVersionImpact();
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

    /**
     * Function diff builder implementation.
     */
    public static class Builder extends NodeDiffImpl.Builder<FunctionDefinitionNode> {

        private final FunctionDiff functionDiff;

        public Builder(FunctionDefinitionNode newNode, FunctionDefinitionNode oldNode) {
            super(newNode, oldNode);
            functionDiff = new FunctionDiff(newNode, oldNode);
        }

        @Override
        public Optional<FunctionDiff> build() {
            if (!functionDiff.getChildDiffs().isEmpty()) {
                functionDiff.computeVersionImpact();
                functionDiff.setType(DiffType.MODIFIED);
                return Optional.of(functionDiff);
            } else if (functionDiff.getType() == DiffType.NEW || functionDiff.getType() == DiffType.REMOVED) {
                return Optional.of(functionDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            functionDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            functionDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            functionDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            functionDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            functionDiff.childDiffs.addAll(childDiffs);
            return this;
        }
    }
}
