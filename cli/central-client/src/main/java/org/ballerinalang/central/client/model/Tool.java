/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.central.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code Tool} represents tool json from central.
 */
public class Tool {
    public static final String JSON_PROPERTY_ID = "balToolId";
    @SerializedName(JSON_PROPERTY_ID) private String balToolId;

    public static final String JSON_PROPERTY_ORGANIZATION = "organization";
    @SerializedName(JSON_PROPERTY_ORGANIZATION) private String organization;

    public static final String JSON_PROPERTY_NAME = "name";
    @SerializedName(JSON_PROPERTY_NAME) private String name;

    public static final String JSON_PROPERTY_VERSION = "version";
    @SerializedName(JSON_PROPERTY_VERSION) private String version;

    public static final String JSON_PROPERTY_PLATFORM = "platform";
    @SerializedName(JSON_PROPERTY_PLATFORM) private String platform;

    public static final String JSON_PROPERTY_LANGUAGE_SPECIFICATION_VERSION = "languageSpecificationVersion";
    @SerializedName(JSON_PROPERTY_LANGUAGE_SPECIFICATION_VERSION) private String languageSpecificationVersion;

    public static final String JSON_PROPERTY_IS_DEPRECATED = "isDeprecated";
    @SerializedName(JSON_PROPERTY_IS_DEPRECATED) private Boolean isDeprecated;

    public static final String JSON_PROPERTY_DEPRECATE_MESSAGE = "deprecateMessage";
    @SerializedName(JSON_PROPERTY_DEPRECATE_MESSAGE) private Boolean deprecateMessage;

    public static final String JSON_PROPERTY_U_R_L = "URL";
    @SerializedName(JSON_PROPERTY_U_R_L) private String url;

    public static final String JSON_PROPERTY_BALA_VERSION = "balaVersion";
    @SerializedName(JSON_PROPERTY_BALA_VERSION) private String balaVersion;

    public static final String JSON_PROPERTY_BALA_U_R_L = "balaURL";
    @SerializedName(JSON_PROPERTY_BALA_U_R_L) private String balaURL;

    public static final String JSON_PROPERTY_README = "readme";
    @SerializedName(JSON_PROPERTY_README) private String readme;

    public static final String JSON_PROPERTY_LICENSES = "licenses";
    @SerializedName(JSON_PROPERTY_LICENSES) private List<String> licenses = new ArrayList<>();

    public static final String JSON_PROPERTY_AUTHORS = "authors";
    @SerializedName(JSON_PROPERTY_AUTHORS) private List<String> authors = new ArrayList<>();

    public static final String JSON_PROPERTY_SOURCE_CODE_LOCATION = "sourceCodeLocation";
    @SerializedName(JSON_PROPERTY_SOURCE_CODE_LOCATION) private String sourceCodeLocation;

    public static final String JSON_PROPERTY_KEYWORDS = "keywords";
    @SerializedName(JSON_PROPERTY_KEYWORDS) private List<String> keywords = new ArrayList<>();

    public static final String JSON_PROPERTY_BALLERINA_VERSION = "ballerinaVersion";
    @SerializedName(JSON_PROPERTY_BALLERINA_VERSION) private String ballerinaVersion;

    public static final String JSON_PROPERTY_CREATED_DATE = "createdDate";
    @SerializedName(JSON_PROPERTY_CREATED_DATE) private Long createdDate;

    public static final String JSON_PROPERTY_MODULES = "modules";
    @SerializedName(JSON_PROPERTY_MODULES) private List<Module> modules = new ArrayList<>();

    public static final String JSON_PROPERTY_SUMMARY = "summary";
    @SerializedName(JSON_PROPERTY_SUMMARY) private String summary;

    public static final String JSON_PROPERTY_ICON = "icon";
    @SerializedName(JSON_PROPERTY_ICON) private String icon;

    public String getBalToolId() {
        return balToolId;
    }

    public void setBalToolId(String balToolId) {
        this.balToolId = balToolId;
    }

    public Tool balToolId(String balToolId) {
        this.balToolId = balToolId;
        return this;
    }

