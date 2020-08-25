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
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optimize variables by reusing temporary variables of the same type.
 */
public class BirVariableOptimizer extends BIRVisitor {
    private Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts;
    private List<BIRNode.BIRVariableDcl> unusedVars;
    private List<BIRAbstractInstruction> instructionList;
    private ControlFlowGraph graph;
    private LivenessAnalyzer analyzer;

    public void optimizeNode(BIRNode node) {
        node.accept(this);
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
        birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
        birPackage.functions.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
        birTypeDefinition.attachedFuncs.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        graph = new ControlFlowGraph(birFunction);
        analyzer = new LivenessAnalyzer(graph.getNodes());
        liveOuts = analyzer.getInstructionLiveOuts();
        unusedVars = new ArrayList<>();
        instructionList = getInstructionList(birFunction);
        reuseVars();
        removeDeadCode(birFunction);
        unusedVars.forEach(var -> birFunction.localVars.remove(var));
    }

    private void reuseVars() {
        Map<BType, LinkedList<BIRNode.BIRVariableDcl>> freeVars = new HashMap<>();
        for (int i = 0; i < instructionList.size(); i++) {
            if (instructionList.get(i).getKind() == InstructionKind.XML_SEQ_STORE) {
                continue;
            }
            tryToReuseFromFreeVars(freeVars, i);
            checkForFreeVars(freeVars, instructionList.get(i));
        }
    }

    private void checkForFreeVars(Map<BType, LinkedList<BIRNode.BIRVariableDcl>> freeVars,
                                  BIRAbstractInstruction instruction) {
        for (BIROperand operand : BirOptimizerUtil.getUse(instruction)) {
            BType type = operand.variableDcl.type;
            if (operand.variableDcl.kind == VarKind.TEMP && !liveOuts.get(instruction).contains(operand.variableDcl)) {
                if (!freeVars.containsKey(type)) {
                    freeVars.put(type, new LinkedList<>());
                }
                LinkedList<BIRNode.BIRVariableDcl> ls = freeVars.get(type);
                ls.add(operand.variableDcl);
            }
        }
    }

    private void tryToReuseFromFreeVars(Map<BType, LinkedList<BIRNode.BIRVariableDcl>> freeVars, int index) {
        BIRAbstractInstruction instruction = instructionList.get(index);
        BIROperand defOp = BirOptimizerUtil.getDef(instruction);
        if (defOp == null) {
            return;
        }
        BType type = defOp.variableDcl.type;
        LinkedList<BIRNode.BIRVariableDcl> defLs = freeVars.get(type);
        if (defLs == null || defLs.isEmpty()) {
            return;
        }
        BIRNode.BIRVariableDcl newLhsOp = defLs.peek();
        BIRNode.BIRVariableDcl oldLhsOp = defOp.variableDcl;
        if (oldLhsOp.kind == VarKind.TEMP && !checkVarUse(instruction, oldLhsOp)) {
            defLs.remove();
            unusedVars.add(oldLhsOp);
            replaceOldOp(index, oldLhsOp, newLhsOp);
            defOp.variableDcl = newLhsOp;
            updateLiveOut(oldLhsOp, newLhsOp, instruction);
        }
    }

    private void replaceOldOp(int index, BIRNode.BIRVariableDcl oldLhsOp, BIRNode.BIRVariableDcl newLhsOp) {
        for (int i = index + 1; i < instructionList.size(); i++) {
            BIRAbstractInstruction instruction = instructionList.get(i);
            boolean changed = false;
            BIROperand[] operands = BirOptimizerUtil.getUse(instruction);
            for (BIROperand operand : operands) {
                if (operand.variableDcl == oldLhsOp) {
                    operand.variableDcl = newLhsOp;
                    changed = true;
                }
            }

            if (!changed) {
                continue;
            }
            updateLiveOut(oldLhsOp, newLhsOp, instruction);
        }
    }

