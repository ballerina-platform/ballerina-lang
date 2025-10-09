/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.environment;

/**
 * Locking Mode for package resolution.
 *
 * @since 2.0.0
 */
public enum PackageLockingMode {
    /**
     * Locks to major versions of dependencies.
     * This is the default mode.
     */
    SOFT("soft"),
    /**
     * Summary:
     * major never
     * minor as needed
     * patch always
     * <p>
     * Locks to major and minor versions of dependencies.
     * <p>
     * For every dependency we always update to latest patch version
     * (not conservative about patch versions)
     *
     * When different dependencies require different minor versions
     * of a library, we take the least minor version needed to satisfy
     * all requirements
     *
     * Flag allows upgrade to latest minor version available overriding
     * the need question
     *
     */
    MEDIUM("medium"),
    /**
     * Locks to exact major.minor.patch versions of dependencies.
     * If a conflict is detected, if the versions are semver compatible,
     * the latest version is picked. Else, the build will fail.
     */
    HARD("hard"),
    /**
     * Locks to exact major.minor.patch versions of dependencies.
     * If a conflict is detected, the build will fail.
     */
    LOCKED("locked");

    final String lockingMode;

    PackageLockingMode(String lockingMode) {
        this.lockingMode = lockingMode;
    }

    public String value() {
        return lockingMode;
    }

    @Override
    public String toString() {
        return lockingMode;
    }

    public static PackageLockingMode parse(String value) {
        for (PackageLockingMode mode : PackageLockingMode.values()) {
            if (mode.lockingMode.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("expected one of [soft, medium, hard, locked]");
    }
}
