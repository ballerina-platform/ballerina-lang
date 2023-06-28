/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.projects.plugins.completion;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.Document;
import io.ballerina.projects.plugins.codeaction.PositionedActionContextImpl;
import io.ballerina.tools.text.LinePosition;

import java.nio.file.Path;

/**
 * Implementation of completion plugin context.
 *
 * @since 2201.7.0
 */
public class CompletionContextImpl extends PositionedActionContextImpl implements CompletionContext {

    private final Node nodeAtCursor;
    private final int cursorPosInTree;

    protected CompletionContextImpl(String fileUri,
                                    Path filePath,
                                    Node nodeAtCursor,
                                    int cursorPosInTree,
                                    LinePosition cursorPosition,
                                    Document document,
                                    SemanticModel semanticModel) {
        super(fileUri, filePath, cursorPosition, document, semanticModel);
        this.nodeAtCursor = nodeAtCursor;
        this.cursorPosInTree = cursorPosInTree;
    }

    @Override
    public Node nodeAtCursor() {
        return nodeAtCursor;
    }

    @Override
    public int cursorPosInTree() {
        return this.cursorPosInTree;
    }

    public static CompletionContextImpl from(String fileUri,
                                             Path filePath,
                                             LinePosition cursorPosition,
                                             int cursorPosInTree,
                                             Node nodeAtCursor,
                                             Document document,
                                             SemanticModel semanticModel) {
        return new CompletionContextImpl(fileUri, filePath, nodeAtCursor, cursorPosInTree,
                cursorPosition, document, semanticModel);
    }
}
