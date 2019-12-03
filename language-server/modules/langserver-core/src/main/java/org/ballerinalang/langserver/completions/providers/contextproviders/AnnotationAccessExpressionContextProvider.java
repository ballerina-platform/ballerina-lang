/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Annotation access expression context provider.
 * 
 * @since 1.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class AnnotationAccessExpressionContextProvider extends LSCompletionProvider {

    public AnnotationAccessExpressionContextProvider() {
        this.attachmentPoints.add(AnnotationAccessExpressionContextProvider.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        return filterAnnotations(ctx);
    }

    /**
     * Filter the annotations.
     * 
     * @return {@link List}
     */
    private List<CompletionItem> filterAnnotations(LSContext ctx) {
        List<CompletionItem> completionItems = new ArrayList<>();
        List<Integer> defaultTokenTypes = ctx.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<CommonToken> defaultTokens = ctx.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        int annotationAccessIndex = defaultTokenTypes.indexOf(BallerinaParser.ANNOTATION_ACCESS);
        int pkgDelimiterIndex = defaultTokenTypes.lastIndexOf(BallerinaParser.COLON);
        CommonToken pkgAlias = null;
        
        if (pkgDelimiterIndex > annotationAccessIndex) {
            pkgAlias = defaultTokens.get(pkgDelimiterIndex - 1);
        }

        Map<String, String> pkgAliasMap = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY).stream()
                .collect(Collectors.toMap(pkg -> pkg.alias.value, pkg -> pkg.symbol.pkgID.toString()));

        if (pkgAlias != null) {
            CommonToken finalPkgAlias = pkgAlias;
            LSAnnotationCache.getInstance().getAnnotations()
                    .forEach(annotation -> {
                        String annotationPkgAlias = annotation.pkgID.nameComps
                                .get(annotation.pkgID.nameComps.size() - 1).value;
                        String annotationPkg = annotation.pkgID.toString();
                        // compare with the import statements' package alias
                        if (finalPkgAlias.getText().equals(annotationPkgAlias)
                                || annotationPkg.equals(pkgAliasMap.get(finalPkgAlias.getText()))) {
                            completionItems.add(this.getAnnotationCompletionItem(annotation.pkgID, annotation,
                                    ctx, finalPkgAlias, pkgAliasMap));
                        }
                    });
            return completionItems;
        }
        
        completionItems.addAll(getAnnotationsInModule(ctx));
        completionItems.addAll(getPackagesCompletionItems(ctx));
        return completionItems;
    }

    private List<CompletionItem> getAnnotationsInModule(LSContext ctx) {
        BLangPackage bLangPackage = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        List<CompletionItem> completionItems = new ArrayList<>();
        List<BLangAnnotation> annotations = bLangPackage.topLevelNodes.stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangAnnotation)
                .map(topLevelNode -> (BLangAnnotation) topLevelNode)
                .collect(Collectors.toList());

        annotations.forEach(bLangAnnotation -> {
            BAnnotationSymbol symbol =  (BAnnotationSymbol) bLangAnnotation.symbol;
            CompletionItem annotationItem = new CompletionItem();
            annotationItem.setLabel(symbol.getName().getValue());
            annotationItem.setInsertText(symbol.getName().getValue());
            annotationItem.setInsertTextFormat(InsertTextFormat.Snippet);
            annotationItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
            annotationItem.setKind(CompletionItemKind.Property);
            completionItems.add(annotationItem);
        });
        
        return completionItems;
    }

    private CompletionItem getAnnotationCompletionItem(PackageID packageID, BAnnotationSymbol annotationSymbol, 
                                                       LSContext ctx, CommonToken pkgAlias, 
                                                       Map<String, String> pkgAliasMap) {
        PackageID currentPkgID = ctx.get(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY);
        String currentProjectOrgName = currentPkgID == null ? "" : currentPkgID.orgName.value;

        String aliasComponent = "";
        if (pkgAliasMap.containsKey(packageID.toString())) {
            // Check if the imported packages contains the particular package with the alias
            aliasComponent = pkgAliasMap.get(packageID.toString());
        } else if (!currentPkgID.name.value.equals(packageID.name.value)) {
            aliasComponent = CommonUtil.getLastItem(packageID.getNameComps()).getValue();
        }

        boolean withAlias = (pkgAlias == null && !aliasComponent.isEmpty());

        String label = CommonUtil.getAnnotationLabel(aliasComponent, annotationSymbol, withAlias);
        String insertText = getInsertText(aliasComponent, annotationSymbol, withAlias);
        CompletionItem annotationItem = new CompletionItem();
        annotationItem.setLabel(label);
        annotationItem.setInsertText(insertText);
        annotationItem.setInsertTextFormat(InsertTextFormat.Snippet);
        annotationItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        annotationItem.setKind(CompletionItemKind.Property);
        if (currentPkgID != null && currentPkgID.name.value.equals(packageID.name.value)) {
            // If the annotation resides within the current package, no need to set the additional text edits
            return annotationItem;
        }
        List<BLangImportPackage> imports = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        Optional pkgImport = imports.stream()
                .filter(bLangImportPackage -> {
                    String orgName = bLangImportPackage.orgName.value;
                    String importPkgName = (orgName.equals("") ? currentProjectOrgName : orgName) + "/"
                            + CommonUtil.getPackageNameComponentsCombined(bLangImportPackage);
                    String annotationPkgOrgName = packageID.orgName.getValue();
                    String annotationPkgName = annotationPkgOrgName + "/"
                            + packageID.nameComps.stream()
                            .map(Name::getValue)
                            .collect(Collectors.joining("."));
                    return importPkgName.equals(annotationPkgName);
                })
                .findAny();
        // if the particular import statement not available we add the additional text edit to auto import
        if (!pkgImport.isPresent()) {
            annotationItem.setAdditionalTextEdits(CommonUtil.getAutoImportTextEdits(packageID.orgName.getValue(),
                    packageID.name.getValue(), ctx));
        }
        return annotationItem;
    }

    private String getInsertText(String aliasComponent, BAnnotationSymbol annotationSymbol, boolean withAlias) {
        StringBuilder annotationStart = new StringBuilder();
        if (withAlias) {
            annotationStart.append(aliasComponent).append(CommonKeys.PKG_DELIMITER_KEYWORD);
        }
        annotationStart.append(annotationSymbol.getName().getValue());

        return annotationStart.toString();
    }
}
