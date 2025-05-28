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

import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingGlobalVarCollector;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;

import java.util.ArrayList;
import java.util.List;

public class LazyLoadingDesugar {

    LazyLoadingGlobalVarCollector lazyLoadingGlobalVarCollector;

    List<BIRNode.BIRBasicBlock> newBBs;

    BIRNode.BIRBasicBlock currentBB;

    public LazyLoadingDesugar(LazyLoadingGlobalVarCollector lazyLoadingGlobalVarCollector) {
        this.lazyLoadingGlobalVarCollector = lazyLoadingGlobalVarCollector;
        this.newBBs = new ArrayList<>();
    }

    public void lazyLoadInitFunctionsGlobalVars(List<BIRNode.BIRFunction> functions) {
        for (BIRNode.BIRFunction function : functions) {
            if (function.originalName.value.contains(JvmConstants.INIT_FUNCTION_SUFFIX)) {
                lazyLoadInitFunctionsGlobalVars(function);
                function.basicBlocks = newBBs;
                newBBs = new ArrayList<>();
            }
        }
    }

    private void lazyLoadInitFunctionsGlobalVars(BIRNode.BIRFunction function) {
        for (BIRNode.BIRBasicBlock basicBlock : function.basicBlocks) {
            List<BIRNonTerminator> instructions = basicBlock.instructions;
            currentBB = new BIRNode.BIRBasicBlock(basicBlock.id, basicBlock.number);
            currentBB.terminator = basicBlock.terminator;
            newBBs.add(currentBB);
            for (int i = 0; i < instructions.size(); i++) {
                BIRNonTerminator instruction = instructions.get(i);
                switch (instruction.kind) {
                    case FP_LOAD -> i = lazyLoadFpLoad((BIRNonTerminator.FPLoad) instruction, instructions, i);
                    case NEW_TYPEDESC -> i = lazyLoadNewTypeDesc((BIRNonTerminator.NewTypeDesc) instruction,
                            instructions, i);
//                    case NEW_ARRAY-> lazyLoadNewArray((BIRNonTerminator.NewArray) instruction, i);
                    default -> currentBB.instructions.add(instruction);
                }
            }
        }
    }

    private int lazyLoadNewTypeDesc(BIRNonTerminator.NewTypeDesc newTypeDesc, List<BIRNonTerminator> instructions,
                                    int i) {
        if (newTypeDesc.lhsOp.variableDcl.kind == VarKind.GLOBAL) {
            String varName = newTypeDesc.lhsOp.variableDcl.name.value;
            if (instructions.size() > i + 1) {
                BIRNonTerminator nextIns = instructions.get(i + 1);
                if (nextIns.kind == InstructionKind.TYPE_CAST && nextIns.lhsOp.variableDcl.kind == VarKind.GLOBAL) {
                    this.lazyLoadingGlobalVarCollector.add(varName, List.of(newTypeDesc, nextIns));
                    i++;
                } else {
                    this.lazyLoadingGlobalVarCollector.add(varName, List.of(newTypeDesc));
                }
            } else {
                this.lazyLoadingGlobalVarCollector.add(varName, List.of(newTypeDesc));
            }
        } else {
            currentBB.instructions.add(newTypeDesc);
        }
        return i;
    }

    private int lazyLoadFpLoad(BIRNonTerminator.FPLoad fpLoad, List<BIRNonTerminator> instructions, int i) {
        if (fpLoad.lhsOp.variableDcl.kind == VarKind.GLOBAL) {
            String varName = fpLoad.lhsOp.variableDcl.name.value;
            if (instructions.size() > i + 1) {
                BIRNonTerminator nextIns = instructions.get(i + 1);
                if (nextIns.kind == InstructionKind.RECORD_DEFAULT_FP_LOAD) {
                    this.lazyLoadingGlobalVarCollector.add(varName, List.of(fpLoad, nextIns));
                    i++;
                } else {
                    this.lazyLoadingGlobalVarCollector.add(varName, List.of(fpLoad));
                }
            } else {
                this.lazyLoadingGlobalVarCollector.add(varName, List.of(fpLoad));
            }
        } else {
            currentBB.instructions.add(fpLoad);
        }
        return i;
    }

    private void lazyLoadNewArray(BIRNonTerminator.NewArray newArray, int i) {
        if (newArray.lhsOp.variableDcl.kind != VarKind.GLOBAL) {
            currentBB.instructions.add(newArray);
            return;
        }
        String varName = newArray.lhsOp.variableDcl.name.value;
        BIROperand sizeOp = newArray.sizeOp;
        List<BIRInstruction> arrayInstructions = new ArrayList<>();
        // if there are any terminators like method call for array values will not be added to lazy loader. HEnce
        // only current instructions set will be checked.
        int sizeOpInsIndex = -1;
        int currentBBLastInsIndex = currentBB.instructions.size() - 1;
        for (int j = currentBBLastInsIndex; j >= 0; j--) {
            if (currentBB.instructions.get(j).lhsOp == sizeOp) {
                sizeOpInsIndex = j;
                break;
            }
        }
        if (sizeOpInsIndex == -1) {
            currentBB.instructions.add(newArray);
            return;
        }
        for (int k = sizeOpInsIndex; k <= currentBBLastInsIndex; k++) {
            arrayInstructions.add(currentBB.instructions.remove(sizeOpInsIndex));
        }
        arrayInstructions.add(newArray);
        this.lazyLoadingGlobalVarCollector.add(varName, arrayInstructions);
    }
}
