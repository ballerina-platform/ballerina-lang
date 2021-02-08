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
import io.ballerina.tools.text.LineRange;
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
        Optional<Document> srcFile = context.workspace().document(context.filePath());
        Optional<SemanticModel> semanticModel = context.workspace().semanticModel(context.filePath());
        
        Map<Module, List<Location>> moduleLocationMap = new HashMap<>();

        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return moduleLocationMap;
        }

        Position position = context.getCursorPosition();
        Optional<Project> project = context.workspace().project(context.filePath());
        Optional<Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(),
                LinePosition.from(position.getLine(),
                        position.getCharacter()));

        if (project.isEmpty() || symbolAtCursor.isEmpty()) {
            return moduleLocationMap;
        }
        
        project.get().currentPackage().modules().forEach(module -> {
            List<Location> references = module.getCompilation().getSemanticModel().references(symbolAtCursor.get());
            moduleLocationMap.put(module, references);
        });

        return moduleLocationMap;
    }
    
    public static String getUriFromLocation(Module module, Location location, Path prjRoot) {
        LineRange lineRange = location.lineRange();
        String filePath = lineRange.filePath();

        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return prjRoot.toUri().toString();
        } else if (module.isDefaultModule()) {
            module.project();
            return prjRoot.resolve(lineRange.filePath()).toUri().toString();
        } else {
            return prjRoot.resolve("modules")
                    .resolve(module.moduleName().moduleNamePart())
                    .resolve(filePath).toUri().toString();
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
