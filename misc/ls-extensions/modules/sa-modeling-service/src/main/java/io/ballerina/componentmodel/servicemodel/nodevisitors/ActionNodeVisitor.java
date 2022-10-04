/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.componentmodel.servicemodel.components.Interaction;
import io.ballerina.componentmodel.servicemodel.components.ResourceId;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.componentmodel.ComponentModelingConstants.TYPE_MAP;

/**
 * Visitor class for RemoteMethodCallAction nodes.
 */
public class ActionNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;
    private final List<Interaction> interactionList = new ArrayList<>();

    public ActionNodeVisitor(SemanticModel semanticModel) {

        this.semanticModel = semanticModel;
    }

    public List<Interaction> getInteractionList() {

        return interactionList;
    }

    @Override
    public void visit(ClientResourceAccessActionNode clientResourceAccessActionNode) {

        String clientName = String.valueOf(clientResourceAccessActionNode.expression()).trim();
        String resourceMethod = String.valueOf(clientResourceAccessActionNode.methodName().get()).trim();
        String resourcePath = getResourcePath(clientResourceAccessActionNode.resourceAccessPath());

        StatementNodeVisitor statementVisitor = new StatementNodeVisitor(clientName, semanticModel);
        NonTerminalNode parent = clientResourceAccessActionNode.parent().parent();

        //todo : implement using semantic model. Need to wait till bug fix
        while (statementVisitor.getServiceId() == null || statementVisitor.getServiceId().isEmpty()) { //isEmpty
            parent = parent.parent();
            if (parent != null) {
                parent.accept(statementVisitor);
            } else {
                break;
            }
        }
        Interaction resourceId = new Interaction(
                new ResourceId(statementVisitor.getServiceId(), resourceMethod, resourcePath),
                statementVisitor.getConnectorType());
        interactionList.add(resourceId);

    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {

        String clientName = String.valueOf(remoteMethodCallActionNode.expression()).trim();
        String resourceMethod = String.valueOf(remoteMethodCallActionNode.methodName()).trim();
        NonTerminalNode parent = remoteMethodCallActionNode.parent().parent();
        StatementNodeVisitor statementVisitor = new StatementNodeVisitor(clientName, semanticModel);

        //todo : implement using semantic model. Need to wait till bug fix
        // semanticModel.symbol(remoteMethodCallActionNode.expression()); -> returns null

        while (statementVisitor.getServiceId() == null || statementVisitor.getServiceId().isEmpty()) {
            parent = parent.parent();
            if (parent != null) {
                parent.accept(statementVisitor);
            } else {
                break;
            }
        }
        Interaction interaction = new Interaction(new ResourceId(statementVisitor.getServiceId(),
                resourceMethod, null), statementVisitor.getConnectorType());
        interactionList.add(interaction);

    }

    private String getResourcePath(SeparatedNodeList<Node> accessPathNodes) {

        StringBuilder resourcePathBuilder = new StringBuilder();
        for (Node accessPathNode : accessPathNodes) {
            if (accessPathNode.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                resourcePathBuilder.append(((IdentifierToken) accessPathNode).text());
            } else if (accessPathNode.kind() == SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT) {
                ComputedResourceAccessSegmentNode accessSegmentNode =
                        (ComputedResourceAccessSegmentNode) accessPathNode;
                ExpressionNode expressionNode = accessSegmentNode.expression();
                if (expressionNode.kind() == SyntaxKind.STRING_LITERAL) {
                    resourcePathBuilder.append(String.format("/[%s]", TYPE_MAP.get(SyntaxKind.STRING_LITERAL)));
                } else if (expressionNode.kind().equals(SyntaxKind.NUMERIC_LITERAL)) {
                    SyntaxKind numericKind = ((BasicLiteralNode) expressionNode).literalToken().kind();
                    if (numericKind.equals(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN)) {
                        resourcePathBuilder.append(String.format("/[%s]", TYPE_MAP.get(
                                SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN)));
                    } else if (numericKind.equals(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN)) {
                        resourcePathBuilder.append(String.format("/[%s]", SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN));
                    } else {
                        resourcePathBuilder.append(String.format("/[%s]", SyntaxKind.NUMERIC_LITERAL));
                    }
                } else if (expressionNode.kind().equals(SyntaxKind.BOOLEAN_LITERAL)) {
                    resourcePathBuilder.append(String.format("/[%s]", SyntaxKind.BOOLEAN_LITERAL));
                } else if (expressionNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                        expressionNode.kind() == SyntaxKind.FIELD_ACCESS) {
                    String varType = semanticModel.typeOf(expressionNode).get().signature();
                    resourcePathBuilder.append("/").append("[").append(varType.trim()).append("]");
                }
            }
        }
        return resourcePathBuilder.toString();
    }
}
