/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.componentmodel.servicemodel.components.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor class for RemoteMethodCallAction nodes.
 */
public class RemoteMethodCallActionNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;
    private final List<Resource.ResourceId> interactionList = new ArrayList<>();

    public RemoteMethodCallActionNodeVisitor(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public List<Resource.ResourceId> getInteractionList() {
        return interactionList;
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        String clientName = String.valueOf(remoteMethodCallActionNode.expression()).trim();
        String resourceMethod = String.valueOf(remoteMethodCallActionNode.methodName()).trim();
        String resourcePath = getPath(remoteMethodCallActionNode);

        StatementNodeVisitor statementVisitor = new StatementNodeVisitor(clientName, semanticModel);
        NonTerminalNode parent = remoteMethodCallActionNode.parent().parent();

        while (statementVisitor.getServiceId() == null || statementVisitor.getServiceId().isEmpty()) { //isEmpty

            parent = parent.parent();
            if (parent != null) {
                parent.accept(statementVisitor);
            } else {
                break;
            }
        }


        Resource.ResourceId interaction = new Resource.ResourceId(statementVisitor.getServiceId(),
                resourceMethod, resourcePath);
        interactionList.add(interaction);

    }

    private String getPath(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        FunctionArgumentNode functionArgumentNode = remoteMethodCallActionNode.arguments().get(0);

        if (functionArgumentNode.kind() == SyntaxKind.POSITIONAL_ARG) {
            SyntaxKind parameterKind = ((PositionalArgumentNode) functionArgumentNode).expression().kind();
            if (parameterKind == SyntaxKind.STRING_LITERAL) {
                String resourcePath = remoteMethodCallActionNode.arguments().get(0).toString().replace("\"", "");
                if (resourcePath.startsWith("/")) {
                    resourcePath = resourcePath.substring(1);
                }
                return resourcePath.trim();
            } else if (parameterKind == SyntaxKind.STRING_TEMPLATE_EXPRESSION) {

            } else if (parameterKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {

            }
        }

        return "";
    }
}
