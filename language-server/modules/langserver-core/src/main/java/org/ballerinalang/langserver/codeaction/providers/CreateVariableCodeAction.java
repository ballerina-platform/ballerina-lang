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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Code Action for variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeAction extends AbstractCodeActionProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 999;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails,
                                                    List<Diagnostic> allDiagnostics, SyntaxTree syntaxTree,
                                                    LSContext context) {
        List<CodeAction> actions = new ArrayList<>();
        String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
        if (!(diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return actions;
        }

        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(diagnostic, positionDetails, context);
        List<String> types = createVarTextEdits.types;
        for (int i = 0; i < types.size(); i++) {
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            List<TextEdit> edits = new ArrayList<>();
            edits.add(createVarTextEdits.edits.get(i));
            edits.addAll(createVarTextEdits.imports);
            String type = types.get(i);
            if (createVarTextEdits.types.size() > 1) {
                // When there's multiple types; suffix code actions with `with <type>`
                boolean isTuple = type.startsWith("[") && type.endsWith("]") && !type.endsWith("[]");
                String typeLabel = isTuple && type.length() > 10 ? "Tuple" : type;
                commandTitle = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", typeLabel);
            }
            actions.add(createQuickFixCodeAction(commandTitle, edits, uri));
        }
        return actions;
    }

    CreateVariableOut getCreateVariableTextEdits(Diagnostic diagnostic,
                                                 PositionDetails positionDetails,
                                                 LSContext context) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        Symbol matchedSymbol = positionDetails.matchedSymbol();
        TypeSymbol typeDescriptor = positionDetails.matchedSymbolTypeDesc();

        Set<String> allNameEntries = CommonUtil.getAllNameEntries(compilerContext);
        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        String filePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        SemanticModel semanticModel = new BallerinaSemanticModel(bLangPackage, compilerContext);
        semanticModel.visibleSymbols(filePath, positionDetails.matchedNode().lineRange().startLine()).stream()
                .map(Symbol::name)
                .forEach(allNameEntries::add);

        String name = CommonUtil.generateVariableName(matchedSymbol, typeDescriptor, allNameEntries);

        List<TextEdit> importEdits = new ArrayList<>();
        List<TextEdit> edits = new ArrayList<>();
        List<String> types = CodeActionUtil.getPossibleTypes(typeDescriptor, importEdits, context, compilerContext);
        Position pos = diagnostic.getRange().getStart();
        for (String type : types) {
            Position insertPos = new Position(pos.getLine(), pos.getCharacter());
            String edit = type + " " + name + " = ";
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
        }
        return new CreateVariableOut(name, types, edits, importEdits);
    }

    static class CreateVariableOut {
        String name;
        List<String> types;
        List<TextEdit> edits;
        List<TextEdit> imports;

        public CreateVariableOut(String name, List<String> types, List<TextEdit> edits, List<TextEdit> imports) {
            this.name = name;
            this.types = types;
            this.edits = edits;
            this.imports = imports;
        }
    }
}
