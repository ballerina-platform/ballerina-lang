/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ModulePartNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ModulePartNodeContext extends AbstractCompletionProvider<ModulePartNode> {

    public ModulePartNodeContext() {
        super(Kind.MODULE_MEMBER);
        this.attachmentPoints.add(ModulePartNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ModulePartNode node) {
        Optional<Node> routeToChild = this.routeToChild(context, node);
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (routeToChild.isPresent()) {
            try {
                return CompletionUtil.route(context, routeToChild.get());
            } catch (LSCompletionException e) {
                // ignore
            }
        } else {
            completionItems.addAll(addTopLevelItems(context));
            completionItems.addAll(this.getTypeItems(context));
            completionItems.addAll(this.getPackagesCompletionItems(context));
        }
        return completionItems;
    }

    private Optional<Node> routeToChild(LSContext context, ModulePartNode modulePartNode) {
        for (ModuleMemberDeclarationNode member : modulePartNode.members()) {
            Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
            LinePosition endLine = member.lineRange().endLine();
            LinePosition startLine = member.lineRange().startLine();

            if (cursor.getLine() < startLine.line()
                    || (cursor.getLine() == startLine.line() && cursor.getCharacter() <= startLine.offset())) {
                return Optional.empty();
            } else if (cursor.getLine() == endLine.line() && cursor.getCharacter() <= endLine.offset()) {
                return Optional.of(member);
            }
        }

        return Optional.empty();
    }
}
