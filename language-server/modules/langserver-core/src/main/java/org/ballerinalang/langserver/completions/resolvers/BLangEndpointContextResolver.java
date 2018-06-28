/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.common.utils.completion.BLangRecordLiteralUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.List;

/**
 * BLangEndpoint context Item Resolver.
 */
public class BLangEndpointContextResolver extends AbstractItemResolver {
    
    @Override
    @SuppressWarnings("unchecked")
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        BLangNode bLangEndpoint = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        
        if (bLangEndpoint instanceof  BLangEndpoint
                && ((BLangEndpoint) bLangEndpoint).configurationExpr instanceof BLangRecordLiteral) {
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) ((BLangEndpoint) bLangEndpoint).configurationExpr;
            completionItems.addAll(BLangRecordLiteralUtil.getFieldsForMatchingRecord(recordLiteral, completionContext));
        }

        return completionItems;
    }
}
