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
package org.ballerinalang.protobuf.utils;

/**
 * Bal Generation Tool contants class.
 */
public class BalGenerationConstants {
    public static final String META_LOCATION = "desc_gen/";
    public static final String META_DEPENDENCY_LOCATION = "desc_gen/dependencies/";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");
    public static final String GOOGLE_STANDARD_LIB = "google/protobuf";
    public static final String NEW_LINE_CHARATER = "\\\n";
    public static final String SPACE_CHARATER = " ";
    public static final String EMPTY_STRING = "";
    public static final String EXE_PATH_PLACEHOLDER = "{{EXE_PATH}}";
    public static final String PROTO_PATH_PLACEHOLDER = "{{PROTO_PATH}}";
    public static final String PROTO_FOLDER_PLACEHOLDER = "{{PROTO_FOLDER}}";
    public static final String DESC_PATH_PLACEHOLDER = "{{DESC_PATH}}";
    public static final String PLUGIN_PROTO_FILEPATH = "google/protobuf/compiler/plugin.proto";
}
