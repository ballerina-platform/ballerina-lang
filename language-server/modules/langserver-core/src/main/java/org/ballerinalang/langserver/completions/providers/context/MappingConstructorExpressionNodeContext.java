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

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.VariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link MappingConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MappingConstructorExpressionNodeContext extends
        AbstractCompletionProvider<MappingConstructorExpressionNode> {
    public MappingConstructorExpressionNodeContext() {
        super(MappingConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 MappingConstructorExpressionNode node) throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        NonTerminalNode evalNode = (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE
                || nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE)
                ? nodeAtCursor.parent() : nodeAtCursor;

        if (this.withinValueExpression(context, evalNode)) {
            if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getExpressionsCompletionsForQNameRef(context, qNameRef));
            } else {
                completionItems.addAll(this.expressionCompletions(context));
            }
        } else if (this.withinComputedNameContext(context, evalNode)) {
            if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getExpressionsCompletionsForQNameRef(context, qNameRef));
            } else {
                completionItems.addAll(getComputedNameCompletions(context));
            }
        } else {
            if (!this.hasReadonlyKW(evalNode)) {
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_READONLY.get()));
            }
            Optional<RecordTypeSymbol> recordTypeDesc = this.getRecordTypeDesc(context, node);
            if (recordTypeDesc.isPresent()) {
                Map<String, RecordFieldSymbol> fields = new LinkedHashMap<>(recordTypeDesc.get().fieldDescriptors());
                // TODO: Revamp the implementation
//            completionItems.addAll(BLangRecordLiteralUtil.getSpreadCompletionItems(context, recordType));
                completionItems.addAll(CommonUtil.getRecordFieldCompletionItems(context, fields));
                completionItems.add(CommonUtil.getFillAllStructFieldsItem(context, fields));
                completionItems.addAll(this.getVariableCompletionsForFields(context, fields));
            }
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, MappingConstructorExpressionNode node) {
        return !node.openBrace().isMissing() && !node.closeBrace().isMissing();
    }

    private boolean withinValueExpression(BallerinaCompletionContext context, NonTerminalNode evalNodeAtCursor) {
        Token colon = null;

        if (evalNodeAtCursor.kind() == SyntaxKind.SPECIFIC_FIELD) {
            colon = ((SpecificFieldNode) evalNodeAtCursor).colon().orElse(null);
        } else if (evalNodeAtCursor.kind() == SyntaxKind.COMPUTED_NAME_FIELD) {
            colon = ((ComputedNameFieldNode) evalNodeAtCursor).colonToken();
        }

        if (colon == null) {
            return false;
        }

        int cursorPosInTree = context.getCursorPositionInTree();
        int colonEnd = colon.textRange().endOffset();

        return cursorPosInTree > colonEnd;
    }

    private boolean withinComputedNameContext(BallerinaCompletionContext context, NonTerminalNode evalNodeAtCursor) {
        if (evalNodeAtCursor.kind() != SyntaxKind.COMPUTED_NAME_FIELD) {
            return false;
        }

        int openBracketEnd = ((ComputedNameFieldNode) evalNodeAtCursor).openBracket().textRange().endOffset();
        int closeBracketStart = ((ComputedNameFieldNode) evalNodeAtCursor).closeBracket().textRange().startOffset();
        int cursorPosInTree = context.getCursorPositionInTree();

        return cursorPosInTree >= openBracketEnd && cursorPosInTree <= closeBracketStart;
    }

    private Optional<RecordTypeSymbol> getRecordTypeDesc(BallerinaCompletionContext context,
                                                         MappingConstructorExpressionNode node) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        NonTerminalNode parent = node.parent();
        if (parent.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            TypeDescriptorNode typeDesc = ((VariableDeclarationNode) parent).typedBindingPattern().typeDescriptor();
            if (typeDesc.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                String varName = ((SimpleNameReferenceNode) typeDesc).name().text();
                return visibleSymbols.stream()
                        .filter(symbol -> SymbolUtil.isRecord(symbol) && symbol.name().equals(varName))
                        .map(SymbolUtil::getTypeDescForRecordSymbol)
                        .findFirst();
            }
            if (this.onQualifiedNameIdentifier(context, typeDesc)) {
                QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) typeDesc;
                String modulePrefix = QNameReferenceUtil.getAlias(nameRef);
                String recName = nameRef.identifier().text();
                Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, modulePrefix);
                return module.flatMap(value -> value.typeDefinitions().stream()
                        .filter(typeSymbol -> SymbolUtil.isRecord(typeSymbol) && typeSymbol.name().equals(recName))
                        .map(SymbolUtil::getTypeDescForRecordSymbol)
                        .findFirst());
            }
            /*
            Consider the following case.
            eg: 
                record {
                    int test = 12;   
                } testVar = {
                    <cursor>
                };
             */
            BindingPatternNode bPattern = ((VariableDeclarationNode) parent).typedBindingPattern().bindingPattern();
            if (bPattern.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN) {
                String variableName = ((CaptureBindingPatternNode) bPattern).variableName().text();
                return visibleSymbols.stream()
                        .filter(symbol -> SymbolUtil.isRecord(symbol) && symbol.name().equals(variableName))
                        .map(SymbolUtil::getTypeDescForRecordSymbol)
                        .findFirst();
            }
        } else if (parent.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            Node varRef = ((AssignmentStatementNode) parent).varRef();
            if (varRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                String varName = ((SimpleNameReferenceNode) varRef).name().text();
                return visibleSymbols.stream()
                        .filter(symbol -> SymbolUtil.isRecord(symbol) && symbol.name().equals(varName))
                        .map(SymbolUtil::getTypeDescForRecordSymbol)
                        .findFirst();
            }
        } else if (parent.kind() == SyntaxKind.SPECIFIC_FIELD) {
            return this.getRecordSymbolForInnerMapping(context, node);
        } else if (parent.kind() == SyntaxKind.ANNOTATION) {
            return this.getAnnotationAttachedType(context, (AnnotationNode) parent);
        }

        return Optional.empty();
    }

    private List<LSCompletionItem> getVariableCompletionsForFields(BallerinaCompletionContext ctx,
                                                                   Map<String, RecordFieldSymbol> recFields) {
        List<Symbol> visibleSymbols = ctx.visibleSymbols(ctx.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(symbol -> {
            if (!(symbol instanceof VariableSymbol)) {
                return;
            }
            TypeSymbol typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
            String symbolName = symbol.name();
            if (recFields.containsKey(symbolName)
                    && recFields.get(symbolName).typeDescriptor().typeKind() == typeDescriptor.typeKind()) {
                String bTypeName = typeDescriptor.signature();
                CompletionItem cItem = VariableCompletionItemBuilder.build((VariableSymbol) symbol, symbolName,
                        bTypeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, cItem));
            }
        });

        return completionItems;
    }

    private Optional<RecordTypeSymbol> getRecordSymbolForInnerMapping(BallerinaCompletionContext context,
                                                                      MappingConstructorExpressionNode node) {
        List<String> fieldNames = new ArrayList<>();
        Node evalNode = node;
        while (evalNode.parent().kind() != SyntaxKind.LOCAL_VAR_DECL
                && evalNode.parent().kind() != SyntaxKind.ASSIGNMENT_STATEMENT
                && evalNode.parent().kind() != SyntaxKind.ANNOTATION) {
            if (evalNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                fieldNames.add(((Token) ((SpecificFieldNode) evalNode).fieldName()).text());
            } else if (evalNode.kind() == SyntaxKind.BLOCK_STATEMENT || evalNode.kind() == SyntaxKind.MODULE_PART) {
                return Optional.empty();
            }
            evalNode = evalNode.parent();
        }

        if (evalNode.kind() != SyntaxKind.MAPPING_CONSTRUCTOR) {
            return Optional.empty();
        }

        Optional<RecordTypeSymbol> record = this.getRecordTypeDesc(context,
                (MappingConstructorExpressionNode) evalNode);

        if (record.isEmpty()) {
            return Optional.empty();
        }

        RecordTypeSymbol recordType = record.get();
        Collections.reverse(fieldNames);
        for (String fieldName : fieldNames) {
            if (!recordType.fieldDescriptors().containsKey(fieldName)) {
                return Optional.empty();
            }

            RecordFieldSymbol fieldDesc = recordType.fieldDescriptors().get(fieldName);
            if (CommonUtil.getRawType(fieldDesc.typeDescriptor()).typeKind() != TypeDescKind.RECORD) {
                return Optional.empty();
            }

            recordType = (RecordTypeSymbol) CommonUtil.getRawType(fieldDesc.typeDescriptor());
        }

        return Optional.ofNullable(recordType);
    }

    private Optional<RecordTypeSymbol> getAnnotationAttachedType(BallerinaCompletionContext context,
                                                                 AnnotationNode annotNode) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Node annotRef = annotNode.annotReference();
        List<Symbol> searchableEntries;
        String annotationName;

        if (annotRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            searchableEntries = visibleSymbols;
            annotationName = ((SimpleNameReferenceNode) annotRef).name().text();
        } else if (this.onQualifiedNameIdentifier(context, annotRef)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) annotRef;
            Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context,
                    QNameReferenceUtil.getAlias(qNameRef));
            if (module.isEmpty()) {
                return Optional.empty();
            }
            searchableEntries = new ArrayList<>((module.get()).allSymbols());
            annotationName = qNameRef.identifier().text();
        } else {
            searchableEntries = new ArrayList<>();
            annotationName = "";
        }

        Optional<TypeSymbol> bTypeSymbol = searchableEntries.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.ANNOTATION && symbol.name().equals(annotationName))
                .map(entry -> ((AnnotationSymbol) entry).typeDescriptor().orElse(null))
                .findAny();
        if (bTypeSymbol.isEmpty() || CommonUtil.getRawType(bTypeSymbol.get()).typeKind() != TypeDescKind.RECORD) {
            return Optional.empty();
        }
        return Optional.of((RecordTypeSymbol) CommonUtil.getRawType(bTypeSymbol.get()));
    }

    private List<LSCompletionItem> getComputedNameCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());

        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol instanceof VariableSymbol || symbol.kind() == SymbolKind.FUNCTION)
                .collect(Collectors.toList());
        List<LSCompletionItem> completionItems = this.getCompletionItemList(filteredList, context);
        completionItems.addAll(this.getModuleCompletionItems(context));

        return completionItems;
    }

    private boolean hasReadonlyKW(NonTerminalNode evalNodeAtCursor) {
        return ((evalNodeAtCursor.kind() == SyntaxKind.SPECIFIC_FIELD)
                && ((SpecificFieldNode) evalNodeAtCursor).readonlyKeyword().isPresent());
    }

    private List<LSCompletionItem> getExpressionsCompletionsForQNameRef(BallerinaCompletionContext context,
                                                                        QualifiedNameReferenceNode qNameRef) {
        Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                || symbol.kind() == SymbolKind.FUNCTION;
        List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, filter);

        return this.getCompletionItemList(moduleContent, context);
    }
}
