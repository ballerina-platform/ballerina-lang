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
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.CompletionSubRuleParser;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.contextproviders.AnnotationAttachmentContextProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.ballerinalang.model.tree.NodeKind;
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
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class ObjectTypeNodeScopeProvider extends AbstractCompletionProvider {

    public ObjectTypeNodeScopeProvider() {
        this.attachmentPoints.add(BLangObjectTypeNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context) throws LSCompletionException {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
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

        List<CommonToken> lhsTokens = context.get(SourcePruneKeys.LHS_TOKENS_KEY);
        Optional<String> subRule = this.getSubRule(lhsTokens);
        subRule.ifPresent(rule -> CompletionSubRuleParser.parseWithinObjectTypeDefinition(rule, context));
        ParserRuleContext parserRuleContext = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        List<CommonToken> lhsDefaultTokens = context.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY);

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
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION_SIGNATURE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_INIT_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ATTACH_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_DETACH_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_START_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_GRACEFUL_STOP_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_IMMEDIATE_STOP_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PRIVATE.get()));
        }

        return completionItems;
    }

    private void fillTypes(LSContext context, List<LSCompletionItem> completionItems) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<Scope.ScopeEntry> filteredTypes = visibleSymbols.stream()
                .filter(FilterUtils::isBTypeEntry)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(new ArrayList<>(filteredTypes), context));
        completionItems.addAll(this.getPackagesCompletionItems(context));
    }

    private void fillObjectReferences(List<LSCompletionItem> completionItems, List<CommonToken> lhsDefaultTokens,
                                      LSContext ctx) {
        CommonToken lastItem = CommonUtil.getLastItem(lhsDefaultTokens);
        if (lastItem != null && lastItem.getType() == BallerinaParser.COLON) {
            String pkgName = lhsDefaultTokens.get(1).getText();
            List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
            Optional<Scope.ScopeEntry> pkgSymbolInfo = visibleSymbols.stream()
                    .filter(scopeEntry -> scopeEntry.symbol instanceof BPackageSymbol
                            && scopeEntry.symbol.pkgID.getName().getValue().equals(pkgName))
                    .findAny();

            if (pkgSymbolInfo.isPresent()) {
                List<Scope.ScopeEntry> filteredSymbolInfo = pkgSymbolInfo.get().symbol.scope.entries.values().stream()
                        .filter(scopeEntry -> scopeEntry.symbol instanceof BObjectTypeSymbol
                                && (scopeEntry.symbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT)
                        .map(scopeEntry -> {
                            BObjectTypeSymbol oSymbol = (BObjectTypeSymbol) scopeEntry.symbol;
                            return new Scope.ScopeEntry(oSymbol, null);
                        })
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(filteredSymbolInfo, ctx));
            }
        } else {
            this.fillVisibleObjectsAndPackages(completionItems, ctx);
        }
    }

    private void fillVisibleObjectsAndPackages(List<LSCompletionItem> completionItems, LSContext ctx) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<Scope.ScopeEntry> filteredList = visibleSymbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol instanceof BObjectTypeSymbol
                        && (scopeEntry.symbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(new ArrayList<>(filteredList), ctx));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
    }
}
