/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Util class for SQL DB Tests.
 */
public class SQLDBUtils {

    public static final String DB_DIRECTORY = System.getProperty("libdir") + File.separator + "tempdb" + File.separator;
    private static final Logger LOG = LoggerFactory.getLogger(SQLDBUtils.class);

    /**
     * Delete the given directory along with all files and sub directories.
     *
     * @param directory Directory to delete.
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                boolean success = deleteDirectory(f);
                if (!success) {
                    return false;
                }
            }
        }
        return directory.delete();
    }

    /**
     * Delete all the files and sub directories which matches given prefix in a given directory.
     *
     * @param directory Directory which contains files to delete.
     * @param affix     Affix for finding the matching files to delete.
     */
    public static void deleteFiles(File directory, String affix) {
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                if (f.getName().startsWith(affix) || f.getName().endsWith(affix)) {
                    deleteDirectory(f);
                }
            }
        }
    }

    private static String readFileToString(String path) {
        URL fileResource = BCompileUtil.class.getClassLoader().getResource(path);
        try {
            if (fileResource == null) {
                fileResource = Paths.get(path).toUri().toURL();
            }
            return FileUtils.readFileToString(new File(fileResource.toURI()), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            LOG.error("Error ", e);
        }
        return null;
    }

    /**
     * This class represents a database used for testing data clients.
     */
    public abstract static class TestDatabase {
        String jdbcUrl;
        String username;
        String password;

        public String getJDBCUrl() {
            return jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public abstract void stop();
    }

    /**
     * Create a DB and initialize with given SQL file.
     *
     * @param jdbcURL JDBC URL
     * @param username  Username for the DB
     * @param password Password to connect to the DB
     * @param sqlFile SQL statements for initialization.
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
            LOG.error("Error while initializing database: ", e);
        }
    }

    /**
     * This class represents a file based database used for testing data clients.
     */
    public static class FileBasedTestDatabase extends TestDatabase {
        private String dbDirectory;

        public FileBasedTestDatabase(DBType dbType, String databaseScript, String dbDirectory, String dbName) {
            this.dbDirectory = dbDirectory;
            switch (dbType) {
            case H2:
                SQLDBUtils.deleteFiles(new File(dbDirectory), dbName);
                jdbcUrl = "jdbc:h2:file:" + dbDirectory + dbName;
                username = "sa";
                break;
            case HSQLDB:
                SQLDBUtils.deleteFiles(new File(dbDirectory), dbName);
                jdbcUrl = "jdbc:hsqldb:file:" + dbDirectory + dbName;
                username = "SA";
                break;
            default:
                throw new UnsupportedOperationException(
                        "Creating a file based database is not supported for: " + dbType);
            }
            password = "";
            initDatabase(jdbcUrl, username, password, databaseScript);
        }

        public void stop() {
            SQLDBUtils.deleteDirectory(new File(this.dbDirectory));
        }
    }

    /**
     * Database types used for testing data clients.
     */
    public enum DBType {
        MYSQL, ORACLE, POSTGRES, HSQLDB, H2
    }
}
