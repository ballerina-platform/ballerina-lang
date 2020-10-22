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

import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final CompilerContext.Key<GlobalVariableRefAnalyzer> REF_ANALYZER_KEY = new CompilerContext.Key<>();

    private final BLangDiagnosticLog dlog;
    private BLangPackage pkgNode;
    private Map<BSymbol, Set<BSymbol>> globalNodeDependsOn;
    private Map<BSymbol, Set<BVarSymbol>> globalVariablesDependsOn;
    private final Map<BSymbol, NodeInfo> dependencyNodes;
    private final Deque<NodeInfo> nodeInfoStack;
    private final List<List<NodeInfo>> cycles;
    private final List<NodeInfo> dependencyOrder;
    private int curNodeId;

    public static GlobalVariableRefAnalyzer getInstance(CompilerContext context) {
        GlobalVariableRefAnalyzer refAnalyzer = context.get(REF_ANALYZER_KEY);
        if (refAnalyzer == null) {
            refAnalyzer = new GlobalVariableRefAnalyzer(context);
        }
        return refAnalyzer;
    }

    private GlobalVariableRefAnalyzer(CompilerContext context) {
        context.put(REF_ANALYZER_KEY, this);
        this.dlog = BLangDiagnosticLog.getInstance(context);

        this.dependencyNodes = new HashMap<>();
        this.cycles = new ArrayList<>();
        this.nodeInfoStack = new ArrayDeque<>();
        this.dependencyOrder = new ArrayList<>();
        this.globalVariablesDependsOn = new HashMap<>();
    }

    /**
     * Populate the InvokableSymbols with the dependent global variables.
     * @param globalNodeDependsOn symbol dependency relationship.
     */
    public void populateFunctionDependencies(Map<BSymbol, Set<BSymbol>> globalNodeDependsOn) {
        resetAnalyzer();
        this.globalNodeDependsOn = globalNodeDependsOn;

        Set<BSymbol> dependentSet = this.globalNodeDependsOn.keySet();
        for (BSymbol dependent : dependentSet) {
            if (dependent.kind != SymbolKind.FUNCTION) {
                continue;
            }

            analyzeDependenciesRecursively(dependent);
        }
    }

    /**
     * Get the global variable dependency map.
     * @return global variable dependency map.
     */
    public Map<BSymbol, Set<BVarSymbol>> getGlobalVariablesDependsOn() {
        return this.globalVariablesDependsOn;
    }

    private void analyzeDependenciesRecursively(BSymbol dependent) {
        // Only analyze unvisited nodes.
        // Do DFS into dependency providers to detect cycles.
        if (!dependencyNodes.containsKey(dependent)) {
            NodeInfo node = new NodeInfo(curNodeId++, dependent);
            dependencyNodes.put(dependent, node);
            analyzeDependenciesRecursively(node);
        }
    }

    private Set<BVarSymbol> analyzeDependenciesRecursively(NodeInfo node) {
        if (node.onStack) {
            return getGlobalVarFromCurrentNode(node);
        }
        if (node.visited) {
            return getDependentsFromSymbol(node.symbol);
        }

        node.visited = true;

        node.onStack = true;

        Set<BSymbol> providers = this.globalNodeDependsOn.getOrDefault(node.symbol, new LinkedHashSet<>());

        // Means no dependencies for this node.
        if (providers.isEmpty()) {
            return new HashSet<>();
        }

        // Means the current node has dependencies. Lets analyze its dependencies further.
        Set<BVarSymbol> currentDependencies = new HashSet<>();
        for (BSymbol providerSym : providers) {
            NodeInfo providerNode =
                    this.dependencyNodes.computeIfAbsent(providerSym, s -> new NodeInfo(curNodeId++, providerSym));
            if (isGlobalVarSymbol(providerSym)) {
                currentDependencies.add((BVarSymbol) providerSym);
            }
            currentDependencies.addAll(analyzeDependenciesRecursively(providerNode));
        }

        node.onStack = false;

        Set<BVarSymbol> dependentGlobalVars;
        if (node.symbol.kind == SymbolKind.FUNCTION) {
            dependentGlobalVars = ((BInvokableSymbol) node.symbol).dependentGlobalVars;
        } else {
            dependentGlobalVars = this.globalVariablesDependsOn.computeIfAbsent(node.symbol, s -> new HashSet<>());
        }

        dependentGlobalVars.addAll(currentDependencies);
        return dependentGlobalVars;
    }

    private Set<BVarSymbol> getGlobalVarFromCurrentNode(NodeInfo node) {
        Set<BVarSymbol> globalVars = new HashSet<>();
        Set<BSymbol> providers = this.globalNodeDependsOn.getOrDefault(node.symbol, new LinkedHashSet<>());

        for (BSymbol provider : providers) {
            if (isGlobalVarSymbol(provider)) {
                globalVars.add((BVarSymbol) provider);
            }
        }

        return globalVars;
    }

    private Set<BVarSymbol> getDependentsFromSymbol(BSymbol symbol) {
        if (isFunction(symbol)) {
            return ((BInvokableSymbol) symbol).dependentGlobalVars;
        } else if (isGlobalVarSymbol(symbol)) {
            return this.globalVariablesDependsOn.getOrDefault(symbol, new HashSet<>());
        }

        return new HashSet<>();
    }

    private boolean isFunction(BSymbol symbol) {
        return (symbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION;
    }

    private boolean isGlobalVarSymbol(BSymbol symbol) {
        if (symbol == null) {
            return false;
        }
        if (symbol.owner == null) {
            return false;
        }
        if (symbol.owner.tag != SymTag.PACKAGE) {
            return false;
        }
        if ((symbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION) {
            return false;
        }

        return ((symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE);
    }

    /**
     * Analyze the global variable references and reorder them or emit error if they contain cyclic references.
     *
     * @param pkgNode package to be analyzed.
     * @param globalNodeDependsOn symbol dependency relationship.
     */
    public void analyzeAndReOrder(BLangPackage pkgNode, Map<BSymbol, Set<BSymbol>> globalNodeDependsOn) {
        this.pkgNode = pkgNode;
        this.globalNodeDependsOn = globalNodeDependsOn;
        resetAnalyzer();

        List<BSymbol> globalVarsAndDependentFuncs = getGlobalVariablesAndDependentFunctions();

        pruneDependencyRelations();

        Set<BSymbol> sorted = new LinkedHashSet<>();
        LinkedList<BSymbol> dependencies = new LinkedList<>();

        for (BSymbol symbol : globalVarsAndDependentFuncs) {
            List<BSymbol> dependencyTrain = analyzeDependenciesStartingFrom(symbol);
            dependencies.addAll(dependencyTrain);

            Set<BSymbol> symbolsProviders = globalNodeDependsOn.get(symbol);
            boolean symbolHasProviders = symbolsProviders != null && !symbolsProviders.isEmpty();
            boolean notInSortedList = !sorted.contains(symbol);

            // Independent variable declaration, add to sorted list.
            if (notInSortedList && !symbolHasProviders) {
                moveAndAppendToSortedList(symbol, dependencies, sorted);
            }
            // Dependent variable, and all the dependencies are satisfied, add to sorted list.
            if (notInSortedList && symbolHasProviders && sorted.containsAll(symbolsProviders)) {
                moveAndAppendToSortedList(symbol, dependencies, sorted);
            }

            // If we can satisfy the dependencies' dependencies then we can add those dependencies to sorted list now.
            addDependenciesDependencies(dependencies, sorted);
        }
        sorted.addAll(dependencies);

        if (cycles.stream().anyMatch(cycle -> cycle.size() > 1)) {
            // Cyclic error found no need to sort.
            return;
        }

        sortConstants(sorted);
        projectSortToGlobalVarsList(sorted);
        projectSortToTopLevelNodesList();
    }

    private List<BSymbol> analyzeDependenciesStartingFrom(BSymbol symbol) {
        // Only analyze unvisited nodes.
        // Do DFS into dependency providers to detect cycles.
        if (!dependencyNodes.containsKey(symbol)) {
            NodeInfo node = new NodeInfo(curNodeId++, symbol);
            dependencyNodes.put(symbol, node);
            analyzeProvidersRecursively(node);
        }

        // Extract all the dependencies found in last call to analyzeProvidersRecursively
        if (!dependencyOrder.isEmpty()) {
            List<BSymbol> symbolsProvidersOrdered = this.dependencyOrder.stream()
                    .map(nodeInfo -> nodeInfo.symbol)
                    .collect(Collectors.toList());
            this.dependencyOrder.clear();
            return symbolsProvidersOrdered;
        }
        return new ArrayList<>();
    }

    private void resetAnalyzer() {
        this.dependencyNodes.clear();
        this.cycles.clear();
        this.nodeInfoStack.clear();
        this.dependencyOrder.clear();
        this.curNodeId = 0;
        this.globalVariablesDependsOn = new HashMap<>();
    }

    private void pruneDependencyRelations() {
        List<BSymbol> dependents = new ArrayList<>(this.globalNodeDependsOn.keySet());
        Set<BSymbol> visited = new HashSet<>();
        for (BSymbol dependent : dependents) {
            // Taking a copy as we need to modify the original list.
            List<BSymbol> providers = new ArrayList<>(this.globalNodeDependsOn.get(dependent));
            for (BSymbol provider : providers) {
                pruneFunctions(dependent, provider, this.globalNodeDependsOn, visited);
            }
        }
    }

    private void pruneFunctions(BSymbol dependent, BSymbol provider, Map<BSymbol, Set<BSymbol>> globalNodeDependsOn,
                                Set<BSymbol> visited) {
        if (visited.contains(provider)) {
            return;
        } else {
            visited.add(provider);
        }

        // Dependent has a dependency on a global var.
        if (provider.kind != SymbolKind.FUNCTION) {
            return;
        }

        // Provider is a function.
        // And doesn't have dependency on a global variable. We can prune provider.
        if (!globalNodeDependsOn.containsKey(provider) || globalNodeDependsOn.get(provider).isEmpty()) {
            globalNodeDependsOn.get(dependent).remove(provider);
            return;
        }

        // Taking a copy as we need to modify the original list.
        List<BSymbol> providersProviders = new ArrayList<>(globalNodeDependsOn.get(provider));
        for (BSymbol prov : providersProviders) {
            pruneFunctions(provider, prov, globalNodeDependsOn, visited);
        }
    }

    private void addDependenciesDependencies(LinkedList<BSymbol> dependencies, Set<BSymbol> sorted) {
        // For each dependency if they satisfy their dependencies in sorted list, then add them to sorted list.
        ArrayList<BSymbol> depCopy = new ArrayList<>(dependencies);
        for (BSymbol dep : depCopy) {
            Set<BSymbol> depsDependencies = globalNodeDependsOn.getOrDefault(dep, new LinkedHashSet<>());
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

        topLevelPositions = new ArrayList<>();
        for (BLangConstant constant : pkgNode.constants) {
            topLevelPositions.add(pkgNode.topLevelNodes.indexOf(constant));
        }
        topLevelPositions.sort(Comparator.comparingInt(i -> i));
        for (int i = 0; i < topLevelPositions.size(); i++) {
            Integer targetIndex = topLevelPositions.get(i);
            pkgNode.topLevelNodes.set(targetIndex, pkgNode.constants.get(i));
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

    private void sortConstants(Set<BSymbol> sorted) {
        Map<BSymbol, BLangConstant> varMap = this.pkgNode.constants.stream()
                .collect(Collectors.toMap(k -> k.symbol, k -> k));

        List<BLangConstant> sortedConstants = sorted.stream()
                .filter(varMap::containsKey)
                .map(varMap::get)
                .collect(Collectors.toList());

        if (sortedConstants.size() != this.pkgNode.constants.size()) {
            List<BLangConstant> symbolLessGlobalVars = this.pkgNode.constants.stream()
                    .filter(c -> !sortedConstants.contains(c))
                    .collect(Collectors.toList());
            sortedConstants.addAll(symbolLessGlobalVars);
        }
        this.pkgNode.constants.clear();
        this.pkgNode.constants.addAll(sortedConstants);
    }
    
    private List<BSymbol> getGlobalVariablesAndDependentFunctions() {
        List<BSymbol> dependents = new ArrayList<>();

        for (BSymbol s : globalNodeDependsOn.keySet()) {
            if ((s.tag & SymTag.FUNCTION) == SymTag.FUNCTION) {
                dependents.add(s);
            }
        }

        for (BLangSimpleVariable var : this.pkgNode.globalVars) {
            if (var.symbol != null) {
                dependents.add(var.symbol);
            }
        }

        for (BLangConstant constant : this.pkgNode.constants) {
            if (constant.symbol != null) {
                dependents.add(constant.symbol);
            }
        }

        return dependents;
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

        Set<BSymbol> providers = globalNodeDependsOn.getOrDefault(node.symbol, new LinkedHashSet<>());
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
            handleCyclicReferenceError(node);
        }
        dependencyOrder.add(node);
        return node.lowLink;
    }

    private void handleCyclicReferenceError(NodeInfo node) {
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
                emitErrorMessage(symbolsOfCycle);
            }
        }
    }

    private void emitErrorMessage(List<BSymbol> symbolsOfCycle) {
        Optional<BSymbol> firstGlobalVar = symbolsOfCycle.stream().filter(s -> s.kind != SymbolKind.FUNCTION).findAny();
        if (!firstGlobalVar.isPresent()) {
            return;
        }

        BSymbol firstNodeSymbol = firstGlobalVar.get();
        Optional<BLangNode> firstNode = pkgNode.topLevelNodes.stream()
                .filter(t -> (t.getKind() == NodeKind.VARIABLE || t.getKind() == NodeKind.FUNCTION)
                        && getSymbol(t) == firstNodeSymbol)
                .map(t -> (BLangNode) t)
                .findAny();

        int splitFrom = symbolsOfCycle.indexOf(firstNodeSymbol);
        int len = symbolsOfCycle.size();
        List<BSymbol> firstSubList = new ArrayList<>(symbolsOfCycle.subList(0, splitFrom));
        List<BSymbol> secondSubList = new ArrayList<>(symbolsOfCycle.subList(splitFrom, len));
        secondSubList.addAll(firstSubList);

        if (firstNode.isPresent()) {
            List<BLangIdentifier> names = secondSubList.stream().map(this::getNodeName).collect(Collectors.toList());
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
            if (getSymbol(node) == symbol) {
                if (node.getKind() == NodeKind.VARIABLE) {
                    return ((BLangSimpleVariable) node).name;
                } else if (node.getKind() == NodeKind.FUNCTION) {
                    return ((BLangFunction) node).name;
                } else if (node.getKind() == NodeKind.TYPE_DEFINITION) {
                    if (((BLangTypeDefinition) node).typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                        return ((BLangTypeDefinition) node).name;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Cannot find topLevelNode: " + symbol);
    }

    private BSymbol getSymbol(Node node) {
        if (node.getKind() == NodeKind.VARIABLE) {
            return ((BLangVariable) node).symbol;
        } else if (node.getKind() == NodeKind.FUNCTION) {
            return ((BLangFunction) node).symbol;
        } else if (node.getKind() == NodeKind.TYPE_DEFINITION) {
            if (((BLangTypeDefinition) node).typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                return ((BLangObjectTypeNode) ((BLangTypeDefinition) node).typeNode).symbol;
            }
        }
        return null;
    }

    private static class NodeInfo {
        final int id;
        final BSymbol symbol;
        int lowLink;
        boolean visited;
        boolean onStack;

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
