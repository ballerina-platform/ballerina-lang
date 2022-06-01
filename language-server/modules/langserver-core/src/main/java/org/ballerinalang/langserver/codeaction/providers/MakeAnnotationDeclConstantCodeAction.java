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

import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

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
    public boolean validate(Diagnostic diagnostic,
                            DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODE.equals(diagnostic.diagnosticInfo().code()) &&
                context.currentSyntaxTree().isPresent() && context.currentSemanticModel().isPresent() &&
                CodeActionNodeValidator.validate(context.nodeAtCursor());
    }
    
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }

        Range diagnosticRange = CommonUtil.toRange(diagnostic.location().lineRange());
        NonTerminalNode node = CommonUtil.findNode(diagnosticRange, context.currentSyntaxTree().get());
        if (node.kind() != SyntaxKind.ANNOTATION_DECLARATION) {
            return Collections.emptyList();
        }

        AnnotationDeclarationNode annotationDeclarationNode = (AnnotationDeclarationNode) node;
        Position position = CommonUtil.toPosition(annotationDeclarationNode.annotationKeyword().lineRange()
                .startLine());
        
        TextEdit textEdit = new TextEdit(new Range(position, position), SyntaxKind.CONST_KEYWORD.stringValue() 
                + " ");
        String commandTitle = String.format(CommandConstants.MAKE_ANNOT_DECL_CONST, 
                annotationDeclarationNode.annotationTag().toString().strip());
        return Collections.singletonList(createCodeAction(commandTitle, List.of(textEdit), context.fileUri(),
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
