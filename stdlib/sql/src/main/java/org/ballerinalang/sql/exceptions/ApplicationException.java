/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.sql.exceptions;

/**
 * The exception to throw application related exceptions of SQL client.
 *
 * @since 1.2.0
 */
public class ApplicationException extends Exception {

    private String errorMessage = null;

    /**
     * Constructs a new {@link ApplicationException} with the specified error reason message.
     *
     * @param reason Error Reason
     */
    public ApplicationException(String reason) {
        super(reason);
    }

    /**
     * Constructs a new {@link ApplicationException} with the specified error reason and errorMessage message.
     *
     * @param reason Error Reason
     * @param errorMessage Detail error message
     */
    public ApplicationException(String reason, String errorMessage) {
        super(reason);
        this.errorMessage = errorMessage;
    }

    public String getDetailedErrorMessage() {
        return errorMessage;
    }
}
