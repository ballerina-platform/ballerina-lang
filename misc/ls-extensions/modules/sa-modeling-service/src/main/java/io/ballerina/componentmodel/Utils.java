package io.ballerina.componentmodel;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.concurrent.atomic.AtomicReference;

import static io.ballerina.componentmodel.ComponentModelingConstants.DISPLAY_ANNOTATION;
import static io.ballerina.componentmodel.ComponentModelingConstants.ID;

/**
 * Provide utils functions for component model building.
 */
public class Utils {

    public static String getPackageKey(ComponentModel.PackageId packageId) {

        String key = String.format("%s/%s:%s", packageId.getOrg(),
                packageId.getName(), packageId.getVersion());
        return key;
    }

    public static String getId(NodeList<AnnotationNode> annotationNodes) {

        AtomicReference<String> id = new AtomicReference<>("");
        for (AnnotationNode annotationNode : annotationNodes) {
            String annotationName = annotationNode.annotReference().toString().trim();
            if (annotationName.equals(DISPLAY_ANNOTATION)) {
                if (annotationNode.annotValue().isPresent()) {
                    SeparatedNodeList<MappingFieldNode> fields = annotationNode.annotValue().get().fields();
                    fields.forEach(mappingFieldNode -> {
                        if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                            SpecificFieldNode specificFieldNode = (SpecificFieldNode) mappingFieldNode;
                            String name = specificFieldNode.fieldName().toString().trim();
                            if (specificFieldNode.valueExpr().isPresent()) {
                                if (name.equals(ID)) {
                                    ExpressionNode expressionNode = specificFieldNode.valueExpr().get();
                                    String expressionNodeStr = expressionNode.toString().trim();
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
