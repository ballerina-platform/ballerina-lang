/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.util.definition;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utilities for the definition operations.
 *
 * @since 1.2.0
 */
public class DefinitionUtil {

    /**
     * Get the definition.
     *
     * @param context  Definition context
     * @param position cursor position
     * @return {@link List} List of definition locations
     */
    public static List<Location> getDefinition(DocumentServiceContext context, Position position) {
        Optional<Document> srcFile = context.workspace().document(context.filePath());
        Optional<SemanticModel> semanticModel = context.workspace().semanticModel(context.filePath());

        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return Collections.emptyList();
        }

        LinePosition linePosition = LinePosition.from(position.getLine(), position.getCharacter());
        Optional<Symbol> symbol = semanticModel.get().symbol(srcFile.get(), linePosition);

        if (symbol.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Location> location = getLocation(symbol.get(), context);
        return location.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private static Optional<Location> getLocation(Symbol symbol, DocumentServiceContext context) {
        Path projectRoot = context.workspace().projectRoot(context.filePath());
        Optional<Project> project = context.workspace().project(context.filePath());

        if (project.isEmpty()) {
            return Optional.empty();
        }

        LinePosition startLine = symbol.location().lineRange().startLine();
        LinePosition endLine = symbol.location().lineRange().endLine();
        Position start = new Position(startLine.line(), startLine.offset());

        Position end = new Position(endLine.line(), endLine.offset());
        Range range = new Range(start, end);
        String uri;

        if (project.get().kind() == ProjectKind.SINGLE_FILE_PROJECT && symbol.moduleID().moduleName().equals(".")) {
            uri = projectRoot.toUri().toString();
        } else if (!project.get().currentPackage().packageOrg().value().equals(symbol.moduleID().orgName())) {
            return Optional.empty();
        } else if (project.get().currentPackage().packageName().value().equals(symbol.moduleID().moduleName())) {
            // Symbol is within the default module
            uri = projectRoot.resolve(symbol.location().lineRange().filePath()).toUri().toString();
        } else {
            String moduleName = symbol.moduleID().modulePrefix();
            String fileName = symbol.location().lineRange().filePath();
            uri = projectRoot.resolve("modules").resolve(moduleName).resolve(fileName).toUri().toString();
        }

        return Optional.of(new Location(uri, range));
    }
}
