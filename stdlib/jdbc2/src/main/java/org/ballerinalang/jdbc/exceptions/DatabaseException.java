/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jdbc.exceptions;

import java.sql.SQLException;

/**
 * This exception represents the underneath database communication error.
 */
public class DatabaseException extends SQLException {

    /**
     * Constructs a new {@link DatabaseException} with the specified error reason message.
     *
     * @param errorMessage Error message
     */
    public DatabaseException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new {@link DatabaseException} with the specified error reason and errorMessage message.
     *
     * @param errorMessage Error message for the exception
     * @param ex           Exception that resulted in this error
     */
    public DatabaseException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }
}
