package org.ballerinalang.docgen.model;


import java.util.List;

/**
 * Documentable node abstract class
 */
public abstract class Documentable {
    public final String name;

    public final String icon;

    public final String description;

    public final List<Documentable> children;

    /**
     *
     * @param name name of the node
     * @param icon icon of the node
     * @param description description of the node
     * @param children children of the node if any
     */
    public Documentable(String name, String icon, String description, List<Documentable> children) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.children = children;
    }
}
