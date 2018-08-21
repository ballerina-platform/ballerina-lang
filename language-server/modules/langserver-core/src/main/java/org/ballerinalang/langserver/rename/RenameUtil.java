/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.rename;

import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.util.Lists;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Utility methods for rename operation.
 */
public class RenameUtil {

    private static final Logger logger = LoggerFactory.getLogger(RenameUtil.class);

    /**
     * Get the list of rename related TextEdits.
     *
     * @param locationList      List of locations of occurrences
     * @param documentManager   {@link WorkspaceDocumentManager} instance
     * @param newName           New name to be replaced with
     * @param replaceSymbolName Symbol name being replaced
     * @return {@link List}         List of TextEdits
     */
    public static List<TextDocumentEdit> getRenameTextEdits(List<Location> locationList,
                                                            WorkspaceDocumentManager documentManager, String newName,
                                                            String replaceSymbolName) {
        Map<String, ArrayList<Location>> documentLocationMap = new HashMap<>();
        List<TextDocumentEdit> documentEdits = new ArrayList<>();
        Comparator<Location> locationComparator = Comparator.comparingInt(
                location -> location.getRange().getStart().getCharacter());

        locationList.forEach(location -> {
            if (documentLocationMap.containsKey(location.getUri())) {
                documentLocationMap.get(location.getUri()).add(location);
            } else {
                documentLocationMap.put(location.getUri(), (ArrayList<Location>) Lists.of(location));
            }
        });

        documentLocationMap.forEach((uri, locations) -> {
            Collections.sort(locations, locationComparator);
            try {
                Path filePath = new LSDocument(uri).getPath();
                Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
                String fileContent = documentManager.getFileContent(compilationPath);
                String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
                int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf("\n"), fileContent.lastIndexOf("\r"));
                int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();

                for (Location location : locations) {
                    Position start = location.getRange().getStart();
                    int line = start.getLine();
                    StringBuilder lineComponent = new StringBuilder(contentComponents[line]);

                    int index = lineComponent.indexOf(replaceSymbolName, start.getCharacter());
                    while (index >= 0) {
                        char previousChar = lineComponent.charAt(index - 1);
                        if (Character.isLetterOrDigit(previousChar) || String.valueOf(previousChar).equals("_")) {
                            index = lineComponent.indexOf(replaceSymbolName, index + replaceSymbolName.length());
                        } else {
                            lineComponent.replace(index, index + replaceSymbolName.length(), newName);
                            index = lineComponent.indexOf(replaceSymbolName, index + newName.length());
                        }
                    }
                    contentComponents[line] = lineComponent.toString();
                }

                Range range = new Range(new Position(0, 0), new Position(contentComponents.length, lastCharCol));
                TextEdit textEdit = new TextEdit(range, String.join("\r\n", Arrays.asList(contentComponents)));
                VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
                textDocumentIdentifier.setUri(uri);
                TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier,
                                                                         Collections.singletonList(textEdit));
                documentEdits.add(textDocumentEdit);
            } catch (WorkspaceDocumentException e) {
                logger.error("Error occurred while retrieving text edits", e);
            }
        });

        return documentEdits;
    }
}
