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
package io.ballerina.build;

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

    public DependencyGraph(Map<T, Set<T>> dependencies) {
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

    public Collection<T> getDirectDependents(T node) {
        throw new UnsupportedOperationException();
    }

    public Collection<T> getDirectDependencies(T node) {
        return dependencies.get(node);
    }

    public List<T> toTopologicallySortedList() {
        if (topologicallySortedNodes != null) {
            return topologicallySortedNodes;
        }

        List<T> sorted = new ArrayList<>();
        sortTopologically(dependencies.keySet(), new HashSet<>(), sorted);
        topologicallySortedNodes = Collections.unmodifiableList(sorted);
        return topologicallySortedNodes;
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
}
