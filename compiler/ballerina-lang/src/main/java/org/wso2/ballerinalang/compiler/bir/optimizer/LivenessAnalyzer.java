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

package org.wso2.ballerinalang.compiler.bir.optimizer;

import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Do liveness analysis on the control flow graph to obtain the liveOut value of each function.
 * <p>
 * Reference: Andrew W. Appel - Modern Compiler Implementation in Java, 2Ed (2002), Chapter 10
 */
public class LivenessAnalyzer {
    private final Map<ControlFlowGraph.Node, Set<BIRNode.BIRVariableDcl>> liveIns;
    private final Map<ControlFlowGraph.Node, Set<BIRNode.BIRVariableDcl>> liveOuts;
    private final List<ControlFlowGraph.Node> nodes;

    public LivenessAnalyzer(List<ControlFlowGraph.Node> nodes) {
        this.liveIns = new HashMap<>();
        this.liveOuts = new HashMap<>();
        this.nodes = nodes;
        init();
    }

    private void init() {
        nodes.forEach(node -> {
            liveIns.put(node, new HashSet<>());
            liveOuts.put(node, new HashSet<>());
        });
        analyze();
    }

    /**
     * Analyzes the CFG based on the following equation till there's no change in the liveIns and liveOuts.
     * <code>
     * <p>
     * in[n] ← use[n] ∪ (out[n] − def [n])
     * <p>
     * out[n] ← ∪(s∈succ[n]) (in[s])
     * <p></code>
     * where ∪ indicates union of, in is liveIns, out is liveOuts, def is lhsOp and use is rhs operands.
     */
    private void analyze() {
        if (nodes.isEmpty()) {
            return;
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = nodes.size() - 1; i >= 0; i--) {
                ControlFlowGraph.Node node = nodes.get(i);
                changed |= updateLiveIns(node);
                changed |= updateLiveOuts(node);
            }
        }
    }

    /**
     * If an operand is used at the current node or is liveOut at the current node, it is liveIn at the current node.
     * This is except for the operand defined at the current node.
     *
     * @param node the current node
     * @return whether the set changed
     */
    private boolean updateLiveIns(ControlFlowGraph.Node node) {
        boolean changed = false;
        Set<BIRNode.BIRVariableDcl> in = liveIns.get(node);
        Set<BIRNode.BIRVariableDcl> out = liveOuts.get(node);
        for (BIROperand use : node.instruction.getRhsOperands()) {
            changed |= in.add(use.variableDcl);
        }
        BIRNode.BIRVariableDcl def = getDef(node);
        boolean removed = out.remove(def);
        changed |= in.addAll(out);
        if (removed) {
            out.add(def);
        }
        return changed;
    }

    private BIRNode.BIRVariableDcl getDef(ControlFlowGraph.Node node) {
        BIRNode.BIRVariableDcl def = null;
        if (node.instruction.lhsOp != null) {
            BIRNode.BIRVariableDcl variableDcl = node.instruction.lhsOp.variableDcl;
            if (variableDcl.kind != VarKind.GLOBAL) {
                def = variableDcl;
            }
        }
        return def;
    }

    /**
     * If an operand is liveIn at any of the successors it is liveOut at current node.
     *
     * @param node the current node
     * @return whether the set changed
     */
    private boolean updateLiveOuts(ControlFlowGraph.Node node) {
        boolean changed = false;
        for (ControlFlowGraph.Node successor : node.successors) {
            changed |= liveOuts.get(node).addAll(liveIns.get(successor));
        }
        return changed;
    }

    /**
     * Get the set of variables that are live after the execution of an instruction.
     *
     * @return map of BIRAbstractInstruction and the set of variables that are live after the execution of the
     * instruction.
     */
    public Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> getInstructionLiveOuts() {
        Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> map = new HashMap<>();
        liveOuts.forEach((node, var) -> map.put(node.instruction, var));
        return map;
    }
}
