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
package org.ballerinalang.langserver.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File utils for reading the file content.
 */
public class FileUtils {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
    
    public static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    public static final Path BUILD_DIR = Paths.get("build/").toAbsolutePath();

    /**
     * Get the file content.
     * @param filePath path to the file
     * @return {@link JsonObject} file content as a jsonObject
     */
    public static JsonObject fileContentAsObject(String filePath) {
        String contentAsString = "";
        try {
            contentAsString = new String(Files.readAllBytes(RES_DIR.resolve(filePath)));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return JSON_PARSER.parse(contentAsString).getAsJsonObject();
    }

    /**
     * Get the file content.
     * @param filePath path to the file
     * @return {@link String} file content as a String
     */
    public static String fileContent(String filePath) {
        String stringContent = "";
        try {
            stringContent = new String(Files.readAllBytes(RES_DIR.resolve(filePath)));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return stringContent;
    }
}
