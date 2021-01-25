/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a generic MD document in a Ballerina package.
 *
 * @since 2.0.0
 */
public class MdDocument {
    private final Path filePath;
    private TextDocument textDocument;

    public MdDocument(Path filePath) {
        this.filePath = filePath;
    }

    public TextDocument textDocument() {
        if (textDocument != null) {
            return textDocument;
        }

        try {
            textDocument = TextDocuments.from(Files.readString(filePath));
        } catch (IOException e) {
            throw new ProjectException("Failed to read file: " + filePath, e);
        }
        return textDocument;
    }
}

