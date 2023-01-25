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
package org.ballerinalang.langserver.definition;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaDefinitionContext;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.net.URISyntaxException;
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
    public static List<Location> getDefinition(BallerinaDefinitionContext context, Position position) {
        fillTokenInfoAtCursor(context);
        context.checkCancelled();
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();

        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return Collections.emptyList();
        }

        LinePosition linePosition = LinePosition.from(position.getLine(), position.getCharacter());
        Optional<Symbol> symbol = semanticModel.get().symbol(srcFile.get(), linePosition);
        context.checkCancelled();
        if (symbol.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Location> location;
        if (context.enclosedModuleMember().isPresent() && SymbolUtil.isSelfClassSymbol(symbol.get(), context,
                context.enclosedModuleMember().get())) {
            // Within the #isSelfClassSymbol we do the instance check against the symbol. Hence casting is safe
            // If the self variable is referring to the class instance, navigate to class definition
            TypeSymbol rawType = CommonUtil.getRawType(((VariableSymbol) symbol.get()).typeDescriptor());
            location = getLocation(rawType, context);
        } else {
            location = getLocation(symbol.get(), context);
        }
        return location.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private static Optional<Location> getLocation(Symbol symbol, BallerinaDefinitionContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty() || symbol.getModule().isEmpty() || symbol.getLocation().isEmpty()) {
            return Optional.empty();
        }

        Optional<Path> filepath = PathUtil.getFilePathForSymbol(symbol, project.get(), context);
        if (filepath.isEmpty() || symbol.getLocation().isEmpty()) {
            return Optional.empty();
        }

        String fileUri;
        // Check if file resides in a protected dir
        if (PathUtil.isWriteProtectedPath(filepath.get())) {
            try {
                fileUri = PathUtil.getBalaUriForPath(context.languageServercontext(), filepath.get());
            } catch (URISyntaxException e) {
                throw new UserErrorException("Unable create definition file URI");
            }
        } else {
            fileUri = filepath.get().toUri().toString();
        }

        io.ballerina.tools.diagnostics.Location symbolLocation = symbol.getLocation().get();
        LinePosition startLine = symbolLocation.lineRange().startLine();
        LinePosition endLine = symbolLocation.lineRange().endLine();
        Position start = new Position(startLine.line(), startLine.offset());
        Position end = new Position(endLine.line(), endLine.offset());
        Range range = new Range(start, end);

        return Optional.of(new Location(fileUri, range));
    }

    private static void fillTokenInfoAtCursor(BallerinaDefinitionContext context) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document");
        }
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
    }
}
