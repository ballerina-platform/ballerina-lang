/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.AnnotationNodeKind;
import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
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
 * Annotation Attachment Resolver to resolve the corresponding annotation attachments.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class AnnotationAttachmentContextProvider extends LSCompletionProvider {

    public AnnotationAttachmentContextProvider() {
        this.attachmentPoints.add(AnnotationAttachmentContextProvider.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        List<Integer> rhsTokenTypes = ctx.get(CompletionKeys.RHS_DEFAULT_TOKEN_TYPES_KEY);
        AnnotationNodeKind annotationNodeKind = ctx.get(CompletionKeys.NEXT_NODE_KEY);
        if (annotationNodeKind == null && rhsTokenTypes.contains(BallerinaParser.EXTERNAL)) {
            annotationNodeKind = AnnotationNodeKind.EXTERNAL;
        } else if (annotationNodeKind == null) {
            return new ArrayList<>();
        }
        return filterAnnotations(annotationNodeKind, ctx);
    }

    /**
     * Filter the annotations from the data model.
     * 
     * @return {@link List}
     */
    private ArrayList<CompletionItem> filterAnnotations(AnnotationNodeKind attachmentPoint, LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<Integer> lhsTokenTypes = ctx.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<CommonToken> lhsDefaultTokens = ctx.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        Map<String, String> pkgAliasMap = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY).stream()
                .collect(Collectors.toMap(pkg -> pkg.symbol.pkgID.toString(), pkg -> pkg.alias.value));
        CommonToken pkgAlias = null;
        if (lhsTokenTypes == null) {
            return completionItems;
        }
        int atTokenIndex = lhsTokenTypes.indexOf(BallerinaParser.AT);
        int colonTokenIndex = lhsTokenTypes.indexOf(BallerinaParser.COLON);
        if (atTokenIndex == -1) {
            return completionItems;
        }
        if (colonTokenIndex > 0) {
            pkgAlias = lhsDefaultTokens.get(colonTokenIndex - 1);
        }

        CommonToken finalAlias = pkgAlias;
        LSAnnotationCache.getInstance().getAnnotationMapForType(attachmentPoint, ctx)
                .forEach((key, value) -> value.forEach(annotation -> {
                    String annotationPkgAlias = annotation.pkgID.nameComps
                            .get(annotation.pkgID.nameComps.size() - 1).value;
                    String annotationPkg = annotation.pkgID.toString();
                    // compare with the import statements' package alias
                    if (finalAlias == null || finalAlias.getText().equals(annotationPkgAlias)
                            || finalAlias.getText().equals(pkgAliasMap.get(annotationPkg))) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(key, annotation, ctx, finalAlias,
                                pkgAliasMap));
                    }
                }));
        
        completionItems.addAll(this.getAnnotationsInModule(ctx, attachmentPoint, pkgAliasMap));
        
        return completionItems;
    }
    
    private List<CompletionItem> getAnnotationsInModule(LSContext ctx, AnnotationNodeKind kind,
                                                        Map<String, String> pkgAliasMap) {
        BLangPackage bLangPackage = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        List<CompletionItem> completionItems = new ArrayList<>();
        List<BLangAnnotation> annotations = bLangPackage.topLevelNodes.stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangAnnotation)
                .map(topLevelNode -> (BLangAnnotation) topLevelNode)
                .collect(Collectors.toList());
        BLangNode scopeNode = ctx.get(CompletionKeys.SCOPE_NODE_KEY);

        annotations.forEach(bLangAnnotation -> {
            BAnnotationSymbol symbol =  (BAnnotationSymbol) bLangAnnotation.symbol;
            PackageID pkgId = symbol.pkgID;
            int maskedPoints = symbol.maskedPoints;
            switch (kind) {
                case ANNOTATION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.ANNOTATION)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case FUNCTION:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)
                            || (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT_METHOD)
                            && scopeNode.getKind() == NodeKind.OBJECT_TYPE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case LISTENER:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.LISTENER)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case OBJECT:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.TYPE)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case RESOURCE:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RESOURCE)
                            || Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case SERVICE:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.SERVICE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case RECORD:
                case TYPE:
                    if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.TYPE)) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(pkgId, symbol, ctx, pkgAliasMap));
                    }
                    break;
                case WORKER:
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
}
