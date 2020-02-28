/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.email.util;

/**
 * Constants for POP3-related functions.
 *
 * @since 1.1.5
 */
public class PopConstants {
    public static final String DEFAULT_PORT_NUMBER = "995";
    public static final String PROPS_HOST = "host";
    public static final String PROPS_PORT = "port";
    public static final String PROPS_USERNAME = "username";
    public static final String PROPS_PASSWORD = "password";
    public static final String PROPS_STORE = "store";
    public static final String POP_PROTOCOL = "pop3";
    public static final String PROPS_FILTER_FOLDER = "folder";
    public static final String PROPS_POP_HOST = "mail.pop3.host";
    public static final String PROPS_POP_PORT = "mail.pop3.port";
    public static final String PROPS_POP_AUTH = "mail.pop.auth";
    public static final String PROPS_POP_STARTTLS = "mail.pop3.starttls.enable";
    public static final String POP_ERROR_CODE = "{ballerina/email}Pop";

    static final String EMAIL = "Email";

    private PopConstants() {
        // private constructor
    }
}
