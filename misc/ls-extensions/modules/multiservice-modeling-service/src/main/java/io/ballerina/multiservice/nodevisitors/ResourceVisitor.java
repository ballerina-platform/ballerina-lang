package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.multiservice.MultiServiceModelingConstants.ParameterIn;
import io.ballerina.multiservice.model.Parameter;
import io.ballerina.multiservice.model.Resource;
import io.ballerina.projects.Document;


import java.util.ArrayList;
import java.util.Arrays;
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


            List<Parameter> parameterList = getParameters(functionDefinitionNode.functionSignature());
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
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptor.get();
            // need to split by pipe sign
            return Arrays.asList(returnNode.type().toString().trim().split("\\|"));
        }
        return new ArrayList<>();
    }

    private List<Parameter> getParameters(FunctionSignatureNode functionSignatureNode) {
        List<Parameter> parameterList = new ArrayList<>();
        SeparatedNodeList<ParameterNode> parameterNodes = functionSignatureNode.parameters();
        for (ParameterNode parameterNode : parameterNodes) {
            switch (parameterNode.kind()) {
                case REQUIRED_PARAM:
                    RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                    String requiredParamIn = getParameterIn(requiredParameterNode.annotations());
                    String requiredParamName = requiredParameterNode.paramName().isPresent() ?
                            requiredParameterNode.paramName().get().toString() : "";
                    parameterList.add(new Parameter(requiredParameterNode.typeName().toString(),
                            requiredParamName.trim(), requiredParamIn, true));
                    break;
                case DEFAULTABLE_PARAM:
                    DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                    String defaultableParamIn = getParameterIn(defaultableParameterNode.annotations());
                    String defaultableParamName = defaultableParameterNode.paramName().isPresent() ?
                            defaultableParameterNode.paramName().get().toString() : "";
                    parameterList.add(new Parameter(defaultableParameterNode.typeName().toString().trim(),
                            defaultableParamName.trim(), defaultableParamIn, false));
                    break;
                case INCLUDED_RECORD_PARAM:
                    break;

            }

        }
        return parameterList;
    }

    private String getParameterIn(NodeList<AnnotationNode> annotationNodes) {
        String in = "";
        Optional<AnnotationNode> payloadAnnotation = annotationNodes.stream().filter(annot ->
                annot.toString().trim().equals("@http:Payload")).findAny();
        Optional<AnnotationNode> headerAnnotation = annotationNodes.stream().filter(annot ->
                annot.toString().trim().equals("@http:Header")).findAny();
        // do we need to handle http:Request

        if (payloadAnnotation.isPresent()) {
            in = ParameterIn.BODY.getValue();
        } else if (headerAnnotation.isPresent()) {
            in = ParameterIn.HEADER.getValue();
        } else {
            in = ParameterIn.QUERY.getValue();
        }
        return in;
    }

}
