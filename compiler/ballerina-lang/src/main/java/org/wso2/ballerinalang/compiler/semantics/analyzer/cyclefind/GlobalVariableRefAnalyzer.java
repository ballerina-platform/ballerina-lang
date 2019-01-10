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
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyze global variable reference patterns and reorder them in reverse dependency order.
 */
public class GlobalVariableRefAnalyzer {
    private final BLangDiagnosticLog dlog;
    private final BLangPackage pkgNode;
    private final Map<BSymbol, List<BSymbol>> globalNodeDependsOn;

    public GlobalVariableRefAnalyzer(BLangPackage pkgNode,
                                     BLangDiagnosticLog dlog,
                                     Map<BSymbol, List<BSymbol>> globalNodeDependsOn) {
        this.pkgNode = pkgNode;
        this.dlog = dlog;
        this.globalNodeDependsOn = globalNodeDependsOn;
    }

    /**
     * Analyze the global variable references and reorder them or emit error if they contain cyclic references.
     */
    public void analyzeAndReOrder() {
        Map<BSymbol, NodeIdPair> nodeIndexes = new HashMap<>();
        TarjanSccSolverAdjacencyList graph = populateGraph(this.globalNodeDependsOn, nodeIndexes);

        Map<Integer, BSymbol> nodeIdToNodeSymbol = nodeIndexes.entrySet().stream()
                .collect(Collectors.toMap(v -> v.getValue().nodeId, v -> v.getKey()));

        Map<Integer, BLangNode> nodeIdToNode = nodeIndexes.values().stream()
                .collect(Collectors.toMap(v -> v.nodeId, v -> v.node));

        Map<Integer, List<Integer>> cyclesInGraph = graph.getSCCs();
        // If cyclic references are found, we can't reorder, exit with error.
        if (findCyclicDependencies(cyclesInGraph, pkgNode, nodeIdToNodeSymbol, nodeIdToNode)) {
            return;
        }

        sortNodeLists(graph, nodeIndexes, nodeIdToNodeSymbol);
    }

    private TarjanSccSolverAdjacencyList populateGraph(Map<BSymbol, List<BSymbol>> globalNodeDependsOn,
                                                       Map<BSymbol, NodeIdPair> nodeIndexes) {
        List<BLangNode> topLevelFuncsAndVars = pkgNode.topLevelNodes.stream()
                .filter(n -> n.getKind() == NodeKind.FUNCTION || n.getKind() == NodeKind.VARIABLE)
                .map(n -> (BLangNode) n).collect(Collectors.toList());

        int currNode = 0;
        for (BLangNode node : topLevelFuncsAndVars) {
            BSymbol symbol = getSymbol(node);
            if (!nodeIndexes.containsKey(symbol)) {
                nodeIndexes.put(symbol, new NodeIdPair(currNode++, node));
            }
        }

        TarjanSccSolverAdjacencyList graph = TarjanSccSolverAdjacencyList.createGraph(nodeIndexes.size());
        for (BLangNode node : topLevelFuncsAndVars) {
            BSymbol symbol = getSymbol(node);

            int dependentNodeIndex = nodeIndexes.get(symbol).nodeId;

            List<BSymbol> providers = globalNodeDependsOn.get(symbol);
            if (providers == null) {
                continue;
            }
            for (BSymbol providerSymbol : providers) {
                Integer providerNodeIndex = nodeIndexes.get(providerSymbol).nodeId;
                graph.addEdge(dependentNodeIndex, providerNodeIndex);
            }
        }

        return graph;
    }

    private BSymbol getSymbol(Node node) {
        if (node.getKind() == NodeKind.VARIABLE) {
            return ((BLangVariable) node).symbol;
        } else {
            return ((BLangInvokableNode) node).symbol;
        }
    }

