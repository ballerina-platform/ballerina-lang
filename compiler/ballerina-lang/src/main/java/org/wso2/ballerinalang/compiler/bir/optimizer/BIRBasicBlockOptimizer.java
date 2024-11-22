/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.ballerinalang.compiler.bir.BIRGenUtils;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Remove unnecessary goto basic blocks.
 *
 * @since 2.0.0
 */

public class BIRBasicBlockOptimizer extends BIRVisitor {

    private BIROptimizer.OptimizerEnv env;
    private final Map<BIRBasicBlock, List<BIRBasicBlock>> predecessorMap = new HashMap<>();

    public void optimizeNode(BIRNode node, BIROptimizer.OptimizerEnv env) {
        if (node == null) {
            return;
        }
        BIROptimizer.OptimizerEnv oldEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = oldEnv;
    }

    public void optimizeTerm(BIRTerminator term, BIROptimizer.OptimizerEnv env) {
        this.optimizeNode(term, env);
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
        BIRGenUtils.rearrangeBasicBlocks(birFunction);
        BIROptimizer.OptimizerEnv funcEnv = new BIROptimizer.OptimizerEnv();

        // Get basic blocks vs predecessors map
        populatePredecessorMap(birFunction.basicBlocks);

        // Remove unnecessary goto basic blocks
        Set<BIRBasicBlock> removableGOTOBasicBlocks = getRemovableBasicBlocks(birFunction, funcEnv);
        resetEndBasicBlock(birFunction, removableGOTOBasicBlocks);
        birFunction.basicBlocks.removeAll(removableGOTOBasicBlocks);
        BIRGenUtils.rearrangeBasicBlocks(birFunction);
    }

    // Basic block vs it's predecessors map
    private void populatePredecessorMap(List<BIRBasicBlock> basicBlocks) {
        this.predecessorMap.clear();
        for (BIRBasicBlock basicBlock : basicBlocks) {
            this.predecessorMap.computeIfAbsent(basicBlock, k -> new ArrayList<>());
            for (BIRBasicBlock bb : basicBlock.terminator.getNextBasicBlocks()) {
                this.predecessorMap.computeIfAbsent(bb, k -> new ArrayList<>()).add(basicBlock);
            }
        }
    }

    private void updatePredecessorMap(BIRBasicBlock removable) {
        this.predecessorMap.remove(removable);
        for (Map.Entry<BIRBasicBlock, List<BIRBasicBlock>> entry : this.predecessorMap.entrySet()) {
            BIRBasicBlock key = entry.getKey();
            List<BIRBasicBlock> predecessorList = entry.getValue();
            if (predecessorList.contains(removable)) {
                this.predecessorMap.get(key).remove(removable);
            }
        }
    }

    private Set<BIRBasicBlock> getRemovableBasicBlocks(BIRNode.BIRFunction birFunction,
                                                       BIROptimizer.OptimizerEnv funcEnv) {
        Set<BIRBasicBlock> errorTableTargetBBs = new HashSet<>();
        for (BIRNode.BIRErrorEntry birErrorEntry : birFunction.errorTable) {
            errorTableTargetBBs.add(birErrorEntry.targetBB);
        }

        Set<BIRBasicBlock> removableBasicBlocks = new HashSet<>();
        for (int i = birFunction.basicBlocks.size() - 1; i >= 0; --i) {
            BIRBasicBlock basicBlock = birFunction.basicBlocks.get(i);

            // Remove basic blocks with unnecessary jump statement
            if (basicBlock.terminator instanceof BIRTerminator.GOTO && basicBlock.instructions.isEmpty()
                    && basicBlock.terminator.pos == null && !errorTableTargetBBs.contains(basicBlock)) {
                boolean isLoopBB = false;
                BIRBasicBlock targetBB = ((BIRTerminator.GOTO) basicBlock.terminator).targetBB;
                List<BIRBasicBlock> predecessorBBs = predecessorMap.get(targetBB);
                for (BIRBasicBlock bb : predecessorBBs) {
                    if (targetBB.number < bb.number) {
                        isLoopBB = true;
                        break;
                    }
                }
                if (isLoopBB) {
                    // If target bb is in a loop, that GOTO bb will not be removed.
                    continue;
                }
                funcEnv.currentBB = basicBlock;
                funcEnv.nextBB = targetBB;
                this.optimizeNode(basicBlock, funcEnv);
                birFunction.errorTable.forEach(errorEntry -> this.optimizeNode(errorEntry, funcEnv));
                updatePredecessorMap(basicBlock);
                removableBasicBlocks.add(basicBlock);
            }
        }
        return removableBasicBlocks;
    }

