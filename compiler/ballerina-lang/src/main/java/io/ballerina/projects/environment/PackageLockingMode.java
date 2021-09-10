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
     */
    SOFT,
    /**
     * Summary:
     * major never
     * minor as needed
     * patch always
     *
     * Locks to major versions of dependencies.
     *
     * For every dependency we always update to latest patch version
     * (not conservative about patch versions)
     *
     * When different dependencies require different minor versions
     * of a library, we take the least minor version needed to satisfy
     * all requirements
     *
     * Flag allows upgrade to latest minor version available overriding
     * the need question
     */
    MEDIUM,
    /**
     * Locks to exact major.minor.patch versions of dependencies.
     */
    HARD
}
