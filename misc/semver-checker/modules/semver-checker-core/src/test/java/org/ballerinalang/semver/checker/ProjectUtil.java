/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.semver.checker;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.directory.BuildProject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

/**
 * This is the class for getting the ballerina package instance from the text code.
 */
public class ProjectUtil {

    public static final String BAL_TOML_FILE_NAME = "Ballerina.toml";
    public static final String BAL_FILE_EXT = ".bal";
    private static Path tempProjectDir;
    private static final String TEMP_DIR_PREFIX = "semver-enforcing-dir-";
    private static final String MAIN_FILE_PREFIX = "main-";
    private static final String PACKAGE_ORG = "Semver_validator";
    private static final String PACKAGE_NAME = "patch_validator";
    private static final String PACKAGE_VERSION = "1.0.0";


    public static BuildProject createProject(String mainBalContent) throws Exception {

        try {
            // Creates a new directory in the default temporary file directory.
            tempProjectDir = Files.createTempDirectory(TEMP_DIR_PREFIX + System.currentTimeMillis());

            tempProjectDir.toFile().deleteOnExit();

            // Creates a main file and writes the generated code snippet.
            createMainBalFile(mainBalContent);
            // Creates the Ballerina.toml file and writes the package meta information.
            createBallerinaToml();

            BuildOptions buildOptions = BuildOptions.builder().setOffline(true).build();
            return BuildProject.load(tempProjectDir, buildOptions);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void createMainBalFile(String content) throws Exception {

        File mainBalFile = File.createTempFile(MAIN_FILE_PREFIX, BAL_FILE_EXT, tempProjectDir.toFile());
        mainBalFile.deleteOnExit();
        FileUtils.writeToFile(mainBalFile, content);
    }

    public static void createBallerinaToml() throws Exception {

        Path ballerinaTomlPath = tempProjectDir.resolve(BAL_TOML_FILE_NAME);
        File balTomlFile = Files.createFile(ballerinaTomlPath).toFile();
        balTomlFile.deleteOnExit();
        StringJoiner balTomlContent = new StringJoiner(System.lineSeparator());
        balTomlContent.add("[package]");
        balTomlContent.add(String.format("org = \"%s\"", PACKAGE_ORG));
        balTomlContent.add(String.format("name = \"%s\"", PACKAGE_NAME));
        balTomlContent.add(String.format("version = \"%s\"", PACKAGE_VERSION));
        FileUtils.writeToFile(balTomlFile, balTomlContent.toString());

    }
}
