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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.wso2.siddhi.doc.gen.core.utils.Constants;
import org.wso2.siddhi.doc.gen.core.utils.DocumentationUtils;

import java.io.File;

/**
 * Mojo for deploying mkdocs website on GitHub pages
 */
@Mojo(
        name = "deploy-mkdocs-github-pages",
        aggregator = true,
        defaultPhase = LifecyclePhase.INSTALL
)
public class MkdocsGitHubPagesDeployMojo extends AbstractMojo {
    /**
     * The directory in which the documentation will be generated
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    /**
     * The path of the mkdocs.yml file in the base directory
     * Optional
     */
    @Parameter(property = "mkdocs.config.file")
    private File mkdocsConfigFile;

    /**
     * The directory in which the index will be generated
     * Optional
     */
    @Parameter(property = "doc.gen.base.directory")
    private File docGenBaseDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Finding the root maven project
        MavenProject rootMavenProject = mavenProject;
        while (rootMavenProject.getParent().getBasedir() != null) {
            rootMavenProject = rootMavenProject.getParent();
        }

        // Setting the mkdocs config file path if not set by user
        if (mkdocsConfigFile == null) {
            mkdocsConfigFile = new File(rootMavenProject.getBasedir() + File.separator
                    + Constants.MKDOCS_CONFIG_FILE_NAME + Constants.YAML_FILE_EXTENSION);
        }

        // Setting the documentation output directory if not set by user
        String docGenPath;
        if (docGenBaseDirectory != null) {
            docGenPath = docGenBaseDirectory.getAbsolutePath();
        } else {
            docGenPath = rootMavenProject.getBasedir() + File.separator + Constants.DOCS_DIRECTORY;
        }

        DocumentationUtils.deployMkdocsOnGitHubPages(mkdocsConfigFile, getLog());
        DocumentationUtils.updateDocumentationOnGitHub(docGenPath, mkdocsConfigFile,
                mavenProject.getVersion(), getLog());
    }
}
