/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the diff in between two versions of a Ballerina enum declarations.
 *
 * @since 2201.2.0
 */
public class EnumDiff extends NodeDiffImpl<EnumDeclarationNode> {

    private EnumDiff(EnumDeclarationNode newNode, EnumDeclarationNode oldNode) {
        super(newNode, oldNode, DiffKind.ENUM);
    }

    @Override
    public void computeVersionImpact() {
        boolean isPublic = isPublic();
        if (newNode != null && oldNode == null) {
            // if the enum is newly added
            versionImpact = isPublic ? SemverImpact.MINOR : SemverImpact.PATCH;
        } else if (newNode == null && oldNode != null) {
            // if the enum is removed
            versionImpact = isPublic ? SemverImpact.MAJOR : SemverImpact.PATCH;
        } else {
            // if the enum is modified, checks if enum declaration is public and if its not, all the
            // children-level changes can be considered as patch-compatible changes.
            if (isPublic) {
                super.computeVersionImpact();
            } else {
                versionImpact = SemverImpact.PATCH;
            }
        }
    }

    private boolean isPublic() {
        boolean isNewPublic = newNode != null && newNode.qualifier().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);
        boolean isOldPublic = oldNode != null && oldNode.qualifier().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);

        return isNewPublic || isOldPublic;
    }

    /**
     * Enum declaration diff builder implementation.
     */
    public static class Builder extends NodeDiffImpl.Builder<EnumDeclarationNode> {

        private final EnumDiff enumDiff;

        public Builder(EnumDeclarationNode newNode, EnumDeclarationNode oldNode) {
            super(newNode, oldNode);
            enumDiff = new EnumDiff(newNode, oldNode);
        }

        @Override
        public Optional<EnumDiff> build() {
            if (!enumDiff.getChildDiffs().isEmpty()) {
                enumDiff.computeVersionImpact();
                enumDiff.setType(DiffType.MODIFIED);
                return Optional.of(enumDiff);
            } else if (enumDiff.getType() == DiffType.NEW || enumDiff.getType() == DiffType.REMOVED) {
                return Optional.of(enumDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            enumDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            enumDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            enumDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            enumDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            enumDiff.childDiffs.addAll(childDiffs);
            return this;
        }
    }
}
