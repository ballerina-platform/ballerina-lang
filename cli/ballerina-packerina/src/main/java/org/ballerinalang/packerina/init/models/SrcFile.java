/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.init.models;

/**
 * Model class for a source file.
 */
public class SrcFile {
    private SrcFileType srcFileType;
    private String content = "";
    private String name;
    public SrcFile(String name, SrcFileType fileType) {
        this.srcFileType = fileType;
        this.name = name;
        switch (fileType) {
            case SERVICE:
                content = "service<http> service1 {}";
                break;
            case MAIN:
            default:
                if (!this.name.toLowerCase().endsWith(".bal")) {
                    this.name = this.name + ".bal";
                }
                content = "public function main(string[] args){}";
                break;
        }
    }
    
    public SrcFileType getSrcFileType() {
        return srcFileType;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Enum for the source file type.
     */
    public enum SrcFileType {
        SERVICE,
        MAIN
    }
}

