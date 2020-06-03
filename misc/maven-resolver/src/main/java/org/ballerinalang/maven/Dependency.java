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
package org.ballerinalang.maven;

import java.util.ArrayList;
import java.util.List;

/**
 * Dependency representation.
 */
public class Dependency {
    private String groupId;
    private String artifactId;
    private String version;

    List<Dependency> depedencies;

    public Dependency() {
        this.groupId = "";
        this.artifactId = "";
        this.version = "";
        this.depedencies = new ArrayList<>();
    }

    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.depedencies = new ArrayList<>();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public List<Dependency> getDepedencies() {
        return depedencies;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDepedencies(List<Dependency> depedencies) {
        this.depedencies = depedencies;
    }

    public void addDependency(Dependency dependency) {
        this.depedencies.add(dependency);
    }
}
