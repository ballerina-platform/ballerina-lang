/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.file.service;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import org.wso2.transport.localfilesystem.server.util.Constants;

/**
 * Constants for Directory Listener Server connector.
 */
public class DirectoryListenerConstants {

    public static final String MODULE_NAME = "file";
    public static final String MODULE_VERSION = "0.5.0";

    //Annotation
    public static final BString ANNOTATION_PATH = StringUtils.fromString("path");
    public static final BString ANNOTATION_DIRECTORY_RECURSIVE = StringUtils.fromString(
            Constants.DIRECTORY_WATCH_RECURSIVE);

    public static final String FILE_SYSTEM_EVENT = "FileEvent";
    public static final String FS_SERVER_CONNECTOR = "serverConnector";
    public static final BString SERVICE_ENDPOINT_CONFIG = StringUtils.fromString("config");

    public static final String EVENT_CREATE = Constants.EVENT_CREATE;
    public static final String EVENT_DELETE = Constants.EVENT_DELETE;
    public static final String EVENT_MODIFY = Constants.EVENT_MODIFY;

    public static final String RESOURCE_NAME_ON_CREATE = "onCreate";
    public static final String RESOURCE_NAME_ON_DELETE = "onDelete";
    public static final String RESOURCE_NAME_ON_MODIFY = "onModify";
    public static final String RESOURCE_NAME_ON_MESSAGE = "onMessage";
}
