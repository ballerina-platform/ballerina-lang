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
 * Constants for SMTP-related functions.
 *
 * @since 1.1.5
 */
public class SmtpConstants {

    public static final String DEFAULT_PORT_NUMBER = "587";
    public static final String PROPS_HOST = "host";
    public static final String PROPS_PORT = "port";
    public static final String PROPS_USERNAME = "username";
    public static final String PROPS_PASSWORD = "password";
    public static final String PROPS_SESSION = "session";
    public static final String PROPS_SMTP_HOST = "mail.smtp.host";
    public static final String PROPS_SMTP_PORT = "mail.smtp.port";
    public static final String PROPS_SMTP_AUTH = "mail.smtp.auth";
    public static final String PROPS_SMTP_STARTTLS = "mail.smtp.starttls.enable";
    public static final String SMTP_ERROR_CODE = "{ballerina/email}Smtp";
    public static final String SMTP_ERROR_MESSAGE = "Error while sending the email.";

    private SmtpConstants() {
        // private constructor
    }

}
