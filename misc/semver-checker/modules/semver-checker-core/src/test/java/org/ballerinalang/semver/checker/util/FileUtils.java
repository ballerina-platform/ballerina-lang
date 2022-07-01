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

package org.ballerinalang.semver.checker.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * File I/O related utilities.
 *
 * @since 2201.2.0
 */
public class FileUtils {

    /**
     * @param file    Name of the expected file
     * @param content string data which we want to write into the file
     * @throws IOException for handling the error while handling the file
     */
    public static void writeToFile(File file, String content) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file, Charset.defaultCharset())) {
            fileWriter.write(content);
        }
    }
}
