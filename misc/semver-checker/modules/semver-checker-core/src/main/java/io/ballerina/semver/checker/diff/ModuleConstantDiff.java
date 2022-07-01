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

import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the diff in between two versions of a Ballerina module constant declaration.
 *
 * @since 2201.2.0
 */
public class ModuleConstantDiff extends NodeDiffImpl<ConstantDeclarationNode> {

    private ModuleConstantDiff(ConstantDeclarationNode newNode, ConstantDeclarationNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public void computeVersionImpact() {
        boolean isPublic = isPublic();
        if (newNode != null && oldNode == null) {
            // if the function is newly added
            versionImpact = isPublic ? SemverImpact.MINOR : SemverImpact.PATCH;
        } else if (newNode == null && oldNode != null) {
            // if the function is removed
            versionImpact = isPublic ? SemverImpact.MAJOR : SemverImpact.PATCH;
        } else {
            // if the constant is modified, checks if constant declaration is public and if its not, all the
            // children-level changes can be considered as patch-compatible changes.
            if (isPublic) {
                super.computeVersionImpact();
            } else {
                versionImpact = SemverImpact.PATCH;
            }
        }
    }

    private boolean isPublic() {
        boolean isNewPublic = newNode != null && newNode.visibilityQualifier().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);
        boolean isOldPublic = oldNode != null && oldNode.visibilityQualifier().stream().anyMatch(qualifier ->
                qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD);

        return isNewPublic || isOldPublic;
    }

    /**
     * Function diff builder implementation.
     */
    public static class Builder extends NodeDiffImpl.Builder<ConstantDeclarationNode> {

        private final ModuleConstantDiff moduleConstDiff;

        public Builder(ConstantDeclarationNode newNode, ConstantDeclarationNode oldNode) {
            super(newNode, oldNode);
            moduleConstDiff = new ModuleConstantDiff(newNode, oldNode);
        }

        @Override
        public Optional<ModuleConstantDiff> build() {
            moduleConstDiff.setKind(DiffKind.MODULE_CONST);
            if (!moduleConstDiff.getChildDiffs().isEmpty()) {
                moduleConstDiff.computeVersionImpact();
                moduleConstDiff.setType(DiffType.MODIFIED);
                return Optional.of(moduleConstDiff);
            } else if (moduleConstDiff.getType() == DiffType.NEW || moduleConstDiff.getType() == DiffType.REMOVED) {
                return Optional.of(moduleConstDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            moduleConstDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            moduleConstDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            moduleConstDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            moduleConstDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            moduleConstDiff.childDiffs.addAll(childDiffs);
            return this;
        }
    }
}
