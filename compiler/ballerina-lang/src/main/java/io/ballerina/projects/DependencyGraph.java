/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a generic immutable dependency graph.
 *
 * @param <T> type of the node
 * @since 2.0.0
 */
public class DependencyGraph<T> {
    @SuppressWarnings("rawtypes")
    private static final DependencyGraph EMPTY_GRAPH = new DependencyGraph<>(new HashMap<>());
    private final Map<T, Set<T>> dependencies;

    private List<T> topologicallySortedNodes;

    @SuppressWarnings("unchecked")
    public static <T> DependencyGraph<T> emptyGraph() {
        return (DependencyGraph<T>) EMPTY_GRAPH;
    }

    @SuppressWarnings("unchecked")
    public static <T> DependencyGraph<T> from(Map<T, Set<T>> dependencies) {
        if (dependencies.isEmpty()) {
            return (DependencyGraph<T>) EMPTY_GRAPH;
        }

        return new DependencyGraph<>(dependencies);
    }

    private DependencyGraph(Map<T, Set<T>> dependencies) {
        this.dependencies = Collections.unmodifiableMap(dependencies);
    }

    public void findCycles() {
    }

    public DependencyGraph<T> add(T node) {
        Map<T, Set<T>> newDependencies = new HashMap<>(this.dependencies);
        newDependencies.put(node, new HashSet<>());
        return new DependencyGraph<>(newDependencies);
    }

    public DependencyGraph<T> addAll(Collection<T> nodes) {
        throw new UnsupportedOperationException();
    }

    public DependencyGraph<T> addDependencies(T dependent, Collection<T> dependencies) {
        throw new UnsupportedOperationException();
    }

    public Collection<T> getNodes() {
        return dependencies.keySet();
    }

    public Collection<T> getDirectDependents(T node) {
        throw new UnsupportedOperationException();
    }

    public Collection<T> getDirectDependencies(T node) {
        Set<T> deps = dependencies.get(node);
        if (deps == null) {
            return Collections.emptySet();
        } else {
            return deps;
        }
    }

    public boolean contains(T node) {
        return dependencies.containsKey(node);
    }

    public List<T> toTopologicallySortedList() {
        // TODO detect whether there are cycles here, before sorting
        if (topologicallySortedNodes != null) {
            return topologicallySortedNodes;
        }

        List<T> sorted = new ArrayList<>();
        sortTopologically(dependencies.keySet(), new HashSet<>(), sorted);
        topologicallySortedNodes = Collections.unmodifiableList(sorted);
        return topologicallySortedNodes;
    }

    public boolean isEmpty() {
        return this == EMPTY_GRAPH;
    }

    private void sortTopologically(Collection<T> nodesToSort, Set<T> visited, List<T> sorted) {
        for (T node : nodesToSort) {
            if (!visited.contains(node)) {
                visited.add(node);
                sortTopologically(dependencies.get(node), visited, sorted);
                sorted.add(node);
            }
        }
    }

    /**
     * Builds a {@code DependencyGraph}.
     *
     * @param <T> type of the node
     * @since 2.0.0
     */
    public static class DependencyGraphBuilder<T> {
        private final Map<T, Set<T>> dependenciesMap;

        private DependencyGraphBuilder(Map<T, Set<T>> dependencies) {
            this.dependenciesMap = dependencies;
        }

        public static <T> DependencyGraphBuilder<T> getBuilder() {
            return new DependencyGraphBuilder<>(new HashMap<>());
        }

        public DependencyGraphBuilder<T> add(T node) {
            if (!dependenciesMap.containsKey(node)) {
                dependenciesMap.put(node, new HashSet<>());
            }
            return this;
        }

        public DependencyGraphBuilder<T> addDependency(T dependent, T dependency) {
            getCurrentDependencies(dependent).add(dependency);
            // This step guaranties that all the nodes have an entry in the map
            return add(dependency);
        }

        public DependencyGraphBuilder<T> addDependencies(T dependent, Collection<T> dependencies) {
            getCurrentDependencies(dependent).addAll(dependencies);
            // This step guaranties that all the nodes have an entry in the map
            dependencies.forEach(this::add);
            return this;
        }

        public DependencyGraphBuilder<T> mergeGraph(DependencyGraph<T> theirGraph) {
            Map<T, Set<T>> ourDependenciesMap = dependenciesMap;
            for (Map.Entry<T, Set<T>> theirDependencyEntry : theirGraph.dependencies.entrySet()) {
                if (ourDependenciesMap.containsKey(theirDependencyEntry.getKey())) {
                    Set<T> ourCurrentDependencies = ourDependenciesMap.get(theirDependencyEntry.getKey());
                    ourCurrentDependencies.addAll(theirDependencyEntry.getValue());
                } else {
                    ourDependenciesMap.put(theirDependencyEntry.getKey(), theirDependencyEntry.getValue());
                }
            }
            return this;
        }

        public DependencyGraph<T> build() {
            return new DependencyGraph<>(dependenciesMap);
        }

        private Set<T> getCurrentDependencies(T dependent) {
            Set<T> currentDependencies;
            if (dependenciesMap.containsKey(dependent)) {
                currentDependencies = dependenciesMap.get(dependent);
            } else {
                currentDependencies = new HashSet<>();
                dependenciesMap.put(dependent, currentDependencies);
            }
            return currentDependencies;
        }
    }
}
