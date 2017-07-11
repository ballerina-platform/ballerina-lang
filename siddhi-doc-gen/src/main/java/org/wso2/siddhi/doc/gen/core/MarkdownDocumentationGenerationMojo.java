/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.siddhi.doc.gen.core;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.wso2.siddhi.doc.gen.commons.metadata.NamespaceMetaData;
import org.wso2.siddhi.doc.gen.core.utils.Constants;
import org.wso2.siddhi.doc.gen.core.utils.DocumentationUtils;

import java.io.File;
import java.util.List;

/**
 * Mojo for generating Siddhi Documentation
 */
@Mojo(
        name = "generate-md-docs",
        defaultPhase = LifecyclePhase.INSTALL,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class MarkdownDocumentationGenerationMojo extends AbstractMojo {
    /**
     * The maven project object for the current module
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    /**
     * The target module for which the files will be generated
     * Optional
     */
    @Parameter(property = "module.target.directory")
    private File moduleTargetDirectory;

    /**
     * The path of the readme file in the base directory
     * Optional
     */
    @Parameter(property = "read.me.file")
    private File readMeFile;

    /**
     * The path of the mkdocs.yml file in the base directory
     * Optional
     */
    @Parameter(property = "mkdocs.config.file")
    private File mkdocsConfigFile;

    /**
     * The directory in which the docs will be generated
     * Optional
     */
    @Parameter(property = "doc.gen.base.directory")
    private File docGenBaseDirectory;

    /**
     * The name of the index file
     * Optional
     */
    @Parameter(property = "home.page.file.name")
    private String homePageFileName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Setting the relevant modules target directory if not set by user
        String moduleTargetPath;
        if (moduleTargetDirectory != null) {
            moduleTargetPath = moduleTargetDirectory.getAbsolutePath();
        } else {
            moduleTargetPath = mavenProject.getBuild().getDirectory();
        }

        // Setting the documentation output directory if not set by user
        String docGenPath;
        if (docGenBaseDirectory != null) {
            docGenPath = docGenBaseDirectory.getAbsolutePath();
        } else {
            docGenPath = mavenProject.getParent().getBasedir() + File.separator + Constants.DOCS_DIRECTORY;
        }

        // Setting the read me file path if not set by user
        if (readMeFile == null) {
            readMeFile = new File(mavenProject.getParent().getBasedir() + File.separator + Constants.README_FILE_NAME
                    + Constants.MARKDOWN_FILE_EXTENSION);
        }

        // Setting the mkdocs config file path if not set by user
        if (mkdocsConfigFile == null) {
            mkdocsConfigFile = new File(mavenProject.getParent().getBasedir() + File.separator
                    + Constants.MKDOCS_CONFIG_FILE_NAME + Constants.YAML_FILE_EXTENSION);
        }

        // Setting the index file name if not set by user
        if (homePageFileName == null) {
            homePageFileName = Constants.MARKDOWN_HOME_PAGE_TEMPLATE;
        }

        // Retrieving metadata
        List<NamespaceMetaData> namespaceMetaDataList;
        try {
            namespaceMetaDataList = DocumentationUtils.getExtensionMetaData(
                    moduleTargetPath,
                    mavenProject.getRuntimeClasspathElements(),
                    getLog()
            );
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoFailureException("Unable to resolve dependencies of the project", e);
        }

        // Generating the documentation
        if (namespaceMetaDataList.size() > 0) {
            DocumentationUtils.generateDocumentation(namespaceMetaDataList, docGenPath, mavenProject.getVersion());
            DocumentationUtils.updateHomePage(readMeFile, docGenPath, homePageFileName, mkdocsConfigFile, getLog());
        }
    }
}
