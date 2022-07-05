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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link AnnotationDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class AnnotationDeclarationNodeContext extends AbstractCompletionProvider<AnnotationDeclarationNode> {

    public AnnotationDeclarationNodeContext() {
        super(AnnotationDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, AnnotationDeclarationNode node) {
        // TODO: Handle the cases blocked by #30556
        List<LSCompletionItem> completionItemList = new ArrayList<>();
        if (this.onTypeDescriptorContext(context, node)) {
            Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION
                    && this.isValidTypeDescForAnnotations((TypeDefinitionSymbol) symbol);
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> filteredSymbols = QNameRefCompletionUtil.getModuleContent(context, qNameRef, predicate);

                completionItemList.addAll(this.getCompletionItemList(filteredSymbols, context));
            } else {
                List<Symbol> filteredSymbols = context.visibleSymbols(context.getCursorPosition()).stream()
                        .filter(predicate)
                        .collect(Collectors.toList());
                completionItemList.addAll(this.getCompletionItemList(filteredSymbols, context));
                completionItemList.addAll(this.getModuleCompletionItems(context));
                completionItemList.add(new SnippetCompletionItem(context, Snippet.DEF_RECORD_TYPE_DESC.get()));
                completionItemList.add(new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD_TYPE_DESC.get()));
            }
        } else if (this.onSuggestOnKeyword(context, node)) {
            completionItemList.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
        } else if (this.onSuggestAttachmentPoints(context, node)) {
            completionItemList.addAll(this.getAnnotationAttachmentPoints(context, node));
        }

        this.sort(context, node, completionItemList);
        return completionItemList;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, AnnotationDeclarationNode node) {
        Token annotationKeyword = node.annotationKeyword();
        Token semicolonToken = node.semicolonToken();
        int cursor = context.getCursorPositionInTree();
        
        return !annotationKeyword.isMissing() && cursor >= annotationKeyword.textRange().endOffset()
                && (semicolonToken.isMissing() || cursor < semicolonToken.textRange().endOffset());
    }

    private boolean onTypeDescriptorContext(BallerinaCompletionContext context, AnnotationDeclarationNode node) {
        Optional<Node> typeDesc = node.typeDescriptor();
        Token annotationTag = node.annotationTag();
        int cursor = context.getCursorPositionInTree();

        return (typeDesc.isEmpty() && annotationTag.isMissing())
                || (typeDesc.isEmpty() && !annotationTag.isMissing()
                && cursor <= annotationTag.textRange().endOffset())
                || (typeDesc.isPresent() && cursor >= typeDesc.get().textRange().startOffset()
                && cursor <= typeDesc.get().textRange().endOffset());
    }

    private boolean onSuggestOnKeyword(BallerinaCompletionContext context, AnnotationDeclarationNode node) {
        Token annotationTag = node.annotationTag();
        SeparatedNodeList<Node> attachPoints = node.attachPoints();
        int cursor = context.getCursorPositionInTree();

        return !annotationTag.isMissing() && attachPoints.isEmpty() && node.onKeyword().isEmpty()
                && annotationTag.textRange().endOffset() + 1 <= cursor;
    }

    private boolean onSuggestAttachmentPoints(BallerinaCompletionContext context, AnnotationDeclarationNode node) {
        Optional<Token> onKeyword = node.onKeyword();
        int cursor = context.getCursorPositionInTree();

        return onKeyword.isPresent() && cursor >= onKeyword.get().textRange().endOffset() + 1;
    }

    private boolean isValidTypeDescForAnnotations(TypeDefinitionSymbol typeDefinitionSymbol) {
        TypeSymbol typeSymbol = typeDefinitionSymbol.typeDescriptor();
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        rawType = rawType.typeKind() == TypeDescKind.ARRAY
                ? CommonUtil.getRawType(((ArrayTypeSymbol) rawType).memberTypeDescriptor()) : rawType;

        return rawType.typeKind() == TypeDescKind.MAP || rawType.typeKind() == TypeDescKind.RECORD;
    }

    private List<LSCompletionItem> getAnnotationAttachmentPoints(BallerinaCompletionContext context,
                                                                 AnnotationDeclarationNode node) {
        AttachmentPointContext attachmentPointContext = getAttachmentPointContext(context, node);
        List<Snippet> itemSnippets = new ArrayList<>();
        switch (attachmentPointContext) {
            case ANY:
                itemSnippets.addAll(anyAttachmentPoints());
                break;
            case SOURCE:
                // both dual and source only attachment points are suggested
                itemSnippets.addAll(dualAttachmentPoints());
                itemSnippets.addAll(sourceOnlyAttachmentPoints());
                break;
            case OBJECT:
                itemSnippets.addAll(Arrays.asList(Snippet.KW_FUNCTION, Snippet.KW_FIELD));
                break;
            case RECORD:
                itemSnippets.addAll(Collections.singletonList(Snippet.KW_FIELD));
                break;
            case REMOTE:
                itemSnippets.addAll(Collections.singletonList(Snippet.KW_FUNCTION));
                break;
            case SERVICE:
                itemSnippets.addAll(Arrays.asList(Snippet.KW_REMOTE, Snippet.KW_REMOTE_FUNCTION));
                break;
            default:
                break;
        }

        return itemSnippets.stream()
                .map(snippet -> new SnippetCompletionItem(context, snippet.get()))
                .collect(Collectors.toList());
    }

    private List<Snippet> anyAttachmentPoints() {
        return Arrays.asList(Snippet.KW_SOURCE, Snippet.KW_TYPE, Snippet.KW_CLASS, Snippet.KW_FUNCTION,
                Snippet.KW_OBJ_FUNCTION, Snippet.KW_SERVICE_REMOTE_FUNCTION, Snippet.KW_PARAMETER,
                Snippet.KW_RETURN, Snippet.KW_SERVICE, Snippet.KW_OBJECT, Snippet.KW_RECORD, Snippet.KW_OBJECT_FIELD,
                Snippet.KW_RECORD_FIELD, Snippet.KW_FIELD, Snippet.KW_SOURCE_ANNOTATION, Snippet.KW_SOURCE_EXTERNAL,
                Snippet.KW_SOURCE_VAR, Snippet.KW_SOURCE_CONST, Snippet.KW_SOURCE_LISTENER, Snippet.KW_SOURCE_WORKER);
    }

    private List<Snippet> dualAttachmentPoints() {
        return Arrays.asList(Snippet.KW_TYPE, Snippet.KW_CLASS, Snippet.KW_OBJ_FUNCTION,
                Snippet.KW_SERVICE_REMOTE_FUNCTION, Snippet.KW_PARAMETER, Snippet.KW_RETURN, Snippet.KW_SERVICE,
                Snippet.KW_OBJECT_FIELD, Snippet.KW_RECORD_FIELD, Snippet.KW_FIELD, Snippet.KW_FUNCTION);
    }

    private List<Snippet> sourceOnlyAttachmentPoints() {
        return Arrays.asList(Snippet.KW_ANNOTATION, Snippet.KW_EXTERNAL, Snippet.KW_VAR,
                Snippet.KW_CONST, Snippet.KW_LISTENER, Snippet.KW_WORKER);
    }

    private AttachmentPointContext getAttachmentPointContext(BallerinaCompletionContext context,
                                                             AnnotationDeclarationNode node) {
        SeparatedNodeList<Node> attachmentPoints = node.attachPoints();
        Optional<AnnotationAttachPointNode> attachmentPointAtCursor =
                this.attachmentPointAtCursor(context, attachmentPoints);

        if (attachmentPointAtCursor.isEmpty()) {
            return AttachmentPointContext.ANY;
        }

        NodeList<Token> identifiers = attachmentPointAtCursor.get().identifiers();
        int cursor = context.getCursorPositionInTree();
        Optional<Token> immediatePreviousToken = Optional.empty();
        for (int i = identifiers.size() - 1; i >= 0; i--) {
            Token token = identifiers.get(i);
            if (token.isMissing()) {
                continue;
            }
            if (cursor > token.textRange().endOffset()) {
                immediatePreviousToken = Optional.of(token);
                break;
            }
        }

        Optional<Token> sourceKeyword = attachmentPointAtCursor.get().sourceKeyword();

        if (sourceKeyword.isPresent() && sourceKeyword.get().textRange().endOffset() < cursor
                && (immediatePreviousToken.isEmpty()
                || immediatePreviousToken.get().textRange().endOffset() > cursor)) {
            return AttachmentPointContext.SOURCE;
        }

        if (immediatePreviousToken.isEmpty()) {
            return AttachmentPointContext.ANY;
        }

        SyntaxKind immediatePreviousTokenKind = immediatePreviousToken.get().kind();

        if (immediatePreviousTokenKind == SyntaxKind.OBJECT_KEYWORD) {
            return AttachmentPointContext.OBJECT;
        }

        if (immediatePreviousTokenKind == SyntaxKind.SERVICE_KEYWORD) {
            return AttachmentPointContext.SERVICE;
        }

        if (immediatePreviousTokenKind == SyntaxKind.REMOTE_KEYWORD) {
            return AttachmentPointContext.REMOTE;
        }

        if (immediatePreviousTokenKind == SyntaxKind.RECORD_KEYWORD) {
            return AttachmentPointContext.RECORD;
        }

        return AttachmentPointContext.NONE;
    }

    private Optional<AnnotationAttachPointNode> attachmentPointAtCursor(BallerinaCompletionContext context,
                                                                        SeparatedNodeList<Node> nodes) {
        if (nodes.isEmpty()) {
            return Optional.empty();
        }
        int cursor = context.getCursorPositionInTree();
        int separatorIndex = -1;
        for (int i = nodes.separatorSize(); i > 0; i--) {
            Token separator = nodes.getSeparator(i - 1);
            if (separator.textRange().endOffset() <= cursor) {
                separatorIndex = i - 1;
                break;
            }
        }

        int nodeIndex = separatorIndex + 1;
        if (nodeIndex > nodes.size() || nodes.get(nodeIndex).isMissing()) {
            return Optional.empty();
        }

        return Optional.of((AnnotationAttachPointNode) nodes.get(nodeIndex));
    }

    private enum AttachmentPointContext {
        ANY,
        SOURCE,
        DUAL,
        OBJECT,
        SERVICE,
        REMOTE,
        RECORD,
        NONE
    }
}
