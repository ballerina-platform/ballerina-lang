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
package org.ballerinalang.langserver.completions.util.sorters;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Completion item sorter for the transformer scope context.
 */
class TransformerContextItemSorter extends CompletionItemSorter {
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        ParserRuleContext parserRuleContext = ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY);
        if (parserRuleContext == null || parserRuleContext instanceof BallerinaParser.StatementContext) {
            List<String> typesToRemove = new ArrayList<>(Arrays.asList(ItemResolverConstants.STATEMENT_TYPE,
                    ItemResolverConstants.FUNCTION_TYPE));
            this.removeCompletionsByType(typesToRemove, completionItems);
            this.setPriorities(completionItems);
        }
    }
}