    private void resetEndBasicBlock(BIRNode.BIRFunction birFunc, Set<BIRBasicBlock> removableBBs) {
        for (BIRNode.BIRVariableDcl localVar : birFunc.localVars) {
            if (localVar.endBB != null && removableBBs.contains(localVar.endBB)) {
                int index = birFunc.basicBlocks.indexOf(localVar.endBB);
                localVar.endBB = getLocalVarEndBB(birFunc, removableBBs, index);
            }
        }
    }

    private BIRBasicBlock getLocalVarEndBB(BIRNode.BIRFunction birFunc, Set<BIRBasicBlock> removableBBs, int index) {
        index++;
        if (!removableBBs.contains(birFunc.basicBlocks.get(index))) {
            return birFunc.basicBlocks.get(index);
        }
        return getLocalVarEndBB(birFunc, removableBBs, index);
    }

    @Override
    public void visit(BIRBasicBlock basicBlock) {
        this.predecessorMap.get(basicBlock).forEach(bb -> this.optimizeTerm(bb.terminator, this.env));
    }

    @Override
    public void visit(BIRNode.BIRErrorEntry errorEntry) {

        if (errorEntry.endBB == this.env.currentBB) {
            errorEntry.endBB = this.env.nextBB;
        }
        if (errorEntry.trapBB == this.env.currentBB) {
            errorEntry.trapBB = this.env.nextBB;
        }
        if (errorEntry.targetBB == this.env.currentBB) {
            errorEntry.targetBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.GOTO birGoto) {
        if (birGoto.targetBB == this.env.currentBB) {
            birGoto.targetBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.Call birCall) {
        if (birCall.thenBB == this.env.currentBB) {
            birCall.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.AsyncCall birCall) {
        if (birCall.thenBB == this.env.currentBB) {
            birCall.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.FPCall fpCall) {
        if (fpCall.thenBB == this.env.currentBB) {
            fpCall.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.Return birReturn) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Branch birBranch) {
        if (birBranch.trueBB == this.env.currentBB) {
            birBranch.trueBB = this.env.nextBB;
        }
        if (birBranch.falseBB == this.env.currentBB) {
            birBranch.falseBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.Lock lock) {
        if (lock.lockedBB == this.env.currentBB) {
            lock.lockedBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.FieldLock lock) {
        if (lock.lockedBB == this.env.currentBB) {
            lock.lockedBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.Unlock unlock) {
        if (unlock.unlockBB == this.env.currentBB) {
            unlock.unlockBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.Panic birPanic) {
        // Do nothing
    }

    @Override
    public void visit(BIRTerminator.Wait birWait) {
        if (birWait.thenBB == this.env.currentBB) {
            birWait.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.WaitAll waitAll) {
        if (waitAll.thenBB == this.env.currentBB) {
            waitAll.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.Flush birFlush) {
        if (birFlush.thenBB == this.env.currentBB) {
            birFlush.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.WorkerReceive workerReceive) {
        if (workerReceive.thenBB == this.env.currentBB) {
            workerReceive.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.WorkerSend workerSend) {
        if (workerSend.thenBB == this.env.currentBB) {
            workerSend.thenBB = this.env.nextBB;
        }
    }

    @Override
    public void visit(BIRTerminator.WorkerAlternateReceive workerAlternateReceive) {
        if (workerAlternateReceive.thenBB == this.env.currentBB) {
            workerAlternateReceive.thenBB = this.env.nextBB;
        }
    }
}
