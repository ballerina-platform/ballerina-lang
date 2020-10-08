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
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optimize variables by reusing temporary variables of the same type.
 */
public class BirVariableOptimizer extends BIRVisitor {
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
        ControlFlowGraph graph = new ControlFlowGraph(birFunction);
        LivenessAnalyzer analyzer = new LivenessAnalyzer(graph.getNodes());
        Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts = analyzer.getInstructionLiveOuts();
        List<BIRNode.BIRVariableDcl> unusedVars = new ArrayList<>();
        reuseVars(liveOuts, unusedVars, birFunction);
        unusedVars.forEach(var -> birFunction.localVars.remove(var));
    }

    private void reuseVars(Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts,
                           List<BIRNode.BIRVariableDcl> unusedVars, BIRNode.BIRFunction birFunction) {
        List<BIRAbstractInstruction> instructionList = getInstructionList(birFunction);
        Map<BType, LinkedList<BIRNode.BIRVariableDcl>> freeVars = new HashMap<>();
        for (int i = 0; i < instructionList.size(); i++) {
            if (instructionList.get(i).getKind() == InstructionKind.XML_SEQ_STORE) {
                continue;
            }
            tryToReuseFromFreeVars(liveOuts, freeVars, instructionList, i, unusedVars);
            checkForFreeVars(freeVars, liveOuts, instructionList.get(i));
        }
    }

    private void checkForFreeVars(Map<BType, LinkedList<BIRNode.BIRVariableDcl>> freeVars,
                                  Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts,
                                  BIRAbstractInstruction instruction) {
        for (BIROperand operand : instruction.getRhsOperands()) {
            BType type = operand.variableDcl.type;
            if ((isReusableVarKind(operand.variableDcl)) && !liveOuts.get(instruction).contains(operand.variableDcl)) {
                if (!freeVars.containsKey(type)) {
                    freeVars.put(type, new LinkedList<>());
                }
                LinkedList<BIRNode.BIRVariableDcl> ls = freeVars.get(type);
                ls.add(operand.variableDcl);
            }
        }
    }

    private void tryToReuseFromFreeVars(Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts,
                                        Map<BType, LinkedList<BIRNode.BIRVariableDcl>> freeVars,
                                        List<BIRAbstractInstruction> instructionList, int index,
                                        List<BIRNode.BIRVariableDcl> unusedVars) {
        BIRAbstractInstruction instruction = instructionList.get(index);
        if (instruction.lhsOp == null) {
            return;
        }
        BType type = instruction.lhsOp.variableDcl.type;
        LinkedList<BIRNode.BIRVariableDcl> defLs = freeVars.get(type);
        if (defLs == null || defLs.isEmpty()) {
            return;
        }
        BIRNode.BIRVariableDcl newLhsOp = defLs.peek();
        BIRNode.BIRVariableDcl oldLhsOp = (instruction).lhsOp.variableDcl;
        if (isReusableVarKind(oldLhsOp) && !checkVarUse(instruction, oldLhsOp)) {
            defLs.remove();
            unusedVars.add(oldLhsOp);
            replaceOldOp(instructionList, index, oldLhsOp, newLhsOp, liveOuts);
            instruction.lhsOp.variableDcl = newLhsOp;
        }
    }

    private boolean isReusableVarKind(BIRNode.BIRVariableDcl oldLhsOp) {
        return oldLhsOp.kind == VarKind.TEMP;
    }

    private void replaceOldOp(List<BIRAbstractInstruction> instructionList, int index, BIRNode.BIRVariableDcl oldLhsOp,
                              BIRNode.BIRVariableDcl newLhsOp,
                              Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts) {
        for (int i = index + 1; i < instructionList.size(); i++) {
            BIRAbstractInstruction instruction = instructionList.get(i);
            boolean changed = false;
            BIROperand[] operands = instruction.getRhsOperands();
            for (BIROperand operand : operands) {
                if (operand.variableDcl == oldLhsOp) {
                    operand.variableDcl = newLhsOp;
                    changed = true;
                }
            }

            if (!changed) {
                continue;
            }
            Set<BIRNode.BIRVariableDcl> insLiveOuts = liveOuts.get(instruction);
            if (insLiveOuts.remove(oldLhsOp)) {
                insLiveOuts.add(newLhsOp);
            }
        }
    }

    private boolean checkVarUse(BIRAbstractInstruction instruction, BIRNode.BIRVariableDcl oldLhsOp) {
        for (BIROperand operand : instruction.getRhsOperands()) {
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
