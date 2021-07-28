/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.toml.ballerinatoml.completion;

import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.toml.AbstractTomlCompletionContext;
import org.ballerinalang.langserver.toml.common.completion.TomlCompletionUtil;
import org.ballerinalang.langserver.toml.common.completion.visitor.TomlNode;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Responsible for providing completion items based on the context.
 *
 * @since 2.0.0
 */
public class BallerinaTomlCompletionUtil extends TomlCompletionUtil {

    public static List<CompletionItem> getCompletionItems(AbstractTomlCompletionContext ctx,
                                                          LanguageServerContext serverContext) {
        List<CompletionItem> completionItems = new ArrayList<>();
        fillNodeAtCursor(ctx);
        completionItems.addAll(getCompletionItemsBasedOnPosition(ctx, serverContext));
        return completionItems;
    }

    /**
     * Returns a set of completion items based on the completion context.
     *
     * @return {@link List<CompletionItem>}completion items for the current context.
     */
    public static List<CompletionItem> getCompletionItemsBasedOnPosition(AbstractTomlCompletionContext ctx,
                                                                         LanguageServerContext serverContext) {
        Optional<NonTerminalNode> node = ctx.getNodeAtCursor();
        if (node.isEmpty()) {
            return Collections.emptyList();
        }
        Node reference = node.get();

        //Get possible completions based on schema
        Map<String, CompletionItem> completions;
        Map<TomlNode, Map<String, CompletionItem>> snippets =
                BallerinaTomlSnippetManager.getInstance(serverContext).getCompletions();

        //Filter snippets based on the reference node.
        while (reference != null) {
            if (reference.kind() == SyntaxKind.TABLE) {
                completions = getFilteredCompletions(Either.forLeft((TableNode) reference), snippets);
                return new ArrayList<>(completions.values());
            }
            if (reference.kind() == SyntaxKind.TABLE_ARRAY) {
                completions = getFilteredCompletions(Either.forRight((TableArrayNode) reference), snippets);
                return new ArrayList<>(completions.values());
            }
            reference = reference.parent();
        }
        return new ArrayList<>(addTopLevelNodeCompletions(snippets.keySet(), new HashMap<>()).values());
    }
}
