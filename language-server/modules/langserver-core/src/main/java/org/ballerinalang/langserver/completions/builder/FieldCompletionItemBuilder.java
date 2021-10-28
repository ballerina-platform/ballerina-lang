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
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.Optional;

/**
 * Completion item builder for the object fields and for record fields.
 *
 * @since 2.0.0
 */
public class FieldCompletionItemBuilder {
    private FieldCompletionItemBuilder() {
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol record field symbol or the object field symbol
     * @return {@link CompletionItem} generated completion item
     */
    private static CompletionItem getCompletionItem(Symbol symbol) {
        String recordFieldName = symbol.getName().orElseThrow();

        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(recordFieldName);
        completionItem.setInsertText(recordFieldName);
        completionItem.setKind(CompletionItemKind.Field);
        return completionItem;
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol  {@link RecordFieldSymbol}
     * @param context Completion context
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(RecordFieldSymbol symbol, BallerinaCompletionContext context) {
        String recordFieldName = symbol.getName().orElseThrow();
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(recordFieldName);
        completionItem.setKind(CompletionItemKind.Field);
        OptionalFieldAccessWriteAmbiguityResolver resolver = new OptionalFieldAccessWriteAmbiguityResolver(context);
        Boolean readableContext = context.getNodeAtCursor().apply(resolver);

        String insertText = recordFieldName;
        // Here we added null check since the value is null for ExternalTreeNodeList.
        if (readableContext != null && readableContext) {
            Optional<TextEdit> recordFieldAdditionalTextEdit = getRecordFieldAdditionalTextEdit(symbol, context);
            if (recordFieldAdditionalTextEdit.isPresent()) {
                insertText = "?." + recordFieldName;
                completionItem.setAdditionalTextEdits(Collections.singletonList(recordFieldAdditionalTextEdit.get()));
            }
        }

        completionItem.setInsertText(insertText);

        return completionItem;
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param objectFieldSymbol {@link ObjectFieldSymbol}
     * @param withSelfPrefix    {@link Boolean}
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(ObjectFieldSymbol objectFieldSymbol, boolean withSelfPrefix) {
        if (withSelfPrefix) {
            String label = "self." + objectFieldSymbol.getName().get();

            CompletionItem item = new CompletionItem();
            item.setLabel(label);
            item.setInsertText(label);
            item.setKind(CompletionItemKind.Field);

            return item;
        }

        return build(objectFieldSymbol);
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol {@link ObjectFieldSymbol}
     * @return {@link CompletionItem} generated completion item
     */
    private static CompletionItem build(ObjectFieldSymbol symbol) {
        return getCompletionItem(symbol);
    }

    private static Optional<TextEdit> getRecordFieldAdditionalTextEdit(RecordFieldSymbol recordFieldSymbol,
                                                                       BallerinaCompletionContext context) {
        if (!recordFieldSymbol.isOptional()) {
            return Optional.empty();
        }

        NonTerminalNode evalNode = context.getNodeAtCursor();
        if (evalNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            evalNode = evalNode.parent();
        }

        LineRange dotTokenLineRange;
        if (evalNode.kind() == SyntaxKind.FIELD_ACCESS) {
            dotTokenLineRange = ((FieldAccessExpressionNode) evalNode).dotToken().lineRange();
        } else if (evalNode.kind() == SyntaxKind.METHOD_CALL) {
            // Added for safety
            dotTokenLineRange = ((MethodCallExpressionNode) evalNode).dotToken().lineRange();
        } else {
            return Optional.empty();
        }

        LinePosition startLine = dotTokenLineRange.startLine();
        LinePosition endLine = dotTokenLineRange.endLine();
        TextEdit textEdit = new TextEdit();
        Range range = new Range();
        range.setStart(new Position(startLine.line(), startLine.offset()));
        range.setEnd(new Position(endLine.line(), endLine.offset()));
        textEdit.setRange(range);
        textEdit.setNewText("");

        return Optional.of(textEdit);
    }
}
