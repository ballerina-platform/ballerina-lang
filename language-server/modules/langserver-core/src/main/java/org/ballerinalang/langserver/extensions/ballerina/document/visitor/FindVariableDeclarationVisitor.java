package org.ballerinalang.langserver.extensions.ballerina.document.visitor;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

/**
 * Visitor used for extracting variable declaration node with a given variableName
 */
public class FindVariableDeclarationVisitor extends NodeVisitor {
    private final String variableName;
    private VariableDeclarationNode varDeclarationNode;

    public FindVariableDeclarationVisitor(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {
        for (int i = 0; i < modulePartNode.members().size(); i++) {
            modulePartNode.members().get(i).accept(this);
        }
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        functionDefinitionNode.functionBody().accept(this);
    }

    @Override
    public void visit(FunctionBodyBlockNode functionBodyBlockNode) {
        for (int i = 0; i < functionBodyBlockNode.statements().size(); i++) {
            functionBodyBlockNode.statements().get(i).accept(this);
        }
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        String varName = variableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim();

        if (varName.equals(variableName)) {
            this.varDeclarationNode = variableDeclarationNode;
        }
    }

    public VariableDeclarationNode getVarDeclarationNode() {
        return varDeclarationNode;
    }
}
