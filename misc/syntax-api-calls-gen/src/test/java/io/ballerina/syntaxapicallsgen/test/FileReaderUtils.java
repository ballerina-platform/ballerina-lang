/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.syntaxapicallsgen.test;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

/**
 * Reused file IO utils.
 *
 * @since 2.0.0
 */
public class FileReaderUtils {
    private static final String SPECIAL_DELIMITER = "\\A";

    private FileReaderUtils() {
    }

    /**
     * Reads a file path content from the resources directory.
     *
     * @param path path of the file. (root is the resources directory)
     * @return Content of the file.
     */
    public static String readFileAsResource(String path) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(path);
        Objects.requireNonNull(inputStream, "File open failed");
        Scanner scanner = new Scanner(inputStream, Charset.defaultCharset()).useDelimiter(SPECIAL_DELIMITER);
        return scanner.hasNext() ? scanner.next() : "";
    }

    public static File getResourceFile(String fileName) throws URISyntaxException {
        URL fileUrl = FileReaderUtils.class.getClassLoader().getResource(fileName);
        Objects.requireNonNull(fileUrl, "Template file resource could not be found.");
        return Paths.get(fileUrl.toURI()).toFile();
    }
}
