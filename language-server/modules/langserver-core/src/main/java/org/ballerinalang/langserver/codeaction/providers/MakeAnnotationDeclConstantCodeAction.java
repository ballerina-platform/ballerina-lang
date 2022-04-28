/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action to make annotation declaration constant.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class MakeAnnotationDeclConstantCodeAction extends AbstractCodeActionProvider {
    public static final String NAME = "Make Annotation Declaration Constant";
    public static final String DIAGNOSTIC_CODE = "BCE2638";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        Optional<Pair<Symbol, Path>> symbolAndPath = CodeActionUtil.getSymbolAndPath(diagnostic, DIAGNOSTIC_CODE,
                context);
        if (symbolAndPath.isEmpty()) {
           return Collections.emptyList();
        } 
        Symbol symbol = symbolAndPath.get().getLeft();
        Path filePath = symbolAndPath.get().getRight();
        
        URI uri = filePath.toUri();
        Optional<NonTerminalNode> node = CommonUtil.findNode(symbol,
                context.workspace().syntaxTree(filePath).get());
        if (node.isEmpty()) {
            return Collections.emptyList();
        }

        AnnotationDeclarationNode annotationDeclarationNode = (AnnotationDeclarationNode) node.get();
        Position position = CommonUtil.toPosition(annotationDeclarationNode.annotationKeyword().lineRange().startLine());

        Range range = new Range(position, position);
        String editText = SyntaxKind.CONST_KEYWORD.stringValue() + " ";
        TextEdit textEdit = new TextEdit(range, editText);
        List<TextEdit> editList = List.of(textEdit);
        String commandTitle = String.format(CommandConstants.MAKE_ANNOT_DECL_CONST, symbol.getName().orElse(""));
        return Collections.singletonList(createCodeAction(commandTitle, editList, uri.toString(),
                CodeActionKind.QuickFix));
    }

    @Override
    public int priority() {
        return super.priority();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
