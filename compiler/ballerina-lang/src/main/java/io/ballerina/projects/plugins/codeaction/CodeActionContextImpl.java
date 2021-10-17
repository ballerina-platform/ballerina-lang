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
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;

import java.nio.file.Path;

/**
 * Implementation of code action plugin context.
 *
 * @since 2.0.0
 */
public class CodeActionContextImpl extends PositionedActionContextImpl implements CodeActionContext {

    private Diagnostic diagnostic;

    private CodeActionContextImpl(String fileUri, Path filePath, LinePosition cursorPosition,
                                  Document document, SemanticModel semanticModel, Diagnostic diagnostic) {
        super(fileUri, filePath, cursorPosition, document, semanticModel);
        this.diagnostic = diagnostic;
    }

    @Override
    public Diagnostic diagnostic() {
        return this.diagnostic;
    }

    public static CodeActionContextImpl from(String fileUri, Path filePath, LinePosition cursorPosition,
                                             Document document, SemanticModel semanticModel, Diagnostic diagnostic) {
        return new CodeActionContextImpl(fileUri, filePath, cursorPosition, document, semanticModel, diagnostic);
    }
}
