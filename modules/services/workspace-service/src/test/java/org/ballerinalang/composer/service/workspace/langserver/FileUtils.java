/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.composer.service.workspace.langserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File utils for reading the file content
 */
public class FileUtils {
    private static final ClassLoader CLASS_LOADER = FileUtils.class.getClassLoader();
    private static final String SAMPLES_DIR = "samples/langserver/";

    /**
     * Get the file content
     * @param filePath path to the file
     * @return file content as a string
     * @throws IOException IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static String fileContent(String filePath) throws IOException, URISyntaxException {
        String location = SAMPLES_DIR + filePath;
        URI fileLocation = CLASS_LOADER.getResource(location).toURI();
        return new String(Files.readAllBytes(Paths.get(fileLocation)));
    }
}
