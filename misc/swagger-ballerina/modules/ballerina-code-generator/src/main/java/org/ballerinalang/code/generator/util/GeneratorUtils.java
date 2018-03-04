package org.ballerinalang.code.generator.util;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;

import java.util.List;

/**
 * Utilities used by ballerina code generator
 */
public class GeneratorUtils {

    /**
     * Retrieve a specific annotation by name from a list of annotations
     *
     * @param name        name of the required annotation
     * @param annotations list of annotations containing the required annotation
     * @return returns annotation with the name <code>name</code> if found or
     * null if annotation not found in the list
     */
    public static AnnotationAttachmentNode getAnnotationFromList(String name,
            List<AnnotationAttachmentNode> annotations) {
        AnnotationAttachmentNode annotation = null;

        for (AnnotationAttachmentNode ann : annotations) {
            if (name.equals(ann.getAnnotationName().getValue())) {
                annotation = ann;
            }
        }

        return annotation;
    }
}
