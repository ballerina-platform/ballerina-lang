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
package org.ballerinalang.langserver.completions.providers.subproviders;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Item provider for the BLangFunction context.
 */
public class FunctionCompletionProvider extends LSCompletionProvider {
    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        ParserRuleContext parserRuleContext = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        if (parserRuleContext == null) {
            List<CompletionItem> completionItems = new ArrayList<>();
            CompletionItem workerItem = Snippet.DEF_WORKER.get().build(context);
            completionItems.add(workerItem);
            return completionItems;
        }
        return new ArrayList<>();
    }
}
