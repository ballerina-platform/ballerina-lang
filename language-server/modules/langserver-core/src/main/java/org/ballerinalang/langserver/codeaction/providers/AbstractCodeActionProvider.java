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
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.ballerinalang.model.Name;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * Represents the common class for the default Ballerina Code Action Providers.
 *
 * @since 1.1.1
 */
public abstract class AbstractCodeActionProvider implements LSCodeActionProvider {
    protected List<CodeActionNodeType> codeActionNodeTypes;
    protected boolean isNodeTypeBased;
    protected static final int DIAG_PROPERTY_NAME_INDEX = 0;
    protected static final int DIAG_PROPERTY_SYMBOL_INDEX = 1;

    protected static final Set<String> DIAGNOSTICCODES = Set.of(
            DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_CLASS.diagnosticId(),
            DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_SERVICE_DECL.diagnosticId(),
            DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_OBJECT_CTOR.diagnosticId()
    );

    @Override
    public boolean isEnabled(LanguageServerContext serverContext) {
        return true;
    }

    /**
     * Create a diagnostic based code action provider.
     */
    public AbstractCodeActionProvider() {
        this.isNodeTypeBased = false;
    }

    /**
     * Create a node type based code action provider.
     *
     * @param nodeTypes code action node types list
     */
    public AbstractCodeActionProvider(List<CodeActionNodeType> nodeTypes) {
        this.codeActionNodeTypes = nodeTypes;
        this.isNodeTypeBased = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isNodeBasedSupported() {
        return this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDiagBasedSupported() {
        return !this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<CodeActionNodeType> getCodeActionNodeTypes() {
        return this.codeActionNodeTypes;
    }

    /**
     * Returns a QuickFix Code action.
     *
     * @param commandTitle title of the code action
     * @param uri          uri
     * @return {@link CodeAction}
     */
    public static CodeAction createQuickFixCodeAction(String commandTitle, List<TextEdit> edits, String uri) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        CodeAction action = new CodeAction(commandTitle);
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
        action.setDiagnostics(CodeActionUtil.toDiagnostics(diagnostics));
        return action;
    }


    /**
     * Returns a list of text edits based on diagnostics.
     *      *
     *      * @param diagnostic diagnostic to evaluate
     *      * @param positionDetails   {@link DiagBasedPositionDetails}
     *      * @param context    language server context
     *      * @return list of Text Edits
     */
    public static List<TextEdit> getDiagBasedTextEdits(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                                       CodeActionContext context) {
        if (!DIAGNOSTICCODES.contains(diagnostic.diagnosticInfo().code())) {
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
        return edits;
    }
}
