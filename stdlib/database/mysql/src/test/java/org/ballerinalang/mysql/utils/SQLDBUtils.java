/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.mysql.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Util class for MySQL DB Tests.
 *
 * @since 1.2.0
 */
public class SQLDBUtils {

    public static final String DB_DIRECTORY = Paths.get(".", "target", "mysql-data").toString()
            + File.separator;
    public static final String DB_HOST = "localhost";
    public static final int DB_PORT = 3305;
    public static final String DB_USER_NAME = "test";
    public static final String DB_USER_PW = "test123";
    public static final String SQL_APPLICATION_ERROR_REASON = "{ballerina/sql}ApplicationError";
    public static final String SQL_ERROR_MESSAGE = "message";

    public static final String SQL_RESOURCE_DIR = Paths.get("datafiles", "sql").toString();
    public static final String CONNECTIONS_DIR = "connections";
    public static final boolean ENABLE_TEST = !SQLDBUtils.isWindows();

    private static final Logger log = LoggerFactory.getLogger(SQLDBUtils.class);

    /**
     * Delete the given directory along with all files and sub directories.
     *
     * @param directory Directory to delete.
     */
    public static void deleteDirectory(File directory) {
        try {
            Files.walk(directory.toPath(), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            log.error("Error while deleting database directory: ", e);
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
}
