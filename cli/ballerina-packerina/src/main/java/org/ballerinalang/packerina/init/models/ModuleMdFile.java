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
 * Model class for a Module.md.
 */
public class ModuleMdFile {

    private static final String SERVICE_CONTENT = "# Hello Service\n" +
            "\n" +
            "Serves \"hello world\" over HTTP GET";

    private static final String MAIN_FUNCTION_CONTENT = "# Hello World\n" +
            "\n" +
            "Prints \"hello world\" to command line output";

    private String content;
    private String name;

    public ModuleMdFile(String name, FileType fileType) {
        this.name = name;
        switch (fileType) {
            case SERVICE:
                content = SERVICE_CONTENT;
                break;
            case MAIN:
            default:
                content = MAIN_FUNCTION_CONTENT;
                break;
        }
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }
}
