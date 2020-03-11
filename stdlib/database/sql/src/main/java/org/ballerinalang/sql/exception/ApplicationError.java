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
package org.ballerinalang.sql.exception;

import java.sql.SQLException;

/**
 * This exception represents the errors and exception caused due to the application mis configurations or data.
 *
 * @since 1.2.0
 */
public class ApplicationError extends Exception {

    public ApplicationError(String message) {
        super(message);
    }

    public ApplicationError(String message, SQLException error) {
        super(message, error);
    }

}
