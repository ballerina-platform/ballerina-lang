package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentable node for Functions
 */
public class FunctionDoc extends Documentable {

    public final List<Variable> parameters;
    public final List<Variable> returnParams;
    public final boolean isFunction;

    /**
     * Constructor
     * @param name function name
     * @param description description
     * @param icon icon
     * @param children children if any
     * @param parameters parameters of the function
     * @param returnParams return parameters of the function
     */
    public FunctionDoc(String name, String icon, String description, ArrayList<Documentable> children,
                       List<Variable> parameters, List<Variable> returnParams) {
        super(name, icon, description, children);
        this.parameters = parameters;
        this.returnParams = returnParams;
        isFunction = true;
    }
}
