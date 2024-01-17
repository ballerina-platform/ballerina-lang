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
package org.ballerinalang.langserver.references;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for go to definition functionality of language server.
 */
public class ReferencesUtil {
    private ReferencesUtil() {
    }
    
    public static Map<Module, List<Location>> getReferences(PositionedOperationContext context) {
        Map<Module, List<Location>> references = new HashMap<>();
        Optional<Project> project = context.workspace().project(context.filePath());
        Optional<Symbol> symbol = getSymbolAtCursor(context);
        if (project.isEmpty() || symbol.isEmpty()) {
            return references;
        }
        references.putAll(getReferences(project.get(), symbol.get()));
        references.forEach((module, locations) -> {
            List<Location> docReferences = new LinkedList<>();
            // Find references in documentation
            locations.forEach(location -> {
                List<Location> refs = findReferencesInDocumentation(location, module, context, symbol.get());
                if (refs != null && !refs.isEmpty()) {
                    docReferences.addAll(refs);
                }
            });
            locations.addAll(docReferences);
        });
        return references;
    }

    /**
     * Given a project and a symbol, returns references to that symbol within the project.
     *
     * @param project Project
     * @param symbol  Symbol to be searched for references
     * @return Map of module and list of reference locations.
     */
    public static Map<Module, List<Location>> getReferences(Project project, Symbol symbol) {
        Map<Module, List<Location>> moduleLocationMap = new HashMap<>();
        project.currentPackage().moduleIds().forEach(moduleId -> {
            List<Location> references = project.currentPackage()
                    .getCompilation().getSemanticModel(moduleId).references(symbol);
            if (references.isEmpty()) {
                return;
            }
            Module module = project.currentPackage().module(moduleId);
            moduleLocationMap.put(module, references);
        });
        
        return moduleLocationMap;
    }
    
    private static List<Location> findReferencesInDocumentation(Location location, 
                                                                Module module, 
                                                                PositionedOperationContext context,
                                                                Symbol symbol) {
        Path filePath = PathUtil.getPathFromLocation(module, location);
        Range range = PositionUtil.getRangeFromLineRange(location.lineRange());
        Optional<NonTerminalNode> node = context.workspace().syntaxTree(filePath)
                .map(syntaxTree -> CommonUtil.findNode(range, syntaxTree));
        if (node.isEmpty() || node.get().kind() == SyntaxKind.LIST) {
            return Collections.emptyList();
        }

        DocumentationReferenceFinder finder = new DocumentationReferenceFinder(symbol);
        return node.get().apply(finder);
    }
    
    /**
     * Returns the symbol at cursor handling a special case where cursor position is at the RHS end of the symbol.
     *
     * @param context Operation context triggered with a cursor position
     * @return Symbol at cursor
     */
    public static Optional<Symbol> getSymbolAtCursor(PositionedOperationContext context) {
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();

        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return Optional.empty();
        }

        Document document = srcFile.get();
        Position position = context.getCursorPosition();
        TextRange range = TextRange.from(
                document.textDocument().textPositionFrom(PositionUtil.getLinePosition(position)), 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.syntaxTree().rootNode()).findNode(range);
        SyntaxKind parentKind = nonTerminalNode.parent().kind();

        if (parentKind == SyntaxKind.TYPE_PARAMETER || parentKind == SyntaxKind.STREAM_TYPE_PARAMS) {
            if (nonTerminalNode.lineRange().endLine().offset() == position.getCharacter()) {
                // When there is a type parameter and cursor is at the end of the type parameter, semantic API does not
                // provide the correct symbol. Therefore, here we search for the symbol at (col - 1).
                return semanticModel.get().symbol(document,
                        LinePosition.from(position.getLine(), position.getCharacter() - 1));
            }
        }

        Optional<Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(),
                LinePosition.from(position.getLine(), position.getCharacter()));

        if (symbolAtCursor.isEmpty()) {
            if (position.getCharacter() == 0) {
                return Optional.empty();
            }

            // If we did not find the symbol, there are 2 possibilities.
            //  1. Cursor is at the end (RHS) of the symbol
            //  2. Semantic API has a limitation
            // Out of those, 2nd one is ignored assuming semantic API behaves correctly. 1st one is caused due to the
            // right end column being excluded when searching. To overcome that, here we search for the symbol at 
            // (col - 1). Ideally this shouldn't be an issue, because if we had cursor at the start col or middle, the
            // 1st search (above) would have found that.
            symbolAtCursor = semanticModel.get().symbol(srcFile.get(),
                    LinePosition.from(position.getLine(), position.getCharacter() - 1));
            if (symbolAtCursor.isEmpty()) {
                return Optional.empty();
            }
        }

        return symbolAtCursor;
    }
}
