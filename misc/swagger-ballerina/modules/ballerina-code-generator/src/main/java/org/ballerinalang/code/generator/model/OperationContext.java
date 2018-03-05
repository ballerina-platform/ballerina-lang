package org.ballerinalang.code.generator.model;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;

/**
 * OperationContext to hold ballerina resource information.
 */
public class OperationContext {
    private static final String RES_CONFIG_ANNOTATION = "resourceConfig";

    private String method;
    private String name;
    private String path;

    public static OperationContext buildOperation(ResourceNode resource) {
        OperationContext context = new OperationContext();
        context.name = resource.getName().getValue();

        // Iterate through all resource level annotations and find out resource configuration information
        for (AnnotationAttachmentNode ann: resource.getAnnotationAttachments()) {

            if (RES_CONFIG_ANNOTATION.equals(ann.getAnnotationName().getValue())) {
                for (AnnotationAttachmentAttributeNode attr: ann.getAttributes()) {
                    String attrName = attr.getName().getValue();

                    if ("path".equals(attrName)) {
                        context.path = attr.getValue().getValue().toString();
                    } else if ("methods".equals(attrName)) {
                        // Consider only first http method since we don't expect multiple http methods to be
                        // supported by single action
                        context.method = attr.getValue().getValueArray().get(0).getValue().toString();
                    }
                }

                break;
            }
        }

        return context;
    }

    public String getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
