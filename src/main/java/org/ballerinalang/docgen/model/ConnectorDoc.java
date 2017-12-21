package org.ballerinalang.docgen.model;


import java.util.List;

/**
 * Documentable node for Connectors
 */
public class ConnectorDoc extends Documentable {
    public final boolean isConnector;
    public final List<Variable> parameters;

    /**
     * Constructor
     * @param name connector name
     * @param description description
     * @param icon icon
     * @param children connector actions
     * @param parameters parameters of the connector
     */
    public ConnectorDoc(String name, String description, String icon, List<Documentable> children,
                        List<Variable> parameters) {
        super(name, icon, description, children);
        this.parameters = parameters;
        isConnector = true;
    }
}
