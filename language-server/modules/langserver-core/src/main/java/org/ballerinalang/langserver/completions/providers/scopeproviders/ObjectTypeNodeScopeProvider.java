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
package org.ballerinalang.langserver.completions.providers.scopeproviders;

import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.providers.contextproviders.AnnotationAttachmentContextProvider;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.index.dao.BPackageSymbolDAO;
import org.ballerinalang.langserver.index.dao.DAOType;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.model.tree.NodeKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion item provider for the object type.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class ObjectTypeNodeScopeProvider extends LSCompletionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypeNodeScopeProvider.class);
    public ObjectTypeNodeScopeProvider() {
        this.attachmentPoints.add(BLangObjectTypeNode.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode objectNode = context.get(CompletionKeys.SCOPE_NODE_KEY);
        int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

        if (!objectNode.getKind().equals(NodeKind.OBJECT_TYPE)) {
            return completionItems;
        }

        List<CommonToken> lhsDefaultTokens = context.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);

        if (this.isAnnotationAttachmentContext(context)) {
            return this.getProvider(AnnotationAttachmentContextProvider.class).getCompletions(context);
        }

        if (!lhsDefaultTokens.isEmpty() && lhsDefaultTokens.get(0).getType() == BallerinaParser.MUL) {
            this.fillObjectReferences(completionItems, lhsDefaultTokens, context);
        } else if (invocationOrDelimiterTokenType > -1) {
            Either<List<CompletionItem>, List<SymbolInfo>> eitherList = SymbolFilters
                    .get(DelimiterBasedContentFilter.class).filterItems(context);
            completionItems.addAll(this.getCompletionItemList(eitherList, context));
        } else {
            fillTypes(context, completionItems);
            completionItems.add(Snippet.DEF_FUNCTION_SIGNATURE.get().build(context));
            completionItems.add(Snippet.DEF_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_NEW_OBJECT_INITIALIZER.get().build(context));
        }

        return completionItems;
    }

    private void fillTypes(LSContext context, List<CompletionItem> completionItems) {
        List<SymbolInfo> filteredTypes = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredTypes, context));
        completionItems.addAll(this.getPackagesCompletionItems(context));
    }

    private void fillObjectReferences(List<CompletionItem> completionItems, List<CommonToken> lhsDefaultTokens,
                                      LSContext ctx) {
        if (CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.COLON) {
            String pkgName = lhsDefaultTokens.get(1).getText();
            LSIndexImpl lsIndex = ctx.get(LSGlobalContextKeys.LS_INDEX_KEY);
            String relativePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            BLangPackage currentBLangPkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
            BLangPackage sourceOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, currentBLangPkg);
            Optional bLangImport = CommonUtil.getCurrentFileImports(sourceOwnerPkg, ctx).stream()
                    .filter(importPkg -> importPkg.getAlias().getValue().equals(pkgName))
                    .findFirst();

            String realPkgName;
            String realOrgName;

            if (bLangImport.isPresent()) {
                // There is an added import statement.
                realPkgName = CommonUtil.getPackageNameComponentsCombined(((BLangImportPackage) bLangImport.get()));
                realOrgName = ((BLangImportPackage) bLangImport.get()).getOrgName().getValue();
            } else {
                realPkgName = pkgName;
                realOrgName = "";
            }

            try {
                BPackageSymbolDTO dto = new BPackageSymbolDTO.BPackageSymbolDTOBuilder()
                        .setName(realPkgName)
                        .setOrgName(realOrgName)
                        .build();
                List<BPackageSymbolDTO> result = ((BPackageSymbolDAO) lsIndex.getDaoFactory()
                        .get(DAOType.PACKAGE_SYMBOL)).get(dto);

                if (result.isEmpty()) {
                    this.fillSymbolsInPackageOnFallback(completionItems, ctx, realPkgName);
                } else {
                    HashMap<Integer, ArrayList<CompletionItem>> completionMap = new HashMap<>();
                    BPackageSymbolDAO pkgSymbolDAO = ((BPackageSymbolDAO) lsIndex.getDaoFactory()
                            .get(DAOType.PACKAGE_SYMBOL));
                    ArrayList<BObjectTypeSymbolDTO> objDTOs = new ArrayList<>(pkgSymbolDAO.getObjects(dto, false));

                    if (bLangImport.isPresent()) {
                        completionItems.addAll(objDTOs.stream()
                                .map(BObjectTypeSymbolDTO::getCompletionItem)
                                .collect(Collectors.toList()));
                    } else {
                        objDTOs.forEach(objDto ->
                                CommonUtil.populateIdCompletionMap(completionMap, objDto.getPackageId(),
                                        objDto.getCompletionItem()));
                        completionItems.addAll(CommonUtil.fillCompletionWithPkgImport(completionMap, ctx));
                    }
                }
            } catch (LSIndexException e) {
                LOGGER.warn("Error retrieving Completion Items from Index DB.");
                this.fillSymbolsInPackageOnFallback(completionItems, ctx, realPkgName);
            }
        } else {
            this.fillVisibleObjectsAndPackages(completionItems, ctx);
        }
    }

    private void fillSymbolsInPackageOnFallback(List<CompletionItem> completionItems, LSContext ctx, String pkgName) {
        List<SymbolInfo> visibleSymbols = ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<SymbolInfo> pkgSymbolInfo = visibleSymbols.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BPackageSymbol
                        && symbolInfo.getScopeEntry().symbol.pkgID.getName().getValue().equals(pkgName))
                .findAny();

        if (pkgSymbolInfo.isPresent()) {
            List<SymbolInfo> filteredSymbolInfo = pkgSymbolInfo.get().getScopeEntry().symbol.scope.entries.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().symbol instanceof BObjectTypeSymbol)
                    .map(entry -> {
                        BObjectTypeSymbol objSymbol = (BObjectTypeSymbol) entry.getValue().symbol;
                        return new SymbolInfo(objSymbol.getName().getValue(), new Scope.ScopeEntry(objSymbol, null));
                    })
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(filteredSymbolInfo, ctx));
        }
    }

    private void fillVisibleObjectsAndPackages(List<CompletionItem> completionItems, LSContext ctx) {
        List<SymbolInfo> visibleSymbols = ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        List<SymbolInfo> filteredList = visibleSymbols.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BObjectTypeSymbol)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, ctx));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
    }
}
