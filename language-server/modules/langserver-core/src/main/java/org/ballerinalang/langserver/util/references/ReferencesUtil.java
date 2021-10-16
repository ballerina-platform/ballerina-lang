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
package org.ballerinalang.langserver.util.references;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.HashMap;
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
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        
        Map<Module, List<Location>> moduleLocationMap = new HashMap<>();

        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return moduleLocationMap;
        }

        Position position = context.getCursorPosition();
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return moduleLocationMap;
        }

        Optional<Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(),
                LinePosition.from(position.getLine(), position.getCharacter()));

        if (symbolAtCursor.isEmpty()) {
            if (position.getCharacter() == 0) {
                return moduleLocationMap;
            }
            
            // If we did not find the symbol, there are 2 possibilities.
            //  1. Cursor is at the end (RHS) of the symbol
            //  2. Semantic API has a limitation
            // Out of those, 2nd one is ignored assuming semantic API behaves correctly. 1st one is caused due to the
            // right end column being excluded when searching. To overcome that, here we search for the symbol at 
            // (col - 1). Ideally this shouldn't be an issue, because if we had cursor at the start col or middle, the
            // 1st search (above) would have found that.
            position = new Position(position.getLine(), position.getCharacter() - 1);
            symbolAtCursor = semanticModel.get().symbol(srcFile.get(),
                    LinePosition.from(position.getLine(), position.getCharacter()));
            if (symbolAtCursor.isEmpty()) {
                return moduleLocationMap;
            }
        }
        
        Symbol symbol = symbolAtCursor.get();
        project.get().currentPackage().moduleIds().forEach(moduleId -> {
            List<Location> references = project.get().currentPackage()
                    .getCompilation().getSemanticModel(moduleId).references(symbol);
            if (references.isEmpty()) {
                return;
            }
            Module module = project.get().currentPackage().module(moduleId);
            moduleLocationMap.put(module, references);
        });

        return moduleLocationMap;
    }
    
    public static String getUriFromLocation(Module module, Location location) {
        return getPathFromLocation(module, location).toUri().toString();
    }
    
    public static Path getPathFromLocation(Module module, Location location) {
        String filePath = location.lineRange().filePath();

        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return module.project().sourceRoot();
        }

        if (module.project().kind() == ProjectKind.BALA_PROJECT) {
            // TODO Check if bala projects can exist within nested modules dir
            return module.project().sourceRoot().resolve("modules")
                    .resolve(module.moduleName().toString())
                    .resolve(filePath);
        }
        
        if (module.isDefaultModule()) {
            return module.project().sourceRoot().resolve(filePath);
        } else {
            return module.project().sourceRoot()
                    .resolve("modules")
                    .resolve(module.moduleName().moduleNamePart())
                    .resolve(filePath);
        }
    }

    public static Range getRange(Location referencePos) {
        Position start = new Position(
                referencePos.lineRange().startLine().line(), referencePos.lineRange().startLine().offset());
        Position end = new Position(
                referencePos.lineRange().endLine().line(), referencePos.lineRange().endLine().offset());
        return new Range(start, end);
    }
}
