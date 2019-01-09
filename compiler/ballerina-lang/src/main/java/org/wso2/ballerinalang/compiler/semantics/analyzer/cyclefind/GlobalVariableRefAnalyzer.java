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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.DataflowAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
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
    private final Map<BSymbol, DataflowAnalyzer.DefPosition> globalVarSymbolDefPositions;
    private final Map<BSymbol, List<DataflowAnalyzer.RefPosition>> globalVarSymbolRefPositions;
    private final Map<BSymbol, List<BSymbol>> funcDependsOnGlobalVars;
    private final Map<BSymbol, List<BSymbol>> funcDependsOnFunc;
    private final Map<BSymbol, List<BSymbol>> funcToDependentGlobalVar;

    public GlobalVariableRefAnalyzer(BLangPackage pkgNode,
                                     Map<BSymbol, DataflowAnalyzer.DefPosition> globalVarSymbolDefPositions,
                                     Map<BSymbol, List<DataflowAnalyzer.RefPosition>> globalVarSymbolRefPositions,
                                     Map<BSymbol, List<BSymbol>> funcDependsOnGlobalVars,
                                     Map<BSymbol, List<BSymbol>> funcDependsOnFunc,
                                     Map<BSymbol, List<BSymbol>> funcToDependentGlobalVar,
                                     BLangDiagnosticLog dlog) {
        this.pkgNode = pkgNode;
        this.globalVarSymbolDefPositions = globalVarSymbolDefPositions;
        this.globalVarSymbolRefPositions = globalVarSymbolRefPositions;
        this.funcDependsOnGlobalVars = funcDependsOnGlobalVars;
        this.funcDependsOnFunc = funcDependsOnFunc;
        this.funcToDependentGlobalVar = funcToDependentGlobalVar;
        this.dlog = dlog;
    }

    /**
     * Analyze the global variable references and reorder them or emit error if they contain cyclic references.
     */
    public void analyzeAndReOrder() {
        int nodeCount = getNodeCount();

        TarjanSccSolverAdjacencyList graph = TarjanSccSolverAdjacencyList.createGraph(nodeCount);
        Map<BSymbol, NodeIdPair> nodeIndexes = new HashMap<>();

        int currNode = addGlobalVariableDependsOnGlobalVariable(graph, nodeIndexes);

        for (TopLevelNode node : pkgNode.topLevelNodes) {
            if (node.getKind() == NodeKind.FUNCTION) {
                BLangFunction func = (BLangFunction) node;
                BInvokableSymbol funcSymbol = func.symbol;

                if (!nodeIndexes.containsKey(funcSymbol)) {
                    nodeIndexes.put(funcSymbol, new NodeIdPair(currNode++, func));
                }
                int funcNodeIndex = nodeIndexes.get(funcSymbol).nodeId;

                currNode = addFunctionsDependOnGlobalVar(graph, nodeIndexes, currNode, func, funcSymbol, funcNodeIndex);
                currNode = addGlobalVarDependOnFunction(graph, nodeIndexes, currNode, func, funcSymbol, funcNodeIndex);
                currNode = addFunctionDependOnFunctions(graph, nodeIndexes, currNode, func, funcSymbol, funcNodeIndex);
            }
        }


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

    private void sortNodeLists(TarjanSccSolverAdjacencyList graph,
                               Map<BSymbol, NodeIdPair> nodeIndexes,
                               Map<Integer, BSymbol> nodeIdToNodeSymbol) {
        // Sort global variable definitions.
        // Tarjan's algorithm as a by product topologically sorts the graph.

        List<Integer> dependencyOrderFiltered = graph.getDependencyOrderFiltered();
        List<Integer> globalVarPositions = dependencyOrderFiltered
                .stream()
                .filter(i -> pkgNode.globalVars.contains(getNodeFromGraphIndex(nodeIndexes, nodeIdToNodeSymbol, i)))
                .collect(Collectors.toList());

        List<Integer> sortedPos = new ArrayList<>(globalVarPositions);
        Collections.sort(sortedPos);

        List<BLangSimpleVariable> sorted = new ArrayList<>(pkgNode.globalVars);
        for (int i = 0; i < globalVarPositions.size(); i++) {
            Integer index = globalVarPositions.get(i);
            Integer destinationPos = sortedPos.get(i);
            BLangNode bLangNode = getNodeFromGraphIndex(nodeIndexes, nodeIdToNodeSymbol, index);
            sorted.set(destinationPos, (BLangSimpleVariable) bLangNode);
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
            pkgNode.topLevelNodes.set(topLevelPositions.get(i), pkgNode.globalVars.get(i));
        }
    }

    private int addFunctionDependOnFunctions(TarjanSccSolverAdjacencyList graph, Map<BSymbol, NodeIdPair> graphIndices,
                                             int curNodeId, BLangFunction func, BInvokableSymbol funcSymbol,
                                             int topLevelNodeIndex) {
        // func a = b() + c(), here b, c are producers and 'a' depends on them.
        List<BSymbol> producers = funcDependsOnFunc.getOrDefault(funcSymbol, new ArrayList<>());
        for (BSymbol producer : producers) {
            if (!graphIndices.containsKey(producer)) {
                graphIndices.put(producer, new NodeIdPair(curNodeId++, func));
            }

            graph.addEdge(topLevelNodeIndex, graphIndices.get(producer).nodeId);
        }
        return curNodeId;
    }

    private int addGlobalVarDependOnFunction(TarjanSccSolverAdjacencyList graph, Map<BSymbol, NodeIdPair> graphIndices,
                                             int curNodeId, BLangFunction func, BInvokableSymbol symbol,
                                             int topLevelNodeIndex) {
        // int globalVar = theFunc(), globalVar depends on 'theFunc'.
        List<BSymbol> dependentGlobalVars = funcToDependentGlobalVar.getOrDefault(symbol, new ArrayList<>());
        for (BSymbol dependent : dependentGlobalVars) {
            if (!graphIndices.containsKey(dependent)) {
                graphIndices.put(dependent, new NodeIdPair(curNodeId++, func));
            }

            graph.addEdge(graphIndices.get(dependent).nodeId, topLevelNodeIndex);
        }
        return curNodeId;
    }

    private int addFunctionsDependOnGlobalVar(TarjanSccSolverAdjacencyList graph, Map<BSymbol, NodeIdPair> graphIndices,
                                              int nodeId, BLangFunction func, BInvokableSymbol symbol,
                                              int topLevelNodeIndex) {
        // func foo contain global variable, hence foo depends on global variable.
        List<BSymbol> providers = funcDependsOnGlobalVars.getOrDefault(symbol, new ArrayList<>());
        for (BSymbol provider : providers) {
            if (!graphIndices.containsKey(provider)) {
                graphIndices.put(provider, new NodeIdPair(nodeId++, func));
            }

            graph.addEdge(topLevelNodeIndex, graphIndices.get(provider).nodeId);
        }
        return nodeId;
    }

    private int addGlobalVariableDependsOnGlobalVariable(TarjanSccSolverAdjacencyList graph,
                                                         Map<BSymbol, NodeIdPair> graphIndices) {
        // int foo = bar, global variable foo depends on global variable bar.
        for (BLangSimpleVariable globalVar : pkgNode.globalVars) {
            BSymbol symbol = globalVar.symbol;
            DataflowAnalyzer.DefPosition defPosition = globalVarSymbolDefPositions.get(symbol);
            graphIndices.put(symbol, new NodeIdPair(defPosition.refId, globalVar));
            List<DataflowAnalyzer.RefPosition> accessedSequence = globalVarSymbolRefPositions.get(symbol);

            for (DataflowAnalyzer.RefPosition refPosition : accessedSequence) {
                DataflowAnalyzer.DefPosition dependentDefPosition =
                        globalVarSymbolDefPositions.get(refPosition.dependentSymbol);
                graph.addEdge(dependentDefPosition.refId, defPosition.refId);
            }
        }
        return pkgNode.globalVars.size();
    }

    private int getNodeCount() {
        int functionCount = (int) pkgNode.topLevelNodes.stream().filter(n -> n.getKind() == NodeKind.FUNCTION).count();
        return pkgNode.globalVars.size() + functionCount;
    }

    private boolean findCyclicDependencies(Map<Integer, List<Integer>> multimap, BLangPackage bLangPackage,
                                           Map<Integer, BSymbol> graphIndices, Map<Integer, BLangNode> nodeIdToNode) {

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



        boolean cyclicDepFound = false;
        for (List<Integer> cyclicNodes : multimap.values()) {
            boolean atLeastOneNodeIsGlobalVar = cyclicNodes
                    .stream()
                    .map(n -> nodeIdToNode.get(n))
                    .noneMatch(n -> pkgNode.globalVars.contains(n));
            if (cyclicNodes.size() <= 1 || atLeastOneNodeIsGlobalVar) {
                continue;
            }

            cyclicDepFound = true;
            List<BLangIdentifier> cycle = cyclicNodes.stream()
                    .map(index -> graphIndices.get(index))
                    .map(symbol -> symbolToIdentifier.get(symbol).identifier)
                    .collect(Collectors.toList());

            Integer firstOccurrence = cyclicNodes.get(0);
            BSymbol firstOccurrenceSymbol = graphIndices.get(firstOccurrence);
            DiagnosticPos position = symbolToIdentifier.get(firstOccurrenceSymbol).position;

            dlog.error(position, DiagnosticCode.GLOBAL_VARIABLE_CYCLIC_DEFINITION, cycle);
        }

        return cyclicDepFound;
    }

    private BLangNode getNodeFromGraphIndex(Map<BSymbol, NodeIdPair> graphIndices,
                                            Map<Integer, BSymbol> nodeIdToNodeSymbol, Integer index) {
        return graphIndices.get(nodeIdToNodeSymbol.get(index)).node;
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
