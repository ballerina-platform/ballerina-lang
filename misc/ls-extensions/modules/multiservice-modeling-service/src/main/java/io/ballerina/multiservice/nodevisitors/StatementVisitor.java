package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.Optional;

/**
 * Visitor class to identify client declaration nodes inside a Ballerina service.
 */
public class StatementVisitor extends NodeVisitor {
    private String serviceId;

    private final String clientName;

    public StatementVisitor(String clientName) {
        this.clientName = clientName;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        if (variableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim().equals(clientName)) {
            NodeList<AnnotationNode> annotations = variableDeclarationNode.annotations();
            this.serviceId = ModelGeneratorUtil.getId(annotations).trim();
        }
    }

    @Override
    public void visit(ObjectFieldNode objectFieldNode) {
        if (objectFieldNode.fieldName().text().trim().equals(clientName)) {
            Optional<MetadataNode> metadataNode = objectFieldNode.metadata();
            if (metadataNode.isPresent()) {
                NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
                serviceId = ModelGeneratorUtil.getId(annotationNodes).trim();
            }
        }
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        if (moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim().equals(clientName)) {
            Optional<MetadataNode> metadataNode = moduleVariableDeclarationNode.metadata();
            if (metadataNode.isPresent()) {
                NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
                serviceId = ModelGeneratorUtil.getId(annotationNodes).trim();
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
    public void visit(FunctionDefinitionNode functionDefinitionNode) {

    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {

    }


}
