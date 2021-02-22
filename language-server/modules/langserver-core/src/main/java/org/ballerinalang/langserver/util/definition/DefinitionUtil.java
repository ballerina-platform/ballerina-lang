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
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

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
        if (symbol.getLocation().isEmpty()) {
            return Optional.empty();
        }

        io.ballerina.tools.diagnostics.Location symbolLocation = symbol.getLocation().get();
        LinePosition startLine = symbolLocation.lineRange().startLine();
        LinePosition endLine = symbolLocation.lineRange().endLine();
        Position start = new Position(startLine.line(), startLine.offset());
        Position end = new Position(endLine.line(), endLine.offset());
        Range range = new Range(start, end);

        if (symbol.getModule().isEmpty()) {
            return Optional.empty();
        }
        Optional<String> uri = CommonUtil.getSymbolUriInProject(context, symbol);

        return Optional.of(new Location(uri.orElseThrow(), range));
    }
}
