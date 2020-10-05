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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.AttachPoints;

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
            return this.getAnnotationsInModule(context, qNameRef.modulePrefix().text(), attachedNode, pkgAliasMap);
        }

        LSAnnotationCache.getInstance().getAnnotationMapForType(attachedNode, context)
                .forEach((key, value) -> value.forEach(annotation -> {
                    boolean withAlias = this.withAlias(context, node, annotation.pkgID);
                    completionItems.add(CommonUtil.getAnnotationCompletionItem(key, annotation, context, withAlias,
                            pkgAliasMap));
                }));
        completionItems.addAll(this.getCurrentModuleAnnotations(context, attachedNode, pkgAliasMap));
        return completionItems;
    }

    private boolean withAlias(LSContext context, AnnotationNode node, PackageID annotationOwner) {
        PackageID currentModule = context.get(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY);
        String orgName = annotationOwner.orgName.getValue();
        String value = annotationOwner.getName().getValue();
        return node.annotReference().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE
                && !currentModule.name.value.equals(annotationOwner.name.value) && !("ballerina".equals(orgName)
                && "lang.annotations".equals(value));
    }

    private List<LSCompletionItem> getCurrentModuleAnnotations(LSContext ctx, SyntaxKind kind,
                                                               Map<String, String> pkgAliasMap) {
        BLangPackage bLangPackage = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        List<BAnnotationSymbol> annotations = bLangPackage.topLevelNodes.stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangAnnotation)
                .map(topLevelNode -> (BAnnotationSymbol) ((BLangAnnotation) topLevelNode).symbol)
                .collect(Collectors.toList());

        return this.getAnnotationCompletionsForSymbols(ctx, annotations, kind, pkgAliasMap);
    }

    private List<LSCompletionItem> getAnnotationsInModule(LSContext context, String alias, SyntaxKind kind,
                                                          Map<String, String> pkgAliasMap) {
        Optional<Scope.ScopeEntry> moduleEntry = CommonUtil.packageSymbolFromAlias(context, alias);
        if (!moduleEntry.isPresent()) {
            List<LSCompletionItem> completionItems = new ArrayList<>();
            // Import statement has not been added. Hence try resolving from the annotation cache
            LSAnnotationCache.getInstance().getAnnotationsInModule(context, alias, kind)
                    .forEach((key, value) -> value.forEach(annotation -> {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(key, annotation, context, false,
                                pkgAliasMap));
                    }));

            return completionItems;
        }

        List<BAnnotationSymbol> collect = moduleEntry.get().symbol.scope.entries.values().stream()
                .filter(entry -> {
                    BSymbol symbol = entry.symbol;
                    return symbol instanceof BAnnotationSymbol && ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC);
                })
                .map(scopeEntry -> (BAnnotationSymbol) scopeEntry.symbol)
                .collect(Collectors.toList());

        return this.getAnnotationCompletionsForSymbols(context, collect, kind, pkgAliasMap);
    }

    private SyntaxKind getParentSyntaxKind(AnnotationNode node) {
        if (node.parent().kind() == SyntaxKind.METADATA) {
            return node.parent().parent().kind();
        }

        return node.parent().kind();
    }

    private List<LSCompletionItem> getAnnotationCompletionsForSymbols(LSContext ctx,
                                                                      List<BAnnotationSymbol> annotations,
                                                                      SyntaxKind kind,
                                                                      Map<String, String> pkgAliasMap) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        annotations.forEach(symbol -> {
            PackageID pkgId = symbol.pkgID;
            int maskedPoints = symbol.maskedPoints;

            switch (kind) {
                case SERVICE_DECLARATION:
                case SERVICE_CONSTRUCTOR_EXPRESSION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.SERVICE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case RESOURCE_KEYWORD:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RESOURCE)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
                case IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
                case FUNCTION_DEFINITION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case METHOD_DECLARATION:
                case OBJECT_METHOD_DEFINITION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT_METHOD)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case LISTENER_DECLARATION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.LISTENER)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case NAMED_WORKER_DECLARATION:
                case START_ACTION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.WORKER)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case CONST_DECLARATION:
                case ENUM_MEMBER:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.CONST)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case ENUM_DECLARATION:
                case TYPE_CAST_PARAM:
                case TYPE_DEFINITION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.TYPE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case CLASS_DEFINITION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.CLASS)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case RETURN_TYPE_DESCRIPTOR:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RETURN)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case OBJECT_FIELD:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FIELD)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT_FIELD)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case RECORD_FIELD:
                case RECORD_FIELD_WITH_DEFAULT_VALUE:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FIELD)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RECORD_FIELD)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case MODULE_VAR_DECL:
                case LOCAL_VAR_DECL:
                case LET_VAR_DECL:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.VAR)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case EXTERNAL_FUNCTION_BODY:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.EXTERNAL)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case ANNOTATION_DECLARATION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.ANNOTATION)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case REQUIRED_PARAM:
                case DEFAULTABLE_PARAM:
                case REST_PARAM:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.PARAMETER)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                default:
                    break;
            }
        });

        return completionItems;
    }

    @Override
    public boolean onPreValidation(LSContext context, AnnotationNode node) {
        return !node.atToken().isMissing();
    }
}
