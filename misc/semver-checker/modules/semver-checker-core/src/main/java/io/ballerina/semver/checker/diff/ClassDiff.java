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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the diff in between two versions of a Ballerina class definition.
 *
 * @since 2201.2.0
 */
public class ClassDiff extends NodeDiffImpl<ClassDefinitionNode> {

    private ClassDiff(ClassDefinitionNode newNode, ClassDefinitionNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public void computeVersionImpact() {
        boolean isPublic = isPublic();
        if (newNode != null && oldNode == null) {
            // if the class is newly added
            versionImpact = isPublic ? SemverImpact.MINOR : SemverImpact.PATCH;
        } else if (newNode == null && oldNode != null) {
            // if the class is removed
            versionImpact = isPublic ? SemverImpact.MAJOR : SemverImpact.PATCH;
        } else {
            // if the class is modified, checks if class definition is public and if its not, all the
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
    public static class Builder extends NodeDiffImpl.Builder<ClassDefinitionNode> {

        private final ClassDiff moduleConstDiff;

        public Builder(ClassDefinitionNode newNode, ClassDefinitionNode oldNode) {
            super(newNode, oldNode);
            moduleConstDiff = new ClassDiff(newNode, oldNode);
        }

        @Override
        public Optional<ClassDiff> build() {
            moduleConstDiff.setKind(DiffKind.CLASS);
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
