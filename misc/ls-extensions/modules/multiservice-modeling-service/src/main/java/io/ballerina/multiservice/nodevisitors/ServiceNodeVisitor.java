package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.multiservice.model.Service;
import io.ballerina.projects.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Visitor class for ServiceDeclaration nodes.
 */
public class ServiceNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;

    private final Document document;
    private final List<Service> services = new ArrayList<>();

    public List<Service> getServices() {
        return services;
    }

    public ServiceNodeVisitor(SemanticModel semanticModel, Document document) {
        this.semanticModel = semanticModel;
        this.document = document;
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        StringBuilder serviceName = new StringBuilder();
        String serviceId = "";
        NodeList<Node> serviceNameNodes = serviceDeclarationNode.absoluteResourcePath();
        for (Node serviceNameNode : serviceNameNodes) {
            serviceName.append(serviceNameNode.toString());
        }

        Optional<MetadataNode> metadataNode = serviceDeclarationNode.metadata();
        if (metadataNode.isPresent()) {
            NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
            serviceId = ModelGeneratorUtil.getId(annotationNodes).replace("\"", "");
        }

        ResourceVisitor resourceVisitor = new ResourceVisitor(serviceId, semanticModel, document);
        serviceDeclarationNode.accept(resourceVisitor);
        services.add(new Service(serviceName.toString(), serviceId, resourceVisitor.getResources()));

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
}
