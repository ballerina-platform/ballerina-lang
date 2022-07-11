/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.shell.service;

import java.io.File;

/**
 * Response format for balShell/getShellFileSource endpoint.
 *
 * @since 2201.1.1
 */
public class ShellFileSourceResponse {
    private final String filePath;
    private final String content;

    public ShellFileSourceResponse(File file, String content) {
        this.filePath = file.toURI().toString();
        this.content = content;
    }

    public ShellFileSourceResponse() {
        this.filePath = "";
        this.content = "";
    }

    /**
     * Returns buffer file path.
     *
     * @return buffer file path.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Get content from the file.
     *
     * @return content of file
     */
    public String getContent() {
        return content;
    }
}