    public Tool organization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Tool name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tool version(String version) {

        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Tool platform(String platform) {

        this.platform = platform;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Tool languageSpecificationVersion(String languageSpecificationVersion) {

        this.languageSpecificationVersion = languageSpecificationVersion;
        return this;
    }

    public String getLanguageSpecificationVersion() {
        return languageSpecificationVersion;
    }

    public void setLanguageSpecificationVersion(String languageSpecificationVersion) {
        this.languageSpecificationVersion = languageSpecificationVersion;
    }

    public Boolean getDeprecated() {
        return isDeprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        isDeprecated = deprecated;
    }

    public Boolean getDeprecateMessage() {
        return deprecateMessage;
    }

    public void setDeprecateMessage(Boolean deprecateMessage) {
        this.deprecateMessage = deprecateMessage;
    }

    public Tool url(String url) {

        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Tool balaVersion(String balaVersion) {

        this.balaVersion = balaVersion;
        return this;
    }

    public String getBalaVersion() {
        return balaVersion;
    }

    public void setBalaVersion(String balaVersion) {
        this.balaVersion = balaVersion;
    }

    public Tool balaURL(String balaURL) {

        this.balaURL = balaURL;
        return this;
    }

    public String getBalaURL() {
        return balaURL;
    }

    public void setBalaURL(String balaURL) {
        this.balaURL = balaURL;
    }

    public Tool summary(String summary) {

        this.summary = summary;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Tool readme(String readme) {

        this.readme = readme;
        return this;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public Tool licenses(List<String> licenses) {

        this.licenses = licenses;
        return this;
    }

    public Tool addLicensesItem(String licensesItem) {
        this.licenses.add(licensesItem);
        return this;
    }

    public List<String> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<String> licenses) {
        this.licenses = licenses;
    }

    public Tool authors(List<String> authors) {

        this.authors = authors;
        return this;
    }

    public Tool addAuthorsItem(String authorsItem) {
        this.authors.add(authorsItem);
        return this;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public Tool sourceCodeLocation(String sourceCodeLocation) {

        this.sourceCodeLocation = sourceCodeLocation;
        return this;
    }

    public String getSourceCodeLocation() {
        return sourceCodeLocation;
    }

    public void setSourceCodeLocation(String sourceCodeLocation) {
        this.sourceCodeLocation = sourceCodeLocation;
    }

    public Tool keywords(List<String> keywords) {

        this.keywords = keywords;
        return this;
    }

    public Tool addKeywordsItem(String keywordsItem) {
        this.keywords.add(keywordsItem);
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Tool ballerinaVersion(String ballerinaVersion) {

        this.ballerinaVersion = ballerinaVersion;
        return this;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerinaVersion = ballerinaVersion;
    }

    public Tool createdDate(Long createdDate) {

        this.createdDate = createdDate;
        return this;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Tool modules(List<Module> modules) {

        this.modules = modules;
        return this;
    }

    public Tool addModulesItem(Module modulesItem) {
        this.modules.add(modulesItem);
        return this;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public Tool icon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tool packageJsonSchema = (Tool) o;
        return Objects.equals(this.balToolId, packageJsonSchema.balToolId)
                && Objects.equals(this.organization, packageJsonSchema.organization)
                && Objects.equals(this.name, packageJsonSchema.name)
                && Objects.equals(this.version, packageJsonSchema.version)
                && Objects.equals(this.platform, packageJsonSchema.platform)
                && Objects.equals(this.languageSpecificationVersion, packageJsonSchema.languageSpecificationVersion)
                && Objects.equals(this.url, packageJsonSchema.url)
                && Objects.equals(this.balaVersion, packageJsonSchema.balaVersion)
                && Objects.equals(this.balaURL, packageJsonSchema.balaURL)
                && Objects.equals(this.readme, packageJsonSchema.readme)
                && Objects.equals(this.licenses, packageJsonSchema.licenses)
                && Objects.equals(this.authors, packageJsonSchema.authors)
                && Objects.equals(this.sourceCodeLocation, packageJsonSchema.sourceCodeLocation)
                && Objects.equals(this.keywords, packageJsonSchema.keywords)
                && Objects.equals(this.ballerinaVersion, packageJsonSchema.ballerinaVersion)
                && Objects.equals(this.createdDate, packageJsonSchema.createdDate)
                && Objects.equals(this.modules, packageJsonSchema.modules);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(balToolId, organization, name, version, platform, languageSpecificationVersion, url, balaVersion,
                        balaURL, readme, licenses, authors, sourceCodeLocation, keywords, ballerinaVersion,
                        createdDate, modules);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ToolJsonSchema {\n");
        sb.append("    balToolId: ").append(toIndentedString(balToolId)).append("\n");
        sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
        sb.append("    languageSpecificationVersion: ").append(toIndentedString(languageSpecificationVersion))
                .append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    balaVersion: ").append(toIndentedString(balaVersion)).append("\n");
        sb.append("    balaURL: ").append(toIndentedString(balaURL)).append("\n");
        sb.append("    readme: ").append(toIndentedString(readme)).append("\n");
        sb.append("    licenses: ").append(toIndentedString(licenses)).append("\n");
        sb.append("    authors: ").append(toIndentedString(authors)).append("\n");
        sb.append("    sourceCodeLocation: ").append(toIndentedString(sourceCodeLocation)).append("\n");
        sb.append("    keywords: ").append(toIndentedString(keywords)).append("\n");
        sb.append("    ballerinaVersion: ").append(toIndentedString(ballerinaVersion)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
        sb.append("    modules: ").append(toIndentedString(modules)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
