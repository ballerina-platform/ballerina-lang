/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.database.sql.exceptions;

import java.sql.SQLException;

/**
 * The exception to throw Ballerina SQL database related exceptions.
 *
 * @since 1.0.0
 */
public class DatabaseException extends Exception {

    private int sqlErrorCode;
    private String sqlState = null;
    private String sqlErrorMessage = null;

    public DatabaseException() {
        super();
    }

    /**
     * Constructs a new {@link DatabaseException} with the specified Sql error code, Sql state and Sql error message.
     *
     * @param reason Error Reason
     * @param sqlErrorCode SQL database error code
     * @param sqlState SQL database error state
     * @param sqlErrorMessage SQL database error message
     */
    public DatabaseException(String reason, int sqlErrorCode, String sqlState, String sqlErrorMessage) {
        super(reason);
        this.sqlErrorCode = sqlErrorCode;
        this.sqlState = sqlState;
        this.sqlErrorMessage = sqlErrorMessage;
    }

    /**
     * Constructs a new {@link DatabaseException} with the specified detail message and cause.
     *
     * @param reason Error Reason
     * @param cause   Cause
     */
    public DatabaseException(String reason, SQLException cause) {
        super(reason);
        this.sqlErrorCode = cause.getErrorCode();
        this.sqlState = cause.getSQLState();
        this.sqlErrorMessage = cause.getMessage();
    }

    public String getSqlState() {
        return sqlState;
    }

    public String getSqlErrorMessage() {
        return sqlErrorMessage;
    }

    public int getSqlErrorCode() {
        return sqlErrorCode;
    }
}
