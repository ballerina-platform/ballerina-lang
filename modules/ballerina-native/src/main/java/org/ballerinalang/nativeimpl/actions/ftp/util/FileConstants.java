/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.actions.ftp.util;

/**
 * Constants for ftp client connector.
 */
public class FileConstants {
    public static final String FTP_CONNECTOR_NAME = "file";
    public static final String CONNECTOR_NAME = "ClientConnector";
    public static final String PROPERTY_URI = "uri";
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_DESTINATION = "destination";
    public static final String PROPERTY_ACTION = "action";
    public static final String PROPERTY_FOLDER = "create-folder";
    public static final String ACTION_COPY = "copy";
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_EXISTS = "exists";
    public static final String ACTION_MOVE = "move";
    public static final String ACTION_READ = "read";
    public static final String ACTION_WRITE = "write";
    public static final String TYPE_FILE = "file";
    public static final String TYPE_FOLDER = "folder";
}
