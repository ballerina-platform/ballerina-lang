package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentable node for Structs
 */
public class StructDoc extends Documentable {
    public final List<Variable> fields;
    public final boolean isStruct;
    /**
     * Constructor
     * @param name struct name
     * @param description description
     * @param icon icon
     * @param children children if any
     * @param fields struct fields
     */
    public StructDoc(String name, String description, String icon, ArrayList<Documentable> children,
                     List<Variable> fields) {
        super(name, icon, description, children);
        this.fields = fields;
        isStruct = true;
    }
}
