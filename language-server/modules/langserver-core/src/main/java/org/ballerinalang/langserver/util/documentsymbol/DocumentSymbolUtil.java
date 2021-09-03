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
package org.ballerinalang.langserver.util.documentsymbol;

import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.commons.DocumentSymbolContext;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Finds Symbols within a document.
 *
 * @since 2.0.0
 */
public class DocumentSymbolUtil {

    /**
     * get document symbols given the context.
     *
     * @param context document symbol context.
     * @return {@link List<Either<SymbolInformation, DocumentSymbol>>} generated document symbols.
     */
    public static List<Either<SymbolInformation, DocumentSymbol>> documentSymbols(DocumentSymbolContext context) {
        List<Either<SymbolInformation, DocumentSymbol>> symbols = new ArrayList<>();
        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        if (syntaxTree.isEmpty()) {
            return symbols;
        }
        Node rootNode = syntaxTree.get().rootNode();
        DocumentSymbolResolver symbolResolver = new DocumentSymbolResolver(context);
        rootNode.apply(symbolResolver);
        symbolResolver.getDocumentSymbolStore().forEach(symbol -> symbols.add(Either.forRight(symbol)));
        return symbols;
    }

    /**
     * Generate the node range provided the node.
     *
     * @param node Syntax tree node.
     * @return {@link Range} node range.
     */
    public static Range generateNodeRange(Node node) {
        Position startPosition = new Position(node.lineRange().startLine().line(),
                node.lineRange().startLine().offset());
        Position endPosition = new Position(node.lineRange().endLine().line(),
                node.lineRange().endLine().offset());
        return new Range(startPosition, endPosition);
    }

    /**
     * Returns if the given metadata contains deprecated annotation.
     *
     * @param metadata Metadata.
     * @return isDeprecated.
     */
    public static boolean isDeprecated(MetadataNode metadata) {
        return metadata.annotations().stream().filter(annotation ->
                annotation.annotReference().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                        && ((SimpleNameReferenceNode) annotation.annotReference()).name().text()
                        .equals("deprecated")).collect(Collectors.toList()).size() > 0;
    }
}
