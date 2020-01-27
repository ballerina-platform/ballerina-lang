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
import org.antlr.v4.runtime.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the External Function body context.
 *
 * @since 1.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class ExternalFunctionBodyContextProvider extends AbstractCompletionProvider {

    public ExternalFunctionBodyContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ExternalFunctionBodyContext.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<CommonToken> lhsDefaultTokens = context.get(CompletionKeys.LHS_TOKENS_KEY).stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        
        if (!lhsDefaultTokens.isEmpty() && CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.ASSIGN
                || (lhsDefaultTokens.size() > 3
                && lhsDefaultTokens.get(lhsDefaultTokens.size() - 2).getType() == BallerinaParser.ASSIGN)) {
            /*
            Consider the following case
            Eg: function x =
                function x = ex
                Suggest the external keyword
             */
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_EXTERNAL.get()));
        }
        return completionItems;
    }
}
