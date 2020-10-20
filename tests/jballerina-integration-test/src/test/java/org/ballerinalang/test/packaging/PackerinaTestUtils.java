/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.packaging;

import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Util methods needed for Packaging test cases.
 *
 * @since 0.982.0
 */
public class PackerinaTestUtils {

    /**
     * Delete files inside directories.
     *
     * @param dirPath directory path
     * @throws IOException throw an exception if an issue occurs
     */
    public static void deleteFiles(Path dirPath) throws IOException {
        if (dirPath == null) {
            return;
        }
        Files.walk(dirPath)
             .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    public static Map<String, String> getEnvVariables() {
        Map<String, String> envVarMap = System.getenv();
        Map<String, String> retMap = new HashMap<>();
        envVarMap.forEach(retMap::put);
        return retMap;
    }

    /**
     * Generate random module name.
     *
     * @param count number of characters required
     * @return generated name
     */
    public static String randomModuleName(int count) {
        String upperCaseAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseAlpha = "abcdefghijklmnopqrstuvwxyz";
        String alpha = upperCaseAlpha + lowerCaseAlpha;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * alpha.length());
            builder.append(alpha.charAt(character));
        }
        return builder.toString();
    }

    /**
     * Get token of ballerina-central-bot required to push the module.
     *
     * @return token required to push the module.
     */
    public static String getToken() {
        // staging and dev both has the same access token
        return new String(Base64.getDecoder().decode("YWYwMjkyODgtNjhkZC0zOTVmLTk5MzQtYTgyYWRjM2NlYzZi"));
    }

    /**
     * Create Settings.toml inside the home repository.
     *
     * @throws IOException i/o exception when writing to file
     */
    public static void createSettingToml(Path dirPath) throws IOException {
        String content = "[central]\n accesstoken = \"" + getToken() + "\"";
        Files.write(dirPath.resolve("Settings.toml"), content.getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    
    
    /**
     * Update the organization name by changing the default organization name.
     * @param projectRootPath The project root.
     * @param orgName The organization name.
     * @throws IOException Error when writing to Ballerina.toml
     */
    protected static void updateManifestOrgName(Path projectRootPath, String orgName) throws IOException {
        String guessOrgName = System.getProperty("user.name");
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        
        Path tomlPath = projectRootPath.resolve("Ballerina.toml");
        Stream<String> lines = Files.lines(tomlPath);
        String finalGuessOrgName = guessOrgName;
        List<String> replaced = lines.map(line -> line.replaceAll(finalGuessOrgName, orgName))
                .collect(Collectors.toList());
        Files.write(tomlPath, replaced);
        lines.close();
    }

    /**
     * Copy directory to target directory.
     *
     * @param src  source file
     * @param dest destination path to copy.
     * @throws IOException throw if there is any error occur while copying directories.
     */
    public static void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    /**
     * Copy a file to a target file.
     *
     * @param src  source file
     * @param dest destination path to copy.
     */
    public static void copy(Path src, Path dest) {
        try {
            Files.copy(src, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Modify the content of the file.
     *
     * @param filePath path to the file
     * @param regex regex to be replaced
     * @param replacedWith string to replaced with
     * @throws IOException When modifying the content of the file
     */
    public static void modifyContent(Path filePath, String regex, String replacedWith) throws IOException {
        Stream<String> lines = Files.lines(filePath);
        List<String> replaced = lines.map(line -> line.replaceAll(regex, replacedWith))
                .collect(Collectors.toList());
        Files.write(filePath, replaced);
        lines.close();
    }
}
