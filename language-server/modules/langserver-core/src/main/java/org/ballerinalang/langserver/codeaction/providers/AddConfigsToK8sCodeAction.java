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

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.ValueNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Code Action for resource into Kubernetes.toml
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddConfigsToK8sCodeAction extends AbstractCodeActionProvider {

    public AddConfigsToK8sCodeAction() {
        super(Collections.singletonList(
                CodeActionNodeType.MODULE_VARIABLE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context) {
        NonTerminalNode matchedNode = context.positionDetails().matchedNode();
        if (!isConfigurableVariable(matchedNode)) {
            return Collections.emptyList();
        }

        Path k8sPath = context.workspace().projectRoot(context.filePath()).resolve("Kubernetes.toml");
        Optional<SyntaxTree> tomlSyntaxTreeOptional = TomlSyntaxTreeUtil.getTomlSyntaxTree(k8sPath);
        if (tomlSyntaxTreeOptional.isEmpty()) {
            return Collections.emptyList();
        }

        SyntaxTree tomlSyntaxTree = tomlSyntaxTreeOptional.get();
        DocumentNode documentNode = tomlSyntaxTree.rootNode();

        List<CodeAction> codeActionList = new ArrayList<>();
        ModuleVariableDeclarationNode variableNode = (ModuleVariableDeclarationNode) matchedNode;
        BindingPatternNode bindingPatternNode = variableNode.typedBindingPattern().bindingPattern();
        if (bindingPatternNode.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN) {
            CaptureBindingPatternNode captureBindingPatternNode = (CaptureBindingPatternNode) bindingPatternNode;
            String variableName = captureBindingPatternNode.variableName().text();
            if (isEnvExistInK8sToml(documentNode, variableName)) {
                return codeActionList;
            }

            String importText = generateEnvArrayTest(variableName);
            int endLine = documentNode.members().get(documentNode.members().size() - 1).lineRange().endLine().line();
            Position position = new Position(endLine + 1, 0);
            List<TextEdit> edits = Collections.singletonList(new TextEdit(new Range(position, position), importText));
            CodeAction action =
                    createQuickFixCodeAction("Add as env to Kubernetes.toml", edits, k8sPath.toUri().toString());
            codeActionList.add(action);
        }
        return codeActionList;
    }

    private boolean isConfigurableVariable(NonTerminalNode matchedNode) {
        if (!(matchedNode.kind() == SyntaxKind.MODULE_VAR_DECL)) {
            return false;
        }
        ModuleVariableDeclarationNode variableDeclarationNode = (ModuleVariableDeclarationNode) matchedNode;
        for (Token qualifier : variableDeclarationNode.qualifiers()) {
            if (qualifier.kind() == SyntaxKind.CONFIGURABLE_KEYWORD) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnvExistInK8sToml(DocumentNode documentNode, String variableName) {
        for (DocumentMemberDeclarationNode member : documentNode.members()) {
            if (member.kind() == io.ballerina.toml.syntax.tree.SyntaxKind.TABLE_ARRAY) {
                TableArrayNode tableNode = (TableArrayNode) member;
                String tableArrayName = TomlSyntaxTreeUtil.toDottedString(tableNode.identifier());
                if (tableArrayName.equals("cloud.config.envs")) {
                    if (isEnvExistInTableArray(variableName, tableNode)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isEnvExistInTableArray(String variableName, TableArrayNode tableNode) {
        for (KeyValueNode keyValueNode : tableNode.fields()) {
            String key = TomlSyntaxTreeUtil.toDottedString(keyValueNode.identifier());
            if (key.equals("key_ref")) {
                ValueNode value = keyValueNode.value();
                if (value.kind() == io.ballerina.toml.syntax.tree.SyntaxKind.STRING_LITERAL) {
                    StringLiteralNode stringNode = (StringLiteralNode) value;
                    Optional<io.ballerina.toml.syntax.tree.Token> content = stringNode.content();
                    if (content.isPresent()) {
                        if (content.get().text().equals(variableName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String toAbsoluteServicePath(NodeList<Node> servicePathNodes) {
        StringBuilder absoluteServicePath = new StringBuilder();
        for (Node serviceNode : servicePathNodes) {
            absoluteServicePath.append(serviceNode.toString());
        }
        return absoluteServicePath.toString().trim();
    }

    private String generateEnvArrayTest(String configVariable) {
        return CommonUtil.LINE_SEPARATOR + "[[cloud.config.envs]]" + CommonUtil.LINE_SEPARATOR +
                "key_ref=\"" + configVariable + "\"" + CommonUtil.LINE_SEPARATOR +
                "config_name=\"" + configVariable + "\"" + CommonUtil.LINE_SEPARATOR;
    }
}
