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
import java.util.List;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_FUNC;
import static org.wso2.ballerinalang.compiler.bir.codegen.optimizer.LargeMethodOptimizer.SPLIT_METHOD;

public class LazyLoadingDesugar {

    LazyLoadingDataCollector lazyLoadingDataCollector;
    List<BIRNode.BIRBasicBlock> newBBs;
    BIRNode.BIRBasicBlock currentBB;

    public LazyLoadingDesugar(LazyLoadingDataCollector lazyLoadingDataCollector) {
        this.lazyLoadingDataCollector = lazyLoadingDataCollector;
        this.newBBs = new ArrayList<>();
    }

    public void lazyLoadInitFunctionsGlobalVars(List<BIRNode.BIRFunction> functions) {
        for (BIRNode.BIRFunction function : functions) {
            if (function.originalName.value.contains(Names.INIT_FUNCTION_SUFFIX.value)) {
                lazyLoadInitFunctionsGlobalVars(function);
                function.basicBlocks = newBBs;
                newBBs = new ArrayList<>();
            }
        }
    }

    private void lazyLoadInitFunctionsGlobalVars(BIRNode.BIRFunction function) {
        List<BIRNode.BIRBasicBlock> basicBlocks = function.basicBlocks;
        for (int i = 0; i < basicBlocks.size(); i++) {
            BIRNode.BIRBasicBlock basicBlock = basicBlocks.get(i);
            List<BIRNonTerminator> instructions = basicBlock.instructions;
            currentBB = new BIRNode.BIRBasicBlock(basicBlock.id, basicBlock.number);
            lazyLoadInstructions(instructions);
            BIRTerminator terminator = basicBlock.terminator;
            currentBB.terminator = terminator;
            if (terminator.kind == InstructionKind.CALL) {
                lazyLoadCall((BIRTerminator.Call) terminator, currentBB, basicBlocks, i);
            }
            newBBs.add(currentBB);
        }
    }

    private void lazyLoadInstructions(List<BIRNonTerminator> instructions) {
        for (int j = 0; j < instructions.size(); j++) {
            BIRNonTerminator instruction = instructions.get(j);
            switch (instruction.kind) {
                case CONST_LOAD -> lazyLoadConstantLoad((BIRNonTerminator.ConstantLoad) instruction);
                case FP_LOAD -> j = lazyLoadFpLoad((BIRNonTerminator.FPLoad) instruction, instructions, j);
                case NEW_TYPEDESC -> lazyLoadNewTypeDesc((BIRNonTerminator.NewTypeDesc) instruction);
                case NEW_STRUCTURE -> lazyLoadNewStructure((BIRNonTerminator.NewStructure) instruction);
                case TYPE_CAST -> lazyLoadTypeCast((BIRNonTerminator.TypeCast) instruction);
                case NEW_ARRAY -> lazyLoadNewArray((BIRNonTerminator.NewArray) instruction);
                default -> currentBB.instructions.add(instruction);
            }
        }
    }

    private void lazyLoadCall(BIRTerminator.Call terminator, BIRNode.BIRBasicBlock currentBB,
                              List<BIRNode.BIRBasicBlock> basicBlocks, int currentBBIndex) {

        BIRNode.BIRBasicBlock nextBB = basicBlocks.get(currentBBIndex + 1);
        if (terminator.lhsOp != null) {
            BIRNode.BIRVariableDcl variableDcl = terminator.lhsOp.variableDcl;
            if (variableDcl.kind == VarKind.GLOBAL && terminator.name.value.contains(SPLIT_METHOD)) {
                lazyLoadSplitCall(terminator, currentBB, terminator, variableDcl, nextBB);
            } else if (terminator.name.value.contains(ANNOTATION_FUNC)) {
                lazyLoadAnnotationProcessCall(terminator, currentBB, nextBB, terminator);
            }
        }
    }

    private void lazyLoadAnnotationProcessCall(BIRTerminator.Call terminator, BIRNode.BIRBasicBlock currentBB,
                                               BIRNode.BIRBasicBlock nextBB, BIRTerminator.Call call) {
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
        currentBB.terminator = new BIRTerminator.GOTO(terminator.pos, nextBB);
    }

