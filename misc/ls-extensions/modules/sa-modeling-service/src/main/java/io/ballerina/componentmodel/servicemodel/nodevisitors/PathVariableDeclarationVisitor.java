package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

/**
 * Visitor class to identify path variables.
 */
public class PathVariableDeclarationVisitor extends NodeVisitor {
    private final String varName;
    private String varType = null;

    public PathVariableDeclarationVisitor(String varName) {
        this.varName = varName;
    }

    public String getVarType() {
        return varType;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        if (variableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim().equals(varName)) {
            this.varType = variableDeclarationNode.typedBindingPattern().typeDescriptor().toString();
        }
    }

    @Override
    public void visit(ObjectFieldNode objectFieldNode) {
        if (objectFieldNode.fieldName().text().trim().equals(varName)) {
            this.varType = objectFieldNode.typeName().toString();
        }
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        if (moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim().equals(varName)) {
            this.varType = moduleVariableDeclarationNode.typedBindingPattern().typeDescriptor().toString();
        }
    }

    @Override
    public void visit(ResourcePathParameterNode resourcePathParameterNode) {
        if (resourcePathParameterNode.paramName().toString().trim().equals(varName)) {
            this.varType = resourcePathParameterNode.typeDescriptor().toString();
        }
    }

    @Override
    public void visit(RequiredParameterNode requiredParameterNode) {
        if (requiredParameterNode.paramName().toString().equals(varName)) {
            this.varType = requiredParameterNode.typeName().toString();
        }
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
            SeparatedNodeList<ParameterNode> parameters = functionDefinitionNode.functionSignature().parameters();
            for (ParameterNode parameterNode : parameters) {
                if (parameterNode.kind() == SyntaxKind.REQUIRED_PARAM) {
                    RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                    if (requiredParameterNode.paramName().get().toString().equals(varName)) {
                        this.varType = requiredParameterNode.typeName().toString();
                    }
                }
            }
        }

    }

    @Override
    public void visit(TypeDefinitionNode typeDefinitionNode) {

    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {

    }

    @Override
    public void visit(EnumDeclarationNode enumDeclarationNode) {

    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {

    }
}