    private void updateLiveOut(BIRNode.BIRVariableDcl oldLhsOp, BIRNode.BIRVariableDcl newLhsOp,
                               BIRAbstractInstruction instruction) {
        Set<BIRNode.BIRVariableDcl> insLiveOuts = liveOuts.get(instruction);
        if (insLiveOuts.remove(oldLhsOp)) {
            insLiveOuts.add(newLhsOp);
        }
    }

    private boolean checkVarUse(BIRAbstractInstruction instruction, BIRNode.BIRVariableDcl oldLhsOp) {
        for (BIROperand operand : BirOptimizerUtil.getUse(instruction)) {
            if (operand.variableDcl == oldLhsOp) {
                return true;
            }
        }
        return false;
    }

    private List<BIRAbstractInstruction> getInstructionList(BIRNode.BIRFunction birFunction) {
        List<BIRAbstractInstruction> ls = new ArrayList<>();
        birFunction.basicBlocks.forEach(basicBlock -> {
            ls.addAll(basicBlock.instructions);
            ls.add(basicBlock.terminator);
        });
        return ls;
    }

    private void removeDeadCode(BIRNode.BIRFunction birFunction) {
        List<BIRNode.BIRVariableDcl> lhsOpOfRemovedInstructions = new ArrayList<>();
        boolean changed = true;
        while (changed) {
            if (birFunction.basicBlocks.isEmpty()) {
                break;
            }
            changed = false;
            for (BIRNode.BIRBasicBlock basicBlock : birFunction.basicBlocks) {
                changed |= basicBlock.instructions.removeIf(instruction -> isDeadInstruction(
                        lhsOpOfRemovedInstructions, instruction, birFunction.localVars.get(0)));
            }
        }
        checkUsage(lhsOpOfRemovedInstructions);
    }

    private boolean isDeadInstruction(List<BIRNode.BIRVariableDcl> lhsOpOfRemovedInstructions,
                                      BIRNonTerminator instruction, BIRNode.BIRVariableDcl first) {
        BIROperand lhsOp = BirOptimizerUtil.getDef(instruction);
        if (lhsOp == null) {
            return false;
        }
        BIRNode.BIRVariableDcl lhsOpVar = lhsOp.variableDcl;
        Set<BIRNode.BIRVariableDcl> varDcl = liveOuts.get(instruction);
        if ((lhsOpVar.kind == VarKind.LOCAL || lhsOpVar.kind == VarKind.TEMP) &&
                !(first == lhsOpVar || varDcl.contains(lhsOpVar))) {
            instructionList.remove(instruction);
            lhsOpOfRemovedInstructions.add(lhsOpVar);
            fixLiveout(instruction);
            return true;
        }
        return false;
    }

    private void fixLiveout(BIRNonTerminator instruction) {
        graph.removeNode(instruction);
        analyzer = new LivenessAnalyzer(graph.getNodes());
        liveOuts = analyzer.getInstructionLiveOuts();
    }

    private void checkUsage(List<BIRNode.BIRVariableDcl> lhsOpOfRemovedInstructions) {
        for (BIRAbstractInstruction instruction : instructionList) {
            lhsOpOfRemovedInstructions.removeIf(variableDcl -> usedInLhs(instruction.lhsOp, variableDcl) ||
                    usedInRhs(BirOptimizerUtil.getUse(instruction), variableDcl));
        }
        unusedVars.addAll(lhsOpOfRemovedInstructions);
    }

    private boolean usedInLhs(BIROperand lhsOp, BIRNode.BIRVariableDcl variableDcl) {
        return lhsOp != null && variableDcl == lhsOp.variableDcl;
    }

    private boolean usedInRhs(BIROperand[] rhsOps, BIRNode.BIRVariableDcl variableDcl) {
        return Arrays.stream(rhsOps).anyMatch(var -> var.variableDcl == variableDcl);
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock basicBlock) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.GOTO birGoto) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Call birCall) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.AsyncCall birCall) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Return birReturn) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Branch birBranch) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.FPCall fpCall) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Lock lock) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.FieldLock lock) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Unlock unlock) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Panic birPanic) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Wait birWait) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.WaitAll waitAll) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Flush birFlush) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.WorkerReceive workerReceive) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.WorkerSend workerSend) {
        // Do nothing
    }
}
