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

package io.ballerina.projects;

import java.util.Objects;

/**
 * Represents an id of a tool integrated with package build.
 *
 * @since 2201.9.0
 */
public class BuildToolId {
    private final String idStr;

    private BuildToolId(String idStr) {
        this.idStr = idStr;
    }

    /**
     * Create a build tool id instance.
     *
     * @param id build tool id
     * @return build tool id instance
     */
    public static BuildToolId from(String id) {
        return new BuildToolId(id);
    }

    /**
     * Get the id as a string.
     *
     * @return build tool id
     */
    public String value() {
        return idStr;
    }

    /**
     * Get the id as a string.
     *
     * @return build tool id
     */
    @Override
    public String toString() {
        return idStr;
    }

    /**
     * Check whether another tool id instance is equal to the current tool instance id.
     *
     * @return true if the tool id is equal to the current tool id
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BuildToolId that = (BuildToolId) other;
        return idStr.equals(that.idStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStr);
    }
}
