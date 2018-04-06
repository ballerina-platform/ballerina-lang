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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;

/**
 * expression context resolver for the completion items.
 * @since 0.961.0
 */
public class ParserRuleExpressionContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ParserRuleContext contextParent = completionContext
                .get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).getParent();
        // TODO: Specially add the check in case of binary expressions
        if (contextParent instanceof BallerinaParser.BinaryEqualExpressionContext) {
            contextParent = contextParent.getParent();
        }
        if (contextParent != null) {
            return CompletionItemResolver.getResolverByClass(contextParent.getClass()).resolveItems(completionContext);
        }
        return new ArrayList<>();
    }
}
