/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.swagger.code.generator.cmd;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.codegen.config.CodegenConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.swagger.codegen.config.CodegenConfiguratorUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * User: lanwen
 * Date: 24.03.15
 * Time: 20:22
 */

public class Generate implements Runnable {

    public static final Logger LOG = LoggerFactory.getLogger(Generate.class);

    private Boolean verbose;

    private String lang;

    private String output = "";


   private String spec;

    private String templateDir;

    private String auth;

    private String systemProperties;

    private String configFile;

    private Boolean skipOverwrite;

    private String apiPackage;

    private String modelPackage;

    private String modelNamePrefix;

    private String modelNameSuffix;

    private String instantiationTypes;

    private String typeMappings;

    private String additionalProperties;

    private String languageSpecificPrimitives;

    private String importMappings;

    private String invokerPackage;

    private String groupId;

    private String artifactId;

    private String artifactVersion;

    private String library;

    private String gitUserId;

    private String gitRepoId;

    private String releaseNote;

    private String httpUserAgent;

    @Override
    public void run() {

        //attempt to read from config file
        CodegenConfigurator configurator = CodegenConfigurator.fromFile(configFile);

        //if a config file wasn't specified or we were unable to read it
        if (configurator == null) {
            //create a fresh configurator
            configurator = new CodegenConfigurator();
        }

        //now override with any specified parameters
        if (verbose != null) {
            configurator.setVerbose(verbose);
        }

        if (skipOverwrite != null) {
            configurator.setSkipOverwrite(skipOverwrite);
        }

        if (isNotEmpty(spec)) {
            configurator.setInputSpec(spec);
        }

        if (isNotEmpty(lang)) {
            configurator.setLang(lang);
        }

        if (isNotEmpty(output)) {
            configurator.setOutputDir(output);
        }

        if (isNotEmpty(auth)) {
            configurator.setAuth(auth);
        }

        if (isNotEmpty(templateDir)) {
            configurator.setTemplateDir(templateDir);
        }

        if (isNotEmpty(apiPackage)) {
            configurator.setApiPackage(apiPackage);
            configurator.setOutputDir(output + "/"+apiPackage.replace(".","/"));
        }

        if (isNotEmpty(modelPackage)) {
            configurator.setModelPackage(modelPackage);
        }

        if (isNotEmpty(modelNamePrefix)) {
            configurator.setModelNamePrefix(modelNamePrefix);
        }

        if (isNotEmpty(modelNameSuffix)) {
            configurator.setModelNameSuffix(modelNameSuffix);
        }

        if (isNotEmpty(invokerPackage)) {
            configurator.setInvokerPackage(invokerPackage);
        }

        if (isNotEmpty(groupId)) {
            configurator.setGroupId(groupId);
        }

        if (isNotEmpty(artifactId)) {
            configurator.setArtifactId(artifactId);
        }

        if (isNotEmpty(artifactVersion)) {
            configurator.setArtifactVersion(artifactVersion);
        }

        if (isNotEmpty(library)) {
            configurator.setLibrary(library);
        }

        if (isNotEmpty(gitUserId)) {
            configurator.setGitUserId(gitUserId);
        }

        if (isNotEmpty(gitRepoId)) {
            configurator.setGitRepoId(gitRepoId);
        }

        if (isNotEmpty(releaseNote)) {
            configurator.setReleaseNote(releaseNote);
        }

        if (isNotEmpty(httpUserAgent)) {
            configurator.setHttpUserAgent(httpUserAgent);
        }

        applySystemPropertiesKvp(systemProperties, configurator);
        applyInstantiationTypesKvp(instantiationTypes, configurator);
        applyImportMappingsKvp(importMappings, configurator);
        applyTypeMappingsKvp(typeMappings, configurator);
        applyAdditionalPropertiesKvp(additionalProperties, configurator);
        applyLanguageSpecificPrimitivesCsv(languageSpecificPrimitives, configurator);
        final ClientOptInput clientOptInput = configurator.toClientOptInput();

        new DefaultGenerator().opts(clientOptInput).generate();
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setApiPackage(String apiPackage) {
        this.apiPackage = apiPackage;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setSkipOverwrite(Boolean skipOverwrite) {
        this.skipOverwrite = skipOverwrite;
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public void setModelNamePrefix(String modelNamePrefix) {
        this.modelNamePrefix = modelNamePrefix;
    }

    public void setModelNameSuffix(String modelNameSuffix) {
        this.modelNameSuffix = modelNameSuffix;
    }

    public void setInstantiationTypes(String instantiationTypes) {
        this.instantiationTypes = instantiationTypes;
    }

    public void setTypeMappings(String typeMappings) {
        this.typeMappings = typeMappings;
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public void setLanguageSpecificPrimitives(String languageSpecificPrimitives) {
        this.languageSpecificPrimitives = languageSpecificPrimitives;
    }

    public void setImportMappings(String importMappings) {
        this.importMappings = importMappings;
    }

    public void setInvokerPackage(String invokerPackage) {
        this.invokerPackage = invokerPackage;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public void setGitUserId(String gitUserId) {
        this.gitUserId = gitUserId;
    }

    public void setGitRepoId(String gitRepoId) {
        this.gitRepoId = gitRepoId;
    }

    public void setReleaseNote(String releaseNote) {
        this.releaseNote = releaseNote;
    }

    public void setHttpUserAgent(String httpUserAgent) {
        this.httpUserAgent = httpUserAgent;
    }

}
