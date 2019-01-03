/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer.cyclefind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implement Tarjan's strongly connected components algorithm.
 * See <a href="https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">
 * https://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm</a>
 *
 * @since 0.990.3
 */
public class TarjanSccSolverAdjacencyList {

    private int nodeCount;
    private List<List<Integer>> adjacencyList;
    private boolean solved;
    private int sccCount;
    private int id;
    private boolean[] onStack;
    private int[] ids;
    private int[] lowLinks;
    private Deque<Integer> stack;
    public final List<Integer> dependencyOrder;

    private static final int UNVISITED = -1;

    private TarjanSccSolverAdjacencyList(List<List<Integer>> adjacencyList) {
        if (adjacencyList == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }
        nodeCount = adjacencyList.size();
        this.adjacencyList = adjacencyList;
        dependencyOrder = new ArrayList<>();
    }

    /**
     * Find number of strongly connected components in the graph.
     * @return number of SCCs
     */
    public int sccCount() {
        if (!solved) {
            solve();
        }
        return sccCount;
    }

    private void solve() {
        if (solved) {
            return;
        }

        ids = new int[nodeCount];
        lowLinks = new int[nodeCount];
        onStack = new boolean[nodeCount];
        stack = new ArrayDeque<>();
        Arrays.fill(ids, UNVISITED);

        for (int i = 0; i < nodeCount; i++) {
            if (ids[i] == UNVISITED) {
                dfs(i);
            }
        }

        solved = true;
    }

    private void dfs(int at) {
        stack.push(at);
        onStack[at] = true;
        ids[at] = lowLinks[at] = id++;

        for (int to : adjacencyList.get(at)) {
            if (ids[to] == UNVISITED) {
                dfs(to);
            }
            if (onStack[to]) {
                lowLinks[at] = Math.min(lowLinks[at], lowLinks[to]);
            }
        }

        // On recursive callback, if we're at the root node (start of SCC)
        // empty the seen stack until back to root.
        if (ids[at] == lowLinks[at]) {
            for (int node = stack.pop(); true; node = stack.pop()) {
                onStack[node] = false;
                lowLinks[node] = ids[at];
                if (node == at) {
                    break;
                }
            }
            sccCount++;
        }

        dependencyOrder.add(at);
    }

    /**
     * Initialized adjacency list for graph with numberOfNodes nodes.
     * @param numberOfNodes number of nodes
     * @return initialized list to fill with edges
     */
    public static TarjanSccSolverAdjacencyList createGraph(int numberOfNodes) {
        List<List<Integer>> graph = new ArrayList<>(numberOfNodes);
        for (int i = 0; i < numberOfNodes; i++) {
            graph.add(new ArrayList<>());
        }
        return new TarjanSccSolverAdjacencyList(graph);
    }

    /**
     * Adds a directed edge from node 'from' to node 'to'.
     * @param from edge starting from node
     * @param to to node
     */
    public void addEdge(int from, int to) {
        this.adjacencyList.get(from).add(to);
    }

    /**
     * Get the connected components of this adjacencyList. If two indexes
     * have the same low link value then they're in the same SCC.
     * @return map of SCCs
     */
    public Map<Integer, List<Integer>> getSCCs() {
        solve();

        Map<Integer, List<Integer>> multimap = new TreeMap<>();
        for (int i = 0; i < this.nodeCount; i++) {
            if (!multimap.containsKey(lowLinks[i])) {
                multimap.put(lowLinks[i], new ArrayList<>());
            }
            multimap.get(lowLinks[i]).add(i);
        }
        return multimap;
    }
}
