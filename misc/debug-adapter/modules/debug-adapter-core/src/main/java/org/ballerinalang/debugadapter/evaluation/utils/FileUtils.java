/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.utils;

import org.ballerinalang.debugadapter.evaluation.EvaluationException;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;

/**
 * Utilities related to file system.
 *
 * @since 2.0.0
 */
public class FileUtils {

    /**
     * Helper method to write a string source to a file.
     *
     * @param content Content to write to the file.
     * @throws EvaluationException If writing was unsuccessful.
     */
    public static void writeToFile(File file, String content) throws EvaluationException {
        try (FileWriter fileWriter = new FileWriter(file, Charset.defaultCharset())) {
            fileWriter.write(content);
        } catch (Exception e) {
            throw createEvaluationException(String.format("error occurred while writing to a temp file at: '%s'",
                    file.getPath()));
        }
    }

    /**
     * Delete the given directory along with all files and sub directories.
     *
     * @param directoryPath Directory to delete.
     */
    public static boolean deleteDirectory(Path directoryPath) {
        try {
            File directory = new File(String.valueOf(directoryPath));
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File f : files) {
                        boolean success = deleteDirectory(f.toPath());
                        if (!success) {
                            return false;
                        }
                    }
                }
            }
            return directory.delete();
        } catch (Exception ignored) {
            return false;
        }
    }
}
