package io.ballerina.multiservice.nodevisitors;

import io.ballerina.compiler.syntax.tree.*;

import java.util.concurrent.atomic.AtomicReference;

public class ModelGeneratorUtil {
    public static String getId(NodeList<AnnotationNode> annotationNodes) {
        AtomicReference<String> id = new AtomicReference<>("");
        for (AnnotationNode annotationNode : annotationNodes)  {
            String annotationName = annotationNode.annotReference().toString().trim();
            if(annotationName.equals("choreo:Service") || annotationName.equals("choreo:Client")) {
                if (annotationNode.annotValue().isPresent()) {
                    SeparatedNodeList<MappingFieldNode> fields = annotationNode.annotValue().get().fields();
                    fields.forEach(mappingFieldNode -> {
                        if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                            SpecificFieldNode specificFieldNode = (SpecificFieldNode) mappingFieldNode;
                            // generate string
                            String name = specificFieldNode.fieldName().toString().trim();
                            if (specificFieldNode.valueExpr().isPresent()) {
                                if (name.equals("id") && annotationName.equals("choreo:Service")) {
                                    ExpressionNode expressionNode = specificFieldNode.valueExpr().get();
                                    id.set(expressionNode.toString().trim());
                                } else if (name.equals("serviceId") && annotationName.equals("choreo:Client")) {
                                    ExpressionNode expressionNode = specificFieldNode.valueExpr().get();
                                    String expressionNodeStr = expressionNode.toSourceCode();
                                    String annotationId = expressionNodeStr.replace("\"", "");
                                    id.set(annotationId);
                                }
                            }
                        }
                    });
                }
            }
        }
        return id.get();
    }
}
