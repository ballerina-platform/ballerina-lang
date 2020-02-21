/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinax.mysql.utils;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;

/**
 * Util class for SQL DB Tests.
 *
 * @since 0.990.3
 */
public class SQLDBUtils {

    public static final String DB_DIRECTORY = Paths.get(".", "target", "mysql-data").toString()
            + File.separator;
    public static final String DB_HOST = "localhost";
    public static final int DB_PORT = 3305;
    public static final String DB_USER_NAME  = "test";
    public static final String DB_USER_PW  = "test123";

    public static final String SQL_RESOURCE_DIR = Paths.get("datafiles" , "sql").toString();
    public static final String ACTIONS_DIR = "actions";
    public static final String CONNECTIONS_DIR = "connections";
    private static final Logger log = LoggerFactory.getLogger(SQLDBUtils.class);

    /**
     * Create H2 DB with the given name and initialize with given SQL file.
     *
     * @param dbDirectory Name of the DB directory.
     * @param dbName      Name of the DB instance.
     * @param sqlFile     SQL statements for initialization.
     */
    public static void initH2Database(String dbDirectory, String dbName, String sqlFile) {
        String jdbcURL = "mysql:h2:file:" + dbDirectory + dbName;
        initDatabase(jdbcURL, "sa", "", sqlFile);
    }

    /**
     * Create a DB and initialize with given SQL file.
     *
     * @param jdbcURL  JDBC URL
     * @param username Username for the DB
     * @param password Password to connect to the DB
     * @param sqlFile  SQL statements for initialization.
     */
    private static void initDatabase(String jdbcURL, String username, String password, String sqlFile) {
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement st = connection.createStatement()) {
            String sql = readFileToString(sqlFile);
            String[] sqlQuery = sql.trim().split("/");
            for (String query : sqlQuery) {
                st.executeUpdate(query.trim());
            }
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            log.error("Error while initializing database: ", e);
        }
    }

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

    /**
     * Delete all the files and sub directories which matches given prefix in a given directory.
     *
     * @param directory Directory which contains files to delete.
     * @param affix     Affix for finding the matching files to delete.
     */
    public static void deleteFiles(File directory, String affix) {
        if (!directory.isDirectory()) {
            return;
        }

        for (File f : directory.listFiles()) {
            if (f.getName().startsWith(affix) || f.getName().endsWith(affix)) {
                deleteDirectory(f);
            }
        }
    }

    private static String readFileToString(String path) {
        // The name of a resource is a '/'-separated path name that identifies the resource.
        // Hence regardless of the separator corresponding to the OS forward slash should be used.
        URL fileResource = BCompileUtil.class.getClassLoader().getResource(path.replace("\\", "/"));
        try {
            return FileUtils.readFileToString(new File(fileResource.toURI()), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            log.error("File reading failed", e);
        }
        return null;
    }

    /**
     * This class represents a database used for testing data clients.
     */
}
