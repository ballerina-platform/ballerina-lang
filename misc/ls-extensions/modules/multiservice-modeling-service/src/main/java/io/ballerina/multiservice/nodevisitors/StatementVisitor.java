package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.Objects;
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
        if (Objects.equals(variableDeclarationNode.typedBindingPattern().bindingPattern().toString().trim(),
                clientName)) {
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
}
