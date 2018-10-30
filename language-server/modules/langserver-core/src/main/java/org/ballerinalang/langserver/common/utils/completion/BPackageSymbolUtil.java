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
package org.ballerinalang.langserver.common.utils.completion;

import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * Utilities for BPackageSymbol items.
 */
public class BPackageSymbolUtil {
    /**
     * Get the Completion Item for a BType.
     * @param name                      Name of the BType
     * @return {@link CompletionItem}   Generated Completion Item
     */
    public static CompletionItem getBTypeCompletionItem(String name) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(name);
        String[] delimiterSeparatedTokens = (name).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        completionItem.setDetail(ItemResolverConstants.B_TYPE);
        completionItem.setKind(CompletionItemKind.Reference);

        return completionItem;
    }
}
