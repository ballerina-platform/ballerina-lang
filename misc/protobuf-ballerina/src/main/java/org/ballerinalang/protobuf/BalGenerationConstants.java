/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.protobuf;

import java.io.File;

/**
 * Bal Generation Tool contants class.
 */
public class BalGenerationConstants {
    public static final String OS_NAME_SYSTEM_PROPERTY = "os.name";
    public static final String OS_ARCH_SYSTEM_PROPERTY = "os.arch";
    public static final String FILE_SEPARATOR = File.separator;
    public static final String META_LOCATION = "desc_gen" + FILE_SEPARATOR;
    public static final String META_DEPENDENCY_LOCATION = "desc_gen" + FILE_SEPARATOR
            + "dependencies";
    public static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");
    public static final String GOOGLE_STANDARD_LIB = "google" + FILE_SEPARATOR + "protobuf";
    public static final String SPACE_CHARACTER = " ";
    public static final String EMPTY_STRING = "";
    public static final String EXE_PATH_PLACEHOLDER = "{{EXE_PATH}}";
    public static final String PROTO_PATH_PLACEHOLDER = "{{PROTO_PATH}}";
    public static final String PROTO_FOLDER_PLACEHOLDER = "{{PROTO_FOLDER}}";
    public static final String DESC_PATH_PLACEHOLDER = "{{DESC_PATH}}";
    public static final String PLUGIN_PROTO_FILEPATH = "google" + FILE_SEPARATOR + "protobuf" +
            FILE_SEPARATOR + "compiler" + FILE_SEPARATOR +
            "plugin.proto";
    public static final String COMPONENT_IDENTIFIER = "grpc";
    public static final String PROTOC_PLUGIN_EXE_PREFIX = ".exe";
    public static final String PROTO_SUFFIX = ".proto";
    public static final String DESC_SUFFIX = ".desc";
    public static final String PROTOC_PLUGIN_EXE_URL_SUFFIX = "http://repo1.maven.org/maven2/com/google/" +
            "protobuf/protoc/";
}
