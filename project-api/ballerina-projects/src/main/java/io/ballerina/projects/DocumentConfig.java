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
package io.ballerina.projects;

import java.nio.file.Path;
import java.util.Optional;

/**
 * {@code DocumentConfig} contains necessary configuration elements required to
 * create an instance of a {@code Document}.
 *
 * @since 2.0.0
 */
public class DocumentConfig {
    // This class should contain project-agnostic information
    private final DocumentId documentId;
    private final Path filePath;

    private DocumentConfig(DocumentId documentId, Path filePath) {
        this.documentId = documentId;
        this.filePath = filePath;
    }

    public static DocumentConfig from(DocumentId documentId, Path filePath) {
        return new DocumentConfig(documentId, filePath);
    }

    public DocumentId documentId() {
        return documentId;
    }

    public Optional<Path> filePath() {
        return Optional.ofNullable(filePath);
    }
}
