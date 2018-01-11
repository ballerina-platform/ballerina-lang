/**
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

package org.ballerinalang.langserver.definition.util;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.definition.constants.DefinitionConstants;
import org.ballerinalang.langserver.hover.HoverKeys;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
/**
 * Utility class for go to definition functionality of language server.
 */
public class DefinitionUtil {
    /**
     * Get definition position for the given hover context.
     *
     * @param currentBLangPackage resolved bLangPackage for the hover context.
     * @param hoverContext context of the hover.
     * @return position
     */
    public static List<Location> getDefinitionPosition(TextDocumentServiceContext hoverContext,
                                                       BLangPackage currentBLangPackage) {
        List<Location> contents = new ArrayList<>();
        if (hoverContext.get(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY) == null) {
            return contents;
        }
        String nodeKind = hoverContext.get(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY).name();
        BLangNode bLangNode = null;
        switch (nodeKind) {
            case DefinitionConstants.FUNCTION:
                bLangNode = currentBLangPackage.functions.stream()
                        .filter(function -> function.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                break;
            case DefinitionConstants.STRUCT:
                bLangNode = currentBLangPackage.structs.stream()
                        .filter(struct -> struct.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                break;
            case DefinitionConstants.ENUM:
                bLangNode = currentBLangPackage.enums.stream()
                        .filter(struct -> struct.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                break;
            default:

                break;
        }
        if (bLangNode == null) {
            return contents;
        }

        Location l = new Location();
        TextDocumentPositionParams position = hoverContext.get(DocumentServiceKeys.POSITION_KEY);
        Path parentPath = TextDocumentServiceUtil.getPath(position.getTextDocument().getUri()).getParent();
        if (parentPath != null) {
            String fileName = bLangNode.getPosition().getSource().getCompilationUnitName();

            Path filePath = Paths.get(parentPath.toString(), fileName);
            l.setUri(filePath.toUri().toString());
            Range r = new Range();
            // Subtract 1 to convert the token lines and char positions to zero based indexing
            r.setStart(new Position(bLangNode.getPosition().getStartLine() - 1,
                    bLangNode.getPosition().getStartColumn() - 1));
            r.setEnd(new Position(bLangNode.getPosition().getEndLine() - 1,
                    bLangNode.getPosition().getEndColumn() - 1));
            l.setRange(r);
            contents.add(l);
        }

        return contents;

    }
}
