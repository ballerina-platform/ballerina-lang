/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.plugin.maven.doc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;

/**
 * Generates Ballerina API docs for a given ballerina package.
 */
@Mojo(name = "docerina", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DocerinaMojo extends AbstractMojo {
    /**
     * Location of the output directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = false)
    private String outputDir;

    /**
     * Comma separated list of the ballerina sources.
     */
    @Parameter(property = "sourceDir", required = true)
    private String sourceDir;
    
    /**
     * A custom templates directory.
     */
    @Parameter(property = "templatesDir", required = false)
    private String templatesDir;

    /**
     * Comma separated list of packages to be excluded.
     */
    @Parameter(property = "packageFilter", required = false)
    private String packageFilter;
    
    /**
     * treat the source as native ballerina code.
     */
    @Parameter(property = "nativeCode", required = false)
    private boolean nativeCode;

    @Parameter(property = "outputZip", required = false)
    private String outputZip;

    /**
     * enable debug level logs.
     */
    @Parameter(property = "debugDocerina", required = false)
    private boolean debugDocerina;

    public void execute() throws MojoExecutionException {
        if (debugDocerina) {
            System.setProperty(BallerinaDocConstants.ENABLE_DEBUG_LOGS, "true");
        }
        if (templatesDir != null) {
            System.setProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, templatesDir);
        }
        if (outputZip != null) {
            System.setProperty(BallerinaDocConstants.OUTPUT_ZIP_PATH, outputZip);
        }

        String[] sources = sourceDir.split(",");
        BallerinaDocGenerator.generateApiDocs(null, outputDir, packageFilter, nativeCode, sources);
    }
}
