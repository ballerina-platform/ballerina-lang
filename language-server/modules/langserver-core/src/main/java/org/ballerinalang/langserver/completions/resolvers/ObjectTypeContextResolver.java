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
package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion item resolver for the object type.
 */
public class ObjectTypeContextResolver extends AbstractItemResolver {

    private static final Logger logger = LoggerFactory.getLogger(ObjectTypeContextResolver.class);

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode objectNode = context.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        boolean isSnippet = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();
        
        if (!objectNode.getKind().equals(NodeKind.OBJECT_TYPE)) {
            return completionItems;
        }

        List<String> poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());

        if (!poppedTokens.isEmpty() && poppedTokens.get(0).equals(UtilSymbolKeys.OBJECT_REFERENCE_SYMBOL_KEY)) {
            this.fillObjectReferences(completionItems, poppedTokens, context);
        } else if (this.isInvocationOrInteractionOrFieldAccess(context)) {
            Either<List<CompletionItem>, List<SymbolInfo>> eitherList = SymbolFilters
                    .get(DelimiterBasedContentFilter.class).filterItems(context);
            completionItems.addAll(this.getCompletionItemList(eitherList, context));
        } else if (poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
            // If the popped tokens contains the equal symbol, then the variable definition is being writing
            context.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY,
                    new BallerinaParser.VariableDefinitionStatementContext(null, -1));
            return CompletionItemResolver
                    .get(BallerinaParser.VariableDefinitionStatementContext.class)
                    .resolveItems(context);
        } else {
            fillTypes(context, completionItems);
            completionItems.add(Snippet.DEF_FUNCTION_SIGNATURE.get().build(isSnippet));
            completionItems.add(Snippet.DEF_FUNCTION.get().build(isSnippet));
            completionItems.add(Snippet.DEF_NEW_OBJECT_INITIALIZER.get().build(isSnippet));
        }

        return completionItems;
    }

    private void fillTypes(LSContext context, List<CompletionItem> completionItems) {
        List<SymbolInfo> filteredTypes = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredTypes, context));
        completionItems.addAll(this.getPackagesCompletionItems(context));
    }
    
    private void fillObjectReferences(List<CompletionItem> completionItems, List<String> poppedTokens, LSContext ctx) {
        if (CommonUtil.getLastItem(poppedTokens).equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)) {
            String pkgName = poppedTokens.get(1);
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
                logger.warn("Error retrieving Completion Items from Index DB.");
                this.fillSymbolsInPackageOnFallback(completionItems, ctx, realPkgName);
            }
        } else {
            this.fillVisibleObjectsAndPackages(completionItems, ctx);
        }
    }

    private void fillSymbolsInPackageOnFallback(List<CompletionItem> completionItems, LSContext ctx, String pkgName) {
        List<SymbolInfo> visibleSymbols = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
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
        List<SymbolInfo> visibleSymbols = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        List<SymbolInfo> filteredList = visibleSymbols.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BObjectTypeSymbol)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, ctx));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
    }
}
