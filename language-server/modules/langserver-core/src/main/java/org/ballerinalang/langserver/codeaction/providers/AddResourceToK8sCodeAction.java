/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.CloudToml;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for adding a resource into Cloud.toml.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddResourceToK8sCodeAction extends AbstractCodeActionProvider {

    public AddResourceToK8sCodeAction() {
        super(Collections.singletonList(CodeActionNodeType.RESOURCE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails positionDetails) {
        NonTerminalNode matchedNode = positionDetails.matchedTopLevelNode();
        if (matchedNode.kind() != SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
            return Collections.emptyList();
        }

        Path k8sPath = context.workspace().projectRoot(context.filePath()).resolve(ProjectConstants.CLOUD_TOML);
        Optional<CloudToml> cloudToml =
                context.workspace().project(context.filePath()).orElseThrow().currentPackage().cloudToml();

        if (cloudToml.isEmpty()) {
            return Collections.emptyList();
        }

        SyntaxTree tomlSyntaxTree = cloudToml.get().tomlDocument().syntaxTree();
        DocumentNode documentNode = tomlSyntaxTree.rootNode();

        List<ProbeType> probs = getAvailableProbes(documentNode);
        List<CodeAction> codeActionList = new ArrayList<>();
        for (ProbeType probe : probs) {
            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) matchedNode;
            String resourcePath = toAbsoluteServicePath(functionDefinitionNode.relativeResourcePath());

            ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) functionDefinitionNode.parent();
            String servicePath = toAbsoluteServicePath(serviceDeclarationNode.absoluteResourcePath());
            int port = getPortOfService(serviceDeclarationNode);

            String importText = generateProbeText(probe, port, servicePath, resourcePath);
            int endLine = documentNode.members().get(documentNode.members().size() - 1).lineRange().endLine().line();
            Position position = new Position(endLine + 1, 0);
            List<TextEdit> edits = Collections.singletonList(
                    new TextEdit(new Range(position, position), importText));
            CodeAction action = createQuickFixCodeAction("Add as " + probe.tableName + " probe", edits,
                                                         k8sPath.toUri().toString());
            codeActionList.add(action);
        }
        return codeActionList;
    }

    private List<ProbeType> getAvailableProbes(DocumentNode documentNode) {
        List<ProbeType> probs = new ArrayList<>();
        probs.add(ProbeType.LIVENESS);
        probs.add(ProbeType.READINESS);
        for (DocumentMemberDeclarationNode member : documentNode.members()) {
            if (member.kind() == io.ballerina.toml.syntax.tree.SyntaxKind.TABLE) {
                TableNode tableNode = (TableNode) member;
                String tableName = TomlSyntaxTreeUtil.toDottedString(tableNode.identifier());
                if (tableName.equals("cloud.deployment.probes.liveness")) {
                    probs.remove(ProbeType.LIVENESS);
                }
                if (tableName.equals("cloud.deployment.probes.readiness")) {
                    probs.remove(ProbeType.READINESS);
                }
            }
        }
        return probs;
    }

    private int getPortOfService(ServiceDeclarationNode serviceDeclarationNode) {
        int port;
        ExpressionNode expressionNode = serviceDeclarationNode.expressions().get(0);
        if (expressionNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode referenceNode = (SimpleNameReferenceNode) expressionNode;
            String listenerName = referenceNode.name().text();
            ModulePartNode modulePartNode = (ModulePartNode) serviceDeclarationNode.parent();
            ListenerVisitor listenerVisitor = new ListenerVisitor(listenerName);
            modulePartNode.accept(listenerVisitor);
            port = listenerVisitor.getPort();
        } else {
            ExplicitNewExpressionNode refNode = (ExplicitNewExpressionNode) expressionNode;
            port = Integer.parseInt(refNode.parenthesizedArgList().arguments().get(0).toString());
        }
        return port;
    }

    private String toAbsoluteServicePath(NodeList<Node> servicePathNodes) {
        StringBuilder absoluteServicePath = new StringBuilder();
        for (Node serviceNode : servicePathNodes) {
            absoluteServicePath.append(serviceNode.toString());
        }
        return absoluteServicePath.toString().trim();
    }

    private String generateProbeText(ProbeType probeType, int port, String servicePath, String resourcePath) {
        return CommonUtil.LINE_SEPARATOR + "[cloud.deployment.probes." + probeType.tableName + "]" +
                CommonUtil.LINE_SEPARATOR + "port = " + port + CommonUtil.LINE_SEPARATOR +
                "path = \"" + servicePath + "/" + resourcePath + "\"" + CommonUtil.LINE_SEPARATOR;
    }

    private static class ListenerVisitor extends NodeVisitor {

        private String targetListenerName;
        private int port;

        public ListenerVisitor(String targetListenerName) {
            this.targetListenerName = targetListenerName;
        }

        @Override
        public void visit(ListenerDeclarationNode listenerDeclarationNode) {
            String listenerName = listenerDeclarationNode.variableName().text();
            Node initializer = listenerDeclarationNode.initializer();
            if (initializer.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION && listenerName.equals(targetListenerName)) {
                ImplicitNewExpressionNode initializerNode = (ImplicitNewExpressionNode) initializer;
                ParenthesizedArgList parenthesizedArgList = initializerNode.parenthesizedArgList().get();
                FunctionArgumentNode functionArgumentNode = parenthesizedArgList.arguments().get(0);
                ExpressionNode expression = ((PositionalArgumentNode) functionArgumentNode).expression();
                this.port = Integer.parseInt(((BasicLiteralNode) expression).literalToken().text());
            }
        }

        public int getPort() {
            return port;
        }
    }

    enum ProbeType {
        LIVENESS("liveness"),
        READINESS("readiness");

        final String tableName;

        ProbeType(String tableName) {
            this.tableName = tableName;
        }
    }
}
