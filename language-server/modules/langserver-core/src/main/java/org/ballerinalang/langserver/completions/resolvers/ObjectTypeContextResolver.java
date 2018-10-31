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
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.model.tree.NodeKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion item resolver for the object type.
 */
public class ObjectTypeContextResolver extends AbstractItemResolver {
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
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) objectNode;
        List<BLangVariable> fields = objectTypeNode.fields;
        List<BLangFunction> functions = objectTypeNode.functions;
        BLangFunction initFunction = objectTypeNode.initFunction;
        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int line = position.getLine();
        int col = position.getCharacter();
        
        DiagnosticPos lastFieldPos = fields.isEmpty() ? null
                : CommonUtil.toZeroBasedPosition(CommonUtil.getLastItem(fields).pos);
        if (poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
            // If the popped tokens contains the equal symbol, then the variable definition is being writing
            context.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY,
                    new BallerinaParser.VariableDefinitionStatementContext(null, -1));
            return CompletionItemResolver
                    .getResolverByClass(BallerinaParser.VariableDefinitionStatementContext.class)
                    .resolveItems(context);
        } else if (lastFieldPos != null
                && (line < lastFieldPos.sLine || (line == lastFieldPos.sLine && col < lastFieldPos.sCol))) {
            fillTypes(context, completionItems);
        } else if (initFunction != null && objectTypeNode.initFunction.objInitFunction) {
            DiagnosticPos initFuncPos = CommonUtil.toZeroBasedPosition(initFunction.pos);
            DiagnosticPos firstFuncPos = functions.isEmpty() ? null
                    : CommonUtil.toZeroBasedPosition(functions.get(0).pos);
            if (line < initFuncPos.sLine || (line == initFuncPos.sLine && col < initFuncPos.sCol)) {
                fillTypes(context, completionItems);
            } else if ((firstFuncPos != null && line < firstFuncPos.sLine) || firstFuncPos == null) {
                fillFunctionSignature(completionItems, isSnippet);
            }
        } else if (!functions.isEmpty()) {
            DiagnosticPos firstFuncPos = CommonUtil.toZeroBasedPosition(functions.get(0).pos);
            if (line < firstFuncPos.sLine) {
                fillTypes(context, completionItems);
                fillInitializerSignature(completionItems, isSnippet);
                fillFunctionSignature(completionItems, isSnippet);
            } else {
                fillFunctionSignature(completionItems, isSnippet);
            }
        } else {
            fillTypes(context, completionItems);
            fillInitializerSignature(completionItems, isSnippet);
            fillFunctionSignature(completionItems, isSnippet);
        }
        
        return completionItems;
    }
    
    private void fillTypes(LSContext context, List<CompletionItem> completionItems) {
        List<SymbolInfo> filteredTypes = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredTypes));
    }
    
    private void fillFunctionSignature(List<CompletionItem> completionItems, boolean snippetCapability) {
        CompletionItem functionSignatureItem = new CompletionItem();
        Snippet.DEF_FUNCTION_SIGNATURE.get().build(functionSignatureItem, snippetCapability);
        completionItems.add(functionSignatureItem);
        
        CompletionItem functionItem = new CompletionItem();
        Snippet.DEF_FUNCTION.get().build(functionItem, snippetCapability);
        completionItems.add(functionItem);
    }
    
    private void fillInitializerSignature(List<CompletionItem> completionItems, boolean snippetCapability) {
        CompletionItem constructorItem = new CompletionItem();
        Snippet.DEF_NEW_OBJECT_CONSTRUCTOR.get().build(constructorItem, snippetCapability);
        completionItems.add(constructorItem);
    }
}
