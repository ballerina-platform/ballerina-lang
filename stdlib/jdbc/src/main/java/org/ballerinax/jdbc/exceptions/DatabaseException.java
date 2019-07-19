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
package org.ballerinax.jdbc.exceptions;

import java.sql.SQLException;

/**
 * The exception to throw SQL database related exceptions of JDBC client.
 *
 * @since 1.0.0
 */
public class DatabaseException extends SQLException {

    private int sqlErrorCode;
    private String sqlState;
    private String sqlErrorMessage;
    private String reason;

    /**
     * Constructs a new {@link DatabaseException} with the specified detail message and cause.
     *
     * @param reason Error Reason
     * @param cause   Cause
     */
    public DatabaseException(String reason, SQLException cause) {
        super(reason);
        this.reason = reason;
        this.sqlErrorCode = cause.getErrorCode();
        this.sqlState = cause.getSQLState();
        this.sqlErrorMessage = cause.getMessage();
    }

    @Override
    public String getSQLState() {
        return sqlState;
    }

    @Override
    public String getMessage() {
        return reason + sqlErrorMessage;
    }

    @Override
    public int getErrorCode() {
        return (sqlErrorCode);
    }
}
