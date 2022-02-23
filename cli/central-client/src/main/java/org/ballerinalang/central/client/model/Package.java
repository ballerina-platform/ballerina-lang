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

package org.ballerinalang.central.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code Package} represents package json from central.
 */
public class Package {
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

    public static final String JSON_PROPERTY_U_R_L = "URL";
    @SerializedName(JSON_PROPERTY_U_R_L) private String url;

    public static final String JSON_PROPERTY_BALA_VERSION = "balaVersion";
    @SerializedName(JSON_PROPERTY_BALA_VERSION) private String balaVersion;

    public static final String JSON_PROPERTY_BALA_U_R_L = "balaURL";
    @SerializedName(JSON_PROPERTY_BALA_U_R_L) private String balaURL;

    public static final String JSON_PROPERTY_README = "readme";
    @SerializedName(JSON_PROPERTY_README) private String readme;

    public static final String JSON_PROPERTY_TEMPLATE = "template";
    @SerializedName(JSON_PROPERTY_TEMPLATE) private Boolean template;

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

    public Package organization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Package name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Package version(String version) {

        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Package platform(String platform) {

        this.platform = platform;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Package languageSpecificationVersion(String languageSpecificationVersion) {

        this.languageSpecificationVersion = languageSpecificationVersion;
        return this;
    }

    public String getLanguageSpecificationVersion() {
        return languageSpecificationVersion;
    }

    public void setLanguageSpecificationVersion(String languageSpecificationVersion) {
        this.languageSpecificationVersion = languageSpecificationVersion;
    }

    public Package url(String url) {

        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Package balaVersion(String balaVersion) {

        this.balaVersion = balaVersion;
        return this;
    }

    public String getBalaVersion() {
        return balaVersion;
    }

    public void setBalaVersion(String balaVersion) {
        this.balaVersion = balaVersion;
    }

    public Package balaURL(String balaURL) {

        this.balaURL = balaURL;
        return this;
    }

    public String getBalaURL() {
        return balaURL;
    }

    public void setBalaURL(String balaURL) {
        this.balaURL = balaURL;
    }

    public Package summary(String summary) {

        this.summary = summary;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Package readme(String readme) {

        this.readme = readme;
        return this;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public Package template(Boolean template) {

        this.template = template;
        return this;
    }

    public Boolean getTemplate() {
        return template;
    }

    public void setTemplate(Boolean template) {
        this.template = template;
    }

    public Package licenses(List<String> licenses) {

        this.licenses = licenses;
        return this;
    }

    public Package addLicensesItem(String licensesItem) {
        this.licenses.add(licensesItem);
        return this;
    }

    public List<String> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<String> licenses) {
        this.licenses = licenses;
    }

    public Package authors(List<String> authors) {

        this.authors = authors;
        return this;
    }

    public Package addAuthorsItem(String authorsItem) {
        this.authors.add(authorsItem);
        return this;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public Package sourceCodeLocation(String sourceCodeLocation) {

        this.sourceCodeLocation = sourceCodeLocation;
        return this;
    }

    public String getSourceCodeLocation() {
        return sourceCodeLocation;
    }

    public void setSourceCodeLocation(String sourceCodeLocation) {
        this.sourceCodeLocation = sourceCodeLocation;
    }

    public Package keywords(List<String> keywords) {

        this.keywords = keywords;
        return this;
    }

    public Package addKeywordsItem(String keywordsItem) {
        this.keywords.add(keywordsItem);
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Package ballerinaVersion(String ballerinaVersion) {

        this.ballerinaVersion = ballerinaVersion;
        return this;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerinaVersion = ballerinaVersion;
    }

    public Package createdDate(Long createdDate) {

        this.createdDate = createdDate;
        return this;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Package modules(List<Module> modules) {

        this.modules = modules;
        return this;
    }

    public Package addModulesItem(Module modulesItem) {
        this.modules.add(modulesItem);
        return this;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public Package icon(String icon) {
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
        Package packageJsonSchema = (Package) o;
        return Objects.equals(this.organization, packageJsonSchema.organization) && Objects
                .equals(this.name, packageJsonSchema.name) && Objects.equals(this.version, packageJsonSchema.version)
                && Objects.equals(this.platform, packageJsonSchema.platform) && Objects
                .equals(this.languageSpecificationVersion, packageJsonSchema.languageSpecificationVersion) && Objects
                .equals(this.url, packageJsonSchema.url) && Objects
                .equals(this.balaVersion, packageJsonSchema.balaVersion) && Objects
                .equals(this.balaURL, packageJsonSchema.balaURL) && Objects
                .equals(this.readme, packageJsonSchema.readme) && Objects
                .equals(this.template, packageJsonSchema.template) && Objects
                .equals(this.licenses, packageJsonSchema.licenses) && Objects
                .equals(this.authors, packageJsonSchema.authors) && Objects
                .equals(this.sourceCodeLocation, packageJsonSchema.sourceCodeLocation) && Objects
                .equals(this.keywords, packageJsonSchema.keywords) && Objects
                .equals(this.ballerinaVersion, packageJsonSchema.ballerinaVersion) && Objects
                .equals(this.createdDate, packageJsonSchema.createdDate) && Objects
                .equals(this.modules, packageJsonSchema.modules);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(organization, name, version, platform, languageSpecificationVersion, url, balaVersion, balaURL,
                        readme, template, licenses, authors, sourceCodeLocation, keywords, ballerinaVersion,
                        createdDate, modules);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PackageJsonSchema {\n");
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
        sb.append("    template: ").append(toIndentedString(template)).append("\n");
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
