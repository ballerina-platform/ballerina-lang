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

import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the diff in between two versions of a Ballerina module variable declaration.
 *
 * @since 2201.2.0
 */
public class ModuleVarDiff extends NodeDiffImpl<ModuleVariableDeclarationNode> {

    private ModuleVarDiff(ModuleVariableDeclarationNode newNode, ModuleVariableDeclarationNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public void computeVersionImpact() {
        boolean isPublic = isPublic();
        if (newNode != null && oldNode == null) {
            // if the module variable is newly added
            versionImpact = isPublic ? SemverImpact.MINOR : SemverImpact.PATCH;
        } else if (newNode == null && oldNode != null) {
            // if the module variable is removed
            versionImpact = isPublic ? SemverImpact.MAJOR : SemverImpact.PATCH;
        } else {
            // if the variable is modified, checks if variable declaration is public and if its not, all the
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
    public static class Builder extends NodeDiffImpl.Builder<ModuleVariableDeclarationNode> {

        private final ModuleVarDiff moduleVarDiff;

        public Builder(ModuleVariableDeclarationNode newNode, ModuleVariableDeclarationNode oldNode) {
            super(newNode, oldNode);
            moduleVarDiff = new ModuleVarDiff(newNode, oldNode);
        }

        @Override
        public Optional<ModuleVarDiff> build() {
            moduleVarDiff.setKind(DiffKind.MODULE_VAR);
            if (!moduleVarDiff.getChildDiffs().isEmpty()) {
                moduleVarDiff.computeVersionImpact();
                moduleVarDiff.setType(DiffType.MODIFIED);
                return Optional.of(moduleVarDiff);
            } else if (moduleVarDiff.getType() == DiffType.NEW || moduleVarDiff.getType() == DiffType.REMOVED) {
                return Optional.of(moduleVarDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            moduleVarDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            moduleVarDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            moduleVarDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            moduleVarDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            moduleVarDiff.childDiffs.addAll(childDiffs);
            return this;
        }
    }
}
