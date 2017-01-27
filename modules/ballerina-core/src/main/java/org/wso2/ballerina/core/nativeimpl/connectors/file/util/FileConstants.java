/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.connectors.file.util;

/**
 * Constants for File
 *
 * @since 1.0.0
 */
public final class FileConstants {
    public static final String FILE_LOCATION = "source";
    public static final String CONTENT_TYPE = "contentType";
    public static final String FILE_PATTERN = "filePattern";
    public static final String NEW_FILE_LOCATION = "destination";
    public static final String CONTENT = "inputContent";
    public static final String ENCODING = "encoding";
    public static final String NAMESPACE = "ns";
    public static final String RESULT = "result";
    public static final String FILE = "file";
    public static final String FILECON = "http://org.wso2.esbconnectors.FileConnector";
    public static final int BUFFER_SIZE = 4096;
    public static final String DEFAULT_ENCODING = "UTF8";
    public static final String SET_TIME_OUT = "setTimeout";
    public static final String SET_PASSIVE_MODE = "setPassiveMode";
    public static final String SET_SO_TIMEOUT = "setSoTimeout";
    public static final String SET_STRICT_HOST_KEY_CHECKING = "setStrictHostKeyChecking";
    public static final String SET_USER_DIRISROOT = "setUserDirIsRoot";
    public static final int TIME_OUT = 100000;
    public static final String START_TAG = "<result><success>";
    public static final String END_TAG = "</success></result>";
    public static final String FILE_EXIST_START_TAG = "<result><fileExist>";
    public static final String FILE_EXIST_END_TAG = "</fileExist></result>";
    public static final String RECURSIVE_SEARCH = "recursiveSearch";
    public static final String PROXY_HOST = "proxyHost";
    public static final String PROXY_PORT = "proxyPort";
    public static final String PROXY_USERNAME = "proxyUsername";
    public static final String PROXY_PASSWORD = "proxyPassword";
    public static final String FTP_SERVER = "ftpServer";
    public static final String FTP_OVER_HTTP = "ftpOverHttp";
    public static final String FTP_PORT = "ftpPort";
    public static final String KEEP_ALIVE_TIMEOUT = "keepAliveTimeout";
    public static final String CONTROL_KEEP_ALIVE_REPLY_TIMEOUT = "controlKeepAliveReplyTimeout";
    public static final String FTP_USERNAME = "ftpUsername";
    public static final String FTP_PASSWORD = "ftpPassword";
    public static final String TARGET_PATH = "targetPath";
    public static final String TARGET_FILE = "targetFile";
    public static final String BINARY_TRANSFER = "binaryTransfer";
    public static final String LOCAL_ACTIVE = "localActive";
    public static final String STREAMING = "streaming";
    public static final String ADDRESS = "address";
    public static final String APPEND = "append";
    public static final String DEFAULT_RESPONSE_FILE = "/response.xml";

}
