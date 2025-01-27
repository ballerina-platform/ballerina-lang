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
public class LockingModeResolver {
    private final LockingModeResolutionOptions options;

    public LockingModeResolver(LockingModeResolutionOptions options) {
        this.options = options;
    }

    /**
     * Resolves the locking modes for the package based on the update policy and the package status.
     *
     * @return the resolved locking modes
     */
    public LockingModes resolveLockingModes() {
        if (!options.hasDependencyManifest()) {
            return resolveNoManifestLockingMode();
        }
        if (options.distributionChange()) {
            return resolveDistributionChangeLockingMode();
        }
        if (options.importAddition()) {
            return resolveImportAdditionLockingMode();
        }
        if (options.lessThan24HrsAfterBuild()) {
            return new LockingModes(PackageLockingMode.LOCKED);
        }
        return resolveDefaultLockingMode();
    }

    public record LockingModes(
            PackageLockingMode existingDirectDepMode,
            PackageLockingMode existingTransitiveDepMode,
            PackageLockingMode newDirectDepMode,
            PackageLockingMode newTransitiveDepMode) {
        public LockingModes(PackageLockingMode existingDirectDepMode, PackageLockingMode existingTransDepMode) {
            this(existingDirectDepMode, existingTransDepMode, existingDirectDepMode, existingTransDepMode);
        }
        public LockingModes(PackageLockingMode mode) {
            this(mode, mode, mode, mode);
        }
    }

    private LockingModes resolveNoManifestLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new LockingModes(PackageLockingMode.LATEST, PackageLockingMode.SOFT);
            case MEDIUM -> new LockingModes(PackageLockingMode.LATEST, PackageLockingMode.MEDIUM);
            case HARD -> new LockingModes(PackageLockingMode.LATEST, PackageLockingMode.HARD);
            case LOCKED -> new LockingModes(PackageLockingMode.INVALID);
        };
    }

    private LockingModes resolveDistributionChangeLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new LockingModes(PackageLockingMode.SOFT);
            case MEDIUM, HARD -> new LockingModes(PackageLockingMode.MEDIUM);
            case LOCKED -> new LockingModes(PackageLockingMode.LOCKED);
        };
    }

    private LockingModes resolveImportAdditionLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new LockingModes(PackageLockingMode.SOFT, PackageLockingMode.SOFT,
                    PackageLockingMode.LATEST, PackageLockingMode.SOFT);
            case MEDIUM -> new LockingModes(PackageLockingMode.MEDIUM, PackageLockingMode.MEDIUM,
                    PackageLockingMode.LATEST, PackageLockingMode.MEDIUM);
            case HARD -> new LockingModes(PackageLockingMode.HARD, PackageLockingMode.HARD,
                    PackageLockingMode.LATEST, PackageLockingMode.HARD);
            case LOCKED -> new LockingModes(PackageLockingMode.INVALID);
        };
    }

    private LockingModes resolveDefaultLockingMode() {
        return switch (options.updatePolicy()) {
            case SOFT -> new LockingModes(PackageLockingMode.SOFT);
            case MEDIUM -> new LockingModes(PackageLockingMode.MEDIUM);
            case HARD -> new LockingModes(PackageLockingMode.HARD);
            case LOCKED -> new LockingModes(PackageLockingMode.LOCKED);
        };
    }
}