    private void lazyLoadSplitCall(BIRTerminator.Call terminator, BIRNode.BIRBasicBlock currentBB,
                                   BIRTerminator.Call call, BIRNode.BIRVariableDcl variableDcl,
                                   BIRNode.BIRBasicBlock nextBB) {
        if (!call.args.isEmpty()) {
            // This can be split method with multiple calls
            return;
        }
        // Extract split method call
        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(null, call);
        this.lazyLoadingDataCollector.lazyLoadingBBMap.put(variableDcl.name.value, lazyBB);
        currentBB.terminator = new BIRTerminator.GOTO(terminator.pos, nextBB);
    }

    private void lazyLoadConstantLoad(BIRNonTerminator.ConstantLoad constantLoad) {
        if (constantLoad.lhsOp.variableDcl.kind != VarKind.GLOBAL) {
            currentBB.instructions.add(constantLoad);
            return;
        }
        String varName = constantLoad.lhsOp.variableDcl.name.value;
        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(List.of(constantLoad), null);
        this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
    }

    private void lazyLoadNewTypeDesc(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        if (newTypeDesc.lhsOp.variableDcl.kind != VarKind.GLOBAL) {
            currentBB.instructions.add(newTypeDesc);
            return;
        }
        String varName = newTypeDesc.lhsOp.variableDcl.name.value;
        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(List.of(newTypeDesc), null);
        this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
    }

    private void lazyLoadNewStructure(BIRNonTerminator.NewStructure newStructure) {
        if (newStructure.lhsOp.variableDcl.kind != VarKind.GLOBAL) {
            currentBB.instructions.add(newStructure);
            return;
        }
        BIROperand rhsOp = newStructure.rhsOp;
        String varName = newStructure.lhsOp.variableDcl.name.value;
        if (rhsOp.variableDcl.kind != VarKind.GLOBAL) {
            if (copyAndRemovePreviousInstructions(newStructure, rhsOp, varName)) {
                return;
            }
            currentBB.instructions.add(newStructure);
            return;
        }
        List<BIRNode.BIRMappingConstructorEntry> initialValues = newStructure.initialValues;
        if (initialValues.isEmpty()) {
            LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(List.of(newStructure), null);
            this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
            return;
        }
        BIRNode.BIRMappingConstructorEntry firstEntry = initialValues.getFirst();
        if (firstEntry.isKeyValuePair()) {
            BIRNode.BIRMappingConstructorKeyValueEntry keyValueEntry =
                    (BIRNode.BIRMappingConstructorKeyValueEntry) firstEntry;
            if (copyAndRemovePreviousInstructions(newStructure, keyValueEntry.keyOp, varName)) {
                return;
            }
            currentBB.instructions.add(newStructure);
            return;
        }
        BIROperand exprOp = ((BIRNode.BIRMappingConstructorSpreadFieldEntry) firstEntry).exprOp;
        if (exprOp.variableDcl.kind != VarKind.GLOBAL) {
            currentBB.instructions.add(newStructure);
            return;
        }
        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(List.of(newStructure), null);
        this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
    }

    private int lazyLoadFpLoad(BIRNonTerminator.FPLoad fpLoad, List<BIRNonTerminator> instructions, int i) {
        if (fpLoad.lhsOp.variableDcl.kind != VarKind.GLOBAL) {
            currentBB.instructions.add(fpLoad);
            return i;
        }
        String varName = fpLoad.lhsOp.variableDcl.name.value;
        if (instructions.size() > i + 1) {
            BIRNonTerminator nextIns = instructions.get(i + 1);
            if (nextIns.kind == InstructionKind.RECORD_DEFAULT_FP_LOAD) {
                LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(List.of(fpLoad, nextIns), null);
                this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
                return ++i;
            }
            LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(List.of(fpLoad), null);
            this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
            return i;
        }
        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(List.of(fpLoad), null);
        this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
        return i;
    }

