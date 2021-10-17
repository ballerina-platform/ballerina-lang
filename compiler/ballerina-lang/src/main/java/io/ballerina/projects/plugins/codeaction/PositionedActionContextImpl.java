/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.projects.plugins.codeaction;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Abstract implementation of {@link PositionedActionContext}.
 *
 * @since 2.0.0
 */
public abstract class PositionedActionContextImpl implements PositionedActionContext {

    private final String fileUri;
    private final Path filePath;
    private final LinePosition cursorPosition;
    private final Document document;
    private final SemanticModel semanticModel;

    protected PositionedActionContextImpl(String fileUri, Path filePath, LinePosition cursorPosition,
                                        Document document, SemanticModel semanticModel) {
        this.fileUri = fileUri;
        this.filePath = filePath;
        this.cursorPosition = cursorPosition;
        this.document = document;
        this.semanticModel = semanticModel;
    }

    @Override
    public String fileUri() {
        return fileUri;
    }

    @Override
    public Path filePath() {
        return filePath;
    }

    @Override
    public Optional<LinePosition> cursorPosition() {
        return Optional.ofNullable(cursorPosition);
    }

    @Override
    public Document currentDocument() {
        return document;
    }

    @Override
    public SemanticModel currentSemanticModel() {
        return semanticModel;
    }
}