    private void sortNodeLists(TarjanSccSolverAdjacencyList graph,
                               Map<BSymbol, NodeIdPair> nodeIndexes,
                               Map<Integer, BSymbol> nodeIdToNodeSymbol) {
        // Sort global variable definitions.
        // Tarjan's algorithm as a by product topologically sorts the graph.
        List<Integer> dependencyOrderFiltered = graph.getDependencyOrderFiltered();
        List<Integer> sortedNodeIdList = dependencyOrderFiltered.stream()
                .filter(i -> pkgNode.globalVars.contains(getVarNodeFromGraphIndex(nodeIndexes, nodeIdToNodeSymbol, i)))
                .collect(Collectors.toList());

        List<Integer> sortableGlobalVarPositions = new ArrayList<>(sortedNodeIdList);
        Collections.sort(sortableGlobalVarPositions);

        List<BLangSimpleVariable> sorted = new ArrayList<>(pkgNode.globalVars);
        for (int i = 0; i < sortedNodeIdList.size(); i++) {
            Integer targetIndex = sortableGlobalVarPositions.get(i);
            BLangNode targetNode = getVarNodeFromGraphIndex(nodeIndexes, nodeIdToNodeSymbol, targetIndex);
            int destinationIndex = pkgNode.globalVars.indexOf(targetNode);

            Integer index = sortedNodeIdList.get(i);
            BLangSimpleVariable varNode = getVarNodeFromGraphIndex(nodeIndexes, nodeIdToNodeSymbol, index);
            sorted.set(destinationIndex, varNode);
        }
        pkgNode.globalVars.clear();
        pkgNode.globalVars.addAll(sorted);

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

    private boolean findCyclicDependencies(Map<Integer, List<Integer>> cycles, BLangPackage bLangPackage,
                                           Map<Integer, BSymbol> graphIndices, Map<Integer, BLangNode> nodeIdToNode) {

        Map<BSymbol, IdentifierPositionPair> symbolToIdentifier = mapSymbolToNamePositionPair(bLangPackage);
        boolean cyclicDepFound = false;
        for (List<Integer> cyclicNodes : cycles.values()) {
            // Ignore cycles formed by mutually recursive function calls.
            boolean noGlobalVarNodeInCycle = cyclicNodes.stream()
                            .map(nodeIdToNode::get)
                            .filter(n -> n.getKind() == NodeKind.VARIABLE)
                            .map(n -> (BLangSimpleVariable) n)
                            .noneMatch(n -> pkgNode.globalVars.contains(n));
            if (cyclicNodes.size() <= 1 || noGlobalVarNodeInCycle) {
                continue;
            }

            cyclicDepFound = true;
            emitCyclicError(graphIndices, symbolToIdentifier, cyclicNodes);
        }

        return cyclicDepFound;
    }

    private void emitCyclicError(Map<Integer, BSymbol> graphIndices,
                                 Map<BSymbol, IdentifierPositionPair> symbolToIdentifier, List<Integer> cyclicNodes) {
        List<BLangIdentifier> cycle = cyclicNodes.stream()
                .map(graphIndices::get)
                .map(symbol -> symbolToIdentifier.get(symbol).identifier)
                .collect(Collectors.toList());

        Integer firstOccurrence = cyclicNodes.get(0);
        BSymbol firstOccurrenceSymbol = graphIndices.get(firstOccurrence);
        DiagnosticPos position = symbolToIdentifier.get(firstOccurrenceSymbol).position;

        dlog.error(position, DiagnosticCode.GLOBAL_VARIABLE_CYCLIC_DEFINITION, cycle);
    }

    private Map<BSymbol, IdentifierPositionPair> mapSymbolToNamePositionPair(BLangPackage bLangPackage) {
        // Map symbols to functions and variables from topLevelNodes list.
        Map<BSymbol, IdentifierPositionPair> symbolToIdentifier = new HashMap<>();
        bLangPackage.topLevelNodes.stream()
                .filter(n -> n.getKind() == NodeKind.FUNCTION)
                .map(n -> (BLangFunction) n)
                .forEach(f -> symbolToIdentifier.put(f.symbol,
                        new IdentifierPositionPair(f.getName(), f.getPosition())));

        bLangPackage.topLevelNodes.stream()
                .filter(n -> n.getKind() == NodeKind.VARIABLE)
                .map(n -> (BLangSimpleVariable) n)
                .forEach(f -> symbolToIdentifier.put(f.symbol,
                        new IdentifierPositionPair(f.getName(), f.getPosition())));
        return symbolToIdentifier;
    }

    private BLangSimpleVariable getVarNodeFromGraphIndex(Map<BSymbol, NodeIdPair> graphIndices,
                                               Map<Integer, BSymbol> nodeIdToNodeSymbol, Integer index) {

        BLangNode node = graphIndices.get(nodeIdToNodeSymbol.get(index)).node;
        if (node.getKind() != NodeKind.VARIABLE) {
            return null;
        }
        return (BLangSimpleVariable) node;
    }

    private static class NodeIdPair {
        final Integer nodeId;
        final BLangNode node;

        NodeIdPair(Integer nodeId, BLangNode node) {
            this.nodeId = nodeId;
            this.node = node;
        }
    }

    private static class IdentifierPositionPair {
        final BLangIdentifier identifier;
        final DiagnosticPos position;

        IdentifierPositionPair(BLangIdentifier identifier, DiagnosticPos position) {
            this.identifier = identifier;
            this.position = position;
        }
    }
}
