package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class RepoDAG {
    private final RepoDAG[] children;
    private final Repo repo;

    public RepoDAG(Repo repos, RepoDAG... children) {
        this.children = children;
        this.repo = repos;
    }

    public Resolution resolve(PackageID pkg) {
        Patten patten = repo.calculate(pkg);
        Resolver resolver = repo.getResolverInstance();
        List<Path> path = patten.convertToPaths(resolver).collect(Collectors.toList());
        System.out.println("Search " + pkg + " in " + repo.getClass().getSimpleName()
                                   + " -> " + patten + " -> found " + path);
        if (!path.isEmpty()) {
            return new Resolution(this, path);
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
