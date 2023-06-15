/*
 *  Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.references;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Finds references for the given symbol within documentation (parameters).
 *
 * @since 2201.6.0
 */
public class DocumentationReferenceFinder extends NodeTransformer<List<Location>> {

    private Symbol symbol;

    public DocumentationReferenceFinder(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public List<Location> transform(RequiredParameterNode node) {
        Optional<FunctionDefinitionNode> fnDefNode = CodeActionUtil.getEnclosedFunction(node);
        if (fnDefNode.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Node> documentationNode = fnDefNode.get().metadata()
                .flatMap(metadataNode -> metadataNode.documentationString());
        if (documentationNode.isEmpty() || documentationNode.get().kind() != SyntaxKind.MARKDOWN_DOCUMENTATION) {
            return Collections.emptyList();
        }

        return getParameterLocations((MarkdownDocumentationNode) documentationNode.get());
    }

    @Override
    public List<Location> transform(RecordFieldNode node) {
        if (node.parent().parent().kind() != SyntaxKind.TYPE_DEFINITION) {
            return Collections.emptyList();
        }
        TypeDefinitionNode typeDefNode = (TypeDefinitionNode) node.parent().parent();
        Optional<Node> documentationNode = typeDefNode.metadata()
                .flatMap(metadataNode -> metadataNode.documentationString());
        if (documentationNode.isEmpty() || documentationNode.get().kind() != SyntaxKind.MARKDOWN_DOCUMENTATION) {
            return Collections.emptyList();
        }
        return getParameterLocations((MarkdownDocumentationNode) documentationNode.get());
    }

    /**
     * Provided the markdown documentation node, this will find the parameter name locations within the documentation
     * that matches the symbol name.
     *
     * @param mdNode Markdown documentation node
     * @return List of locations of the parameters within the documentation
     */
    private List<Location> getParameterLocations(MarkdownDocumentationNode mdNode) {
        return mdNode.documentationLines().stream()
                .filter(line -> line.kind() == SyntaxKind.MARKDOWN_PARAMETER_DOCUMENTATION_LINE)
                .map(line -> (MarkdownParameterDocumentationLineNode) line)
                .map(line -> line.parameterName())
                .filter(token -> token.text().equals(symbol.getName().get()))
                .map(paramToken -> paramToken.location())
                .collect(Collectors.toList());
    }

    @Override
    protected List<Location> transformSyntaxNode(Node node) {
        return Collections.emptyList();
    }
}
