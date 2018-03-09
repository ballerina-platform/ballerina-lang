package org.wso2.ballerinalang.compiler.packaging;

import java.nio.file.Path;
import java.util.List;

/**
 * List of resolved paths and the DAG node used to resolve it.
 * Had to wrap in a class since you can't return multiple items form a java method.
 */
public class Resolution {
    public static final Resolution NOT_FOUND = new Resolution(null, null);
    public final RepoHierarchy resolvedBy;
    public final List<Path> paths;

    Resolution(RepoHierarchy resolvedBy, List<Path> paths) {
        this.resolvedBy = resolvedBy;
        this.paths = paths;
    }
}
