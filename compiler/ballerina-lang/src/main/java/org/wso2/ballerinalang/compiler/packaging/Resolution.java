package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.repository.PackageSourceEntry;

import java.util.List;

/**
 * List of resolved sources and the RepoHierarchy used to resolve it.
 * Had to wrap in a class since you can't return multiple items form a java method.
 */
public class Resolution {
    public static final Resolution NOT_FOUND = new Resolution(null, null);
    public final RepoHierarchy resolvedBy;
    public final List<PackageSourceEntry> sources;

    Resolution(RepoHierarchy resolvedBy, List<PackageSourceEntry> sources) {
        this.resolvedBy = resolvedBy;
        this.sources = sources;
    }
}
