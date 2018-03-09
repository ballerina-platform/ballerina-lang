package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.packaging.resolve.Converter;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Keeps list of n Repos in lookup order.
 * Also has a list of (n-1) child RepoHierarchy objects.
 * <p>
 * (n-1) because, each Repo is matched with one child RepoHierarchy,
 * except for the fist one, it matches with this object.
 */
public class RepoHierarchy {

    private final Repo[] repos;
    private final RepoHierarchy[] dags;

    /**
     * Node in a directed acyclic graph containing repositories.
     */
    public static class RepoNode {
        public final RepoNode[] children;
        public final Repo repo;
        private boolean visited;

        RepoNode(Repo repo, RepoNode[] children) {
            this.children = children;
            this.repo = repo;
        }

    }

    public static RepoHierarchy build(RepoNode root) {
        List<RepoNode> ordered = new ArrayList<>();
        Queue<RepoNode> queue = new LinkedList<>();
        queue.add(root);
        ordered.add(root);
        root.visited = true;
        while (!queue.isEmpty()) {
            RepoNode node = queue.remove();
            for (RepoNode child : node.children) {
                if (child != null && !child.visited) {
                    child.visited = true;
                    ordered.add(child);
                    queue.add(child);
                }
            }
        }

        for (RepoNode repoNode : ordered) {
            repoNode.visited = false;
        }

        int totalNodes = ordered.size();
        Repo[] orderedRepos = new Repo[totalNodes];
        RepoHierarchy[] orderedDAGs = new RepoHierarchy[totalNodes - 1];
        for (int i = 0; i < totalNodes; i++) {
            RepoNode repoNode = ordered.get(i);
            orderedRepos[i] = repoNode.repo;
            if (i != 0) {
                orderedDAGs[i - 1] = build(repoNode);
            }
        }

        return new RepoHierarchy(orderedRepos, orderedDAGs);
    }

    public static RepoNode node(Repo r, RepoNode... n) {
        return new RepoNode(r, n);
    }

    private RepoHierarchy(Repo[] repos, RepoHierarchy[] dags) {
        this.repos = repos;
        this.dags = dags;
    }

    public Resolution resolve(PackageID pkg) {
        PrintStream out = System.out;
        out.println("Searching " + pkg);
        for (int i = 0; i < repos.length; i++) {
            Repo repo = repos[i];
            Patten patten = repo.calculate(pkg);
            if (patten != Patten.NULL) {
                Converter converter = repo.getConverterInstance();
                List<Path> paths = patten.convertToPaths(converter)
                                         .filter(path -> Files.isRegularFile(path))
                                         .collect(Collectors.toList());
                out.println("\t looking in " + repo + " for patten\n\t\t" +
                                    patten + " and found \n\t\t\t" +
                                    paths);
                if (!paths.isEmpty()) {
                    return new Resolution(getChildHierarchyForRepo(i), paths);
                }
            } else {
                out.println("\t skipping " + repo);
            }
        }
        return Resolution.NOT_FOUND;
    }

    private RepoHierarchy getChildHierarchyForRepo(int repoIndex) {
        if (repoIndex > 0) {
            return dags[repoIndex - 1];
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "{r:" + Arrays.toString(repos) +
                ", d:" + Arrays.toString(dags) + "}";
    }
}
