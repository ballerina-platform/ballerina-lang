/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer.cyclefind;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Analyze global variable reference patterns and reorder them in reverse dependency order.
 */
public class GlobalVariableRefAnalyzer {
    private final BLangDiagnosticLog dlog;
    private final BLangPackage pkgNode;
    private final Map<BSymbol, List<BSymbol>> globalNodeDependsOn;
    private final Map<BSymbol, NodeInfo> dependencyNodes;
    private final Deque<NodeInfo> nodeInfoStack;
    private final List<List<NodeInfo>> cycles;
    private final List<NodeInfo> dependencyOrder;
    private int curNodeId;

    public GlobalVariableRefAnalyzer(BLangPackage pkgNode,
                                     BLangDiagnosticLog dlog,
                                     Map<BSymbol, List<BSymbol>> globalNodeDependsOn) {
        this.pkgNode = pkgNode;
        this.dlog = dlog;
        this.globalNodeDependsOn = globalNodeDependsOn;
        this.dependencyNodes = new HashMap<>();
        this.cycles = new ArrayList<>();
        this.nodeInfoStack = new ArrayDeque<>();
        this.dependencyOrder = new ArrayList<>();
    }

    /**
     * Analyze the global variable references and reorder them or emit error if they contain cyclic references.
     */
    public void analyzeAndReOrder() {
        List<BSymbol> globalVarsAndDependentFuncs = getGlobalVariablesAndDependentFunctions();

        Set<BSymbol> sorted = new LinkedHashSet<>();
        LinkedList<BSymbol> dependencies = new LinkedList<>();

        for (BSymbol symbol : globalVarsAndDependentFuncs) {
            // Only analyze unvisited nodes.
            // Do DFS into dependency providers to detect cycles.
            if (!dependencyNodes.containsKey(symbol)) {
                NodeInfo node = new NodeInfo(curNodeId++, symbol);
                dependencyNodes.put(symbol, node);
                analyzeProvidersRecursively(node);
            }
            // Extract all the dependencies found in last call to analyzeProvidersRecursively
            if (!dependencyOrder.isEmpty()) {
                List<BSymbol> symbolsProvidersOrdered = dependencyOrder.stream()
                        .map(n -> n.symbol)
                        .collect(Collectors.toList());
                dependencies.addAll(symbolsProvidersOrdered);
                dependencyOrder.clear();
            }

            List<BSymbol> symbolsProviders = globalNodeDependsOn.get(symbol);
            boolean symbolHasProviders = symbolsProviders != null && !symbolsProviders.isEmpty();

            // Independent variable declaration, add to sorted list.
            if (!symbolHasProviders && (!sorted.contains(symbol))) {
                moveAndAppendToSortedList(symbol, dependencies, sorted);
            }
            // Dependent variable, and all the dependencies are satisfied, add to sorted list.
            if (symbolHasProviders && sorted.containsAll(symbolsProviders) && !sorted.contains(symbol)) {
                moveAndAppendToSortedList(symbol, dependencies, sorted);
            }

            // If we can satisfy the dependencies' dependencies then we can add those dependencies to sorted list now.
            addDependenciesDependencies(dependencies, sorted);
        }
        sorted.addAll(dependencies);

        if (cycles.stream().anyMatch(c -> c.size() > 1)) {
            // Cyclic error found no need to sort.
            return;
        }

        projectSortToGlobalVarsList(sorted);
        projectSortToTopLevelNodesList();

    }

    private void addDependenciesDependencies(LinkedList<BSymbol> dependencies, Set<BSymbol> sorted) {
        // For each dependency if they satisfy their dependencies in sorted list, then add them to sorted list.
        ArrayList<BSymbol> depCopy = new ArrayList<>(dependencies);
        for (BSymbol dep : depCopy) {
            List<BSymbol> depsDependencies = globalNodeDependsOn.getOrDefault(dep, new ArrayList<>());
            if (!depsDependencies.isEmpty() && sorted.containsAll(depsDependencies)) {
                moveAndAppendToSortedList(dep, dependencies, sorted);
            }
        }
    }

    private void projectSortToTopLevelNodesList() {
        // Swap global variable nodes in 'topLevelNodes' list to reflect sorted global variables.
        List<Integer> topLevelPositions = new ArrayList<>();
        for (BLangSimpleVariable globalVar : pkgNode.globalVars) {
            topLevelPositions.add(pkgNode.topLevelNodes.indexOf(globalVar));
        }
        topLevelPositions.sort(Comparator.comparingInt(i -> i));
        for (int i = 0; i < topLevelPositions.size(); i++) {
            Integer targetIndex = topLevelPositions.get(i);
            pkgNode.topLevelNodes.set(targetIndex, pkgNode.globalVars.get(i));
        }
    }

    private void projectSortToGlobalVarsList(Set<BSymbol> sorted) {
        Map<BSymbol, BLangSimpleVariable> varMap = this.pkgNode.globalVars.stream()
                .collect(Collectors.toMap(k -> k.symbol, k -> k));

        List<BLangSimpleVariable> sortedGlobalVars = sorted.stream()
                .filter(varMap::containsKey)
                .map(varMap::get)
                .collect(Collectors.toList());

        if (sortedGlobalVars.size() != this.pkgNode.globalVars.size()) {
            List<BLangSimpleVariable> symbolLessGlobalVars = this.pkgNode.globalVars.stream()
                    .filter(g -> g.symbol == null)
                    .collect(Collectors.toList());
            sortedGlobalVars.addAll(symbolLessGlobalVars);
        }
        this.pkgNode.globalVars.clear();
        this.pkgNode.globalVars.addAll(sortedGlobalVars);
    }

