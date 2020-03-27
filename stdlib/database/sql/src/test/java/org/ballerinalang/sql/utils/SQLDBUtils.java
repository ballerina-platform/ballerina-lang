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
package org.ballerinalang.sql.utils;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

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
 * @since 1.3.0
 */
public class SQLDBUtils {
    private static final Logger log = LoggerFactory.getLogger(SQLDBUtils.class);

    public static final String URL_PREFIX = "jdbc:hsqldb:mem:";
    public static final String DB_DIR = Paths.get(".", "target", "tempdb").toString() + File.separator;
    public static final String DB_USER = "test";
    public static final String DB_PASSWORD = "test123";
    public static final String SQL_APPLICATION_ERROR_REASON = "{ballerina/sql}ApplicationError";
    public static final String SQL_ERROR_MESSAGE = "message";

    /**
     * Create H2 DB with the given name and initialize with given SQL file.
     *
     * @param dbName  Name of the DB instance.
     * @param sqlFile SQL statements for initialization.
     */
    public static void initHsqlDatabase(String dbName, String sqlFile) throws SQLException {
        String url = SQLDBUtils.URL_PREFIX + dbName;
        initDatabase(url, sqlFile);
    }

    /**
     * Create a DB and initialize with given SQL file.
     * @param jdbcURL  JDBC URL
     * @param sqlFile  SQL statements for initialization.
     */
    private static void initDatabase(String jdbcURL, String sqlFile)
            throws SQLException {
        String lastExecQuery = "";
        try (Connection connection = DriverManager.getConnection(jdbcURL, SQLDBUtils.DB_USER, SQLDBUtils.DB_PASSWORD);
             Statement st = connection.createStatement()) {
            String sql = readFileToString(sqlFile);
            String[] sqlQuery = sql.trim().split("/");
            for (String query : sqlQuery) {
                lastExecQuery = query.trim();
                st.executeUpdate(lastExecQuery);
            }
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new SQLException("Error when executing query:" + lastExecQuery, e);
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
     * Resolve the the path of the resource file.
     *
     * @param fileName Name of the resource file
     * @return Absolute path of the resource file
     */
    public static Path getResourcePath(String fileName) {
        return Paths.get("src", "test", "resources", fileName).toAbsolutePath();
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
     * Resolves the SQL script file path.
     *
     * @param subResourceDir  The name of the subdirectory where the sql script file belongs
     * @param resouceFileName The name of the sql script file
     * @return The path of the sql script file
     */
    public static String getSQLResourceDir(String subResourceDir, String resouceFileName) {
        return Paths.get("datafiles", "sql", subResourceDir, resouceFileName).toString();
    }

    /**
     * Resolves the ballerina test source file path.
     *
     * @param subResourceDir  The name of the subdirectory where the ballerina file belongs
     * @param resouceFileName The name of the ballerina file
     * @return The path of the ballerina file
     */
    public static String getBalFilesDir(String subResourceDir, String resouceFileName) {
        return Paths.get("test-src", subResourceDir, resouceFileName).toString();
    }

    /**
     * Resolves the ballerina sql mock module path.
     *
     * @return The path of the ballerina mock module
     */
    public static String getMockModuleDir() {
        return "test-module" + File.separator + "mock-sql";
    }

    /**
     * Validates the provided ballerina object is not an error type.
     *
     * @param value The object which needs to be validated.
     */
    public static void assertNotError(Object value) {
        if (value instanceof BError) {
            BError bError = (BError) value;
            String message = "Not expecting an error. Error details: \nReason:" + bError.getReason();
            Object details = bError.getDetails();
            if (details instanceof BMap) {
                BValue errMessage = ((BMap) details).get("message");
                if (errMessage != null) {
                    message += " , message: " + errMessage.stringValue();
                }
            }
            Assert.fail(message);
        }
    }
}
