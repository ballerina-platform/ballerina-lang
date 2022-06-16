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
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_KEYWORD;

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
        if (isRemote() || isResource()) {
            super.computeVersionImpact();
            return;
        }

        boolean isPublic = isPublic();
        if (newNode != null && oldNode == null) {
            // if the function is newly added
            versionImpact = isPublic ? SemverImpact.MINOR : SemverImpact.PATCH;
        } else if (newNode == null && oldNode != null) {
            // if the function is removed
            versionImpact = isPublic ? SemverImpact.MAJOR : SemverImpact.PATCH;
        } else {
            // if the function is modified, checks if function definition is public and if its not, all the
            // children-level changes can be considered as patch-compatible changes.
            if (isPublic) {
                super.computeVersionImpact();
            } else {
                versionImpact = SemverImpact.PATCH;
            }
        }
    }

    private boolean isPublic() {
        boolean isNewPublic = newNode != null && newNode.qualifierList().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);
        boolean isOldPublic = oldNode != null && oldNode.qualifierList().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);

        return isNewPublic || isOldPublic;
    }

    /**
     * Indicates whether the counterpart function is a Ballerina resource function.
     */
    public boolean isResource() {
        boolean isNewResource = newNode != null && newNode.qualifierList().stream().anyMatch(qualifier ->
                qualifier.kind() == RESOURCE_KEYWORD);
        boolean isOldResource = oldNode != null && oldNode.qualifierList().stream().anyMatch(qualifier ->
                qualifier.kind() == RESOURCE_KEYWORD);

        return isNewResource || isOldResource;
    }

    /**
     * Indicates whether the counterpart function is a Ballerina remote function.
     */
    public boolean isRemote() {
        boolean isNewRemote = newNode != null && newNode.qualifierList().stream().anyMatch(qualifier ->
                qualifier.kind() == REMOTE_KEYWORD);
        boolean isOldRemote = oldNode != null && oldNode.qualifierList().stream().anyMatch(qualifier ->
                qualifier.kind() == REMOTE_KEYWORD);

        return isNewRemote || isOldRemote;
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
            if (functionDiff.isRemote()) {
                functionDiff.setKind(DiffKind.REMOTE_FUNCTION);
            } else if (functionDiff.isResource()) {
                functionDiff.setKind(DiffKind.RESOURCE_FUNCTION);
            } else {
                functionDiff.setKind(DiffKind.FUNCTION);
            }
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
        public NodeDiffBuilder withKind(DiffKind diffKind) {
            functionDiff.setKind(diffKind);
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
