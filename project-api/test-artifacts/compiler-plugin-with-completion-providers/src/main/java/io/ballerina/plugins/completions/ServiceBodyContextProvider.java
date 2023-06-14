/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.plugins.completions;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.completion.CompletionContext;
import io.ballerina.projects.plugins.completion.CompletionException;
import io.ballerina.projects.plugins.completion.CompletionItem;
import io.ballerina.projects.plugins.completion.CompletionProvider;
import io.ballerina.projects.plugins.completion.CompletionUtil;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.util.Collections;
import java.util.List;

/**
 * An example of a completion provider that adds a resource function to a service declaration.
 *
 * @since 2201.7.0
 */
public class ServiceBodyContextProvider implements CompletionProvider<ServiceDeclarationNode> {

    @Override
    public String name() {
        return "ServiceBodyContextProvider";
    }

    @Override
    public List<CompletionItem> getCompletions(CompletionContext context, ServiceDeclarationNode node)
            throws CompletionException {
        //Adds a resource function if one is not present with path foo
        if (node.members().stream().anyMatch(member -> member.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION &&
                ((FunctionDefinitionNode) member).relativeResourcePath()
                        .stream().anyMatch(path -> "foo".equals(path.toSourceCode())))) {
            return Collections.emptyList();
        }

        String insertText = "resource function " + CompletionUtil.getPlaceHolderText(1, "get") + " "
                + CompletionUtil.getPlaceHolderText(2, "foo") + "(" + CompletionUtil.getPlaceHolderText(3) + ")" +
                " returns " + CompletionUtil.getPlaceHolderText(4, "string") + " {" + CompletionUtil.LINE_BREAK +
                CompletionUtil.PADDING + "return " + CompletionUtil.getPlaceHolderText(5, "\"\"") + ";"
                + CompletionUtil.LINE_BREAK + "}";
        String label = "resource function get foo() returns string";

        CompletionItem completionItem = new CompletionItem(label, insertText, CompletionItem.Priority.HIGH);

        //Additional text edit to add documentation for service if not present
        if (node.metadata().isEmpty() || node.metadata().get().documentationString().isEmpty()) {
            String documentation = "#Sample service with foo resource" + CompletionUtil.LINE_BREAK;
            TextRange textRange = TextRange.from(node.textRange().startOffset(), 0);
            TextEdit textEdit = TextEdit.from(textRange, documentation);
            completionItem.setAdditionalTextEdits(List.of(textEdit));
        }
        return List.of(completionItem);
    }

    @Override
    public List<Class<ServiceDeclarationNode>> getSupportedNodes() {
        return List.of(ServiceDeclarationNode.class);
    }
}
