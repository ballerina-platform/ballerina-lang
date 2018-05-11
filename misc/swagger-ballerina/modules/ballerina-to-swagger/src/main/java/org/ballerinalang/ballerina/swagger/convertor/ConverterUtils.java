package org.ballerinalang.ballerina.swagger.convertor;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities used in Ballerina  to Swagger converter.
 */
public class ConverterUtils {
    /**
     * Converts the attributes of an annotation to a map of key being attribute key and value being an annotation
     * attachment value.
     *
     * @param list The BLangRecord list.
     * @return A map of attributes.
     */
    public static Map<String, BLangExpression> listToMap(List<BLangRecordLiteral.BLangRecordKeyValue> list) {
        Map<String, BLangExpression> attrMap = new HashMap<>();

        list.forEach(attr -> attrMap.put(attr.getKey().toString(), attr.getValue()));

        return attrMap;
    }

    /**
     * Coverts the string value of an annotation attachment to a string.
     *
     * @param valueNode The annotation attachment.
     * @return The string value.
     */
    public static String getStringLiteralValue(BLangExpression valueNode) {
        if (valueNode instanceof BLangLiteral) {
            return valueNode.toString();
        } else {
            return null;
        }
    }

    /**
     * Retrieves a specific annotation by name from a list of annotations.
     *
     * @param name        name of the required annotation
     * @param pkg         package of the required annotation
     * @param annotations list of annotations containing the required annotation
     * @return returns annotation with the name <code>name</code> if found or
     * null if annotation not found in the list
     */
    public static AnnotationAttachmentNode getAnnotationFromList(String name, String pkg,
            List<? extends AnnotationAttachmentNode> annotations) {
        AnnotationAttachmentNode annotation = null;
        if (name == null || pkg == null) {
            return null;
        }

        for (AnnotationAttachmentNode ann : annotations) {
            if (pkg.equals(ann.getPackageAlias().getValue()) && name.equals(ann.getAnnotationName().getValue())) {
                annotation = ann;
            }
        }

        return annotation;
    }
}
