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
 * Enum which holds the file type: service or main.
 */
public enum FileType {
    SERVICE("hello_service.bal", FileContentConstants.SERVICE_CONTENT),
    MAIN("main.bal", FileContentConstants.MAIN_FUNCTION_CONTENT),
    MAIN_TEST("main_test.bal", FileContentConstants.MAIN_FUNCTION_TEST_CONTENT),
    SERVICE_TEST("hello_service_test.bal", FileContentConstants.SERVICE_TEST_CONTENT);

    private final String fileName;
    private final String content;

    FileType(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }
}

