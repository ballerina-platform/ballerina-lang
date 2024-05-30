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

package org.ballerinalang.central.client.model;

import java.util.List;

/**
 * Tool Resolution response.
 *
 * @since 2201.9.0
 */
public class ToolResolutionCentralResponse {

    List<ResolvedTool> resolved;
    List<UnresolvedTool> unresolved;

    private ToolResolutionCentralResponse(List<ResolvedTool> resolved, List<UnresolvedTool> unresolved) {
        this.resolved = resolved;
        this.unresolved = unresolved;
    }

    public static ToolResolutionCentralResponse from(List<ResolvedTool> resolved, List<UnresolvedTool> unresolved) {
        return new ToolResolutionCentralResponse(resolved, unresolved);
    }

    /**
     * Tool resolution response unresolved tool model.
     */
    public static class UnresolvedTool {
        private String id;
        private String version;

        public UnresolvedTool(String id, String version) {
            this.id = id;
            this.version = version;
        }

        public String id() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String version() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * Tool resolution response resolved tool model.
     */
    public static class ResolvedTool {
        private String id;
        private String version;
        private String name;
        private String orgName;

        public ResolvedTool(String id, String version, String name, String orgName) {
            this.id = id;
            this.version = version;
            this.name = name;
            this.orgName = orgName;
        }

        public String id() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String version() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String name() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String org() {
            return orgName;
        }

        public void setOrg(String orgName) {
            this.orgName = orgName;
        }
    }

    public List<ResolvedTool> resolved() {
        return resolved;
    }

    public List<UnresolvedTool> unresolved() {
        return unresolved;
    }
}
