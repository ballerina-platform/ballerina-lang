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

import java.util.List;

/**
 * Data class representing a single package entry in Solr-based package search results from maven-metadata.xml.
 *
 * @since 2201.13.2
 */
public class PkgSearchSolrEntry {
    private long id;
    private String org;
    private String name;
    private String version;
    private String summary;
    private long createdDate;
    private List<String> authors;
    private String balToolId;
    private List<String> keywords;
    private long pullCount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getBalToolId() {
        return balToolId;
    }

    public void setBalToolId(String balToolId) {
        this.balToolId = balToolId;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public long getPullCount() {
        return pullCount;
    }

    public void setPullCount(long pullCount) {
        this.pullCount = pullCount;
    }

    @Override
    public String toString() {
        return "PkgSearchSolrEntry{" +
                "id=" + id +
                ", org='" + org + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", summary='" + summary + '\'' +
                ", createdDate=" + createdDate +
                ", authors=" + authors +
                ", balToolId='" + balToolId + '\'' +
                ", keywords=" + keywords +
                ", pullCount=" + pullCount +
                '}';
    }
}
