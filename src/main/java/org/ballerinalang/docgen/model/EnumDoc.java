package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentable node for enums
 */
public class EnumDoc extends Documentable {
    public final boolean isEnum;
    public final List<Variable> enumerators;
    /**
     * Constructor
     * @param name enum name
     * @param description description
     * @param icon icon
     * @param children children if any
     * @param enumerators enumerators of the enum
     */
    public EnumDoc(String name, String description, String icon, ArrayList<Documentable> children,
                   List<Variable> enumerators) {
        super(name, icon, description, children);
        this.enumerators = enumerators;
        isEnum = true;
    }
}
