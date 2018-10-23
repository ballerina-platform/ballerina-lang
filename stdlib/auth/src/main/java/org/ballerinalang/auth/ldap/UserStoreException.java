/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.auth.ldap;

/**
 * The exception to throw when there is a problem with the user store.
 *
 * @since 0.983.0
 */
public class UserStoreException extends Exception {

    /**
     * Default serial.
     */
    private static final long serialVersionUID = -6057036683816666265L;

    /**
     * Constructs a new UserStoreException with {@code null} as its detail message.
     */
    public UserStoreException() {
        super();
    }

    /**
     * Constructs a new UserStoreException with the specified detail message and
     * cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public UserStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public UserStoreException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param  cause the cause.
     */
    public UserStoreException(Throwable cause) {
        super(cause);
    }
}
