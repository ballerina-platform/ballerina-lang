/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.optimizer;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.CONST_LOAD;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.FP_CALL;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.TYPE_CAST;

/**
 * Remove redundant default function calls for record value creation.
 *
 * @since 2201.9.0
 */
public class BIRRecordValueOptimizer extends BIRVisitor {

    private final List<BIROperand> recordOperandList = new ArrayList<>();
    private final Map<BIROperand, BRecordType> recordOperandTypeMap = new HashMap<>();

    private BIRNode.BIRBasicBlock lastBB = null;
    private BIRNode.BIRFunction currentFunction = null;
    private List<BIRNode.BIRBasicBlock> newBBs = new ArrayList<>();
    private List<BIRNode.BIRFunction> moduleFunctions = new ArrayList<>();
    private final Map<String, BIRNode.BIRVariableDcl> typecastVars = new HashMap<>();
    private boolean fpRemoved = false;
    private boolean valueCreated = false;

    public void optimizeNode(BIRNode node) {
        // Optimize record default value function calls
        node.accept(this);
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
        moduleFunctions = birPackage.functions;
        birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
        birPackage.functions.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
        birTypeDefinition.attachedFuncs.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        currentFunction = birFunction;
        birFunction.basicBlocks.forEach(bb -> bb.accept(this));
        birFunction.basicBlocks = newBBs;
        newBBs = new ArrayList<>();
        typecastVars.clear();
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock basicBlock) {
        List<BIRNonTerminator> instructions = basicBlock.instructions;
        for (BIRNonTerminator inst : instructions) {
            if (Objects.requireNonNull(inst.kind) == InstructionKind.NEW_TYPEDESC) {
                handleNewTypeDesc(inst);
            } else if (inst.kind == InstructionKind.NEW_STRUCTURE) {
                handleNewStructure((BIRNonTerminator.NewStructure) inst);
            }
        }
        if (!fpRemoved) {
            newBBs.add(basicBlock);
        } else {
            lastBB.instructions.addAll(basicBlock.instructions);
        }

        if (basicBlock.terminator.kind == FP_CALL) {
            handleFPCall(basicBlock);
        } else if (fpRemoved && valueCreated) {
            resetBasicBlock(basicBlock);
        }
    }

    private void handleFPCall(BIRNode.BIRBasicBlock basicBlock) {
        BIRTerminator.FPCall fpCall = (BIRTerminator.FPCall) basicBlock.terminator;
        BIROperand recOperand = recordOperandList.isEmpty() ? null :
                recordOperandList.get(recordOperandList.size() - 1);
        BRecordType recordType = recordOperandTypeMap.get(recOperand);

        if (recordType == null || recordType.tsymbol == null) {
            resetBasicBlock(basicBlock);
            return;
        }

        if (!fpCall.fp.variableDcl.name.value.contains(recordType.tsymbol.name.value)) {
            resetBasicBlock(basicBlock);
            return;
        }

        BIRNode.BIRFunction defaultFunction = getDefaultBIRFunction(fpCall.fp.variableDcl.name.value);
        if (defaultFunction == null) {
            resetBasicBlock(basicBlock);
            return;
        }

        BIRNode.BIRBasicBlock firstBB = defaultFunction.basicBlocks.get(0);
        lastBB = lastBB != null ? lastBB : basicBlock;

        if (containsOnlyConstantLoad(defaultFunction)) {
            moveConstLoadInstruction(fpCall, firstBB);
            lastBB.terminator = null;
            fpRemoved = true;
        } else {
            resetBasicBlock(basicBlock);
        }
    }

    private void handleNewStructure(BIRNonTerminator.NewStructure inst) {
        recordOperandList.remove(inst.rhsOp);
        valueCreated = true;
    }

    private void handleNewTypeDesc(BIRNonTerminator inst) {
        BType referredType = Types.getReferredType(((BIRNonTerminator.NewTypeDesc) inst).type);
        if (referredType.tag == TypeTags.RECORD) {
            recordOperandList.add(inst.lhsOp);
            recordOperandTypeMap.put(inst.lhsOp, (BRecordType) referredType);
        }
    }

    private void moveConstLoadInstruction(BIRTerminator.FPCall fpCall, BIRNode.BIRBasicBlock firstBB) {
        BIRNonTerminator.ConstantLoad constantLoad = (BIRNonTerminator.ConstantLoad) firstBB.instructions.get(0);
        if (firstBB.instructions.size() == 2) {
            BIRNonTerminator.TypeCast typeCast = (BIRNonTerminator.TypeCast) firstBB.instructions.get(1);
            String tempVarName = "%temp_" + typeCast.rhsOp.variableDcl.name.value;
            BIRNode.BIRVariableDcl tempVar;
            if (typecastVars.containsKey(tempVarName)) {
                tempVar = typecastVars.get(tempVarName);
            } else {
                tempVar = new BIRNode.BIRVariableDcl(null, constantLoad.type, new Name(tempVarName),
                        VarScope.FUNCTION, VarKind.TEMP, null);
                typecastVars.put(tempVarName, tempVar);
                currentFunction.localVars.add(tempVar);
            }
            BIROperand tempVarOperand = new BIROperand(tempVar);
            BIRNonTerminator.ConstantLoad newConstLoad = new BIRNonTerminator.ConstantLoad(constantLoad.pos,
                    constantLoad.value, constantLoad.type, tempVarOperand);
            newConstLoad.scope = fpCall.scope;
            lastBB.instructions.add(newConstLoad);
            BIRNonTerminator.TypeCast newTypeCast = new BIRNonTerminator.TypeCast(typeCast.pos, fpCall.lhsOp,
                    tempVarOperand, typeCast.type, typeCast.checkTypes);
            lastBB.instructions.add(newTypeCast);
        } else {
            BIRNonTerminator.ConstantLoad newConstLoad = new BIRNonTerminator.ConstantLoad(constantLoad.pos,
                    constantLoad.value, constantLoad.type, fpCall.lhsOp);
            newConstLoad.scope = fpCall.scope;
            lastBB.instructions.add(newConstLoad);
        }
    }

    private boolean containsOnlyConstantLoad(BIRNode.BIRFunction defaultFunction) {
        if (defaultFunction.basicBlocks.size() != 2) {
            return false;
        }
        BIRNode.BIRBasicBlock firstBB = defaultFunction.basicBlocks.get(0);
        BIRNode.BIRBasicBlock secondBB = defaultFunction.basicBlocks.get(1);
        if (!secondBB.instructions.isEmpty() || secondBB.terminator.kind != InstructionKind.RETURN) {
            return false;
        }
        return switch (firstBB.instructions.size()) {
            case 1 -> firstBB.instructions.get(0).kind == CONST_LOAD;
            case 2 -> firstBB.instructions.get(0).kind == CONST_LOAD && firstBB.instructions.get(1).kind == TYPE_CAST;
            default -> false;
        };
    }

    private void resetBasicBlock(BIRNode.BIRBasicBlock basicBlock) {
        if (lastBB != null) {
            lastBB.terminator = basicBlock.terminator;
            lastBB = null;
        }
        fpRemoved = false;
    }

    private BIRNode.BIRFunction getDefaultBIRFunction(String funcName) {
        for (BIRNode.BIRFunction func : moduleFunctions) {
            if (func.name.value.equals(funcName)) {
                return func;
            }
        }
        return null;
    }

}
