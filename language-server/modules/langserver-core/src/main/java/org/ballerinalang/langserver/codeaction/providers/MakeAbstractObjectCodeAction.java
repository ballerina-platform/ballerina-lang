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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.tree.TypeDefinition;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Code Action for making object abstract type.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class MakeAbstractObjectCodeAction extends AbstractCodeActionProvider {
    private static final String ABSTRACT_OBJECT = "in abstract object";

    @Override
    public boolean isEnabled() {
        // TODO: Convert this code into new object semantics
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails,
                                                    List<Diagnostic> allDiagnostics, SyntaxTree syntaxTree,
                                                    LSContext context) {
        if (!(diagnostic.getMessage().contains(ABSTRACT_OBJECT))) {
            return Collections.emptyList();
        }

        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);

        TypeSymbol objType = positionDetails.matchedSymbolTypeDesc();
        Symbol symbol = positionDetails.matchedSymbol();
        NonTerminalNode node = positionDetails.matchedNode();
        if (symbol.kind() != SymbolKind.CLASS &&
                node.kind() == SyntaxKind.CLASS_DEFINITION) {
            return Collections.emptyList();
        }

        TypeDefinition typeSymbol = (TypeDefinition) symbol;
//        List<Qualifier> qualifiers = new ArrayList<>(typeSymbol.());

        String simpleObjName = extractObjectName(diagnostic.getMessage());
        if (simpleObjName.isEmpty()) {
            return Collections.emptyList();
        }

//        Set<Whitespace> whitespaces = objType.get().getWS();
//        whitespaces.addAll(objType.get().typeNode.getWS());
//        Iterator<Whitespace> iterator = whitespaces.iterator();
//
        String commandTitle = String.format(CommandConstants.MAKE_OBJ_ABSTRACT_TITLE, simpleObjName);
//        int colBeforeObjKeyword = objType.get().pos.sCol;
//        boolean isFirst = true;
//        StringBuilder str = new StringBuilder();
//        while (iterator.hasNext()) {
//            Whitespace next = iterator.next();
//            if ("object".equals(next.getPrevious())) {
//                break;
//            }
//            String ws = next.getWs();
//            if (!isFirst && !";".equals(ws)) {
//                str.append(ws);
//            }
//            str.append(next.getPrevious());
//            isFirst = false;
//        }
//        colBeforeObjKeyword += str.toString().length();
//
        String editText = " abstract";
//        Position pos = new Position(objType.get().pos.sLine - 1, colBeforeObjKeyword - 1);

//        List<TextEdit> edits = Collections.singletonList(new TextEdit(new Range(pos, pos), editText));
//        return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
        return null;
    }

    private static String extractObjectName(String diagnosticMessage) {
        Matcher matcher = CommandConstants.FUNC_IN_ABSTRACT_OBJ_PATTERN.matcher(diagnosticMessage);
        Matcher matcher2 = CommandConstants.NO_IMPL_FOUND_FOR_FUNCTION_PATTERN.matcher(diagnosticMessage);
        String simpleObjName = "";
        if (matcher.find() && matcher.groupCount() > 1) {
            String objectName = matcher.group(2);
            int colonIndex = objectName.lastIndexOf(":");
            simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
        } else if (matcher2.find() && matcher2.groupCount() > 1) {
            String objectName = matcher2.group(2);
            int colonIndex = objectName.lastIndexOf(":");
            simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
        }
        return simpleObjName;
    }
}
