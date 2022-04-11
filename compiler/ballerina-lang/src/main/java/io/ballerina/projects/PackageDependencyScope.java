/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

/**
 * Represents the scope of the dependency.
 * <p>
 * At the moment a package dependency can be a dependency for src and tests or tests only.
 *
 * @since 2.0.0
 */
public enum PackageDependencyScope {
    /**
     * These dependencies are available for both source and test sources.
     */
    DEFAULT(""),
    /**
     * These dependencies are only available for test sources.
     */
    TEST_ONLY("testOnly");

    private final String value;

    PackageDependencyScope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PackageDependencyScope fromString(String value) {
        if (value == null || value.isEmpty()) {
            return DEFAULT;
        } else if (TEST_ONLY.value.equals(value)) {
            return TEST_ONLY;
        } else {
            throw new IllegalStateException("Unsupported package dependency scope: `" + value + "`");
        }
    }
}
