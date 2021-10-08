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

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BIRBasicBlockOptimizer extends BIRVisitor {

    private BIROptimizer.OptimizerEnv env;
    private final List<BIRBasicBlock> predecessors = new ArrayList<>();
    private final Set<BIRBasicBlock> removableBasicBlocks = new HashSet<>();

    private int currentBBId = -1;
    private static final String BIR_BASIC_BLOCK_PREFIX = "bb";

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
        BIROptimizer.OptimizerEnv funcEnv = new BIROptimizer.OptimizerEnv();
        Map<BIRBasicBlock, List<BIRBasicBlock>> predecessorMap = getPredecessorMap(birFunction.basicBlocks);

        for (Map.Entry<BIRBasicBlock, List<BIRBasicBlock>> entry : predecessorMap.entrySet()) {
            BIRBasicBlock basicBlock = entry.getKey();

            if (basicBlock.terminator instanceof BIRTerminator.GOTO) {
                if (basicBlock.instructions.isEmpty()) {
                    this.predecessors.addAll(predecessorMap.get(basicBlock));
                    this.optimizeNode(basicBlock, funcEnv);
                }
            }
        }

        for (BIRBasicBlock bb : this.removableBasicBlocks) {
            birFunction.localVars.forEach(localVar -> {
                if (localVar.startBB == bb) {
                    localVar.startBB = ((BIRTerminator.GOTO) bb.terminator).targetBB;
                }
                if (localVar.endBB == bb) {
                    localVar.endBB = ((BIRTerminator.GOTO) bb.terminator).targetBB;
                }
            });

            birFunction.errorTable.forEach(errorEntry -> {
                if (errorEntry.endBB == bb) {
                    errorEntry.endBB = predecessorMap.get(bb).get(0);
                }
                if (errorEntry.trapBB == bb) {
                    errorEntry.trapBB = ((BIRTerminator.GOTO) bb.terminator).targetBB;
                }
                if (errorEntry.targetBB == bb) {
                    errorEntry.targetBB = ((BIRTerminator.GOTO) bb.terminator).targetBB;
                }
            });
        }

        // Remove unnecessary goto basic blocks
        this.removableBasicBlocks.forEach(bb -> birFunction.basicBlocks.remove(bb));

        // Re-arrange basic blocks
        birFunction.parameters.values().forEach(basicBlocks -> basicBlocks.forEach(this::rearrangeBasicBlocks));
        birFunction.basicBlocks.forEach(this::rearrangeBasicBlocks);

        // Re-arrange error entries
        birFunction.errorTable.sort(Comparator.comparingInt(o ->
                Integer.parseInt(o.trapBB.id.value.replace(BIR_BASIC_BLOCK_PREFIX, ""))));

        currentBBId = -1;
        this.predecessors.clear();
        this.removableBasicBlocks.clear();
    }

    private void rearrangeBasicBlocks(BIRNode.BIRBasicBlock bb) {
        currentBBId++;
        bb.id = new Name(BIR_BASIC_BLOCK_PREFIX + currentBBId);
    }

    private Map<BIRBasicBlock, List<BIRBasicBlock>> getPredecessorMap(List<BIRBasicBlock> basicBlocks) {
        Map<BIRBasicBlock, List<BIRBasicBlock>> predecessorMap = new HashMap<>();
        for (BIRBasicBlock basicBlock : basicBlocks) {
            if (!predecessorMap.containsKey(basicBlock)) {
                predecessorMap.computeIfAbsent(basicBlock, k -> new ArrayList<>());
            }
            for (BIRBasicBlock bb : basicBlock.terminator.getNextBasicBlocks()) {
                predecessorMap.computeIfAbsent(bb, k -> new ArrayList<>()).add(basicBlock);
            }
        }
        return predecessorMap;
    }

    @Override
    public void visit(BIRBasicBlock basicBlock) {
        this.env.currentBB = basicBlock;
        this.env.nextBB = ((BIRTerminator.GOTO) basicBlock.terminator).targetBB;
        this.predecessors.forEach(bb -> this.optimizeTerm(bb.terminator, this.env));

        this.removableBasicBlocks.add(basicBlock);
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
}
