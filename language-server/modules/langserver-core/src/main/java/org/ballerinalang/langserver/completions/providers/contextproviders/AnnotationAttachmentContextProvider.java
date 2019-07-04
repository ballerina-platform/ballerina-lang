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
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

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
        if (ctx.get(CompletionKeys.NEXT_NODE_KEY) == null) {
            return new ArrayList<>();
        }
        return filterAnnotations(ctx.get(CompletionKeys.NEXT_NODE_KEY), ctx);
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
        BLangPackage bLangPackage = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        Map<String, String> pkgAliasMap = CommonUtil.getCurrentFileImports(bLangPackage, ctx).stream()
                .collect(Collectors.toMap(pkg -> pkg.alias.value, pkg -> pkg.symbol.pkgID.toString()));
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
                            || annotationPkg.equals(pkgAliasMap.get(finalAlias.getText()))) {
                        completionItems.add(CommonUtil.getAnnotationCompletionItem(key, annotation, ctx, finalAlias));
                    }
                }));
        
        return completionItems;
    }
}
