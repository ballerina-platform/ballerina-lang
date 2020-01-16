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
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.CompletionSubRuleParser;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.providers.contextproviders.AnnotationAttachmentContextProvider;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.model.tree.NodeKind;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;

import java.util.ArrayList;
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

        if (!objectNode.getKind().equals(NodeKind.OBJECT_TYPE)) {
            return completionItems;
        }

        if (inFunctionReturnParameterContext(context)) {
            /*
             Added the check before calculation of context and etc, to avoid unnecessary parser context calculations
             */
            return this.getProvider(BallerinaParser.ReturnParameterContext.class).getCompletions(context);
        }

        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        Optional<String> subRule = this.getSubRule(lhsTokens);
        subRule.ifPresent(rule -> CompletionSubRuleParser.parseWithinObjectTypeDefinition(rule, context));
        ParserRuleContext parserRuleContext = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        List<CommonToken> lhsDefaultTokens = context.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);

        if (this.isAnnotationAttachmentContext(context)) {
            return this.getProvider(AnnotationAttachmentContextProvider.class).getCompletions(context);
        }
        if (parserRuleContext != null && this.getProvider(parserRuleContext.getClass()) != null) {
            return this.getProvider(parserRuleContext.getClass()).getCompletions(context);
        }

        if (!lhsDefaultTokens.isEmpty() && lhsDefaultTokens.get(0).getType() == BallerinaParser.MUL) {
            this.fillObjectReferences(completionItems, lhsDefaultTokens, context);
        } else {
            fillTypes(context, completionItems);
            completionItems.add(Snippet.DEF_FUNCTION_SIGNATURE.get().build(context));
            completionItems.add(Snippet.DEF_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_REMOTE_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_INIT_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_ATTACH_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_DETACH_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_START_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_GRACEFUL_STOP_FUNCTION.get().build(context));
            completionItems.add(Snippet.DEF_IMMEDIATE_STOP_FUNCTION.get().build(context));
            completionItems.add(Snippet.KW_PUBLIC.get().build(context));
            completionItems.add(Snippet.KW_PRIVATE.get().build(context));
        }

        return completionItems;
    }

    private void fillTypes(LSContext context, List<CompletionItem> completionItems) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<SymbolInfo> filteredTypes = visibleSymbols.stream()
                .filter(symbolInfo -> FilterUtils.isBTypeEntry(symbolInfo.getScopeEntry()))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredTypes, context));
        completionItems.addAll(this.getPackagesCompletionItems(context));
    }

    private void fillObjectReferences(List<CompletionItem> completionItems, List<CommonToken> lhsDefaultTokens,
                                      LSContext ctx) {
        CommonToken lastItem = CommonUtil.getLastItem(lhsDefaultTokens);
        if (lastItem != null && lastItem.getType() == BallerinaParser.COLON) {
            String pkgName = lhsDefaultTokens.get(1).getText();
            List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
            Optional<SymbolInfo> pkgSymbolInfo = visibleSymbols.stream()
                    .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BPackageSymbol
                            && symbolInfo.getScopeEntry().symbol.pkgID.getName().getValue().equals(pkgName))
                    .findAny();

            if (pkgSymbolInfo.isPresent()) {
                List<SymbolInfo> filteredSymbolInfo = pkgSymbolInfo.get().getScopeEntry().symbol.scope.entries.values()
                        .stream()
                        .filter(scopeEntry -> scopeEntry.symbol instanceof BObjectTypeSymbol
                                && (scopeEntry.symbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT)
                        .map(scopeEntry -> {
                            BObjectTypeSymbol oSymbol = (BObjectTypeSymbol) scopeEntry.symbol;
                            return new SymbolInfo(oSymbol.getName().getValue(), new Scope.ScopeEntry(oSymbol, null));
                        })
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(filteredSymbolInfo, ctx));
            }
        } else {
            this.fillVisibleObjectsAndPackages(completionItems, ctx);
        }
    }

    private void fillVisibleObjectsAndPackages(List<CompletionItem> completionItems, LSContext ctx) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<SymbolInfo> filteredList = visibleSymbols.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BObjectTypeSymbol
                        && (symbolInfo.getScopeEntry().symbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, ctx));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
    }
}
