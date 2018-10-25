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
package org.ballerinalang.util.program;

import org.ballerinalang.util.BLangConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * This class contains a set of static methods to operate on {@code BLangProgram} objects.
 *
 * @since 0.8.0
 */
public class BLangPrograms {

    public static Path validateAndResolveSourcePath(Path programDirPath, Path sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("source module/file cannot be null");
        }

        try {
            Path realSourcePath = programDirPath.resolve(sourcePath).toRealPath();

            if (Files.isDirectory(realSourcePath, LinkOption.NOFOLLOW_LINKS)) {
                return realSourcePath;
            }

            if (!realSourcePath.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                throw new IllegalArgumentException("invalid file: " + sourcePath);
            }

            return realSourcePath;
        } catch (NoSuchFileException x) {
            throw new IllegalArgumentException("no such file or directory: " + sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + sourcePath +
                    " reason: " + e.getMessage(), e);
        }
    }

    public static void createDirectory(Path dirPath) {
        try {
            if (Files.exists(dirPath) && Files.isDirectory(dirPath, LinkOption.NOFOLLOW_LINKS)) {
                return;
            } else if (Files.exists(dirPath) && !Files.isDirectory(dirPath, LinkOption.NOFOLLOW_LINKS)) {
                throw new RuntimeException("error creating user repository: a file with same name as " +
                        "the directory exists: '" + dirPath.toString() + "'");
            }

            Files.createDirectory(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("error creating Ballerina user repository: " + e.getMessage());
        }
    }
}
