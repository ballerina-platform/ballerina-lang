/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * Represents the locking modes for existing/new, direct/transitive dependencies.
 *
 * @since 2201.12.0
 *
 * @param existingDirectDepMode locking mode for existing direct dependencies
 * @param existingTransitiveDepMode locking mode for existing transitive dependencies
 * @param newDirectDepMode locking mode for new direct dependencies
 * @param newTransitiveDepMode locking mode for new transitive dependencies
 */
public record PackageLockingModeMatrix(
                                        PackageLockingMode existingDirectDepMode,
                                        PackageLockingMode existingTransitiveDepMode,
                                        PackageLockingMode newDirectDepMode,
                                        PackageLockingMode newTransitiveDepMode) {
    public PackageLockingModeMatrix(PackageLockingMode existingDirectDepMode, PackageLockingMode existingTransDepMode) {
        this(existingDirectDepMode, existingTransDepMode, existingDirectDepMode, existingTransDepMode);
    }

    public PackageLockingModeMatrix(PackageLockingMode mode) {
        this(mode, mode, mode, mode);
    }
}
