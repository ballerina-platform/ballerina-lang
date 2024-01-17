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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the diff in between two versions of a Ballerina type definition.
 *
 * @since 2201.2.0
 */
public class TypeDefinitionDiff extends NodeDiffImpl<TypeDefinitionNode> {

    private TypeDefinitionDiff(TypeDefinitionNode newNode, TypeDefinitionNode oldNode) {
        super(newNode, oldNode, DiffKind.TYPE_DEFINITION);
    }

    @Override
    public void computeVersionImpact() {
        boolean isPublic = isPublic();
        if (newNode != null && oldNode == null) {
            // if the type is newly added
            versionImpact = isPublic ? SemverImpact.MINOR : SemverImpact.PATCH;
        } else if (newNode == null && oldNode != null) {
            // if the type is removed
            versionImpact = isPublic ? SemverImpact.MAJOR : SemverImpact.PATCH;
        } else {
            // if the type is modified, checks if type definition is public and if its not, all the
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
     * Type definition diff builder implementation.
     */
    public static class Builder extends NodeDiffImpl.Builder<TypeDefinitionNode> {

        private final TypeDefinitionDiff typeDefDiff;

        public Builder(TypeDefinitionNode newNode, TypeDefinitionNode oldNode) {
            super(newNode, oldNode);
            typeDefDiff = new TypeDefinitionDiff(newNode, oldNode);
        }

        @Override
        public Optional<TypeDefinitionDiff> build() {
            if (!typeDefDiff.getChildDiffs().isEmpty()) {
                typeDefDiff.computeVersionImpact();
                typeDefDiff.setType(DiffType.MODIFIED);
                return Optional.of(typeDefDiff);
            } else if (typeDefDiff.getType() == DiffType.NEW || typeDefDiff.getType() == DiffType.REMOVED) {
                return Optional.of(typeDefDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            typeDefDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            typeDefDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            typeDefDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            typeDefDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            typeDefDiff.childDiffs.addAll(childDiffs);
            return this;
        }
    }
}
