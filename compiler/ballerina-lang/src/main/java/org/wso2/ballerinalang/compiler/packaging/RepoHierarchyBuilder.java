package org.wso2.ballerinalang.compiler.packaging;

import org.wso2.ballerinalang.compiler.packaging.repo.Repo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Builds RepoHierarchy by doing a BFS on repo graph.
 */
public class RepoHierarchyBuilder {

    public static RepoHierarchy build(RepoNode root) {
        List<RepoNode> ordered = genBreathFirstOrder(root);
        clearVisitedFlag(ordered);
        return newHierarchyFromOrderedNodes(ordered);
    }

    private static List<RepoNode> genBreathFirstOrder(RepoNode root) {
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
        return ordered;
    }

    private static RepoHierarchy newHierarchyFromOrderedNodes(List<RepoNode> ordered) {
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

    private static void clearVisitedFlag(List<RepoNode> ordered) {
        for (RepoNode repoNode : ordered) {
            repoNode.visited = false;
        }
    }

    public static RepoNode node(Repo r, RepoNode... n) {
        return new RepoNode(r, n);
    }

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
}
