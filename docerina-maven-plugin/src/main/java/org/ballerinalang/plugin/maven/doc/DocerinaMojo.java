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
import org.ballerinalang.docgen.docs.BallerinaDocGeneratorMain;

/**
 * Generates Ballerina API docs for a given ballerina package.
 */
@Mojo(name = "docerina", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DocerinaMojo extends AbstractMojo {
    /**
     * Location of the templates directory.
     */
    @Parameter(property = "templatesDir", required = true)
    private String templatesDir;

    /**
     * Location of the output directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = false)
    private String outputDir;

    /**
     * Location of the ballerina source folder
     */
    @Parameter(property = "sourceDir", required = true)
    private String sourceDir;

    public void execute() throws MojoExecutionException {
        System.setProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY, templatesDir);
        System.setProperty(BallerinaDocConstants.HTML_OUTPUT_PATH_KEY, outputDir);

        BallerinaDocGeneratorMain.main(new String[] { sourceDir });
    }
}
