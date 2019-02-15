/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.LSCompletionException;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.completions.util.sorters.ActionAndFieldAccessContextItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.ballerinalang.model.util.Flags;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Parser rule based variable definition statement context resolver.
 */
public class ParserRuleVariableDefinitionStatementContextResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());

        String checkOrTrapKW = this.getCheckOrTrapKeyword(poppedTokens);

        Class sorterKey;
        if (isInvocationOrInteractionOrFieldAccess(context)) {
            sorterKey = ActionAndFieldAccessContextItemSorter.class;
            Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
            completionItems.addAll(this.getCompletionItemList(filteredList, context));
        } else if (checkOrTrapKW.equalsIgnoreCase(ItemResolverConstants.TRAP)) {
            List<SymbolInfo> filteredList = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
            // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
            filteredList.removeIf(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                return (bSymbol instanceof BInvokableSymbol
                        && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                        && CommonUtil.isValidInvokableSymbol(bSymbol))
                        || ((bSymbol instanceof BTypeSymbol)
                        && !(bSymbol instanceof BPackageSymbol))
                        || (bSymbol instanceof BInvokableSymbol
                        && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED));
            });
            completionItems.addAll(this.getCompletionItemList(filteredList, context));
            sorterKey = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY).getClass();
        } else {
            sorterKey = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY).getClass();
            completionItems.addAll(this.getVarDefCompletionItems(context));
            try {
                Optional<BLangFunctionTypeNode> functionTypeNode = this.getFunctionTypeNode(context);
                if (functionTypeNode.isPresent()) {
                    fillFunctionSnippet(functionTypeNode.get(), context, completionItems);
                    fillArrowFunctionSnippet(functionTypeNode.get(), context, completionItems);
                }
            } catch (LSCompletionException e) {
                // Ignore adding the function snippet and add the remaining completion items only
            }
        }

        CompletionItemSorter itemSorter = ItemSorters.get(sorterKey);
        itemSorter.sortItems(context, completionItems);
        
        return completionItems;
    }
    
    private String getParamsSnippet(List<BLangVariable> paramTypes, boolean withType) throws LSCompletionException {
        String paramName = "param";
        StringBuilder signature = new StringBuilder("(");
        List<String> params = IntStream.range(0, paramTypes.size())
                .mapToObj(index -> {
                    try {
                        int paramIndex = index + 1;
                        String paramPlaceHolder = "${" + paramIndex + ":" + paramName + paramIndex + "}";
                        if (withType) {
                            paramPlaceHolder = this.getTypeName(paramTypes.get(index).getTypeNode()) + " "
                                    + paramPlaceHolder;
                        }
                        return paramPlaceHolder;
                    } catch (LSCompletionException e) {
                        return "";
                    }
                })
                .collect(Collectors.toList());

        if (params.contains("")) {
            throw new LSCompletionException("Contains invalid parameter type");
        }

        signature.append(String.join(", ", params))
                .append(") ");
        
        return signature.toString();
    }
    
    private Optional<BLangFunctionTypeNode> getFunctionTypeNode(LSContext context) throws LSCompletionException {
        List<String> consumedTokens = CommonUtil.getPoppedTokenStrings(context);
        String startToken = consumedTokens.get(0);
        List<String> lastTwoTokens = consumedTokens.size() < 2 ? new ArrayList<>() :
                consumedTokens.subList(consumedTokens.size() - 2, consumedTokens.size());
        if (!startToken.equals(UtilSymbolKeys.FUNCTION_KEYWORD_KEY)
                || !lastTwoTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
            return Optional.empty();
        }
        String combinedTokens = String.join(" ", consumedTokens) + "0;";
        String functionRule = "function testFunction () {" + CommonUtil.LINE_SEPARATOR + "\t" + combinedTokens +
                CommonUtil.LINE_SEPARATOR + "}";

        Optional<BLangPackage> bLangPackage;
        try {
            bLangPackage = LSCompiler.compileContent(functionRule, CompilerPhase.CODE_ANALYZE)
                    .getBLangPackage();
        } catch (LSCompilerException e) {
            throw new LSCompletionException("Error while parsing the sub-rule" +
                    " for anonymous function snippet generation");
        }

        if (!bLangPackage.isPresent()) {
            return Optional.empty();
        }

        BLangStatement evalStatement = bLangPackage.get().getFunctions().get(0).getBody().stmts.get(0);

        if (!(evalStatement instanceof BLangSimpleVariableDef)) {
            return Optional.empty();
        }

        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) ((BLangSimpleVariableDef) evalStatement)
                .getVariable().getTypeNode();
        
        return Optional.of(functionTypeNode);
    }
    
    private void fillFunctionSnippet(BLangFunctionTypeNode functionTypeNode, LSContext context,
                                     List<CompletionItem> completionItems)
            throws LSCompletionException {
        
        List<BLangVariable> params = functionTypeNode.getParams();
        BLangType returnBLangType = functionTypeNode.getReturnTypeNode();
        boolean snippetSupport = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY)
                .getCompletionItem()
                .getSnippetSupport();
        String functionSignature = this.getFunctionSignature(params, returnBLangType);
        String body = this.getAnonFunctionSnippetBody(returnBLangType, params.size());
        String snippet = functionSignature + body;
        String label = this.convertToLabel(functionSignature);
        SnippetBlock snippetBlock = new SnippetBlock(label, snippet, ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.SnippetType.SNIPPET);

        // Populate the anonymous function signature completion item
        completionItems.add(snippetBlock.build(snippetSupport));
    }

    private String getFunctionSignature(List<BLangVariable> paramTypes, BLangType returnType)
            throws LSCompletionException {
        StringBuilder signature = new StringBuilder("function ");

        signature.append(this.getParamsSnippet(paramTypes, true));
        if (!(returnType.type instanceof BNilType)) {
            signature.append("returns (")
                    .append(this.getTypeName(returnType))
                    .append(") ");
        }

        return signature.toString();
    }
    
    private String convertToLabel(String signature) {
        return signature
                .replaceAll("(\\$\\{\\d:)([a-zA-Z\\d]*:*[a-zA-Z\\d]*)(\\})", "$2")
                .replaceAll("(\\$\\{\\d\\})", "");
    }
    
    private void fillArrowFunctionSnippet(BLangFunctionTypeNode functionTypeNode, LSContext context,
                                            List<CompletionItem> completionItems) throws LSCompletionException {
        List<BLangVariable> params = functionTypeNode.getParams();
        BLangType returnBLangType = functionTypeNode.getReturnTypeNode();
        String paramSignature = this.getParamsSnippet(params, false);
        boolean snippetSupport = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY)
                .getCompletionItem()
                .getSnippetSupport();
        StringBuilder signature = new StringBuilder(paramSignature);
        
        signature.append(" => ")
                .append("${");
        if (!(returnBLangType.type instanceof BNilType)) {
            signature.append(params.size() + 1)
                    .append(":")
                    .append(CommonUtil.getDefaultValueForType(returnBLangType.type));
        } else {
            signature.append(params.size() + 1);
        }
        signature.append("};");
        
        String label = "arrow function  " + this.convertToLabel(paramSignature);

        SnippetBlock snippetBlock = new SnippetBlock(label, signature.toString(), ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.SnippetType.SNIPPET);

        // Populate the anonymous function signature completion item
        completionItems.add(snippetBlock.build(snippetSupport));
    }
    
    private String getAnonFunctionSnippetBody(BLangType returnType, int numberOfParams) throws LSCompletionException {
        StringBuilder body = new StringBuilder();
        if (!(returnType.type instanceof BNilType)) {
            body.append("{")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append("\t")
                    .append("return ")
                    .append(CommonUtil.getDefaultValueForType(returnType.type))
                    .append(";")
                    .append(CommonUtil.LINE_SEPARATOR);
        } else {
            body.append("{")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append("\t${")
                    .append(numberOfParams + 1)
                    .append("}")
                    .append(CommonUtil.LINE_SEPARATOR);
        }

        body.append("};");

        return body.toString();
    }

    private String getTypeName(BLangType bLangType) throws LSCompletionException {
        if (bLangType instanceof BLangValueType || bLangType instanceof BLangUnionTypeNode) {
            return bLangType.toString();
        } else if (bLangType instanceof BLangUserDefinedType) {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) bLangType;
            String pkgAlias = userDefinedType.getPackageAlias().getValue();
            String typeName = userDefinedType.getTypeName().getValue();
            return pkgAlias.isEmpty() ? typeName : (pkgAlias + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + typeName);
        } else {
            throw new LSCompletionException("Error identifying the type of anonymous function parameter");
        }
    }



    private String getCheckOrTrapKeyword(List<String> poppedTokens) {
        String retrievedToken = "";
        for (String token : Lists.reverse(poppedTokens)) {
            if (token.equals(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
                break;
            } else if (token.equals(ItemResolverConstants.CHECK_KEYWORD)
                    || token.equals(ItemResolverConstants.TRAP)) {
                retrievedToken = token;
                break;
            }
        }

        return retrievedToken;
    }
}
