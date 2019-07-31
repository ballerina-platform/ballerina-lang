/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugin.gradle.doc;

import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;


/**
 * Generates Ballerina API docs for a given ballerina package.
 */
public class DocerinaGen {

    private static final PrintStream out = System.out;

    public static void main(String[] args) {
        String sourceRoot = args[0];
        String outputDir = args[1];
        String moduleFilter = args[2];

        System.setProperty(BallerinaDocConstants.ENABLE_DEBUG_LOGS, "true");

        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        List<String> sources;

        SourceDirectory srcDirectory = new FileSystemProjectDirectory(sourceRootPath);
        sources = srcDirectory.getSourcePackageNames();

        try {
            BallerinaDocGenerator.generateApiDocs(sourceRoot, outputDir,
                    moduleFilter, false, false,
                    sources.toArray(new String[sources.size()]));
        } catch (Throwable e) {
            out.println(e.getMessage());
        } finally {
            System.clearProperty(BallerinaDocConstants.ENABLE_DEBUG_LOGS);
            System.clearProperty(BallerinaDocConstants.TEMPLATES_FOLDER_PATH_KEY);
            System.clearProperty(BallerinaDocConstants.OUTPUT_ZIP_PATH);
            System.clearProperty(BallerinaDocConstants.ORG_NAME);
        }
    }
}
