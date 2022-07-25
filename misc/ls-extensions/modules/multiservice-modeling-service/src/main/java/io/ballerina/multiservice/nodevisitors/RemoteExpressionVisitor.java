package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.multiservice.model.Resource;
import io.ballerina.projects.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Visitor class for RemoteMethodCallAction nodes.
 */
public class RemoteExpressionVisitor extends NodeVisitor {
    private final SemanticModel semanticModel;

    private final Document document;
    private final List<Resource.ResourceId> interactionList = new ArrayList<>();

    public RemoteExpressionVisitor(SemanticModel semanticModel, Document document) {
        this.semanticModel = semanticModel;
        this.document = document;
    }

    public List<Resource.ResourceId> getInteractionList() {
        return interactionList;
    }

    public SemanticModel getSemanticModel() {
        return semanticModel;
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        String clientName = String.valueOf(remoteMethodCallActionNode.expression()).trim();
        String resourceMethod = String.valueOf(remoteMethodCallActionNode.methodName()).trim();
        // path can be given in a seperate var
        String resourcePath = getPath(remoteMethodCallActionNode);

        StatementVisitor statementVisitor = new StatementVisitor(clientName);
        NonTerminalNode parent = remoteMethodCallActionNode.parent().parent();

        while (statementVisitor.getServiceId() == null || Objects.equals(statementVisitor.getServiceId(), "")) {
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
            }
        }

        return "";
    }
}
