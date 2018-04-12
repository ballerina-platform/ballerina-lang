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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.launcher.LauncherUtils;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Generates Ballerina API docs for a given ballerina package.
 */
@Mojo(name = "docerina", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DocerinaMojo extends AbstractMojo {
    private final PrintStream err = System.err;
    /**
     * Location of the output directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = false)
    private String outputDir;

    /**
     * Comma separated list of the ballerina sources.
     */
    @Parameter(property = "sourceDir", required = false)
    private String sourceDir;

    /**
     * Source root of the ballerina project.
     */
    @Parameter(property = "sourceRoot", required = false)
    private String sourceRoot;
    
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

        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        List<String> sources;
        if (sourceDir == null || sourceDir.isEmpty()) {
            SourceDirectory srcDirectory = new FileSystemProjectDirectory(sourceRootPath);
            sources = srcDirectory.getSourcePackageNames();
        } else {
            sources = Arrays.asList(sourceDir.split(","));
        }

        try {
            BallerinaDocGenerator.generateApiDocs(sourceRoot, outputDir, packageFilter, nativeCode, sources.toArray
                    (new String[sources.size()]));
        } catch (Throwable e) {
            err.println(ExceptionUtils.getStackTrace(e));
        }
    }
}
