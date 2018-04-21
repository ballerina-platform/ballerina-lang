/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the manifest object which is created using the toml file configs.
 *
 * @since 0.964
 */
public class Manifest {
    private String name = "";
    private String version = "";
    private List<String> authors = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();
    private String documentationURL = "";
    private String homepageURL = "";
    private String repositoryURL = "";
    private String description = "";
    private String readmeFilePath = "";
    private String license = "";
    private List<Dependency> dependencies = new ArrayList<>();
    private List<Dependency> patches = new ArrayList<>();

    /**
     * Get the patches list.
     *
     * @return patches list
     */
    public List<Dependency> getPatches() {
        return patches;
    }

    /**
     * Add a patch to the patches list.
     *
     * @param dependency dependency object
     */
    public void addPatches(Dependency dependency) {
        this.patches.add(dependency);
        patches = removeDuplicates(patches);
    }

    /**
     * Get the dependencies list.
     *
     * @return dependencies list
     */
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    /**
     * Add a dependency to the dependencies list.
     *
     * @param dependency dependency object
     */
    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
        dependencies = removeDuplicates(dependencies);
    }

    /**
     * Get keywords.
     *
     * @return list of keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Set keywords list.
     *
     * @param keywords keyword list
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Get file path of the readme file.
     *
     * @return file path of the readme file
     */
    public String getReadmeFilePath() {
        return readmeFilePath;
    }

    /**
     * Set file path of the readme file.
     *
     * @param readmeFilePath file path of the readme file
     */
    public void setReadmeFilePath(String readmeFilePath) {
        this.readmeFilePath = readmeFilePath;
    }

    /**
     * Get documentation URL.
     *
     * @return documentation URL
     */
    public String getDocumentationURL() {
        return documentationURL;
    }

    /**
     * Set documentation URL.
     *
     * @param documentationURL documentation URL
     */
    public void setDocumentationURL(String documentationURL) {
        this.documentationURL = documentationURL;
    }

    /**
     * Get homepage URL.
     *
     * @return homepage URL
     */
    public String getHomepageURL() {
        return homepageURL;
    }

    /**
     * Set homepage URL.
     *
     * @param homepageURL homepage URL
     */
    public void setHomepageURL(String homepageURL) {
        this.homepageURL = homepageURL;
    }

    /**
     * Get repository URL.
     *
     * @return repository URL
     */
    public String getRepositoryURL() {
        return repositoryURL;
    }

    /**
     * Set the repository URL.
     *
     * @param repositoryURL repository URL
     */
    public void setRepositoryURL(String repositoryURL) {
        this.repositoryURL = repositoryURL;
    }

    /**
     * Get description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     *
     * @param description description about the package
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get authors of the toml file.
     *
     * @return authors
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Set authors of the toml file.
     *
     * @param authors list of authors
     */
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    /**
     * Get the package name.
     *
     * @return package name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the package name.
     *
     * @param name package name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the version.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the version.
     *
     * @param version version of the package.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the license.
     *
     * @return license
     */
    public String getLicense() {
        return license;
    }

    /**
     * Set the license.
     *
     * @param license license
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Remove duplicates from dependencies and patches list.
     *
     * @param list list of elements
     * @return dependencies or patches list without duplicates
     */
    private List<Dependency> removeDuplicates(List<Dependency> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}
