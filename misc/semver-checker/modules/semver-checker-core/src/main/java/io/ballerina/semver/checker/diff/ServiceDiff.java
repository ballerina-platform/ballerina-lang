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

import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the diff in between two versions of a Ballerina service declaration.
 *
 * @since 2201.2.0
 */
public class ServiceDiff extends NodeDiffImpl<ServiceDeclarationNode> {

    private ServiceDiff(ServiceDeclarationNode newNode, ServiceDeclarationNode oldNode) {
        super(newNode, oldNode);
    }

    /**
     * Service diff builder implementation.
     */
    public static class Builder extends NodeDiffImpl.Builder<ServiceDeclarationNode> {

        private final ServiceDiff serviceDiff;

        public Builder(ServiceDeclarationNode newNode, ServiceDeclarationNode oldNode) {
            super(newNode, oldNode);
            serviceDiff = new ServiceDiff(newNode, oldNode);
        }

        @Override
        public Optional<ServiceDiff> build() {
            if (!serviceDiff.getChildDiffs().isEmpty()) {
                serviceDiff.computeVersionImpact();
                serviceDiff.setType(DiffType.MODIFIED);
                serviceDiff.setKind(DiffKind.SERVICE);
                return Optional.of(serviceDiff);
            } else if (serviceDiff.getType() == DiffType.NEW || serviceDiff.getType() == DiffType.REMOVED) {
                return Optional.of(serviceDiff);
            }

            return Optional.empty();
        }

        @Override
        public NodeDiffBuilder withType(DiffType diffType) {
            serviceDiff.setType(diffType);
            return this;
        }

        @Override
        public NodeDiffBuilder withVersionImpact(SemverImpact versionImpact) {
            serviceDiff.setVersionImpact(versionImpact);
            return this;
        }

        @Override
        public NodeDiffBuilder withMessage(String message) {
            serviceDiff.setMessage(message);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiff(Diff childDiff) {
            serviceDiff.childDiffs.add(childDiff);
            return this;
        }

        @Override
        public NodeDiffBuilder withChildDiffs(Collection<? extends Diff> childDiffs) {
            serviceDiff.childDiffs.addAll(childDiffs);
            return this;
        }
    }
}
