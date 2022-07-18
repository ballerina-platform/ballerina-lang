package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.multiservice.model.Service;
import io.ballerina.projects.Document;

import java.util.*;

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

        // we cant write resource visitor in this level because resource is inside service and one service can have multiple resources

        ResourceVisitor resourceVisitor = new ResourceVisitor(serviceId, semanticModel, document);
        serviceDeclarationNode.accept(resourceVisitor);
        services.add(new Service(serviceName.toString(), serviceId, resourceVisitor.getResources()));

    }

//    @Override
//    public void visit(FunctionDefinitionNode functionDefinitionNode) {
//        SyntaxKind kind = functionDefinitionNode.kind();
//        if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
//            StringBuilder resourcePathBuilder = new StringBuilder();
//            NodeList<Node> relativeResourcePaths = functionDefinitionNode.relativeResourcePath();
//            for (Node path : relativeResourcePaths) {
//                resourcePathBuilder.append(path.toString());
//            }
//            String resourcePath = resourcePathBuilder.toString();
//            String method = functionDefinitionNode.functionName().text();
//            List<Parameter> parameterList = new ArrayList<>();
//            List<String> returnTypes = getReturnTypes(functionDefinitionNode);
//
//            RemoteExpressionVisitor remoteExpressionVisitor = new RemoteExpressionVisitor();
//            functionDefinitionNode.accept(remoteExpressionVisitor);
//
//            Resource.ResourceId resourceId = new Resource.ResourceId(this.serviceId, resourcePath, method);
//            Resource resource = new Resource(resourceId, parameterList, returnTypes, remoteExpressionVisitor.getInteractionList());
//            resources.add(resource);
//
//        }
//    }


//    @Override
//    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
//        this.clientName = remoteMethodCallActionNode.expression();
////        visitSyntaxNode(remoteMethodCallActionNode.parent());
//        NonTerminalNode blockStatementNode = remoteMethodCallActionNode.parent().parent().parent();
//        if (blockStatementNode instanceof BlockStatementNode) {
//            Collection<ChildNodeEntry> childNodeEntries = blockStatementNode.childEntries();
//            for (ChildNodeEntry childNodeEntry : childNodeEntries) {
//                if (Objects.equals(childNodeEntry.name(), "statements")) {
//
//                }
//            }
//            blockStatementNode.childEntries().forEach(childNodeEntry -> {
//                if (Objects.equals(childNodeEntry.name(), "statements")) {
//                    ChildNodeEntry childNodeEntry1 = childNodeEntry;
////
////                    for (Node statement : statements) {
////
////                    }
//                }
//            });
//        }
//    }

    @Override
    public void visit(TypeDefinitionNode typeDefinitionNode) {

    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {

    }

    @Override
    public void visit(EnumDeclarationNode enumDeclarationNode) {

    }

    private List<String> getReturnTypes(FunctionDefinitionNode functionDefinitionNode) {
        List<String> returnTypes = new ArrayList<>();
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptor.get();
            returnTypes.add(returnNode.type().toString());

        }
        return returnTypes;
    }
}
