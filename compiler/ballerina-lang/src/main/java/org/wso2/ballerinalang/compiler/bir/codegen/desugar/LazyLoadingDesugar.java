/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.desugar;

import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_FUNC;
import static org.wso2.ballerinalang.compiler.bir.codegen.optimizer.LargeMethodOptimizer.SPLIT_METHOD;

public class LazyLoadingDesugar {

    private final LazyLoadingDataCollector lazyLoadingDataCollector;
    private int nextLocalVarBBIndex = 0;
    private int nextLocalVarIndex = 0;

    public LazyLoadingDesugar(LazyLoadingDataCollector lazyLoadingDataCollector) {
        this.lazyLoadingDataCollector = lazyLoadingDataCollector;
    }

    public void lazyLoadInitFunctions(List<BIRNode.BIRFunction> functions) {
        for (BIRNode.BIRFunction function : functions) {
            if (function.originalName.value.contains(Names.INIT_FUNCTION_SUFFIX.value)) {
                lazyLoadInitFunction(function);
                removeUnusedLocalVars(function);
                reset();
            }
        }
    }

    private void lazyLoadInitFunction(BIRNode.BIRFunction function) {
        List<BIRNode.BIRBasicBlock> basicBlocks = function.basicBlocks;
        for (int i = 0; i < basicBlocks.size(); i++) {
            BIRNode.BIRBasicBlock basicBlock = basicBlocks.get(i);
            boolean isLastInstructionGlobalVar = lazyLoadInstructions(basicBlock, i);
            boolean isLastTerminatorsIsGlobal = lazyLoadTerminator(function.basicBlocks, basicBlock, i);
            if (isLastInstructionGlobalVar && isLastTerminatorsIsGlobal) {
                nextLocalVarIndex = 0;
                nextLocalVarBBIndex++;
            }
        }
    }

    private boolean lazyLoadInstructions(BIRNode.BIRBasicBlock bb , int currentBBIndex) {
        BIROperand lhsOp = null;
        List<BIRNonTerminator> instructions = bb.instructions;
        for (int i = 0; i < instructions.size(); i++) {
            BIRNonTerminator instruction = instructions.get(i);
            lhsOp = instruction.lhsOp;
            if (lhsOp == null || lhsOp.variableDcl.kind != VarKind.GLOBAL) {
                continue;
            }
            if (currentBBIndex != nextLocalVarBBIndex) {
                nextLocalVarIndex = i + 1;
                nextLocalVarBBIndex = currentBBIndex;
                continue;
            }
            copyInstructions(lhsOp, instructions, i);
            i = nextLocalVarIndex - 1;
        }
        return lhsOp != null && lhsOp.variableDcl.kind == VarKind.GLOBAL;
    }

    private boolean lazyLoadTerminator(List<BIRNode.BIRBasicBlock> basicBlocks,  BIRNode.BIRBasicBlock bb,
                                    int currentBBIndex) {
        BIRTerminator terminator = bb.terminator;
        BIROperand lhsOp = terminator.lhsOp;
        if (terminator.kind == InstructionKind.CALL) {
            BIRTerminator.Call call = (BIRTerminator.Call) terminator;
            // Handle annotation functions
            if (call.name.value.contains(ANNOTATION_FUNC)) {
                lazyLoadAnnotationProcessCall(bb, basicBlocks.get(currentBBIndex + 1), call);
                return false;
            }
            if (call.args.isEmpty() && lhsOp != null && lhsOp.variableDcl.kind == VarKind.GLOBAL &&
                    call.name.value.contains(SPLIT_METHOD)) {
                lazyLoadSplitCall(call, lhsOp.variableDcl.name.value, bb, basicBlocks, currentBBIndex);
            }
        }
        return (lhsOp != null && lhsOp.variableDcl.kind == VarKind.GLOBAL) || terminator.kind == InstructionKind.GOTO;
    }

