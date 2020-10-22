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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.AnnotationAttachPoint;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.AnnotationUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link AnnotationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class AnnotationNodeContext extends AbstractCompletionProvider<AnnotationNode> {

    public AnnotationNodeContext() {
        super(AnnotationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, AnnotationNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        SyntaxKind attachedNode = this.getParentSyntaxKind(node);

        if (attachedNode == SyntaxKind.FUNCTION_DEFINITION) {
            NodeList<Token> qualifierList = ((FunctionDefinitionNode) node.parent().parent()).qualifierList();
            for (Token qualifier : qualifierList) {
                if (qualifier.kind() == SyntaxKind.RESOURCE_KEYWORD) {
                    attachedNode = SyntaxKind.RESOURCE_KEYWORD;
                    break;
                }
            }
        }

        Map<String, String> pkgAliasMap = context.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY).stream()
                .filter(pkg -> pkg.symbol != null)
                .collect(Collectors.toMap(pkg -> pkg.symbol.pkgID.toString(), pkg -> pkg.alias.value));

        if (this.onQualifiedNameIdentifier(context, node.annotReference())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.annotReference();
            return this.getAnnotationsInModule(context, qNameRef.modulePrefix().text(), attachedNode);
        }

        LSAnnotationCache.getInstance().getAnnotationMapForSyntaxKind(attachedNode, context)
                .forEach((key, value) -> value.forEach(annotation -> {
                    LSCompletionItem cItem;
                    if (this.addAlias(context, node, key)) {
                        cItem = AnnotationUtil.getModuleQualifiedAnnotationItem(key, annotation, context, pkgAliasMap);
                    } else {
                        cItem = AnnotationUtil.getAnnotationItem(annotation, context);
                    }
                    completionItems.add(cItem);
                }));
        completionItems.addAll(this.getCurrentModuleAnnotations(context, attachedNode));

        return completionItems;
    }

    private List<LSCompletionItem> getCurrentModuleAnnotations(LSContext ctx, SyntaxKind kind) {
        List<Symbol> visibleSymbols = ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        return visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.ANNOTATION
                        && this.matchingAnnotation((AnnotationSymbol) symbol, kind)
                        && !CommonUtil.isLangLib(symbol.moduleID()))
                .map(symbol -> AnnotationUtil.getAnnotationItem((AnnotationSymbol) symbol, ctx))
                .collect(Collectors.toList());
    }

    private List<LSCompletionItem> getAnnotationsInModule(LSContext context, String alias, SyntaxKind kind) {
        Optional<ModuleSymbol> moduleEntry = CommonUtil.searchModuleForAlias(context, alias);
        if (moduleEntry.isEmpty()) {
            List<LSCompletionItem> completionItems = new ArrayList<>();
            // Import statement has not been added. Hence try resolving from the annotation cache
            LSAnnotationCache.getInstance().getAnnotationsInModule(context, alias, kind)
                    .forEach((key, value) -> value.forEach(annotation -> {
                        completionItems.add(AnnotationUtil.getAnnotationItem(annotation, context));
                    }));

            return completionItems;
        }

        return moduleEntry.get().allSymbols().stream()
                .filter(symbol -> symbol.kind() == SymbolKind.ANNOTATION
                        && this.matchingAnnotation((AnnotationSymbol) symbol, kind))
                .map(symbol -> AnnotationUtil.getAnnotationItem((AnnotationSymbol) symbol, context))
                .collect(Collectors.toList());
    }

    private SyntaxKind getParentSyntaxKind(AnnotationNode node) {
        if (node.parent().kind() == SyntaxKind.METADATA) {
            return node.parent().parent().kind();
        }

        return node.parent().kind();
    }

    private boolean matchingAnnotation(AnnotationSymbol symbol, SyntaxKind kind) {
        switch (kind) {
            case SERVICE_DECLARATION:
            case SERVICE_CONSTRUCTOR_EXPRESSION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.SERVICE);
            case RESOURCE_KEYWORD:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.RESOURCE)
                        || AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.FUNCTION);
            case EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
            case IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
            case FUNCTION_DEFINITION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.FUNCTION);
            case METHOD_DECLARATION:
            case OBJECT_METHOD_DEFINITION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.FUNCTION)
                        || AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.OBJECT_METHOD);
            case LISTENER_DECLARATION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.LISTENER);
            case NAMED_WORKER_DECLARATION:
            case START_ACTION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.WORKER);
            case CONST_DECLARATION:
            case ENUM_MEMBER:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.CONST);
            case ENUM_DECLARATION:
            case TYPE_CAST_PARAM:
            case TYPE_DEFINITION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.TYPE);
            case CLASS_DEFINITION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.CLASS);
            case RETURN_TYPE_DESCRIPTOR:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.RETURN);
            case OBJECT_FIELD:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.FIELD)
                        || AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.OBJECT_FIELD);
            case RECORD_FIELD:
            case RECORD_FIELD_WITH_DEFAULT_VALUE:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.FIELD)
                        || AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.RECORD_FIELD);
            case MODULE_VAR_DECL:
            case LOCAL_VAR_DECL:
            case LET_VAR_DECL:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.VAR);
            case EXTERNAL_FUNCTION_BODY:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.EXTERNAL);
            case ANNOTATION_DECLARATION:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.ANNOTATION);
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                return AnnotationUtil.hasAttachment(symbol, AnnotationAttachPoint.PARAMETER);
            default:
                return false;
        }
    }

    @Override
    public boolean onPreValidation(LSContext context, AnnotationNode node) {
        return !node.atToken().isMissing();
    }

    private boolean addAlias(LSContext context, AnnotationNode node, ModuleID annotationOwner) {
        ModuleSymbol currentModule = context.get(DocumentServiceKeys.CURRENT_MODULE_KEY);
        String orgName = annotationOwner.orgName();
        String value = annotationOwner.moduleName();
        return node.annotReference().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE
                && !currentModule.moduleID().moduleName().equals(annotationOwner.moduleName())
                && !("ballerina".equals(orgName)
                && "lang.annotations".equals(value));
    }
}
