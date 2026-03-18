/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
package org.ballerinalang.maven.bala.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing Solr-based package search results parsed from a Maven maven-metadata.xml file.
 *
 * @since 2201.13.2
 */
public class PkgSearchSolrMavenMetadata {
    private String groupId;
    private String artifactId;
    private int count;
    private int limit;
    private int offset;
    private List<PkgSearchSolrEntry> packages;

    public PkgSearchSolrMavenMetadata() {
        this.packages = new ArrayList<>();
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

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List<PkgSearchSolrEntry> getPackages() {
        return packages;
    }

    public void setPackages(List<PkgSearchSolrEntry> packages) {
        this.packages = packages;
    }

    @Override
    public String toString() {
        return "PkgSearchSolrMavenMetadata{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", count=" + count +
                ", limit=" + limit +
                ", offset=" + offset +
                ", packages=" + packages +
                '}';
    }
}
