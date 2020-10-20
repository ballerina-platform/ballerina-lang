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
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.WorkerSymbol;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * This class is being used to build worker.
 *
 * @since 0.983.0
 */
public final class WorkerCompletionItemBuilder {
    private WorkerCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param workerSymbol BSymbol
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(WorkerSymbol workerSymbol) {
        CompletionItem item = new CompletionItem();
        String name = workerSymbol.name();
        item.setLabel(name);
        item.setInsertText(name);
        item.setDetail(ItemResolverConstants.WORKER);
        setMeta(item, workerSymbol);
        return item;
    }

    private static void setMeta(CompletionItem item, WorkerSymbol workerSymbol) {
        item.setKind(CompletionItemKind.Variable);
        if (workerSymbol.docAttachment().isPresent() && workerSymbol.docAttachment().get().description().isPresent()) {
            item.setDocumentation(workerSymbol.docAttachment().get().description().get());
        }
    }
}
