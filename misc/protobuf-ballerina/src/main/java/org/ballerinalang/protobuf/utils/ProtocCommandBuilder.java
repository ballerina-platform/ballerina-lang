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

import static org.ballerinalang.protobuf.BalGenerationConstants.DESC_PATH_PLACEHOLDER;
import static org.ballerinalang.protobuf.BalGenerationConstants.EXE_PATH_PLACEHOLDER;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTO_FOLDER_PLACEHOLDER;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTO_PATH_PLACEHOLDER;
import static org.ballerinalang.protobuf.BalGenerationConstants.SPACE_CHARACTER;

/**
 * This class is used to build the protoc compiler command to generate descriptor.
 */
public class ProtocCommandBuilder {
    private static final String COMMAND_PLACEHOLDER = EXE_PATH_PLACEHOLDER + SPACE_CHARACTER +
            "--proto_path=" + PROTO_FOLDER_PLACEHOLDER + SPACE_CHARACTER +
            PROTO_PATH_PLACEHOLDER + SPACE_CHARACTER +
            "--descriptor_set_out=" + DESC_PATH_PLACEHOLDER;
    private String exePath;
    private String protoPath;
    private String protoFolderPath;
    private String descriptorSetOutPath;
    
    public ProtocCommandBuilder(String exePath, String protoPath, String protofolderPath, String descriptorSetOutPath) {
        this.exePath = exePath;
        this.protoPath = protoPath;
        this.descriptorSetOutPath = descriptorSetOutPath;
        this.protoFolderPath = protofolderPath;
    }
    
    public String build() {
        return COMMAND_PLACEHOLDER.replace(EXE_PATH_PLACEHOLDER, this.exePath)
                .replace(PROTO_PATH_PLACEHOLDER, this.protoPath)
                .replace(DESC_PATH_PLACEHOLDER, descriptorSetOutPath)
                .replace(PROTO_FOLDER_PLACEHOLDER, protoFolderPath);
    }
}
