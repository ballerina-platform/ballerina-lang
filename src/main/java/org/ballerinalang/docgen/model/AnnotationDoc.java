package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentable node for Annotations
 */
public class AnnotationDoc extends Documentable {
    public final boolean isAnnotation;
    public final List<Variable> attributes;

    /**
     * Constructor
     * @param name annotation name
     * @param description description
     * @param icon icon
     * @param children if there are any children
     * @param attributes annotation attributes
     */
    public AnnotationDoc(String name, String description, String icon, ArrayList<Documentable> children,
                         List<Variable> attributes) {
        super(name, icon, description, children);
        this.attributes = attributes;
        isAnnotation = true;
    }
}
