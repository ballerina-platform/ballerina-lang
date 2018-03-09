package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.packaging.resolve.Converter;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Node in a directed acyclic graph containing repositories.
 * resolve will walk through the graph
 */
public class RepoDAG {
    private final RepoDAG[] children;
    private final Repo repo;

    public RepoDAG(Repo repos, RepoDAG... children) {
        this.children = children;
        this.repo = repos;
    }

    public Resolution resolve(PackageID pkg) {
        Patten patten = repo.calculate(pkg);
        Converter converter = repo.getConverterInstance();
        List<Path> paths = patten.convertToPaths(converter).collect(Collectors.toList());
        PrintStream out = System.out;
        out.println("Search " + pkg + " in " + repo.getClass().getSimpleName()
                            + " -> " + patten + " -> found " + paths);
        if (!paths.isEmpty()) {
            return new Resolution(this, paths);
        }
        for (RepoDAG child : children) {
            if (child != null) {
                Resolution childResolution = child.resolve(pkg);
                if (childResolution != Resolution.EMPTY) {
                    return childResolution;
                }
            }
        }
        return Resolution.EMPTY;
    }
}
