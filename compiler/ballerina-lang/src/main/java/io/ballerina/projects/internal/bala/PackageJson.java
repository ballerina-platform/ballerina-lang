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

package io.ballerina.projects.internal.bala;

import com.google.gson.JsonArray;

import java.util.List;

/**
 * {@code PackageJson} Model for Package JSON file.
 *
 * @since 2.0.0
 */
public class PackageJson {
    // Information extracted from Ballerina.toml
    private String organization;
    private String name;
    private String version;
    private List<String> licenses; //?
    private List<String> authors; //?
    private String source_repository; //?
    private List<String> keywords; //?
    private List<String> export; //?
    private String visibility;

    // Distribution details
    private String ballerina_version;
    private String implementation_vendor;
    private String language_spec_version;                     // 2020R1

    // Dependencies
    private List<Dependency> dependencies; //?
    private String platform; // target of the bala ie. java11, any etc.
    private JsonArray platformDependencies; // platform dependencies

    // Templating support
    private boolean template; //?
    private String template_version; //?

    public PackageJson(String organization, String name, String version) {
        this.organization = organization;
        this.name = name;
        this.version = version;
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

    public String getSourceRepository() {
        return source_repository;
    }

    public void setSourceRepository(String source_repository) {
        this.source_repository = source_repository;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getExport() {
        return export;
    }

    public void setExport(List<String> export) {
        this.export = export;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getBallerinaVersion() {
        return ballerina_version;
    }

    public void setBallerinaVersion(String ballerina_version) {
        this.ballerina_version = ballerina_version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getImplementationVendor() {
        return implementation_vendor;
    }

    public void setImplementationVendor(String implementation_vendor) {
        this.implementation_vendor = implementation_vendor;
    }

    public String getLanguageSpecVersion() {
        return language_spec_version;
    }

    public void setLanguageSpecVersion(String language_spec_version) {
        this.language_spec_version = language_spec_version;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public JsonArray getPlatformDependencies() {
        return platformDependencies;
    }

    public void setPlatformDependencies(JsonArray platform) {
        this.platformDependencies = platform;
    }

    public boolean getTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public String getTemplateVersion() {
        return template_version;
    }

    public void setTemplateVersion(String template_version) {
        this.template_version = template_version;
    }
}
