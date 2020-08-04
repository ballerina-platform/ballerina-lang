/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.directory;

import java.nio.file.Path;

/**
 * {@code ModuleFileData} represents a Ballerina source file (.bal).
 *
 * @since 2.0.0
 */
public class DocumentData {
    private final String name;
    private final Path filePath;

    private DocumentData(String name, Path filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    public static DocumentData from(String name, Path filePath) {
        return new DocumentData(name, filePath);
    }

    public Path filePath() {
        return filePath;
    }

    public String name() {
        return name;
    }
}
