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

import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Constants of the Email module.
 *
 * @since 1.2.0
 */
public class EmailConstants {

    // Common constants
    public static final String CONNECTOR_NAME = "email";
    public static final BPackage EMAIL_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, CONNECTOR_NAME);
    public static final String PROPS_PORT = "port";
    public static final String PROPS_USERNAME = "username";
    public static final String MESSAGE_TO = "to";
    public static final String MESSAGE_CC = "cc";
    public static final String MESSAGE_BCC = "bcc";
    public static final String MESSAGE_SUBJECT = "subject";
    public static final String MESSAGE_MESSAGE_BODY = "body";
    public static final String MESSAGE_FROM = "from";
    public static final String MESSAGE_SENDER = "sender";
    public static final String MESSAGE_REPLY_TO = "replyTo";
    public static final String MESSAGE_ATTACHMENTS = "attachments";

    // Common constants to POP and IMAP
    public static final String PROPS_SSL = "enableSsl";
    public static final String PROPS_HOST = "host";
    public static final String PROPS_PASSWORD = "password";
    public static final String PROPS_PROTOCOL = "protocol";
    public static final String PROPS_STORE = "store";
    public static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";
    public static final String MIME_CONTENT_TYPE_PATTERN = "multipart/*";
    public static final String READ_CLIENT_INIT_ERROR = "{ballerina/email}ReadClientInitError";
    public static final String READ_ERROR = "{ballerina/email}ReadError";
    public static final String ENDPOINT_CONFIG_SECURE_SOCKET = "secureSocket";
    public static final String ENDPOINT_CONFIG_PRIVATE_KEY = "privateKey";
    public static final String ENDPOINT_CONFIG_PATH = "path";
    public static final String ENDPOINT_CONFIG_PASS_KEY = "password";
    public static final String IDENTITY = "IDENTITY";
    public static final String IDENTITY_PASS_PHRASE = "IDENTITY_PASS_PHRASE";
    public static final String URI = "uri";
    public static final String EMAIL_SERVER_CONNECTOR = "serverConnector";
    public static final String PROTOCOL_CONFIG = "protocolConfig";
    public static final String DEFAULT_STORE_LOCATION = "INBOX";
    public static final String ON_MESSAGE = "onMessage";
    public static final String ON_ERROR = "onError";

    // POP related constants
    public static final String POP = "POP";
    public static final String POP_CLIENT = "PopClient";
    public static final String POP_PROTOCOL = "pop3";
    public static final String PROPS_POP_HOST = "mail.pop3.host";
    public static final String PROPS_POP_PORT = "mail.pop3.port";
    public static final String PROPS_POP_AUTH = "mail.pop.auth";
    public static final String PROPS_POP_STARTTLS = "mail.pop3.starttls.enable";
    public static final String PROPS_POP_SSL_ENABLE = "mail.pop3.ssl.enable";

    // IMAP related constants
    public static final String IMAP = "IMAP";
    public static final String IMAP_CLIENT = "ImapClient";
    public static final String IMAP_PROTOCOL = "imap";
    public static final String PROPS_IMAP_HOST = "mail.imap.host";
    public static final String PROPS_IMAP_PORT = "mail.imap.port";
    public static final String PROPS_IMAP_STARTTLS = "mail.imap.starttls.enable";
    public static final String PROPS_IMAP_SSL_ENABLE = "mail.imap.ssl.enable";
    public static final String PROPS_IMAP_AUTH = "mail.imap.auth";

    // SMTP related constants
    public static final String PROPS_SESSION = "session";
    public static final String PROPS_ENABLE_SSL = "mail.smtp.ssl.enable";
    public static final String PROPS_SMTP_HOST = "mail.smtp.host";
    public static final String PROPS_SMTP_PORT = "mail.smtp.port";
    public static final String PROPS_SMTP_AUTH = "mail.smtp.auth";
    public static final String PROPS_SMTP_STARTTLS = "mail.smtp.starttls.enable";
    public static final String SEND_ERROR = "{ballerina/email}SendError";

    public static final String EMAIL = "Email";
    public static final String ERROR = "Error";

    private EmailConstants() {
        // private constructor
    }
}
