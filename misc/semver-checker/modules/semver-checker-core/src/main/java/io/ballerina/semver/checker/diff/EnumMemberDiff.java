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

import io.ballerina.compiler.syntax.tree.EnumMemberNode;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the diff in between two versions of a Ballerina enum member declaration.
 *
 * @since 2201.2.0
 */
public class EnumMemberDiff extends NodeDiffImpl<EnumMemberNode> {

    private EnumMemberDiff(EnumMemberNode newNode, EnumMemberNode oldNode) {
        super(newNode, oldNode, DiffKind.ENUM_MEMBER);
    }

    /**
     * Enum member diff builder implementation.
     */
    public static class Builder extends NodeDiffImpl.Builder<EnumMemberNode> {

        private final EnumMemberDiff enumDiff;

        public Builder(EnumMemberNode newNode, EnumMemberNode oldNode) {
            super(newNode, oldNode);
            enumDiff = new EnumMemberDiff(newNode, oldNode);
        }

        @Override
        public Optional<EnumMemberDiff> build() {
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
