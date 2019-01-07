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

import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyze global variable dependencies.
 *
 * @since 0.990.xxxx
 */
public class GDependencyAnalyzer extends BLangNodeVisitor {
    // Track the sequence of access to global variables to analyze reference patterns.
    private Map<BSymbol, List<GDependencyAnalyzer.RefPosition>> globalVarSymbolRefPositions;
    private Map<BSymbol, GDependencyAnalyzer.DefPosition> globalVarSymbolDefPositions;
    private SymbolEnv env;
    private BLangDiagnosticLog dlog;

    private int globalVarRefCounter;
    private Deque<BSymbol> currDependentSymbol;
    private static final CompilerContext.Key<GDependencyAnalyzer> GLOBAL_DEPENDENCY_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private SymbolTable symTable;


    private GDependencyAnalyzer(CompilerContext context) {
        context.put(GLOBAL_DEPENDENCY_ANALYZER_KEY, this);
        currDependentSymbol = new ArrayDeque<>();
        this.symTable = SymbolTable.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);

    }

    public static GDependencyAnalyzer getInstance(CompilerContext context) {
        GDependencyAnalyzer gDependencyAnalyzer = context.get(GLOBAL_DEPENDENCY_ANALYZER_KEY);
        if (gDependencyAnalyzer == null) {
            gDependencyAnalyzer = new GDependencyAnalyzer(context);
        }
        return gDependencyAnalyzer;
    }

    /**
     * Perform global dependency analysis on package.
     *
     * @param pkgNode Package to perform data-flow analysis.
     * @return Analyzed package
     */
    public BLangPackage analyze(BLangPackage pkgNode) {
        this.globalVarSymbolRefPositions = new HashMap<>();
        this.globalVarSymbolDefPositions = new HashMap<>();
        this.globalVarRefCounter = 0;
        this.currDependentSymbol.clear();
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeNode(pkgNode, pkgEnv);
        return pkgNode;
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        if (node != null) {
            node.accept(this);
        }
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        populateGlobalVarRefPositionMap(pkgNode.globalVars);
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode, env));
        analyzeGlobalVariableReferencePatterns(pkgNode, this.globalVarSymbolRefPositions, dlog);
    }

    private void analyzeGlobalVariableReferencePatterns(BLangPackage pkgNode,
                                                        Map<BSymbol, List<GDependencyAnalyzer.RefPosition>> globalVarSymbolRefPositions,
                                                        BLangDiagnosticLog dlog) {

        TarjanSccSolverAdjacencyList graph = TarjanSccSolverAdjacencyList.createGraph(pkgNode.globalVars.size());

        boolean forwardRefFound = false;
        for (BLangSimpleVariable globalVar : pkgNode.globalVars) {
            BSymbol symbol = globalVar.symbol;
            GDependencyAnalyzer.DefPosition defPosition = globalVarSymbolDefPositions.get(symbol);
            List<GDependencyAnalyzer.RefPosition> accessedSequence = globalVarSymbolRefPositions.get(symbol);

            boolean frf = validateSameFileForwardReferences(dlog, globalVar, defPosition, accessedSequence);
            forwardRefFound = frf || forwardRefFound;
            if (forwardRefFound) {
                continue;
            }

            for (GDependencyAnalyzer.RefPosition refPosition : accessedSequence) {
                GDependencyAnalyzer.DefPosition position = globalVarSymbolDefPositions.get(refPosition.dependentSymbol);
                graph.addEdge(position.refId, defPosition.refId);
            }
        }

        if (forwardRefFound) { // Single file forward reference found, no further processing required.
            return;
        }

        Map<Integer, List<Integer>> multimap = graph.getSCCs();

        // If cyclic references are found, we can't reorder, exit with error.
        if (findCyclicDependencies(multimap, pkgNode.globalVars)) {
            return;
        }

        // Sort global variable definitions.
        // Tarjan's algorithm as a by product topologically sorts the graph.
        List<BLangSimpleVariable> sorted = new ArrayList<>();
        for (Integer index : graph.dependencyOrder) {
            sorted.add(pkgNode.globalVars.get(index));
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

    private boolean findCyclicDependencies(Map<Integer, List<Integer>> multimap, List<BLangSimpleVariable> globalVars) {
        boolean cyclicDepFound = false;
        for (List<Integer> cyclicNodes : multimap.values()) {
            if (cyclicNodes.size() <= 1) {
                continue;
            }
            cyclicDepFound = true;

            List<BLangIdentifier> cycle = cyclicNodes.stream()
                    .map(index -> globalVars.get(index).name)
                    .collect(Collectors.toList());

            if (!cycle.isEmpty()) {
                DiagnosticPos firstPosition = globalVars.get(cyclicNodes.get(0)).pos;
                dlog.error(firstPosition, DiagnosticCode.GLOBAL_VARIABLE_CYCLIC_DEFINITION, cycle);
            }
        }

        return cyclicDepFound;
    }

    private boolean validateSameFileForwardReferences(BLangDiagnosticLog dlog, BLangSimpleVariable globalVar,
                                                      GDependencyAnalyzer.DefPosition defPosition,
                                                      List<GDependencyAnalyzer.RefPosition> accessedSequence) {
        String defFile = defPosition.position.src.cUnitName;
        boolean foundForwardRef = false;
        List<GDependencyAnalyzer.RefPosition> sameFileForwardReferences = accessedSequence.stream()
                .filter(p -> p.position.src.cUnitName.equals(defFile))
                .filter(p -> globalVarSymbolDefPositions.get(p.dependentSymbol).refId < defPosition.refId)
                .collect(Collectors.toList());

        for (GDependencyAnalyzer.RefPosition refPosition : sameFileForwardReferences) {
            foundForwardRef = true;
            dlog.error(refPosition.position, DiagnosticCode.GLOBAL_VARIABLE_FORWARD_REFERENCE, globalVar.name);
        }
        return foundForwardRef;
    }

    private void populateGlobalVarRefPositionMap(List<BLangSimpleVariable> globalVars) {
        for (TopLevelNode node : globalVars) {
            BLangSimpleVariable globalVar = (BLangSimpleVariable) node;
            globalVarSymbolRefPositions.put(globalVar.symbol, new ArrayList<>());
        }
    }

    private static class RefPosition {
        final DiagnosticPos position;
        final BSymbol dependentSymbol;

        private RefPosition(DiagnosticPos position, BSymbol dependentSymbol) {
            this.position = position;
            this.dependentSymbol = dependentSymbol;
        }

        static GDependencyAnalyzer.RefPosition newRef(DiagnosticPos position, BSymbol dependentSymbol) {
            return new GDependencyAnalyzer.RefPosition(position, dependentSymbol);
        }
    }

    private static class DefPosition {
        final int refId;
        final DiagnosticPos position;

        private DefPosition(int refId, DiagnosticPos position) {
            this.refId = refId;
            this.position = position;
        }

        static GDependencyAnalyzer.DefPosition newDef(int sequenceNo, DiagnosticPos position) {
            return new GDependencyAnalyzer.DefPosition(sequenceNo, position);
        }
    }
}
