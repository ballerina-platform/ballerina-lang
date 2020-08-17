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

import io.ballerinalang.compiler.syntax.tree.AnnotationNode;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.common.utils.completion.BLangRecordLiteralUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Completion provider for {@link MappingConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class MappingConstructorExpressionNodeContext extends
        AbstractCompletionProvider<MappingConstructorExpressionNode> {
    public MappingConstructorExpressionNodeContext() {
        super(Kind.OTHER);
        this.attachmentPoints.add(MappingConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, MappingConstructorExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<BRecordTypeSymbol> recordSymbol = this.getRecordSymbol(context, node);
        if (recordSymbol.isPresent()) {
            BRecordType recordType = (BRecordType) recordSymbol.get().type;
            List<BField> fields = new ArrayList<>(recordType.fields.values());
            completionItems.addAll(BLangRecordLiteralUtil.getSpreadCompletionItems(context, recordType));
            completionItems.addAll(CommonUtil.getRecordFieldCompletionItems(context, fields));
            completionItems.add(CommonUtil.getFillAllStructFieldsItem(context, fields));
            completionItems.addAll(this.getVariableCompletionsForFields(context, fields));
        }
        return completionItems;
    }

    private Optional<BRecordTypeSymbol> getRecordSymbol(LSContext context, MappingConstructorExpressionNode node) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        NonTerminalNode parent = node.parent();
        if (parent.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            TypeDescriptorNode typeDesc = ((VariableDeclarationNode) parent).typedBindingPattern().typeDescriptor();
            if (typeDesc.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                String varName = ((SimpleNameReferenceNode) typeDesc).name().text();
                return visibleSymbols.stream()
                        .filter(entry -> {
                            BSymbol symbol = entry.symbol;
                            return symbol.kind == SymbolKind.RECORD && symbol.getName().getValue().equals(varName);
                        })
                        .map(scopeEntry -> (BRecordTypeSymbol) scopeEntry.symbol)
                        .findFirst();
            } else if (typeDesc.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) typeDesc;
                String modulePrefix = QNameReferenceUtil.getAlias(nameRef);
                String recName = nameRef.identifier().text();
                Optional<Scope.ScopeEntry> module = CommonUtil.packageSymbolFromAlias(context, modulePrefix);
                return module.flatMap(value -> value.symbol.scope.entries.values().stream()
                        .filter(entry -> {
                            BSymbol symbol = entry.symbol;
                            return symbol.kind == SymbolKind.RECORD && symbol.getName().getValue().equals(recName);
                        })
                        .map(scopeEntry -> (BRecordTypeSymbol) scopeEntry.symbol)
                        .findFirst());
            }
        } else if (parent.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            Node varRef = ((AssignmentStatementNode) parent).varRef();
            if (varRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                String varName = ((SimpleNameReferenceNode) varRef).name().text();
                return visibleSymbols.stream()
                        .filter(entry -> {
                            BSymbol symbol = entry.symbol;
                            return symbol.kind == SymbolKind.RECORD && symbol.getName().getValue().equals(varName);
                        })
                        .map(scopeEntry -> (BRecordTypeSymbol) scopeEntry.symbol)
                        .findFirst();

            }
        } else if (parent.kind() == SyntaxKind.SPECIFIC_FIELD) {
            return this.getRecordSymbolForInnerMapping(context, node);
        } else if (parent.kind() == SyntaxKind.ANNOTATION) {
            return this.getAnnotationAttachedType(context, (AnnotationNode) parent);
        }

        return Optional.empty();
    }

    private List<LSCompletionItem> getVariableCompletionsForFields(LSContext ctx, List<BField> recordFields) {
        List<Scope.ScopeEntry> visibleSymbols = ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Map<String, BType> filedTypeMap = new HashMap<>();
        recordFields.forEach(bField -> filedTypeMap.put(bField.name.value, bField.type));
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(scopeEntry -> {
            BSymbol symbol = scopeEntry.symbol;
            BType type = symbol instanceof BConstantSymbol ? ((BConstantSymbol) symbol).literalType : symbol.type;
            String symbolName = symbol.name.getValue();
            if (filedTypeMap.containsKey(symbolName) && filedTypeMap.get(symbolName) == type
                    && symbol instanceof BVarSymbol) {
                String bTypeName = CommonUtil.getBTypeName(type, ctx, false);
                CompletionItem cItem = BVariableCompletionItemBuilder.build((BVarSymbol) symbol, symbolName, bTypeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, cItem));
            }
        });

        return completionItems;
    }

    private Optional<BRecordTypeSymbol> getRecordSymbolForInnerMapping(LSContext context,
                                                                       MappingConstructorExpressionNode node) {
        List<String> fieldNames = new ArrayList<>();
        Node evalNode = node;
        while (evalNode.parent().kind() != SyntaxKind.LOCAL_VAR_DECL
                && evalNode.parent().kind() != SyntaxKind.ASSIGNMENT_STATEMENT
                && evalNode.parent().kind() != SyntaxKind.ANNOTATION) {
            if (evalNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                fieldNames.add(((SpecificFieldNode) evalNode).fieldName().text());
            } else if (evalNode.kind() == SyntaxKind.BLOCK_STATEMENT || evalNode.kind() == SyntaxKind.MODULE_PART) {
                return Optional.empty();
            }
            evalNode = evalNode.parent();
        }

        if (evalNode.kind() != SyntaxKind.MAPPING_CONSTRUCTOR) {
            return Optional.empty();
        }

        Optional<BRecordTypeSymbol> record = this.getRecordSymbol(context, (MappingConstructorExpressionNode) evalNode);

        if (!record.isPresent()) {
            return Optional.empty();
        }

        BRecordType recordType = (BRecordType) record.get().type;
        Collections.reverse(fieldNames);
        for (String fieldName : fieldNames) {
            BType bType = recordType.fields.get(fieldName).type;
            if (!(bType instanceof BRecordType)) {
                return Optional.empty();
            }
            recordType = (BRecordType) bType;
        }

        return Optional.ofNullable((BRecordTypeSymbol) recordType.tsymbol);
    }

    private Optional<BRecordTypeSymbol> getAnnotationAttachedType(LSContext context, AnnotationNode annotationNode) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Node annotRef = annotationNode.annotReference();
        List<Scope.ScopeEntry> searchableEntries;
        String annotationName;

        if (annotRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            searchableEntries = visibleSymbols;
            annotationName = ((SimpleNameReferenceNode) annotRef).name().text();
        } else if (annotRef.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) annotRef;
            Optional<Scope.ScopeEntry> module = CommonUtil.packageSymbolFromAlias(context,
                    QNameReferenceUtil.getAlias(qNameRef));
            if (!module.isPresent()) {
                return Optional.empty();
            }
            searchableEntries = new ArrayList<>(((BPackageSymbol) module.get().symbol).scope.entries.values());
            annotationName = qNameRef.identifier().text();
        } else {
            searchableEntries = new ArrayList<>();
            annotationName = "";
        }

        Optional<BTypeSymbol> bTypeSymbol = searchableEntries.stream()
                .filter(entry -> {
                    BSymbol symbol = entry.symbol;
                    return symbol.kind == SymbolKind.ANNOTATION && symbol.getName().getValue().equals(annotationName);
                })
                .findFirst()
                .map(entry -> ((BAnnotationSymbol) entry.symbol).attachedType);
        if (!bTypeSymbol.isPresent() || !(bTypeSymbol.get() instanceof BRecordTypeSymbol)) {
            return Optional.empty();
        }
        return Optional.of((BRecordTypeSymbol) bTypeSymbol.get());
    }
}