    private void lazyLoadNewArray(BIRNonTerminator.NewArray newArray) {
        if (newArray.lhsOp.variableDcl.kind != VarKind.GLOBAL) {
            currentBB.instructions.add(newArray);
            return;
        }
        String varName = newArray.lhsOp.variableDcl.name.value;
        BIROperand extractedOp;
        if (newArray.typedescOp != null) {
            extractedOp = newArray.typedescOp;
        } else {
            extractedOp = newArray.sizeOp;
        }
        if (copyAndRemovePreviousInstructions(newArray, extractedOp, varName)) {
            return;
        }
        currentBB.instructions.add(newArray);
    }

    private void lazyLoadTypeCast(BIRNonTerminator.TypeCast typeCast) {
        if (typeCast.lhsOp.variableDcl.kind != VarKind.GLOBAL || currentBB.instructions.isEmpty()) {
            currentBB.instructions.add(typeCast);
            return;
        }
        String varName = typeCast.lhsOp.variableDcl.name.value;
        BIROperand extractedOp = typeCast.rhsOp;
        if (currentBB.instructions.getLast().kind == InstructionKind.XML_SEQ_STORE && extractXMLValue(typeCast,
                varName)) {
            return;
        } else if (copyAndRemovePreviousInstructions(typeCast, extractedOp, varName)) {
            return;
        }
        currentBB.instructions.add(typeCast);
    }

    private boolean extractXMLValue(BIRNonTerminator.TypeCast typeCast, String varName) {
        int currentBBInsSize = currentBB.instructions.size();
        int lastQNameIns = getLastQNameIns(currentBBInsSize - 1);
        if (lastQNameIns != -1) {
            List<BIRNonTerminator> newInstructions = new ArrayList<>();
            int nsURIOpIndex = lastQNameIns - 3;
            for (int k = nsURIOpIndex; k < currentBBInsSize; k++) {
                newInstructions.add(currentBB.instructions.remove(nsURIOpIndex));
            }
            newInstructions.add(typeCast);
            LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(newInstructions, null);
            this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
            return true;
        }
        return false;
    }

    private int getLastQNameIns(int currentBBLastIndex) {
        int lastQNameIns = -1;
        boolean exitLoop = false;
        for (int j = currentBBLastIndex; j >= 0 && !exitLoop; j--) {
            BIRNonTerminator ins = currentBB.instructions.get(j);
            switch (ins.kind) {
                case CONST_LOAD, NEW_XML_COMMENT, NEW_XML_ELEMENT, NEW_XML_PI, NEW_XML_SEQUENCE, NEW_XML_TEXT,
                     XML_SEQ_STORE, XML_ATTRIBUTE_LOAD, XML_ATTRIBUTE_STORE ->  {
                }
                case NEW_XML_QNAME -> lastQNameIns = j;
                case null, default -> exitLoop = true;
            }
        }
        return lastQNameIns;
    }

    private boolean copyAndRemovePreviousInstructions(BIRNonTerminator instruction, BIROperand birOperand,
                                                      String varName) {
        BIROperand matchOp = birOperand;
        int currentBBInsSize = currentBB.instructions.size();
        for (int j = currentBBInsSize - 1; j >= 0; j--) {
            BIRNonTerminator ins = currentBB.instructions.get(j);
            if (ins.lhsOp.equals(matchOp)) {
                switch (ins.kind) {
                    case CONST_LOAD, NEW_TYPEDESC, FP_LOAD -> {
                        List<BIRNonTerminator> newInstructions = new ArrayList<>();
                        for (int k = j; k < currentBBInsSize; k++) {
                            newInstructions.add(currentBB.instructions.remove(j));
                        }
                        newInstructions.add(instruction);
                        LazyLoadBirBasicBlock lazyBB = new LazyLoadBirBasicBlock(newInstructions, null);
                        this.lazyLoadingDataCollector.lazyLoadingBBMap.put(varName, lazyBB);
                        return true;
                    }
                    case null, default -> {
                        BIROperand[] rhsOperands = ins.getRhsOperands();
                        if (rhsOperands.length == 1) {
                            matchOp = rhsOperands[0];
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }
}
