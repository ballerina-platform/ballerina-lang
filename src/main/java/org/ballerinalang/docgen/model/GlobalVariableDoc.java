package org.ballerinalang.docgen.model;

import java.util.ArrayList;

/**
 * Documentable node for Global Variables
 */
public class GlobalVariableDoc extends Documentable {
    public final boolean isGlobalVariable;
    public final String dataType;

    /**
     * Construct
     * @param name global variable name
     * @param description description
     * @param icon icon
     * @param children children if any
     * @param dataType data type of the global variable
     */
    public GlobalVariableDoc(String name, String description, String icon, ArrayList<Documentable> children,
                             String dataType) {
        super(name, icon, description, children);
        this.dataType = dataType;
        isGlobalVariable = true;
    }
}
