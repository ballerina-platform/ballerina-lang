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
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSCompletionItem;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.CompletionSubRuleParser;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.StatementTemplateFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Parser rule based statement context provider.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class StatementContextProvider extends LSCompletionProvider {

    public StatementContextProvider() {
        this.attachmentPoints.add(StatementContextProvider.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context) {
        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        Boolean inWorkerReturn = context.get(CompletionKeys.IN_WORKER_RETURN_CONTEXT_KEY);
        int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

        if (this.isAnnotationAccessExpression(context)) {
            return this.getProvider(AnnotationAccessExpressionContextProvider.class).getCompletions(context);
        }
        if (this.isAnnotationAttachmentContext(context)) {
            return this.getProvider(AnnotationAttachmentContextProvider.class).getCompletions(context);
        }

        Optional<String> subRule = this.getSubRule(lhsTokens);
        subRule.ifPresent(rule -> CompletionSubRuleParser.parseWithinFunctionDefinition(rule, context));
        ParserRuleContext parserRuleContext = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);

        if (inWorkerReturn != null && inWorkerReturn) {
            return this.getProvider(BallerinaParser.WorkerDeclarationContext.class).getCompletions(context);
        }
        if (parserRuleContext != null && this.getProvider(parserRuleContext.getClass()) != null) {
            return this.getProvider(parserRuleContext.getClass()).getCompletions(context);
        }
        if (inFunctionReturnParameterContext(context)) {
            /*
             Check added before the invocation token check, since the return parameter context can also include the
             following
             Eg: function xyz() returns http:<cursor> {}
             */
            return this.getProvider(BallerinaParser.ReturnParameterContext.class).getCompletions(context);
        }
        if (invocationOrDelimiterTokenType > -1) {
            /*
            Action invocation context
             */
            return this.getProvider(InvocationOrFieldAccessContextProvider.class).getCompletions(context);
        }

        Boolean forceRemovedStmt = context.get(CompletionKeys.FORCE_REMOVED_STATEMENT_WITH_PARENTHESIS_KEY);
        ArrayList<LSCompletionItem> completionItems;
        List<Scope.ScopeEntry> filteredList = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (forceRemovedStmt == null || !forceRemovedStmt) {
            // Add the visible static completion items
            completionItems = new ArrayList<>(getStaticCompletionItems(context));
            // Add the statement templates
            Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> itemList =
                    SymbolFilters.get(StatementTemplateFilter.class).filterItems(context);
            completionItems.addAll(this.getCompletionItemList(itemList, context));
            completionItems.addAll(this.getTypeguardDestructedItems(filteredList, context));
        } else {
            return this.getProvider(InvocationArgsContextProvider.class).getCompletions(context);
        }

        filteredList.removeIf(this.attachedSymbolFilter());
        completionItems.addAll(this.getCompletionItemList(new ArrayList<>(filteredList), context));
        completionItems.addAll(this.getPackagesCompletionItems(context));
        // Now we need to sort the completion items and populate the completion items specific to the scope owner
        // as an example, resource, action, function scopes are different from the if-else, while, and etc

        return completionItems;
    }

    private List<LSCompletionItem> getStaticCompletionItems(LSContext context) {

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();

        // Add the xmlns snippet
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_NAMESPACE_DECLARATION.get()));
        // Add the var keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        // Add the wait keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_WAIT.get()));
        // Add the start keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_START.get()));
        // Add the flush keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()));
        // Add the function keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        // Add the error snippet
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ERROR.get()));
        // Add the checkpanic keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        // Add the final keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));

        return completionItems;
    }

    private List<LSCompletionItem> getTypeguardDestructedItems(List<Scope.ScopeEntry> scopeEntries,
                                                             LSContext ctx) {
        List<String> capturedSymbols = new ArrayList<>();
        // In the case of type guarded variables multiple symbols with the same symbol name and we ignore those
        return scopeEntries.stream()
                .filter(scopeEntry -> (scopeEntry.symbol.type instanceof BUnionType)
                        && !capturedSymbols.contains(scopeEntry.symbol.name.value))
                .map(entry -> {
                    capturedSymbols.add(entry.symbol.name.getValue());
                    List<BType> errorTypes = new ArrayList<>();
                    List<BType> resultTypes = new ArrayList<>();
                    List<BType> members = new ArrayList<>(((BUnionType) entry.symbol.type).getMemberTypes());
                    members.forEach(bType -> {
                        if (bType.tag == TypeTags.ERROR) {
                            errorTypes.add(bType);
                        } else {
                            resultTypes.add(bType);
                        }
                    });
                    if (errorTypes.size() == 1) {
                        resultTypes.addAll(errorTypes);
                    }
                    String symbolName = entry.symbol.name.getValue();
                    String label = symbolName + " - typeguard " + symbolName;
                    String detail = "Destructure the variable " + symbolName + " with typeguard";
                    StringBuilder snippet = new StringBuilder();
                    int paramCounter = 1;
                    if (errorTypes.size() > 1) {
                        snippet.append("if (").append(symbolName).append(" is ").append("error) {")
                                .append(CommonUtil.LINE_SEPARATOR).append("\t${1}").append(CommonUtil.LINE_SEPARATOR)
                                .append("}");
                        paramCounter++;
                    } else if (errorTypes.size() == 1) {
                        snippet.append("if (").append(symbolName).append(" is ")
                                .append(CommonUtil.getBTypeName(errorTypes.get(0), ctx, true)).append(") {")
                                .append(CommonUtil.LINE_SEPARATOR).append("\t${1}").append(CommonUtil.LINE_SEPARATOR)
                                .append("}");
                        paramCounter++;
                    }
                    int finalParamCounter = paramCounter;
                    String restSnippet = (!snippet.toString().isEmpty() && resultTypes.size() > 2) ? " else " : "";
                    restSnippet += IntStream.range(0, resultTypes.size() - paramCounter).mapToObj(value -> {
                        BType bType = members.get(value);
                        String placeHolder = "\t${" + (value + finalParamCounter) + "}";
                        return "if (" + symbolName + " is " + CommonUtil.getBTypeName(bType, ctx, true) + ") {"
                                + CommonUtil.LINE_SEPARATOR + placeHolder + CommonUtil.LINE_SEPARATOR + "}";
                    }).collect(Collectors.joining(" else ")) + " else {" + CommonUtil.LINE_SEPARATOR + "\t${"
                            + members.size() + "}" + CommonUtil.LINE_SEPARATOR + "}";
                    
                    snippet.append(restSnippet);

                    SnippetBlock cItemSnippet = new SnippetBlock(label, snippet.toString(), detail,
                            SnippetBlock.SnippetType.SNIPPET);
                    return new SnippetCompletionItem(ctx, cItemSnippet);
                }).collect(Collectors.toList());
    }
}

