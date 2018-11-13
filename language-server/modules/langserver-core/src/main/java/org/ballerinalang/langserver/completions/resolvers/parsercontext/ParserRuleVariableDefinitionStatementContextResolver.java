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
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
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
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        Class sorterKey;
        if (isInvocationOrInteractionOrFieldAccess(completionContext)) {
            sorterKey = ActionAndFieldAccessContextItemSorter.class;
            Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(completionContext);
            completionItems.addAll(this.getCompletionItemList(filteredList));
        } else {
            sorterKey = completionContext.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY).getClass();
            completionItems.addAll(this.getVarDefCompletionItems(completionContext));
            try {
                fillFunctionSnippet(completionContext, completionItems);
            } catch (LSCompletionException e) {
                // Ignore adding the function snippet and add the remaining completion items only
            }
        }

        CompletionItemSorter itemSorter = ItemSorters.get(sorterKey);
        itemSorter.sortItems(completionContext, completionItems);
        
        return completionItems;
    }
    
    private void fillFunctionSnippet(LSContext context, List<CompletionItem> completionItems)
            throws LSCompletionException {
        List<String> consumedTokens = CommonUtil.getPoppedTokenStrings(context);
        String startToken = consumedTokens.get(0);
        List<String> lastTwoTokens = consumedTokens.size() < 2 ? new ArrayList<>() : 
                consumedTokens.subList(consumedTokens.size() - 2, consumedTokens.size());
        if (!startToken.equals(UtilSymbolKeys.FUNCTION_KEYWORD_KEY)
                || !lastTwoTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
            return;
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
            return;
        }

        BLangStatement evalStatement = bLangPackage.get().getFunctions().get(0).getBody().stmts.get(0);

        if (!(evalStatement instanceof BLangVariableDef)) {
            return;
        }

        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) ((BLangVariableDef) evalStatement)
                .getVariable().getTypeNode();
        List<BLangVariable> params = functionTypeNode.getParams();
        BLangType returnBLangType = functionTypeNode.getReturnTypeNode();
        boolean snippetSupport = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY)
                .getCompletionItem()
                .getSnippetSupport();
        String functionSignature = this.getFunctionSignature(params, returnBLangType);
        String body = this.getAnonFunctionSnippetBody(returnBLangType, params.size());
        String snippet = functionSignature + body;
        
        SnippetBlock snippetBlock = new SnippetBlock(functionSignature, snippet, ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.SnippetType.SNIPPET);
        
        // Populate the anonymous function signature completion item
        completionItems.add(snippetBlock.build(new CompletionItem(), snippetSupport));
    }

    private String getFunctionSignature(List<BLangVariable> paramTypes, BLangType returnType)
            throws LSCompletionException {
        String paramName = "param";
        StringBuilder signature = new StringBuilder("function (");
        List<String> params = IntStream.range(0, paramTypes.size())
                .mapToObj(index -> {
                    try {
                        int paramIndex = index + 1;
                        String paramPlaceHolder = "${" + paramIndex + ":" + paramName + paramIndex + "}";
                        return this.getTypeName(paramTypes.get(index).getTypeNode()) + " " + paramPlaceHolder;
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
        
        if (!(returnType.type instanceof BNilType)) {
            signature.append("returns (")
                    .append(this.getTypeName(returnType))
                    .append(") ");
        }
        
        return signature.toString();
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
        if (bLangType instanceof BLangValueType) {
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
}
