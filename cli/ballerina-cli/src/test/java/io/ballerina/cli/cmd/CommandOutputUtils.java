/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.utils.OsUtils.isWindows;

/**
 * Doc output utilities.
 *
 * @since 2.0.0
 */
public class CommandOutputUtils {

    private static final Path commandOutputsDir = Paths
            .get("src", "test", "resources", "test-resources", "command-outputs");

    private CommandOutputUtils() {
    }

    static String readFileAsString(Path filePath) throws IOException {
        if (isWindows()) {
            return Files.readString(filePath).replaceAll("\r", "");
        } else {
            return Files.readString(filePath);
        }
    }

    static String getOutput(String outputFileName) throws IOException {
        if (isWindows()) {
            return Files.readString(commandOutputsDir.resolve("windows").resolve(outputFileName))
                    .replaceAll("\r", "");
        } else {
            return Files.readString(commandOutputsDir.resolve("unix").resolve(outputFileName));
        }
    }


    static void assertTomlFilesEquals(Path actualTomlFilePath, Path expectedTomlFilePath) throws IOException {
        Assert.assertEquals(readFileAsString(actualTomlFilePath),
                insertDistributionVersionToDependenciesToml(readFileAsString(expectedTomlFilePath)));
    }

    private static String insertDistributionVersionToDependenciesToml(String dependenciesToml) {
        String distributionVersion = RepoUtils.getBallerinaShortVersion();
        return dependenciesToml.replace("**INSERT_DISTRIBUTION_VERSION_HERE**", distributionVersion);
    }

    public static void replaceDependenciesTomlContent(Path projectDirPath, String searchText, String replaceText)
            throws IOException {
        String filename = projectDirPath.resolve(ProjectConstants.DEPENDENCIES_TOML).toString();
        File tempFile = new File(filename + "-temp.toml");

        try (BufferedReader reader = new BufferedReader(new FileReader(filename));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.contains(searchText)) {
                    currentLine = currentLine.replace(searchText, replaceText);
                }
                writer.write(currentLine);
                writer.newLine();
            }
        }
        File originalFile = new File(filename);
        originalFile.delete();
        tempFile.renameTo(originalFile);
    }
}
