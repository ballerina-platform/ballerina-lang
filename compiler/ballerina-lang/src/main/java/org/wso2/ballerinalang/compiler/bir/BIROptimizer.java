/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir;

import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Optimize BIR.
 *
 * @since 1.0
 */
public class BIROptimizer extends BIRVisitor {

    private static final CompilerContext.Key<BIROptimizer> BIR_OPTIMIZER = new CompilerContext.Key<>();
    private Map<BIROperand, List<BIRAbstractInstruction>> tempVarUpdateInstructions = new HashMap<>();
    private Map<BIROperand, List<BIRErrorEntry>> errorEntries = new HashMap<>();
    private List<BIRVariableDcl> removedTempVars = new ArrayList<>();

    public static BIROptimizer getInstance(CompilerContext context) {
        BIROptimizer birGen = context.get(BIR_OPTIMIZER);
        if (birGen == null) {
            birGen = new BIROptimizer(context);
        }

        return birGen;
    }

    private BIROptimizer(CompilerContext context) {
        context.put(BIR_OPTIMIZER, this);
    }

    public void optimizePackage(BIRPackage pkg) {
        for (BIRFunction func : pkg.functions) {
            func.accept(this);
        }
    }

    @Override
    public void visit(BIRFunction birFunction) {
        for (BIRErrorEntry errorEntry : birFunction.errorTable) {
            addErrorTableDependency(errorEntry);
        }

        // First add all the instructions within the function to a list.
        // This is done since the order of bb's cannot be guaranteed.
        birFunction.parameters.values().forEach(paramBBs -> addDependency(paramBBs));
        addDependency(birFunction.basicBlocks);

        // Then visit and replace any temp moves
        for (List<BIRBasicBlock> paramBBs : birFunction.parameters.values()) {
            paramBBs.forEach(bb -> bb.accept(this));
        }
        birFunction.basicBlocks.forEach(bb -> bb.accept(this));

        // Remove unused temp vars
        List<BIRVariableDcl> newLocalVars = new ArrayList<>();
        for (BIRVariableDcl var : birFunction.localVars) {
            if (var.kind != VarKind.TEMP) {
                newLocalVars.add(var);
                continue;
            }

            if (this.removedTempVars.contains(var)) {
                // do not add
                continue;
            }

            newLocalVars.add(var);
        }

        this.removedTempVars.clear();
        this.tempVarUpdateInstructions.clear();
        this.errorEntries.clear();
        birFunction.localVars = newLocalVars;
    }

    @Override
    public void visit(BIRBasicBlock birBasicBlock) {
        List<BIRInstruction> instructions = birBasicBlock.instructions;
        List<BIRInstruction> newInstructions = new ArrayList<>();

        for (BIRInstruction ins : instructions) {
            // if the current instruction is not a MOVE, don't do anything.
            if (ins.getKind() != InstructionKind.MOVE) {
                newInstructions.add(ins);
                continue;
            }

            // if the current MOVE is not from a TEMP, again, don't do anything.
            Move moveIns = ((Move) ins);
            if (moveIns.rhsOp.variableDcl.kind != VarKind.TEMP) {
                newInstructions.add(ins);
                continue;
            }

            replaceTempVar(moveIns);
        }

        birBasicBlock.instructions = newInstructions;
    }

    private void replaceTempVar(Move moveIns) {
        List<BIRAbstractInstruction> tempUpdateInsList = this.tempVarUpdateInstructions.get(moveIns.rhsOp);
        if (tempUpdateInsList != null) {
            this.removedTempVars.add(moveIns.rhsOp.variableDcl);
            for (BIRAbstractInstruction tempUpdateIns : tempUpdateInsList) {
                // Here it is assumed that we replace only LHS.
                tempUpdateIns.lhsOp = moveIns.lhsOp;
                addDependency(tempUpdateIns);
            }
        }

        // Update error entry
        List<BIRErrorEntry> errorEntriesWithTemp = this.errorEntries.get(moveIns.rhsOp);
        if (errorEntriesWithTemp != null) {
            for (BIRErrorEntry errorEntryWithTemp : errorEntriesWithTemp) {
                errorEntryWithTemp.errorOp = moveIns.lhsOp;
                addErrorTableDependency(errorEntryWithTemp);
            }
        }
    }

    private void addErrorTableDependency(BIRErrorEntry errorEntryWithTemp) {
        List<BIRErrorEntry> errorTableEntries = this.errorEntries.get(errorEntryWithTemp.errorOp);
        if (errorTableEntries != null) {
            errorTableEntries.add(errorEntryWithTemp);
        } else {
            this.errorEntries.put(errorEntryWithTemp.errorOp, Lists.of(errorEntryWithTemp));
        }
    }

    private void addDependency(List<BIRBasicBlock> basicBlocks) {
        for (BIRBasicBlock bb : basicBlocks) {
            for (BIRInstruction ins : bb.instructions) {
                addDependency((BIRAbstractInstruction) ins);
            }
            addDependency(bb.terminator);
        }
    }

    private void addDependency(BIRAbstractInstruction ins) {
        if (ins.lhsOp == null || ins.lhsOp.variableDcl.kind != VarKind.TEMP) {
            return;
        }

        List<BIRAbstractInstruction> tempVarUpdates = this.tempVarUpdateInstructions.get(ins.lhsOp);
        if (tempVarUpdates != null) {
            tempVarUpdates.add(ins);
        } else {
            this.tempVarUpdateInstructions.put(ins.lhsOp, Lists.of(ins));
        }
    }

}
