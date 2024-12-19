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

import io.ballerina.projects.environment.LockingMode;
import io.ballerina.projects.environment.UpdatePolicy;

public class LockingModeResolver {
    // TODO: may be we can encapsulate these fields in a new class LockingModeResolutionOptions
    private final UpdatePolicy updatePolicy;
    private boolean hasDependencyManifest;
    private final boolean distributionChange;
    private final boolean importAddition;
    private final boolean lessThan24HrsAfterBuild;

    public LockingModeResolver(
            UpdatePolicy updatePolicy,
            boolean hasDependencyManifest,
            boolean distributionChange,
            boolean importAddition,
            boolean lessThan24HrsAfterBuild
    ) {
        this.updatePolicy = updatePolicy;
        this.hasDependencyManifest = hasDependencyManifest;
        this.distributionChange = distributionChange;
        this.importAddition = importAddition;
        this.lessThan24HrsAfterBuild = lessThan24HrsAfterBuild;
    }

    public LockingModes resolveLockingModes() {
        if (!hasDependencyManifest) {
            return resolveNoManifestLockingMode();
        }
        if (distributionChange) {
            return resolveDistributionChangeLockingMode();
        }
        if (importAddition) {
            return resolveImportAdditionLockingMode();
        }
        if (lessThan24HrsAfterBuild) {
            return new LockingModes(LockingMode.LOCKED);
        }
        return resolveDefaultLockingMode();
    }

    public record LockingModes(
            LockingMode existingDirectDepMode,
            LockingMode existingTransitiveDepMode,
            LockingMode newDirectDepMode,
            LockingMode newTransitiveDepMode) {
        public LockingModes(LockingMode existingDirectDepMode, LockingMode existingTransDepMode) {
            this(existingDirectDepMode, existingTransDepMode, existingDirectDepMode, existingTransDepMode);
        }
        public LockingModes(LockingMode mode) {
            this(mode, mode, mode, mode);
        }
    }

    private LockingModes resolveNoManifestLockingMode() {
        return switch (updatePolicy) {
            case SOFT -> new LockingModes(LockingMode.LATEST, LockingMode.SOFT);
            case MEDIUM -> new LockingModes(LockingMode.LATEST, LockingMode.MEDIUM);
            case HARD -> new LockingModes(LockingMode.LATEST, LockingMode.HARD);
            default -> new LockingModes(LockingMode.INVALID);
        };
    }

    private LockingModes resolveDistributionChangeLockingMode() {
        return switch (updatePolicy) {
            case SOFT -> new LockingModes(LockingMode.SOFT);
            case MEDIUM, HARD -> new LockingModes(LockingMode.MEDIUM);
            case LOCKED -> new LockingModes(LockingMode.LOCKED);
        };
    }

    private LockingModes resolveImportAdditionLockingMode() {
        return switch (updatePolicy) {
            case SOFT -> new LockingModes(LockingMode.SOFT, LockingMode.SOFT, LockingMode.LATEST, LockingMode.SOFT);
            case MEDIUM -> new LockingModes(LockingMode.MEDIUM, LockingMode.MEDIUM, LockingMode.LATEST, LockingMode.MEDIUM);
            case HARD -> new LockingModes(LockingMode.HARD, LockingMode.HARD, LockingMode.LATEST, LockingMode.HARD);
            case LOCKED -> new LockingModes(LockingMode.INVALID);
        };
    }

    private LockingModes resolveDefaultLockingMode() {
        return switch (updatePolicy) {
            case SOFT -> new LockingModes(LockingMode.SOFT);
            case MEDIUM -> new LockingModes(LockingMode.MEDIUM);
            case HARD -> new LockingModes(LockingMode.HARD);
            default -> new LockingModes(LockingMode.LOCKED);
        };
    }
}
