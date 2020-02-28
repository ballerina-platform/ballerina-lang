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
 * Constants for IMAP-related functions.
 *
 * @since 1.1.3
 */
public class ImapConstants {
    public static final String DEFAULT_PORT_NUMBER = "143";
    public static final String IMAP_PROTOCOL = "imap";
    public static final String PROPS_IMAP_HOST = "mail.imap.host";
    public static final String PROPS_IMAP_PORT = "mail.imap.port";
    public static final String PROPS_IMAP_STARTTLS = "mail.imap.starttls.enable";
    public static final String PROPS_IMAP_AUTH = "mail.imap.auth";

    private ImapConstants() {
        // private constructor
    }
}