    private void copyInstructions(BIROperand lhsOp, List<BIRNonTerminator> instructions, int currentInsIndex) {
        String varName = lhsOp.variableDcl.name.value;
        int startIndex = nextLocalVarIndex;
        List<BIRNonTerminator> lazyInsList = new ArrayList<>();
        for (int i = startIndex; i <= currentInsIndex; i++) {
            lazyInsList.add(instructions.remove(startIndex));
        }
        LazyLoadBirBasicBlock lazyLoadBirBasicBlock = lazyLoadingDataCollector.lazyLoadingBBMap.get(varName);
        if (lazyLoadBirBasicBlock != null) {
            lazyLoadBirBasicBlock.instructions().addAll(lazyInsList);
            return;
        }
        lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, new LazyLoadBirBasicBlock(lazyInsList, null));
    }

    private void lazyLoadAnnotationProcessCall(BIRNode.BIRBasicBlock currentBB, BIRNode.BIRBasicBlock nextBB,
                                               BIRTerminator.Call call) {
        // Extract type annotations
        List<BIRNonTerminator> instructions = nextBB.instructions;
        if (nextBB.instructions.isEmpty()) {
            // function annotation
            return;
        }
        List<BIRNonTerminator> annotationsInsList = new ArrayList<>();
        List<BIRNonTerminator> nextBBInstructions = new ArrayList<>();
        int size = instructions.size();
        int i = 0;
        String typeName = null;
        for (; i < size; i++) {
            BIRNonTerminator instruction = instructions.get(i);
            annotationsInsList.add(instruction);
            if (instruction.kind == InstructionKind.MAP_STORE) {
                typeName = ((BIRNonTerminator.ConstantLoad) instructions.get(i - 1)).value.toString();
                break;
            }
        }
        for (int j = i + 1; j < instructions.size(); j++) {
            nextBBInstructions.add(instructions.get(j));
        }
        nextBB.instructions = nextBBInstructions;
        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(annotationsInsList, call);
        lazyLoadingDataCollector.lazyLoadingAnnotationsBBMap.put(typeName, lazyBB);
        currentBB.terminator = new BIRTerminator.GOTO(call.pos, nextBB);
    }

    private void lazyLoadSplitCall(BIRTerminator.Call call, String varName, BIRNode.BIRBasicBlock bb,
                                   List<BIRNode.BIRBasicBlock> basicBlocks, int currentBBIndex) {
        BIRNode.BIRBasicBlock nextBB = basicBlocks.get(currentBBIndex + 1);
        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(null, call);
        this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
        bb.terminator = new BIRTerminator.GOTO(call.pos, nextBB);
    }

    private void removeUnusedLocalVars(BIRNode.BIRFunction function) {
        Set<BIRNode.BIRVariableDcl> usedLocalVars = new HashSet<>();
        List<BIRNode.BIRBasicBlock> basicBlocks = function.basicBlocks;
        for (BIRNode.BIRBasicBlock basicBlock : basicBlocks) {
            List<BIRNonTerminator> instructions = basicBlock.instructions;
            BIRTerminator terminator = basicBlock.terminator;
            for (BIRNonTerminator instruction : instructions) {
                BIROperand lhsOp = instruction.lhsOp;
                if (lhsOp != null) {
                    usedLocalVars.add(lhsOp.variableDcl);
                }
                for (BIROperand rhsOperand : instruction.getRhsOperands()) {
                    usedLocalVars.add(rhsOperand.variableDcl);
                }
            }
            BIROperand lhsOp = terminator.lhsOp;
            if (lhsOp != null) {
                usedLocalVars.add(lhsOp.variableDcl);
            }
        }
        List<BIRNode.BIRVariableDcl> newLocalVars = new ArrayList<>();
        for (BIRNode.BIRVariableDcl localVar : function.localVars) {
            if (usedLocalVars.contains(localVar)) {
                newLocalVars.add(localVar);
            }
        }
        function.localVars = newLocalVars;
    }

    private void reset() {
        nextLocalVarIndex = 0;
        nextLocalVarBBIndex = 0;
    }
}
