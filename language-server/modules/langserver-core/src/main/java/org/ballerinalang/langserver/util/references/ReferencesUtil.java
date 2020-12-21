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

import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for go to definition functionality of language server.
 */
public class ReferencesUtil {
    private ReferencesUtil() {
    }

    public static List<Location> getLocations(List<Reference> references, String sourceRoot) {
        return references.stream()
                .map(reference -> {
                    io.ballerina.tools.diagnostics.Location position = reference.getPosition();
                    Path baseRoot = reference.getSourcePkgName().equals(".")
                            ? Paths.get(sourceRoot)
                            : Paths.get(sourceRoot).resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                            .resolve(reference.getSourcePkgName());
                    String fileURI = baseRoot.resolve(reference.getCompilationUnit()).toUri().toString();
                    return new Location(fileURI, getRange(position));
                })
                .collect(Collectors.toList());
    }

    private static Range getRange(io.ballerina.tools.diagnostics.Location referencePos) {
        Position start = new Position(
                referencePos.lineRange().startLine().line(), referencePos.lineRange().startLine().offset());
        Position end = new Position(
                referencePos.lineRange().endLine().line(), referencePos.lineRange().endLine().offset());
        return new Range(start, end);
    }
}
