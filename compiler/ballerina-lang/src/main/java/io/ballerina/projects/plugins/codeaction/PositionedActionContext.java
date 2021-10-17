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
 * A parent context to hold information related to an operation performed against a position.
 *
 * @since 2.0.0
 */
public interface PositionedActionContext {

    /**
     * Get the file uri.
     *
     * @return {@link String} file uri
     */
    String fileUri();

    /**
     * Get the file path.
     *
     * @return {@link java.nio.file.Path} file path
     */
    Path filePath();

    /**
     * Get the cursor position.
     *
     * @return Optional {@link LinePosition}
     */
    Optional<LinePosition> cursorPosition();

    /**
     * Get the current document where the given file URI resides.
     *
     * @return {@link Document}
     */

    Document currentDocument();

    /**
     * Get the current semantic model where the given file URI resides.
     *
     * @return {@link SemanticModel}
     */
    SemanticModel currentSemanticModel();
}
