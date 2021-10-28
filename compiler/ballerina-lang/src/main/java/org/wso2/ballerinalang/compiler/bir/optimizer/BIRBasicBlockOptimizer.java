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
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Remove unnecessary basic blocks.
 *
 * @since 2.0
 */

public class BIRBasicBlockOptimizer extends BIRVisitor {

    private int currentBBId = -1;
    private static final String BIR_BASIC_BLOCK_PREFIX = "bb";

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
        Map<BIRBasicBlock, List<BIRBasicBlock>> predecessorMap = getPredecessorMap(birFunction.basicBlocks);

        // Remove unreachable basic blocks
        for (int i = 1; i < birFunction.basicBlocks.size(); i++) {
            BIRBasicBlock basicBlock = birFunction.basicBlocks.get(i);
            List<BIRBasicBlock> unreachableBasicBlocks = getUnreachableBasicBlocks(basicBlock, predecessorMap);
            if (predecessorMap.get(basicBlock).isEmpty()) {
                birFunction.basicBlocks.removeAll(unreachableBasicBlocks);
            }
        }

        // Re-arrange basic blocks
        birFunction.parameters.values().forEach(basicBlocks -> basicBlocks.forEach(this::rearrangeBasicBlocks));
        birFunction.basicBlocks.forEach(this::rearrangeBasicBlocks);

        // Re-arrange error entries
        birFunction.errorTable.sort(Comparator.comparingInt(o ->
                Integer.parseInt(o.trapBB.id.value.replace(BIR_BASIC_BLOCK_PREFIX, ""))));

        currentBBId = -1;
    }

    private List<BIRBasicBlock> getUnreachableBasicBlocks(BIRBasicBlock basicBlock,
                                                          Map<BIRBasicBlock, List<BIRBasicBlock>> predecessorMap) {
        List<BIRBasicBlock> unreachableBasicBlocks = new ArrayList<>();
        unreachableBasicBlocks.add(basicBlock);
        for (BIRBasicBlock nextBB : basicBlock.terminator.getNextBasicBlocks()) {
            if (predecessorMap.get(nextBB).size() == 1) {
                unreachableBasicBlocks.addAll(getUnreachableBasicBlocks(nextBB, predecessorMap));
            }
        }
        return unreachableBasicBlocks;
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

    private void rearrangeBasicBlocks(BIRNode.BIRBasicBlock bb) {
        currentBBId++;
        bb.id = new Name(BIR_BASIC_BLOCK_PREFIX + currentBBId);
    }

}
