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

import org.ballerinalang.langserver.commons.LSContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Completion Item sorter to sort items inside a callable unit body. This is used by functions and resources.
 */
class CallableUnitBodyItemSorter extends CompletionItemSorter {
    @Override
    public void sortItems(LSContext ctx, List<CompletionItem> completionItems) {
        this.setPriorities(completionItems);
    }

    @Nonnull
    @Override
    List<Class> getAttachedContexts() {
        return Collections.singletonList(CallableUnitBodyItemSorter.class);
    }
}
