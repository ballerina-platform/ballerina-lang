/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */
package org.ballerinalang.datamapper.util;

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
    
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

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
}
