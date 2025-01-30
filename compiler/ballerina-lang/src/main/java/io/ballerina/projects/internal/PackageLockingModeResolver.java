/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.projects.internal;

import io.ballerina.projects.environment.PackageLockingMode;

/**
 * This class resolves the locking modes for the packages based on the update policy and the package status.
 *
 * @since 2201.12.0
 */
public class PackageLockingModeResolver {
    private final PackageLockingModeResolutionOptions options;

    public PackageLockingModeResolver(PackageLockingModeResolutionOptions options) {
        this.options = options;
    }

    /**
     * Resolves the locking modes for the package based on the update policy and the package status.
     *
     * @return the resolved locking modes
     */
    public PackageLockingModeMatrix resolveLockingModes(boolean importAddition) {
        if (!options.hasDependencyManifest()) {
            return resolveNoManifestLockingMode();
        }
        if (options.distributionChange()) {
            return resolveDistributionChangeLockingMode();
        }
        if (importAddition) {
            return resolveImportAdditionLockingMode();
        }
        if (options.lessThan24HrsAfterBuild()) {
            return new PackageLockingModeMatrix(PackageLockingMode.LOCKED);
        }
        return resolveDefaultLockingMode();
    }

    private PackageLockingModeMatrix resolveNoManifestLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new PackageLockingModeMatrix(PackageLockingMode.LATEST, PackageLockingMode.SOFT);
            case MEDIUM -> new PackageLockingModeMatrix(PackageLockingMode.LATEST, PackageLockingMode.MEDIUM);
            case HARD -> new PackageLockingModeMatrix(PackageLockingMode.LATEST, PackageLockingMode.HARD);
            case LOCKED -> new PackageLockingModeMatrix(PackageLockingMode.INVALID);
        };
    }

    private PackageLockingModeMatrix resolveDistributionChangeLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new PackageLockingModeMatrix(PackageLockingMode.SOFT);
            case MEDIUM, HARD -> new PackageLockingModeMatrix(PackageLockingMode.MEDIUM);
            case LOCKED -> new PackageLockingModeMatrix(PackageLockingMode.LOCKED);
        };
    }

    private PackageLockingModeMatrix resolveImportAdditionLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new PackageLockingModeMatrix(PackageLockingMode.SOFT, PackageLockingMode.SOFT,
                    PackageLockingMode.LATEST, PackageLockingMode.SOFT);
            case MEDIUM -> new PackageLockingModeMatrix(PackageLockingMode.MEDIUM, PackageLockingMode.MEDIUM,
                    PackageLockingMode.LATEST, PackageLockingMode.MEDIUM);
            case HARD -> new PackageLockingModeMatrix(PackageLockingMode.HARD, PackageLockingMode.HARD,
                    PackageLockingMode.LATEST, PackageLockingMode.HARD);
            case LOCKED -> new PackageLockingModeMatrix(PackageLockingMode.INVALID);
        };
    }

    private PackageLockingModeMatrix resolveDefaultLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new PackageLockingModeMatrix(PackageLockingMode.SOFT);
            case MEDIUM -> new PackageLockingModeMatrix(PackageLockingMode.MEDIUM);
            case HARD -> new PackageLockingModeMatrix(PackageLockingMode.HARD);
            case LOCKED -> new PackageLockingModeMatrix(PackageLockingMode.LOCKED);
        };
    }
}
