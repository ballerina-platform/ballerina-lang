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
package org.ballerinalang.langserver.simulator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a tab in the {@link Editor}. Simulates the behavior of cursor and current text in the document.
 */
public class EditorTab {

    private static final Logger logger = LoggerFactory.getLogger(EditorTab.class);

    private final Path filePath;
    private final Endpoint endpoint;
    private final BallerinaLanguageServer languageServer;

    private TextDocument textDocument;
    private Position cursor;

    private final Random random = new Random();
    private final PrintWriter writer = new PrintWriter(System.out, true, Charset.defaultCharset());

    public EditorTab(Path filePath, Endpoint endpoint, BallerinaLanguageServer languageServer) {
        this.filePath = filePath;
        this.endpoint = endpoint;
        this.languageServer = languageServer;
        try {
            String content = Files.readString(filePath);
            this.textDocument = TextDocuments.from(content);
            logger.info("Opening document: {}", filePath);
            TestUtil.openDocument(endpoint, filePath);
            LinePosition linePosition = textDocument.linePositionFrom(content.length() - 1);
            cursor(linePosition.line(), linePosition.offset());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Simulates a user typing the provided content in the editor. Content is typed character by character similarly to
     * how a user does it.
     *
     * @param content Text content to be typed in the editor.
     */
    public void type(String content) {
        int missCount = 0;
        for (int i = 0; i < content.length(); i++) {
            String typedChar = Character.toString(content.charAt(i));

            int startOffset = textDocument.textPositionFrom(LinePosition.from(cursor.getLine(), cursor.getCharacter()));
            TextEdit edit = TextEdit.from(TextRange.from(startOffset, 0), typedChar);
            TextDocumentChange change = TextDocumentChange.from(new TextEdit[]{edit});
            textDocument = textDocument.apply(change);

            LinePosition newLinePos = textDocument.linePositionFrom(startOffset + 1);
            try {
                TestUtil.didChangeDocument(this.endpoint, this.filePath, textDocument.toString());
            } catch (Throwable t) {
                logger.error("Caught error in didChange", t);
            }
            cursor(newLinePos.line(), newLinePos.offset());
            // logger.info("Added char: {} and cursor advanced to: {}", typedChar, newLinePos);

            if (i % 10 == 0) {
                float completionPercentage = ((float) i / (float) content.length()) * 100;
                writer.printf("%.1f%%\r", completionPercentage);
            }

            // Get completions in the background
            if (i % 3 == 0) {
                CompletableFuture.runAsync(this::completions);
                CompletableFuture.runAsync(this::codeActions);
            }

            if (!isDocumentInSync()) {
                missCount++;
            }

            try {
                Thread.sleep(100 + random.nextInt(300));
            } catch (InterruptedException e) {
                logger.error("Interrupted", e);
                break;
            }
        }
        logger.info("Typed provided content in file: {} -> \n{}",
                filePath, content.substring(0, Math.min(20, content.length())));
        logger.info("Typed {} characters with {} out of sync scenarios", content.length(), missCount);

        while (!isDocumentInSync()) {
            logger.info("Document out of sync. Waiting 30 seconds and syncing...");
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                break;
            }
            TestUtil.didChangeDocument(this.endpoint, this.filePath, textDocument.toString());
        }
    }

    /**
     * Check if the document in this instance is similar to that is in the language server.
     *
     * @return
     */
    private boolean isDocumentInSync() {
        Optional<Document> document = languageServer.getWorkspaceManager().document(filePath);
        if (document.isPresent()) {
            return document.get().textDocument().toString().equals(textDocument.toString());
        } else {
            logger.warn("Document not found in workspace manager: {}", filePath);
        }

        return false;
    }

    /**
     * Get completions for the current cursor position.
     */
    public void completions() {
        String completionResponse = TestUtil.getCompletionResponse(filePath.toString(), cursor, endpoint, "");
        JsonObject json = JsonParser.parseString(completionResponse).getAsJsonObject();
        boolean hasError = false;
        if (json.has("result") && json.get("result").isJsonObject()) {
            JsonObject result = json.getAsJsonObject("result");
            if (!result.has("left") || !result.get("left").isJsonArray()) {
                hasError = true;
            }
        } else {
            hasError = true;
        }

        if (hasError) {
            logger.warn("Completion request unsuccessful! cursor: {} -> {}", filePath, cursor);
        }
    }

    /**
     * Get code actions for the current cursor position.
     */
    public void codeActions() {
        CodeActionContext codeActionContext = new CodeActionContext(Collections.emptyList());
        Range range = new Range(cursor, cursor);
        TestUtil.getCodeActionResponse(endpoint, filePath.toString(), range, codeActionContext);
    }

    public void cursor(int line, int offset) {
        this.cursor = new Position(line, offset);
    }

    public Position cursor() {
        return this.cursor;
    }

    public SyntaxTree syntaxTree() {
        return SyntaxTree.from(textDocument);
    }

    public TextDocument textDocument() {
        return textDocument;
    }

    public Path filePath() {
        return filePath;
    }

    public void close() {
        logger.info("Closing document: {}", filePath());
        TestUtil.closeDocument(endpoint, filePath());
    }

    @Override
    public String toString() {
        return "EditorTab{" +
                "filePath=" + filePath +
                ", cursor=(" + cursor.getLine() + ", " + cursor.getCharacter() + ")" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EditorTab editorTab = (EditorTab) o;
        return Objects.equals(filePath, editorTab.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }
}
