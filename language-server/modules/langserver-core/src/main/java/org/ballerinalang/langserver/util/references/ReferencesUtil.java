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
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for go to definition functionality of language server.
 */
public class ReferencesUtil {
    private ReferencesUtil() {
    }

    public static List<Location> getReferences(PositionedOperationContext context) {
        Optional<SemanticModel> semanticModel = context.workspace().semanticModel(context.filePath());
        List<Location> locations = new ArrayList<>();

        if (semanticModel.isEmpty()) {
            return locations;
        }

        String relPath = context.workspace().relativePath(context.filePath()).orElseThrow();
        Position position = context.getCursorPosition();
        Optional<Project> project = context.workspace().project(context.filePath());
        Optional<Symbol> symbolAtCursor = semanticModel.get().symbol(relPath, LinePosition.from(position.getLine(),
                position.getCharacter()));

        if (project.isEmpty() || symbolAtCursor.isEmpty()) {
            return locations;
        }

        Path projectRoot = context.workspace().projectRoot(context.filePath());

        project.get().currentPackage().modules().forEach(module -> {
            List<io.ballerina.tools.diagnostics.Location> references
                    = module.getCompilation().getSemanticModel().references(symbolAtCursor.get());
            references.forEach(location -> locations.add(getLocation(module, location, projectRoot)));
        });

        return locations;
    }

    private static Location getLocation(Module module, io.ballerina.tools.diagnostics.Location location, Path prjRoot) {
        LineRange lineRange = location.lineRange();
        String filePath = lineRange.filePath();
        String uri;
        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            uri = prjRoot.toUri().toString();
        } else if (module.isDefaultModule()) {
            module.project();
            uri = prjRoot.resolve(lineRange.filePath()).toUri().toString();
        } else {
            uri = prjRoot.resolve("modules")
                    .resolve(module.moduleName().moduleNamePart())
                    .resolve(filePath).toUri().toString();
        }

        return new Location(uri, getRange(location));
    }

    private static Range getRange(io.ballerina.tools.diagnostics.Location referencePos) {
        Position start = new Position(
                referencePos.lineRange().startLine().line(), referencePos.lineRange().startLine().offset());
        Position end = new Position(
                referencePos.lineRange().endLine().line(), referencePos.lineRange().endLine().offset());
        return new Range(start, end);
    }
}
