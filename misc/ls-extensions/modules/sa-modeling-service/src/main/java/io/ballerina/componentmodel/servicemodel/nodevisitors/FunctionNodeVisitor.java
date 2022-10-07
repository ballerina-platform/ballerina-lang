package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.componentmodel.servicemodel.components.Interaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node visitor to identify action invocation in each method in a Ballerina package.
 */
public class FunctionNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;

    private Map<String, List<Interaction>> functionInteractionMap = new HashMap<>();

    public FunctionNodeVisitor(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Map<String, List<Interaction>> getFunctionInteractionMap() {
        return functionInteractionMap;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        ActionNodeVisitor actionNodeVisitor = new ActionNodeVisitor(semanticModel);
        functionDefinitionNode.accept(actionNodeVisitor);
        functionInteractionMap.put(functionDefinitionNode.functionName().text(),
                actionNodeVisitor.getInteractionList());
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {

    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {

    }

    @Override
    public void visit(RecordTypeDescriptorNode recordTypeDescriptorNode) {

    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {

    }
}
