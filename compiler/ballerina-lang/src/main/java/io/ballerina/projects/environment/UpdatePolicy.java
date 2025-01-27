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

package io.ballerina.projects.environment;

/**
 * Represents the dependency update policy of a package.
 *
 * @since 2201.12.0
 */
public enum UpdatePolicy {
    /**
     * LATEST update policy allows updating the package to the latest version within the given major version range.
     */
    SOFT,

    /**
     * MEDIUM update policy allows updating the package to the latest version within the given major.minor version range.
     */
    MEDIUM,

    /**
     * HARD update policy sticks to the given major.minor.patch version of the package. However, if there is a
     * compatible higher minor/ patch version available in the graphs, use the higher version.
     */
    HARD,

    /**
     * LOCKED update policy sticks to the exact version of the package. This will be not allowed if at least one of the
     * following conditions are not met.
     * 1. Dependency conflicts are present.
     * 2. Dependencies.toml is not available.
     * 3. Not all dependencies are locked in the Dependencies.toml.
     */
    LOCKED
}
