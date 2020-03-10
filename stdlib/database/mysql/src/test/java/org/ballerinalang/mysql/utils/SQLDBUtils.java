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

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Util class for MySQL DB Tests.
 *
 * @since 1.2.0
 */
public class SQLDBUtils {

    public static final String DB_HOST = "localhost";
    public static final int DB_PORT = 3305;
    public static final String DB_USER_NAME = "test";
    public static final String DB_USER_PW = "test123";
    public static final String SQL_APPLICATION_ERROR_REASON = "{ballerina/sql}ApplicationError";
    public static final String SQL_ERROR_MESSAGE = "message";

    public static final String SQL_RESOURCE_DIR = Paths.get("datafiles", "sql").toString();
    public static final String CONNECTIONS_DIR = "connection";
    public static final String QUERY_DIR = "query";
    public static final String EXECUTE_DIR = "execute";
    public static final String POOL_DIR = "pool";

    /**
     * Resolves the ballerina test source file path.
     *
     * @param subResourceDir The name of the subdirectory where the ballerina file belongs
     * @param resouceFileName The name of the ballerina file
     * @return The path of the ballerina file
     */
    public static String getBalFilesDir(String subResourceDir, String resouceFileName) {
        return Paths.get("test-src", subResourceDir, resouceFileName).toString();
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
