/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.file.service;

import org.wso2.transport.localfilesystem.server.util.Constants;

/**
 * Constants for Directory Listener Server connector.
 */
public class DirectoryListenerConstants {

    //Annotation
    public static final String ANNOTATION_PATH = "path";
    public static final String ANNOTATION_DIRECTORY_RECURSIVE = Constants.DIRECTORY_WATCH_RECURSIVE;

    public static final String FILE_SYSTEM_EVENT = "FileEvent";
    public static final String FS_SERVER_CONNECTOR = "serverConnector";
    public static final String SERVICE_ENDPOINT_CONFIG = "config";

    public static final String EVENT_CREATE = Constants.EVENT_CREATE;
    public static final String EVENT_DELETE = Constants.EVENT_DELETE;
    public static final String EVENT_MODIFY = Constants.EVENT_MODIFY;

    public static final String RESOURCE_NAME_ON_CREATE = "onCreate";
    public static final String RESOURCE_NAME_ON_DELETE = "onDelete";
    public static final String RESOURCE_NAME_ON_MODIFY = "onModify";
}
