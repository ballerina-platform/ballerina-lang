/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents the common class for the default Ballerina Code Action Providers.
 *
 * @since 1.1.1
 */
public abstract class AbstractCodeActionProvider implements LSCodeActionProvider {
    private List<CodeActionNodeType> codeActionNodeTypes;
    private final boolean isNodeTypeBased;

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Create a diagnostic based code action provider.
     */
    public AbstractCodeActionProvider() {
        this.isNodeTypeBased = false;
    }

    /**
     * Create a node type based code action provider.
     *
     * @param nodeTypes code action node types list
     */
    public AbstractCodeActionProvider(List<CodeActionNodeType> nodeTypes) {
        this.codeActionNodeTypes = nodeTypes;
        this.isNodeTypeBased = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isNodeBasedSupported() {
        return this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDiagBasedSupported() {
        return !this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<CodeActionNodeType> getCodeActionNodeTypes() {
        return this.codeActionNodeTypes;
    }

    /**
     * Returns diagnostic message highlighted content.
     *
     * @param diagnostic {@link Diagnostic}
     * @param context    {@link LSContext}
     * @param document   {@link LSDocumentIdentifier}
     * @return diagnostic highlighted content
     */
    public static String getDiagnosedContent(Diagnostic diagnostic, LSContext context,
                                             LSDocumentIdentifier document) {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        StringBuilder content = new StringBuilder();
        Position start = diagnostic.getRange().getStart();
        Position end = diagnostic.getRange().getEnd();
        try (BufferedReader reader = new BufferedReader(
                new StringReader(docManager.getFileContent(document.getPath())))) {
            String strLine;
            int count = 0;
            while ((strLine = reader.readLine()) != null) {
                if (count >= start.getLine() && count <= end.getLine()) {
                    if (count == start.getLine()) {
                        content.append(strLine.substring(start.getCharacter()));
                        if (start.getLine() != end.getLine()) {
                            content.append(System.lineSeparator());
                        }
                    } else if (count == end.getLine()) {
                        content.append(strLine.substring(0, end.getCharacter()));
                    } else {
                        content.append(strLine).append(System.lineSeparator());
                    }
                }
                if (count == end.getLine()) {
                    break;
                }
                count++;
            }
        } catch (WorkspaceDocumentException | IOException e) {
            // ignore error
        }
        return content.toString();
    }

    /**
     * Returns offset position of the function invocation.
     *
     * @param diagnosedContent diagnose message highlighted content
     * @param position         diagnose message position
     * @return offset position skipping package alias
     */
    public static Position offsetPositionToInvocation(String diagnosedContent, Position position) {
//        Need to capture the correct function invocation position in chain & nested invocations
//        eg. General Invocations: lorry.get_color()
//            Chain invocations: lorry.get_color().print(10),
//            Package Prefixes: http:lorry.get_color()
//            Action invocations: http:lorry->action()
//            Nested invocations: crypto:hashMd5(str.toBytes())
//            Field accesses: http:lorry.get_color
//            String Params: lorry.get_color("test.invoke(\"")
//            Record literal: {a: 1, b: ""}
//            Lambda Functions: function() returns int { return 1; };
//            Type Casts: <int>1.1;
//            Streaming From Clauses: from var person in personList;
        position = new Position(position.getLine(), position.getCharacter() + 1);
        String content = diagnosedContent.trim();
        int pointer = content.length();
        int count = 0;

        // Remove in-line comments
        int counter = 0;
        boolean insideString = false;
        boolean insideStrTemplate = false;
        while (counter < content.length()) {
            char c = content.charAt(counter);
            Optional<Character> nextC = counter + 1 < content.length() ?
                    Optional.of(content.charAt(counter + 1)) : Optional.empty();
            if (c == '"' && (!nextC.isPresent() || nextC.get() != '\\')) {
                insideString = !insideString;
            } else if (c == '`') {
                insideStrTemplate = !insideStrTemplate;
            }
            if (!insideString && !insideStrTemplate && c == '/' && nextC.isPresent() && nextC.get() == '/') {
                // Found a comment, break
                String substring = content.substring(0, counter);
                content = substring.trim();
                pointer = content.length();
                count = diagnosedContent.length() - content.length();
                break;
            }
            counter++;
        }

        int len = content.length();
        len--;
        // In-line record literal
        if (content.charAt(0) == '{' && content.charAt(len) == ';' && content.charAt(--len) == '}') {
            return position;
        }
        // Type Casting
        if (content.charAt(0) == '<' && content.charAt(len) == ';') {
            return position;
        }
        // Streaming `from` clause
        if (content.startsWith("from ")) {
            return position;
        }
        // Streaming `start` clause
        if (content.startsWith("start ")) {
            return position;
        }
        int pendingLParenthesis = 0;
        boolean loop = true;
        insideString = false;
        insideStrTemplate = false;
        while (loop) {
            pointer--;
            if (content.length() == 1) {
                count += 1;
                break;
            }
            // Check for stop-conditions
            char tailChar = content.charAt(pointer);
            char tailPrevChar = content.charAt(pointer - 1);
            Optional<Character> tail2ndPrevChar = pointer > 1
                    ? Optional.of(content.charAt(pointer - 2)) : Optional.empty();
            if (tailChar == '"' && tailPrevChar != '\\') {
                insideString = !insideString;
            } else if (tailChar == '`') {
                insideStrTemplate = !insideStrTemplate;
            }
            if (!insideString && !insideStrTemplate) {
                if (pendingLParenthesis <= 0) {
                    boolean isRangeExpr = tail2ndPrevChar.isPresent()
                            && ((tailChar == '.' || tailChar == '<') && tailPrevChar == '.' &&
                            tail2ndPrevChar.get() == '.');
                    if (isRangeExpr) {
                        pointer -= 2;
                        count += 2;
                    } else if ((tailChar == '.') || tailChar == ':') {
                        // Break on field-access or package-prefix
                        count++;
                        break;
                    } else if ((tailPrevChar == '-' && tailChar == '>')) {
                        // Break on arrow-function invocations
                        break;
                    }
                }
                // Remove chars Right-to-Left
                if (tailChar == '(') {
                    pendingLParenthesis--;
                } else if (tailChar == ')') {
                    pendingLParenthesis++;
                }
            }
            content = content.substring(0, pointer);
            count++;
        }

        // Diagnosed message only contains the erroneous part of the line
        // Thus we offset into last
        int bal = diagnosedContent.length() - count;
        if (bal > 0) {
            position.setCharacter(position.getCharacter() + bal + 1);
        }
        return position;
    }

    /**
     * Returns a QuickFix Code action.
     *
     * @param commandTitle title of the code action
     * @param uri          uri
     * @return {@link CodeAction}
     */
    public static CodeAction createQuickFixCodeAction(String commandTitle, List<TextEdit> edits, String uri) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        CodeAction action = new CodeAction(commandTitle);
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
        action.setDiagnostics(diagnostics);
        return action;
    }
}
