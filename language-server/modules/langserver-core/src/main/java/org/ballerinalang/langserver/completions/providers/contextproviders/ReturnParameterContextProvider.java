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
import org.ballerinalang.langserver.LSCompletionItem;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Return parameter context provider.
 * This will be get hit for the following cases
 * 
 * function xyz() returns _cursor_ {}
 * function xyz() returns i_cursor_ {}
 * function xyz() ret {}
 * 
 * @since 1.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class ReturnParameterContextProvider extends LSCompletionProvider {

    public ReturnParameterContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ReturnParameterContext.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext ctx) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Integer> defaultTokenTypes = ctx.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<CommonToken> defaultTokens = ctx.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        Integer invocationTokenType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        List<Scope.ScopeEntry> visibleSymbols = ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        
        if (invocationTokenType == BallerinaParser.COLON) {
            String pkgName = defaultTokens.get(defaultTokenTypes.lastIndexOf(invocationTokenType) - 1).getText();
            return this.getTypeItemsInPackage(visibleSymbols, pkgName, ctx);
        }
        if (defaultTokenTypes.contains(BallerinaParser.RETURNS)) {
            /*
            suggest visible types and modules
             */
            completionItems.addAll(this.getPackagesCompletionItems(ctx));
            completionItems.addAll(this.getBasicTypesItems(ctx, visibleSymbols));
        } else {
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_RETURNS.get()));
        }

        return completionItems;
    }
}
