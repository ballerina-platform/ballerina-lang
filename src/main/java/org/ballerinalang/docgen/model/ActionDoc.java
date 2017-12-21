package org.ballerinalang.docgen.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Documentable node for Actions
 */
public class ActionDoc extends Documentable {
    public final List<Variable> parameters;
    public final List<Variable> returnParams;

    /**
     * Constructor
     * @param name action name
     * @param description description
     * @param icon icon
     * @param children if there are any children
     * @param parameters parameters of the action
     * @param returnParams return parameters of the action
     */
    public ActionDoc(String name, String description, String icon, ArrayList<Documentable> children,
                     List<Variable> parameters, List<Variable> returnParams) {
        super(name, icon, description, children);
        this.parameters = parameters;
        this.returnParams = returnParams;
    }

}
