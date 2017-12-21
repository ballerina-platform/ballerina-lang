package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Page created by traversing the bal package
 */
public class Page {

    public final String packageName;
    public final List<Documentable> constructs;
    public final List<Link> links;

    /**
     *
     * @param packageName name of the bal package
     * @param constructs constructs in the package
     * @param links links to the other packages
     */
    public Page(String packageName, ArrayList<Documentable> constructs, List<Link> links) {
        this.packageName = packageName;
        this.constructs = constructs;
        this.links = links;
    }
}
