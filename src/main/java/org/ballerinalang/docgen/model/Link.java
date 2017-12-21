package org.ballerinalang.docgen.model;

/**
 * Links of the packages available
 */
public class Link {
    public final String packageName;
    public final boolean active;

    /**
     * Constructor
     * @param packageName package name
     * @param active if package is active/inactive
     */
    public Link(String packageName, boolean active) {
        this.packageName = packageName;
        this.active = active;
    }
}
