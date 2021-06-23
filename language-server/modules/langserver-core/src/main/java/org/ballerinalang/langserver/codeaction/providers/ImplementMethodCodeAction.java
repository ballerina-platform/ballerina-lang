/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.model.Name;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * Code Action for implementing functions of an object.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImplementMethodCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Implement Method";
    private static final int DIAG_PROPERTY_NAME_INDEX = 0;
    private static final int DIAG_PROPERTY_SYMBOL_INDEX = 1;
    
    private static final Set<String> diagnosticCodes = Set.of(
            DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_CLASS.diagnosticId(),
            DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_SERVICE_DECL.diagnosticId(),
            DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_OBJECT_CTOR.diagnosticId()
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!diagnosticCodes.contains(diagnostic.diagnosticInfo().code())) {
            return Collections.emptyList();
        }

        Optional<Name> methodName = positionDetails.diagnosticProperty(DIAG_PROPERTY_NAME_INDEX);
        Optional<TypeSymbol> optionalSymbol = positionDetails.diagnosticProperty(DIAG_PROPERTY_SYMBOL_INDEX);
        if (context.currentSyntaxTree().isEmpty() || methodName.isEmpty() || optionalSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        TypeSymbol rawType = CommonUtil.getRawType(optionalSymbol.get());
        if (rawType.typeKind() != TypeDescKind.OBJECT) {
            return Collections.emptyList();
        }

        ObjectTypeSymbol classSymbol = (ObjectTypeSymbol) rawType;
        if (!classSymbol.methods().containsKey(methodName.get().getValue())) {
            return Collections.emptyList();
        }

        Node matchedNode = CommonUtil.findNode(classSymbol, context.currentSyntaxTree().get());
        if (matchedNode == null) {
            return Collections.emptyList();
        }

        LinePosition editPosition;
        NodeList<Node> members;
        if (matchedNode.kind() == SyntaxKind.CLASS_DEFINITION) {
            ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) matchedNode;
            members = classDefinitionNode.members();
            editPosition = classDefinitionNode.closeBrace().lineRange().startLine();
        } else if (matchedNode.kind() == SyntaxKind.SERVICE_DECLARATION) {
            ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) matchedNode;
            members = serviceDeclarationNode.members();
            editPosition = serviceDeclarationNode.closeBraceToken().lineRange().startLine();
        } else if (matchedNode.kind() == SyntaxKind.OBJECT_CONSTRUCTOR) {
            ObjectConstructorExpressionNode objConstructor = (ObjectConstructorExpressionNode) matchedNode;
            members = objConstructor.members();
            editPosition = objConstructor.closeBraceToken().lineRange().startLine();
        } else {
            return Collections.emptyList();
        }

        MethodSymbol unimplMethod = classSymbol.methods().get(methodName.get().getValue());
        List<FunctionDefinitionNode> concreteMethods = members.stream()
                .filter(member -> member.kind() == SyntaxKind.FUNCTION_DEFINITION)
                .map(member -> (FunctionDefinitionNode) member)
                .collect(Collectors.toList());

        String offsetStr;
        if (!concreteMethods.isEmpty()) {
            // If other methods exists, inherit offset
            FunctionDefinitionNode funcDefNode = concreteMethods.get(0);
            offsetStr = StringUtils.repeat(' ', funcDefNode.location().lineRange().endLine().offset());
        } else {
            // Or else, adjust offset according to the parent class
            offsetStr = StringUtils.repeat(' ', matchedNode.location().lineRange().startLine().offset() + 4);
        }

        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        String typeName = FunctionGenerator.processModuleIDsInText(importsAcceptor, unimplMethod.signature(), context);
        List<TextEdit> edits = new ArrayList<>(importsAcceptor.getNewImportTextEdits());
        String editText = offsetStr + typeName + " {" + LINE_SEPARATOR + offsetStr + "}" + LINE_SEPARATOR;
        Position editPos = CommonUtil.toPosition(editPosition);
        edits.add(new TextEdit(new Range(editPos, editPos), editText));
        String commandTitle = String.format(CommandConstants.IMPLEMENT_FUNCS_TITLE, unimplMethod.getName().get());
        CodeAction quickFixCodeAction = createQuickFixCodeAction(commandTitle, edits, context.fileUri());
        quickFixCodeAction.setDiagnostics(CodeActionUtil.toDiagnostics(Collections.singletonList((diagnostic))));
        return Collections.singletonList(quickFixCodeAction);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
