/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ReturnTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ReturnTypeDescriptorNodeContext extends AbstractCompletionProvider<ReturnTypeDescriptorNode> {

    public ReturnTypeDescriptorNodeContext() {
        super(ReturnTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ReturnTypeDescriptorNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.onQualifiedNameIdentifier(context, node.type())
                && withinIdentifierContext(context, (QualifiedNameReferenceNode) node.type())) {
            /*
            Covers the following cases.
            (1) function test() returns moduleName:<cursor>
            (2) function test() returns moduleName:F<cursor>
            */
            String modulePrefix = QNameReferenceUtil.getAlias(((QualifiedNameReferenceNode) node.type()));
            Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(context, modulePrefix);
            if (moduleSymbol.isPresent()) {
                moduleSymbol.ifPresent(scopeEntry -> {
                    List<TypeSymbol> entries = this.filterTypesInModule(moduleSymbol.get());
                    completionItems.addAll(this.getCompletionItemList(entries, context));
                });
            }
        } else {
            /*
            Covers the following cases.
            (1) function test() returns <cursor>
            (2) function test() returns i<cursor>
            */
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.addAll(this.getTypeItems(context));
        }

        return completionItems;
    }

    private boolean withinIdentifierContext(LSContext context, QualifiedNameReferenceNode node) {
        LineRange colonLineRange = node.colon().lineRange();
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();

        return colonLineRange.endLine().line() == cursor.getLine()
                && colonLineRange.endLine().offset() <= cursor.getCharacter();
    }
}
