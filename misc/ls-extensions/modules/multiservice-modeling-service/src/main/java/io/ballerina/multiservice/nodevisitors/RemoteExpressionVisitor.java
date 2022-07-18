package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.multiservice.model.Resource;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;

public class RemoteExpressionVisitor extends NodeVisitor {
    private final SemanticModel semanticModel;

    private final Document document;
    private List<Resource.ResourceId> interactionList = new ArrayList<>();

    public RemoteExpressionVisitor(SemanticModel semanticModel, Document document) {
        this.semanticModel = semanticModel;
        this.document = document;
    }

    public List<Resource.ResourceId> getInteractionList() {
        return interactionList;
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        String clientName = String.valueOf(remoteMethodCallActionNode.expression());
        String resourceMethod = String.valueOf(remoteMethodCallActionNode.methodName());
        // path can be given in a seperate var
        String resourcePath = remoteMethodCallActionNode.arguments().get(0).toSourceCode();
//        String x = getPath(remoteMethodCallActionNode);

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

        Resource.ResourceId interaction = new Resource.ResourceId(statementVisitor.getServiceId(), resourceMethod,resourcePath);
        interactionList.add(interaction);

    }

//    private String getPath(RemoteMethodCallActionNode remoteMethodCallActionNode) {
//        FunctionArgumentNode functionArgumentNode = remoteMethodCallActionNode.arguments().get(0);
//        functionArgumentNode.
//        return "";
//    }
}
