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
package org.ballerinalang.test.utils;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Util class for SQL DB Tests.
 *
 * @since 0.8.0
 */
public class SQLDBUtils {

    public static final String DB_DIRECTORY = "./target/tempdb/";
    public static final String DB_DIRECTORY_H2_1 = "./target/H2_1/";
    public static final String DB_DIRECTORY_H2_2 = "./target/H2_2/";
    private static final Logger log = LoggerFactory.getLogger(SQLDBUtils.class);

    /**
     * Create H2 DB with the given name and initialize with given SQL file.
     *
     * @param dbDirectory Name of the DB directory.
     * @param dbName  Name of the DB instance.
     * @param sqlFile SQL statements for initialization.
     */
    public static void initH2Database(String dbDirectory, String dbName, String sqlFile) {
        String jdbcURL = "jdbc:h2:file:" + dbDirectory + dbName;
        initDatabase(jdbcURL, "sa", "", sqlFile);
    }

    /**
     * Create a DB and initialize with given SQL file.
     *
     * @param jdbcURL JDBC URL
     * @param username  Username for the DB
     * @param password Password to connect to the DB
     * @param sqlFile SQL statements for initialization.
     */
    public static void initDatabase(String jdbcURL, String username, String password, String sqlFile) {
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
     * @param affix    Affix for finding the matching files to delete.
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
        InputStream is = null;
        String fileAsString = null;
        URL fileResource = BCompileUtil.class.getClassLoader().getResource(path);
        try {
            is = new FileInputStream(fileResource.getFile());
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            fileAsString = sb.toString();
        } catch (IOException e) {
            // Ignore here
        }
        return fileAsString;
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
     * This class represents a container based database used for testing data clients.
     */
    public static class ContainerizedTestDatabase extends TestDatabase {
        private JdbcDatabaseContainer databaseContainer;

        public ContainerizedTestDatabase(DBType dbType, String databaseScript) {
            this(dbType);
            SQLDBUtils.initDatabase(jdbcUrl, username, password, databaseScript);
        }

        public ContainerizedTestDatabase(DBType dbType) {
            switch (dbType) {
            case MYSQL:
                databaseContainer = new MySQLContainer();
                break;
            case POSTGRES:
                databaseContainer = new PostgreSQLContainer();
                break;
            default:
                throw new UnsupportedOperationException(
                        "Creating a containerized database is not supported for: " + dbType);
            }
            databaseContainer.start();
            jdbcUrl = databaseContainer.getJdbcUrl();
            username = databaseContainer.getUsername();
            password = databaseContainer.getPassword();
        }

        public void stop() {
            databaseContainer.stop();
        }
    }

    /**
     * This class represents a file based database used for testing data clients.
     */
    public static class FileBasedTestDatabase extends TestDatabase {
        private String dbDirectory;

        public FileBasedTestDatabase(DBType dbType, String databaseScript, String dbDirectory, String dbName) {
            this(dbType, dbDirectory, dbName);
            SQLDBUtils.initDatabase(jdbcUrl, username, password, databaseScript);
        }

        public FileBasedTestDatabase(DBType dbType, String dbDirectory, String dbName) {
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
