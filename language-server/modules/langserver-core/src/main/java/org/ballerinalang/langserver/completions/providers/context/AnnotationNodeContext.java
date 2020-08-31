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
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.AttachPoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        SyntaxKind attachedNode = node.parent().parent().kind();

        if (attachedNode == SyntaxKind.FUNCTION_DEFINITION) {
            NodeList<Token> qualifierList = ((FunctionDefinitionNode) node.parent().parent()).qualifierList();
            for (Token qualifier : qualifierList) {
                if (qualifier.kind() == SyntaxKind.RESOURCE_KEYWORD) {
                    attachedNode = SyntaxKind.RESOURCE_KEYWORD;
                    break;
                }
            }
        }

        Node annotRef = node.annotReference();
        String finalAlias = annotRef.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE ? null
                : ((QualifiedNameReferenceNode) annotRef).modulePrefix().text();
        Map<String, String> pkgAliasMap = context.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY).stream()
                .filter(pkg -> pkg.symbol != null)
                .collect(Collectors.toMap(pkg -> pkg.symbol.pkgID.toString(), pkg -> pkg.alias.value));

        LSAnnotationCache.getInstance().getAnnotationMapForType(attachedNode, context)
                .forEach((key, value) -> value.forEach(annotation -> {
                    String annotationPkgAlias = annotation.pkgID.nameComps
                            .get(annotation.pkgID.nameComps.size() - 1).value;
                    String annotationPkg = annotation.pkgID.toString();
                    // compare with the import statements' package alias
                    if (finalAlias == null || finalAlias.equals(annotationPkgAlias)
                            || finalAlias.equals(pkgAliasMap.get(annotationPkg))) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(key, annotation, context, finalAlias,
                                pkgAliasMap));
                    }
                }));
        completionItems.addAll(this.getAnnotationsInModule(context, attachedNode, pkgAliasMap));
        return completionItems;
    }

    private List<LSCompletionItem> getAnnotationsInModule(LSContext ctx, SyntaxKind kind,
                                                          Map<String, String> pkgAliasMap) {
        BLangPackage bLangPackage = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<BLangAnnotation> annotations = bLangPackage.topLevelNodes.stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangAnnotation)
                .map(topLevelNode -> (BLangAnnotation) topLevelNode)
                .collect(Collectors.toList());
        BLangNode scopeNode = ctx.get(CompletionKeys.SCOPE_NODE_KEY);

        annotations.forEach(bLangAnnotation -> {
            BAnnotationSymbol symbol = (BAnnotationSymbol) bLangAnnotation.symbol;
            PackageID pkgId = symbol.pkgID;
            int maskedPoints = symbol.maskedPoints;
            switch (kind) {
                case ANNOTATION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.ANNOTATION)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case FUNCTION_DEFINITION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)
                            || (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT_METHOD)
                            && scopeNode.getKind() == NodeKind.OBJECT_TYPE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case LISTENER_DECLARATION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.LISTENER)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case OBJECT_TYPE_DESC:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.TYPE)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case RESOURCE_KEYWORD:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RESOURCE)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case SERVICE_DECLARATION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.SERVICE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case RECORD_TYPE_DESC:
                case TYPE_DEFINITION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.TYPE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case NAMED_WORKER_DECLARATION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.WORKER)) {
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
