package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.repository.CompilerInput;

import java.util.List;

/**
 * List of resolved sources and the RepoHierarchy used to resolve it.
 * Had to wrap in a class since you can't return multiple items form a java method.
 */
public class Resolution {
    public static final Resolution NOT_FOUND = new Resolution(null, null);
    public final RepoHierarchy resolvedBy;
    public final List<CompilerInput> inputs;

    Resolution(RepoHierarchy resolvedBy, List<CompilerInput> inputs) {
        this.resolvedBy = resolvedBy;
        this.inputs = inputs;
    }
}
