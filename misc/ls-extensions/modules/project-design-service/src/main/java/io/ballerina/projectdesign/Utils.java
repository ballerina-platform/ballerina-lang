package io.ballerina.projectdesign;

import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projectdesign.servicemodel.components.ServiceAnnotation;
import io.ballerina.projects.Package;

import java.util.Map;

import static io.ballerina.projectdesign.ProjectDesignConstants.DISPLAY_ANNOTATION;
import static io.ballerina.projectdesign.ProjectDesignConstants.ID;
import static io.ballerina.projectdesign.ProjectDesignConstants.LABEL;

/**
 * Provide utils functions for component model building.
 *
 * @since 2201.2.2
 */
public class Utils {

    public static String getQualifiedPackageName(ComponentModel.PackageId packageId) {

        return String.format("%s/%s:%s", packageId.getOrg(),
                packageId.getName(), packageId.getVersion());
    }

    public static boolean modelAlreadyExists(Map<String, JsonObject> componentModelMap, Package currentPackage) {

        ComponentModel.PackageId packageId = new ComponentModel.PackageId(currentPackage);
        return componentModelMap.containsKey(getQualifiedPackageName(packageId));
    }

    public static ServiceAnnotation getServiceAnnotation(NodeList<AnnotationNode> annotationNodes) {

        String id = "";
        String label = "";
        for (AnnotationNode annotationNode : annotationNodes) {
            String annotationName = annotationNode.annotReference().toString().trim();
            if (annotationName.equals(DISPLAY_ANNOTATION)) {
                if (annotationNode.annotValue().isPresent()) {
                    SeparatedNodeList<MappingFieldNode> fields = annotationNode.annotValue().get().fields();
                    for (MappingFieldNode mappingFieldNode : fields) {
                        if (mappingFieldNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                            SpecificFieldNode specificFieldNode = (SpecificFieldNode) mappingFieldNode;
                            String name = specificFieldNode.fieldName().toString().trim();
                            if (specificFieldNode.valueExpr().isPresent()) {
                                ExpressionNode expressionNode = specificFieldNode.valueExpr().get();
                                String expressionNodeStr = expressionNode.toString().trim();
                                String annotation = expressionNodeStr.replace("\"", "");
                                if (name.equals(ID)) {
                                    id = annotation;
                                } else if (name.equals(LABEL)) {
                                    label = annotation;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ServiceAnnotation(id, label);
    }
}
