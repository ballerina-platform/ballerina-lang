package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.multiservice.model.Parameter;
import io.ballerina.multiservice.model.Resource;
import io.ballerina.projects.Document;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Visitor class for FunctionDefinition node.
 */
public class ResourceVisitor extends NodeVisitor {
    private final String serviceId;

    private final SemanticModel semanticModel;

    private final Document document;
    List<Resource> resources = new ArrayList<>();

    public ResourceVisitor(String serviceId, SemanticModel semanticModel, Document document) {
        this.serviceId = serviceId;
        this.semanticModel = semanticModel;
        this.document = document;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        SyntaxKind kind = functionDefinitionNode.kind();
        if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
            StringBuilder resourcePathBuilder = new StringBuilder();
            NodeList<Node> relativeResourcePaths = functionDefinitionNode.relativeResourcePath();
            for (Node path : relativeResourcePaths) {
                resourcePathBuilder.append(path.toString());
            }
            String resourcePath = resourcePathBuilder.toString().trim();
            String method = functionDefinitionNode.functionName().text().trim();
            List<Parameter> parameterList = new ArrayList<>();
            List<String> returnTypes = getReturnTypes(functionDefinitionNode);

            RemoteExpressionVisitor remoteExpressionVisitor = new RemoteExpressionVisitor(semanticModel, document);
            functionDefinitionNode.accept(remoteExpressionVisitor);

            Resource.ResourceId resourceId = new Resource.ResourceId(this.serviceId, method, resourcePath);

            Resource resource = new Resource(resourceId, parameterList, returnTypes,
                    remoteExpressionVisitor.getInteractionList());
            resources.add(resource);

        }
    }


    private List<String> getReturnTypes(FunctionDefinitionNode functionDefinitionNode) {
        List<String> returnTypes = new ArrayList<>();
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptor.get();
            returnTypes.add(returnNode.type().toString().trim());

        }
        return returnTypes;
    }



}
