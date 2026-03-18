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
 * Data class representing the package information nested within a connector search entry.
 *
 * @since 2201.13.2
 */
public class ConnectorPackageInfo {
    private String id;
    private String organization;
    private String name;
    private String version;
    private String platform;
    private String languageSpecificationVersion;
    private boolean isDeprecated;
    private String deprecateMessage;
    private String url;
    private String balaVersion;
    private String balaURL;
    private String digest;
    private String summary;
    private boolean template;
    private List<String> licenses;
    private List<String> authors;
    private String sourceCodeLocation;
    private List<String> keywords;
    private String ballerinaVersion;
    private String icon;
    private String ownerUUID;
    private long createdDate;
    private long pullCount;
    private String visibility;
    private String balToolId;
    private String graalvmCompatible;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLanguageSpecificationVersion() {
        return languageSpecificationVersion;
    }

    public void setLanguageSpecificationVersion(String languageSpecificationVersion) {
        this.languageSpecificationVersion = languageSpecificationVersion;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public void setDeprecated(boolean deprecated) {
        isDeprecated = deprecated;
    }

    public String getDeprecateMessage() {
        return deprecateMessage;
    }

    public void setDeprecateMessage(String deprecateMessage) {
        this.deprecateMessage = deprecateMessage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBalaVersion() {
        return balaVersion;
    }

    public void setBalaVersion(String balaVersion) {
        this.balaVersion = balaVersion;
    }

    public String getBalaURL() {
        return balaURL;
    }

    public void setBalaURL(String balaURL) {
        this.balaURL = balaURL;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public List<String> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<String> licenses) {
        this.licenses = licenses;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getSourceCodeLocation() {
        return sourceCodeLocation;
    }

    public void setSourceCodeLocation(String sourceCodeLocation) {
        this.sourceCodeLocation = sourceCodeLocation;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerinaVersion = ballerinaVersion;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getPullCount() {
        return pullCount;
    }

    public void setPullCount(long pullCount) {
        this.pullCount = pullCount;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getBalToolId() {
        return balToolId;
    }

    public void setBalToolId(String balToolId) {
        this.balToolId = balToolId;
    }

    public String getGraalvmCompatible() {
        return graalvmCompatible;
    }

    public void setGraalvmCompatible(String graalvmCompatible) {
        this.graalvmCompatible = graalvmCompatible;
    }

    @Override
    public String toString() {
        return "ConnectorPackageInfo{" +
                "id='" + id + '\'' +
                ", organization='" + organization + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", platform='" + platform + '\'' +
                ", isDeprecated=" + isDeprecated +
                ", summary='" + summary + '\'' +
                ", ballerinaVersion='" + ballerinaVersion + '\'' +
                ", pullCount=" + pullCount +
                '}';
    }
}
