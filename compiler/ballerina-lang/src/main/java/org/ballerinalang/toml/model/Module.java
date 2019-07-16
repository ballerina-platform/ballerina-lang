/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.toml.model;

import org.wso2.ballerinalang.programfile.ProgramFileConstants;

import java.util.List;

/**
 * Model for Module TOML file.
 *
 * @since 1.0
 */
public class Module {
    public String manifest_version = "1.0";
    public String module_organization;
    public String module_name;
    public String module_version;
    public List<String> module_licenses;
    public List<String> module_authors;
    public String module_source_repository;
    public List<String> module_keywords;
    public String ballerina_version;
    private String platform;
    public String language_specification_version = ProgramFileConstants.IMPLEMENTATION_VERSION;
    public String implementation_name = "jBallerina";
    public String implementation_version = "1.0.0";
    public String implementation_vendor = "WSO2";
    
    public String getManifest_version() {
        return manifest_version;
    }

    public void setManifest_version(String manifest_version) {
        this.manifest_version = manifest_version;
    }

    public String getModule_organization() {
        return module_organization;
    }

    public void setModule_organization(String module_organization) {
        this.module_organization = module_organization;
    }

    public String getModule_name() {
        return module_name;
    }

    public void setModule_name(String module_name) {
        this.module_name = module_name;
    }

    public String getModule_version() {
        return module_version;
    }

    public void setModule_version(String module_version) {
        this.module_version = module_version;
    }

    public List<String> getModule_licenses() {
        return module_licenses;
    }

    public void setModule_licenses(List<String> module_licenses) {
        this.module_licenses = module_licenses;
    }

    public List<String> getModule_authors() {
        return module_authors;
    }

    public void setModule_authors(List<String> module_authors) {
        this.module_authors = module_authors;
    }

    public String getModule_source_repository() {
        return module_source_repository;
    }

    public void setModule_source_repository(String module_source_repository) {
        this.module_source_repository = module_source_repository;
    }

    public List<String> getModule_keywords() {
        return module_keywords;
    }

    public void setModule_keywords(List<String> module_keywords) {
        this.module_keywords = module_keywords;
    }

    public String getBallerina_version() {
        return ballerina_version;
    }

    public void setBallerina_version(String ballerina_version) {
        this.ballerina_version = ballerina_version;
    }

    public String getLanguage_specification_version() {
        return language_specification_version;
    }

    public void setLanguage_specification_version(String language_specification_version) {
        this.language_specification_version = language_specification_version;
    }

    public String getImplementation_name() {
        return implementation_name;
    }

    public void setImplementation_name(String implementation_name) {
        this.implementation_name = implementation_name;
    }

    public String getImplementation_version() {
        return implementation_version;
    }

    public void setImplementation_version(String implementation_version) {
        this.implementation_version = implementation_version;
    }

    public String getImplementation_vendor() {
        return implementation_vendor;
    }

    public void setImplementation_vendor(String implementation_vendor) {
        this.implementation_vendor = implementation_vendor;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getPlatform() {
        return platform;
    }
}
