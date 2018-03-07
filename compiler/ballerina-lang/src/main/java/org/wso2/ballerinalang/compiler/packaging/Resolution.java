package org.wso2.ballerinalang.compiler.packaging;

import java.nio.file.Path;
import java.util.List;

public class Resolution {
    public static final Resolution EMPTY = new Resolution(null, null);
    public final RepoDAG resolvedBy;
    public final List<Path> path;

    Resolution(RepoDAG resolvedBy, List<Path> path) {
        this.resolvedBy = resolvedBy;
        this.path = path;
    }
}
