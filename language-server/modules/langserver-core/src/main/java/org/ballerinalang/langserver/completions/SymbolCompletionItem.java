/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions;

import org.ballerinalang.langserver.LSCompletionItem;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import javax.annotation.Nullable;

/**
 * Represents a Symbol Based Completion Item.
 * Eg: Invocation Symbol
 *
 * @since 1.2.0
 */
public class SymbolCompletionItem implements LSCompletionItem {
    private LSContext lsContext;
    private BSymbol bSymbol;
    private CompletionItem completionItem;

    public SymbolCompletionItem(LSContext lsContext, @Nullable BSymbol bSymbol, CompletionItem completionItem) {
        this.lsContext = lsContext;
        this.bSymbol = bSymbol;
        this.completionItem = completionItem;
    }

    public BSymbol getSymbol() {
        return bSymbol;
    }

    @Override
    public String getSortText() {
        return null;
    }

    @Override
    public String setSortText() {
        return null;
    }

    @Override
    public CompletionItem getCompletionItem() {
        return this.completionItem;
    }
}