    private List<BSymbol> getGlobalVariablesAndDependentFunctions() {
        List<BSymbol> globalVarsAndDependentFuncs = pkgNode.globalVars.stream()
                .filter(v -> v.symbol != null)
                .map(v -> v.symbol)
                .collect(Collectors.toCollection(ArrayList::new));

        for (BSymbol symbol : this.globalNodeDependsOn.keySet()) {
            if (!globalVarsAndDependentFuncs.contains(symbol)) {
                globalVarsAndDependentFuncs.add(symbol);
            }
        }
        return globalVarsAndDependentFuncs;
    }

    private void moveAndAppendToSortedList(BSymbol symbol, List<BSymbol> moveFrom, Set<BSymbol> sorted) {
        sorted.add(symbol);
        moveFrom.remove(symbol);
    }

    private int analyzeProvidersRecursively(NodeInfo node) {
        if (node.visited) {
            return node.lowLink;
        }

        node.visited = true;
        node.lowLink = node.id;
        node.onStack = true;
        nodeInfoStack.push(node);

        List<BSymbol> providers = globalNodeDependsOn.getOrDefault(node.symbol, new ArrayList<>());
        for (BSymbol providerSym : providers) {
            NodeInfo providerNode =
                    dependencyNodes.computeIfAbsent(providerSym, s -> new NodeInfo(curNodeId++, providerSym));
            int lastLowLink = analyzeProvidersRecursively(providerNode);
            if (providerNode.onStack) {
                node.lowLink = Math.min(node.lowLink, lastLowLink);
            }
        }
        // Cycle detected.
        if (node.id == node.lowLink) {
            List<NodeInfo> cycle = new ArrayList<>();

            while (!nodeInfoStack.isEmpty()) {
                NodeInfo cNode = nodeInfoStack.pop();
                cNode.onStack = false;
                cNode.lowLink = node.id;
                cycle.add(cNode);
                if (cNode.id == node.id) {
                    break;
                }
            }
            cycles.add(cycle);
            if (cycle.size() > 1) {
                cycle = new ArrayList<>(cycle);
                Collections.reverse(cycle);
                List<BSymbol> symbolsOfCycle = cycle.stream()
                        .map(n -> n.symbol)
                        .collect(Collectors.toList());

                if (doesContainAGlobalVar(symbolsOfCycle)) {
                    emitError(symbolsOfCycle);
                }
            }
        }
        dependencyOrder.add(node);
        return node.lowLink;
    }

    private void emitError(List<BSymbol> symbolsOfCycle) {

        BSymbol firstNodeSymbol = symbolsOfCycle.get(0);
        Optional<BLangNode> firstNode = pkgNode.topLevelNodes.stream()
                .filter(t -> t.getKind() == NodeKind.VARIABLE || t.getKind() == NodeKind.FUNCTION)
                .map(t -> (BLangNode) t)
                .filter(n -> getVarOrFuncSymbol(n) == firstNodeSymbol)
                .findAny();

        if (firstNode.isPresent()) {
            List<BLangIdentifier> names = symbolsOfCycle.stream().map(this::getNodeName).collect(Collectors.toList());
            dlog.error(firstNode.get().pos, DiagnosticCode.GLOBAL_VARIABLE_CYCLIC_DEFINITION, names);
        }
    }

    private boolean doesContainAGlobalVar(List<BSymbol> symbolsOfCycle) {
        return pkgNode.globalVars.stream()
                .map(v -> v.symbol)
                .anyMatch(symbolsOfCycle::contains);
    }

    private BLangIdentifier getNodeName(BSymbol symbol) {
        for (TopLevelNode node : pkgNode.topLevelNodes) {
            if (getVarOrFuncSymbol(node) == symbol) {
                if (node.getKind() == NodeKind.VARIABLE) {
                    return ((BLangSimpleVariable) node).name;
                } else if (node.getKind() == NodeKind.FUNCTION) {
                    return ((BLangFunction) node).name;
                }
            }
        }
        throw new IllegalArgumentException("Can not find topLevelNode: " + symbol);
    }

    private BSymbol getVarOrFuncSymbol(Node node) {
        if (node.getKind() == NodeKind.VARIABLE) {
            return ((BLangVariable) node).symbol;
        } else if (node.getKind() == NodeKind.FUNCTION) {
            return ((BLangFunction) node).symbol;
        }
        return null;
    }

    private static class NodeInfo {
        final int id;
        int lowLink;
        boolean visited;
        boolean onStack;
        BSymbol symbol;

        NodeInfo(int id, BSymbol symbol) {
            this.id = id;
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return "NodeInfo{" +
                    "id=" + id +
                    ", lowLink=" + lowLink +
                    ", visited=" + visited +
                    ", onStack=" + onStack +
                    ", symbol=" + symbol +
                    '}';
        }
    }
}
