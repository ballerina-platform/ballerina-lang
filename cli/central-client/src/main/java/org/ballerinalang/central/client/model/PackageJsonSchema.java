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
 * {@code PackageJsonSchema} represents package json.
 */
public class PackageJsonSchema {
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
    @SerializedName(JSON_PROPERTY_U_R_L) private String URL;

    public static final String JSON_PROPERTY_BALO_VERSION = "baloVersion";
    @SerializedName(JSON_PROPERTY_BALO_VERSION) private String baloVersion;

    public static final String JSON_PROPERTY_BALO_U_R_L = "baloURL";
    @SerializedName(JSON_PROPERTY_BALO_U_R_L) private String baloURL;

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
    @SerializedName(JSON_PROPERTY_CREATED_DATE) private Integer createdDate;

    public static final String JSON_PROPERTY_MODULES = "modules";
    @SerializedName(JSON_PROPERTY_MODULES) private List<ModuleJsonSchema> modules = new ArrayList<>();

    public PackageJsonSchema organization(String organization) {

        this.organization = organization;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public PackageJsonSchema name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PackageJsonSchema version(String version) {

        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PackageJsonSchema platform(String platform) {

        this.platform = platform;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public PackageJsonSchema languageSpecificationVersion(String languageSpecificationVersion) {

        this.languageSpecificationVersion = languageSpecificationVersion;
        return this;
    }

    public String getLanguageSpecificationVersion() {
        return languageSpecificationVersion;
    }

    public void setLanguageSpecificationVersion(String languageSpecificationVersion) {
        this.languageSpecificationVersion = languageSpecificationVersion;
    }

    public PackageJsonSchema URL(String URL) {

        this.URL = URL;
        return this;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public PackageJsonSchema baloVersion(String baloVersion) {

        this.baloVersion = baloVersion;
        return this;
    }

    public String getBaloVersion() {
        return baloVersion;
    }

    public void setBaloVersion(String baloVersion) {
        this.baloVersion = baloVersion;
    }

    public PackageJsonSchema baloURL(String baloURL) {

        this.baloURL = baloURL;
        return this;
    }

    public String getBaloURL() {
        return baloURL;
    }

    public void setBaloURL(String baloURL) {
        this.baloURL = baloURL;
    }

    public PackageJsonSchema readme(String readme) {

        this.readme = readme;
        return this;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public PackageJsonSchema template(Boolean template) {

        this.template = template;
        return this;
    }

    public Boolean getTemplate() {
        return template;
    }

    public void setTemplate(Boolean template) {
        this.template = template;
    }

    public PackageJsonSchema licenses(List<String> licenses) {

        this.licenses = licenses;
        return this;
    }

    public PackageJsonSchema addLicensesItem(String licensesItem) {
        this.licenses.add(licensesItem);
        return this;
    }

    public List<String> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<String> licenses) {
        this.licenses = licenses;
    }

    public PackageJsonSchema authors(List<String> authors) {

        this.authors = authors;
        return this;
    }

    public PackageJsonSchema addAuthorsItem(String authorsItem) {
        this.authors.add(authorsItem);
        return this;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public PackageJsonSchema sourceCodeLocation(String sourceCodeLocation) {

        this.sourceCodeLocation = sourceCodeLocation;
        return this;
    }

    public String getSourceCodeLocation() {
        return sourceCodeLocation;
    }

    public void setSourceCodeLocation(String sourceCodeLocation) {
        this.sourceCodeLocation = sourceCodeLocation;
    }

    public PackageJsonSchema keywords(List<String> keywords) {

        this.keywords = keywords;
        return this;
    }

    public PackageJsonSchema addKeywordsItem(String keywordsItem) {
        this.keywords.add(keywordsItem);
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public PackageJsonSchema ballerinaVersion(String ballerinaVersion) {

        this.ballerinaVersion = ballerinaVersion;
        return this;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerinaVersion = ballerinaVersion;
    }

    public PackageJsonSchema createdDate(Integer createdDate) {

        this.createdDate = createdDate;
        return this;
    }

    public Integer getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Integer createdDate) {
        this.createdDate = createdDate;
    }

    public PackageJsonSchema modules(List<ModuleJsonSchema> modules) {

        this.modules = modules;
        return this;
    }

    public PackageJsonSchema addModulesItem(ModuleJsonSchema modulesItem) {
        this.modules.add(modulesItem);
        return this;
    }

    public List<ModuleJsonSchema> getModules() {
        return modules;
    }

    public void setModules(List<ModuleJsonSchema> modules) {
        this.modules = modules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageJsonSchema packageJsonSchema = (PackageJsonSchema) o;
        return Objects.equals(this.organization, packageJsonSchema.organization) && Objects
                .equals(this.name, packageJsonSchema.name) && Objects.equals(this.version, packageJsonSchema.version)
                && Objects.equals(this.platform, packageJsonSchema.platform) && Objects
                .equals(this.languageSpecificationVersion, packageJsonSchema.languageSpecificationVersion) && Objects
                .equals(this.URL, packageJsonSchema.URL) && Objects
                .equals(this.baloVersion, packageJsonSchema.baloVersion) && Objects
                .equals(this.baloURL, packageJsonSchema.baloURL) && Objects
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
                .hash(organization, name, version, platform, languageSpecificationVersion, URL, baloVersion, baloURL,
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
        sb.append("    URL: ").append(toIndentedString(URL)).append("\n");
        sb.append("    baloVersion: ").append(toIndentedString(baloVersion)).append("\n");
        sb.append("    baloURL: ").append(toIndentedString(baloURL)).append("\n");
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
