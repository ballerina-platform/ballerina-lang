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

package org.wso2.ballerinalang.compiler.bir.codegen.optimizer;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Split large BIR functions into smaller methods.
 *
 * @since 2.0
 */
public class LargeMethodOptimizer {

    private final SymbolTable symbolTable;
    // splits are done only if the original function has more instructions than the below number
    private static final int FUNCTION_INSTRUCTION_COUNT_THRESHOLD = 1000;
    // splits are done only if the newly created method will contain more instructions than the below number
    private static final int SPLIT_INSTRUCTION_COUNT_THRESHOLD = 50;
    // splits are done only if the newly created method will have less function arguments than the below number
    private static final int MAX_SPLIT_FUNCTION_ARG_COUNT = 250;
    // current BIR package id
    private PackageID currentPackageId;
    // newly created split BIR function number
    private int splitFuncNum;
    // newly created temporary variable number to handle split function return value
    private int splitTempVarNum;

    public LargeMethodOptimizer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void splitLargeBIRFunctions(BIRPackage birPkg) {
        currentPackageId = birPkg.packageID;
        splitFuncNum = 0;
        splitTempVarNum = 0;

        List<BIRFunction> newlyAddedBIRFunctions = new ArrayList<>();
        for (BIRFunction function : birPkg.functions) {
            if (hasLessInstructionCount(function)) {
                continue;
            }
            List<BIRFunction> newBIRFunctions = splitBIRFunction(function, false);
            newlyAddedBIRFunctions.addAll(newBIRFunctions);
        }
        for (BIRTypeDefinition birTypeDef : birPkg.typeDefs) {
            for (BIRFunction function : birTypeDef.attachedFuncs) {
                if (hasLessInstructionCount(function)) {
                    continue;
                }
                List<BIRFunction> newBIRFunctions = splitBIRFunction(function, true);
                newlyAddedBIRFunctions.addAll(newBIRFunctions);
            }
        }
        birPkg.functions.addAll(newlyAddedBIRFunctions);
    }

    private boolean hasLessInstructionCount(BIRFunction birFunction) {
        int instructionCount = 0;
        for (BIRBasicBlock basicBlock : birFunction.basicBlocks) {
            instructionCount += basicBlock.instructions.size();
        }
        return instructionCount < FUNCTION_INSTRUCTION_COUNT_THRESHOLD;
    }

    private List<BIRFunction> splitBIRFunction(BIRFunction birFunction, boolean fromAttachedFunction) {
        final List<BIRFunction> newlyAddingFunctions = new ArrayList<>();
        List<Split> possibleSplits = getPossibleSplits(birFunction.basicBlocks, birFunction.errorTable);
        if (!possibleSplits.isEmpty()) {
            generateSplits(birFunction, possibleSplits, newlyAddingFunctions, fromAttachedFunction);
        }
        return newlyAddingFunctions;
    }

    /**
     * Obtain a list of possible splits that can be done inside a function due to large no. of instructions.
     *
     * @param basicBlocks available basic block list of the function
     * @param errorTableEntries available error table entries of the function
     * @return a list of possible splits
     */
    private List<Split> getPossibleSplits(List<BIRBasicBlock> basicBlocks, List<BIRErrorEntry> errorTableEntries) {
        List<Split> possibleSplits = new ArrayList<>();
        List<BIRVariableDcl> newFuncArgs;
        int splitEndBBIndex = basicBlocks.size() - 1; // goes from end to beginning
        int splitEndInsIndex = basicBlocks.get(splitEndBBIndex).instructions.size() - 1;
        int errTableEntryIndex = errorTableEntries.size() - 1;
        boolean splitStarted = false;
        boolean returnValAssigned = false;
        boolean splitTypeArray = true; // will be used to mark the split caused from either NewArray or NewStructure
        Set<BIRVariableDcl> neededOperandsVarDcl = new HashSet<>(); // that will need to be passed as function args
        Set<BIRVariableDcl> lhsOperandList = new HashSet<>(); // that will need as localVars in the new function
        BIROperand splitStartOperand = null;
        int splitInsCount = 0; // including terminator instructions

        for (int bbNum = basicBlocks.size() - 1; bbNum >= 0; bbNum--) {
            BIRBasicBlock basicBlock = basicBlocks.get(bbNum);
            BIRTerminator bbTerminator = basicBlock.terminator;
            if (splitStarted) {
                if (bbTerminator.lhsOp != null) {
                    if (bbTerminator.lhsOp.variableDcl.kind == VarKind.LOCAL) {
                        // if a local var is assigned value in a BB terminator, no split is done
                        splitStarted = false;
                    } else if (needToPassLhsVarDclAsArg(bbTerminator.lhsOp)) {
                        neededOperandsVarDcl.add(bbTerminator.lhsOp.variableDcl);
                    } else {
                        neededOperandsVarDcl.remove(bbTerminator.lhsOp.variableDcl);
                        if (isTempOrSyntheticVar(bbTerminator.lhsOp.variableDcl)) {
                            lhsOperandList.add(bbTerminator.lhsOp.variableDcl);
                        }
                    }
                }
                splitInsCount++;
                // RHS operands varDcl will be needed to pass as function args if they won't be available inside split
                BIROperand[] rhsOperands = bbTerminator.getRhsOperands();
                for (BIROperand rhsOperand : rhsOperands) {
                    if (needToPassRhsVarDclAsArg(rhsOperand)) {
                        neededOperandsVarDcl.add(rhsOperand.variableDcl);
                    }
                }
            }
            List<BIRNonTerminator> instructions = basicBlock.instructions;
            for (int insNum = instructions.size() - 1; insNum >= 0; insNum--) {
                BIRNonTerminator currIns = instructions.get(insNum);
                if (currIns.lhsOp.variableDcl.kind == VarKind.LOCAL) {
                    // if the local var is assigned value inside the split, no split is done
                    splitStarted = false;
                }
                if (splitStarted) {
                    if (currIns.lhsOp.variableDcl.kind == VarKind.RETURN) {
                        returnValAssigned = true;
                    } else if (needToPassLhsVarDclAsArg(currIns.lhsOp)) {
                        neededOperandsVarDcl.add(currIns.lhsOp.variableDcl);
                    } else {
                        neededOperandsVarDcl.remove(currIns.lhsOp.variableDcl);
                        if (isTempOrSyntheticVar(currIns.lhsOp.variableDcl)) {
                            lhsOperandList.add(currIns.lhsOp.variableDcl);
                        }
                    }
                    BIROperand[] rhsOperands = currIns.getRhsOperands();
                    for (BIROperand rhsOperand : rhsOperands) {
                        if (needToPassRhsVarDclAsArg(rhsOperand)) {
                            neededOperandsVarDcl.add(rhsOperand.variableDcl);
                        }
                    }
                    splitInsCount++;
                    // now check for the termination of split, i.e. split start
                    if (currIns.lhsOp == splitStartOperand) {
                        if ((neededOperandsVarDcl.size() > MAX_SPLIT_FUNCTION_ARG_COUNT) ||
                                (splitInsCount < SPLIT_INSTRUCTION_COUNT_THRESHOLD)) {
                            splitStarted = false;
                            continue;
                        }
                        newFuncArgs = new ArrayList<>(neededOperandsVarDcl);
                        // create necessary error table entries
                        List<BIRErrorEntry> splitErrorTableEntries = new ArrayList<>();
                        int trapBBNum;
                        while (errTableEntryIndex >= 0) {
                            trapBBNum = Integer.parseInt(
                                    errorTableEntries.get(errTableEntryIndex).trapBB.toString().substring(2));
                            if (trapBBNum <= getBbIdNum(basicBlocks, bbNum)) {
                                break;
                            } else if (trapBBNum < getBbIdNum(basicBlocks, splitEndBBIndex)) {
                                splitErrorTableEntries.add(errorTableEntries.get(errTableEntryIndex));
                            }
                            errTableEntryIndex--;
                        }
                        Collections.reverse(splitErrorTableEntries);

                        // check where the new method needs to be further split
                        boolean splitAgain = splitInsCount >= FUNCTION_INSTRUCTION_COUNT_THRESHOLD;

                        // splitTypeArray variable can be used here if that information is needed later
                        possibleSplits.add(new Split(insNum, splitEndInsIndex, bbNum, splitEndBBIndex,
                                lhsOperandList, newFuncArgs, splitErrorTableEntries, splitAgain, returnValAssigned));
                        splitStarted = false;
                    }
                } else {
                    if ((currIns.kind == InstructionKind.NEW_ARRAY) ||
                            (currIns.kind == InstructionKind.NEW_STRUCTURE)) {
                        // new split can be created from here
                        if (currIns.kind == InstructionKind.NEW_ARRAY) {
                            BIRNonTerminator.NewArray arrayIns = (BIRNonTerminator.NewArray) currIns;
                            splitStartOperand = arrayIns.sizeOp;
                            splitTypeArray = true;
                        } else {
                            BIRNonTerminator.NewStructure structureIns = (BIRNonTerminator.NewStructure) currIns;
                            splitStartOperand = structureIns.rhsOp;
                            splitTypeArray = false;
                        }

                        // if the split will have all the available instructions already in the function -
                        // no need to make that split, avoids doing the same split repeatedly
                        if ((bbNum == basicBlocks.size() - 2) && (!basicBlocks.get(0).instructions.isEmpty()) &&
                                (basicBlocks.get(0).instructions.get(0).lhsOp == splitStartOperand)) {
                            continue;
                        }

                        splitStarted = true;
                        returnValAssigned = false;
                        neededOperandsVarDcl = new HashSet<>();
                        BIROperand[] initialRhsOperands = currIns.getRhsOperands();
                        for (BIROperand rhsOperand : initialRhsOperands) {
                            if (needToPassRhsVarDclAsArg(rhsOperand)) {
                                neededOperandsVarDcl.add(rhsOperand.variableDcl);
                            }
                        }
                        lhsOperandList = new HashSet<>();
                        splitInsCount = 1;
                        splitEndInsIndex = insNum;
                        splitEndBBIndex = bbNum;
                    }
                }
            }
        }
        Collections.reverse(possibleSplits);
        return possibleSplits;
    }

    private int getBbIdNum(List<BIRBasicBlock> bbList, int bbIndex) {
        return Integer.parseInt(bbList.get(bbIndex).toString().substring(2));
    }

    private boolean needToPassRhsVarDclAsArg(BIROperand rhsOp) {
        return (!rhsOp.variableDcl.ignoreVariable) && (rhsOp.variableDcl.kind != VarKind.GLOBAL) &&
                (rhsOp.variableDcl.kind != VarKind.CONSTANT);
    }

    private boolean needToPassLhsVarDclAsArg(BIROperand lhsOp) {
        return (!lhsOp.variableDcl.ignoreVariable) && (lhsOp.variableDcl.kind == VarKind.SELF);
    }

    /**
     * Generate the given possible splits.
     * @param function original BIR function
     * @param possibleSplits list of possible splits
     * @param newlyAddedFunctions list to add the newly created functions
     * @param fromAttachedFunction flag which indicates an original attached function is being split
     */
    private void generateSplits(BIRFunction function, List<Split> possibleSplits,
                                List<BIRFunction> newlyAddedFunctions, boolean fromAttachedFunction) {
        int splitNum = 0; // next split to be done or the ongoing split index
        List<BIRBasicBlock> basicBlocks = function.basicBlocks;
        List<BIRBasicBlock> newBBList = new ArrayList<>();
        int startInsNum = 0; // start instruction index
        int bbNum = 0; // ongoing BB index
        int newBBNum = getBbIdNum(basicBlocks, basicBlocks.size() - 1); // used for newly created BB's id
        BIRBasicBlock currentBB  = new BIRBasicBlock(new Name("bb" + getBbIdNum(basicBlocks, bbNum)));
        Map<String, String> changedLocalVarStartBB = new HashMap<>(); // key: oldBBId, value: newBBId
        Map<String, String> changedLocalVarEndBB = new HashMap<>(); // key: oldBBId, value: newBBId

        while (bbNum < basicBlocks.size()) {
            // it is either the start of a new BB or after split across BBs (say jump) now in the middle of a BB
            // here variable currentBB is the BB where next instructions are going to be added
            if (startInsNum == 0) {
                if (splitNum >= possibleSplits.size()) {
                    // if there are no more splits just copy the remaining BBs and break
                    for (; bbNum < basicBlocks.size(); bbNum++) {
                        newBBList.add(basicBlocks.get(bbNum));
                    }
                    break;
                } else if (bbNum < possibleSplits.get(splitNum).startBBNum) {
                    // can just copy the BBs till the BB where a split starts
                    for (; bbNum < possibleSplits.get(splitNum).startBBNum; bbNum++) {
                        newBBList.add(basicBlocks.get(bbNum));
                    }
                    currentBB = new BIRBasicBlock(new Name("bb" + getBbIdNum(basicBlocks, bbNum)));
                    continue;
                }
            } else if (splitNum >= possibleSplits.size()) {
                // came from a jump, so if there are no more splits can put the remaining instructions in the bb
                // and continue to next bb
                if (startInsNum < basicBlocks.get(bbNum).instructions.size()) {
                    currentBB.instructions.addAll(basicBlocks.get(bbNum).instructions.subList(startInsNum,
                            basicBlocks.get(bbNum).instructions.size()));
                }
                currentBB.terminator = basicBlocks.get(bbNum).terminator;
                newBBList.add(currentBB);
                changedLocalVarStartBB.put(basicBlocks.get(bbNum).id.value, currentBB.id.value);
                changedLocalVarEndBB.put(basicBlocks.get(bbNum).id.value, currentBB.id.value);
                startInsNum = 0;
                bbNum += 1;
                continue;
                // do not need to create a new BB because hereafter BBs will be just copied
            }

            // now there is definitely a split starts form this BB, first will handle the splits inside the same BB
            // here a list of splits starting from this BB and ends in this BB is obtained
            // so after creating splits have to handle the last instructions in the BB or a jump to another BB
            List<Split> splitsInSameBBList = new ArrayList<>();
            while (splitNum < possibleSplits.size()) {
                if (possibleSplits.get(splitNum).endBBNum == bbNum) {
                    splitsInSameBBList.add(possibleSplits.get(splitNum));
                    splitNum++;
                } else {
                    break;
                }
            }
            // so the next splitNum contains a split starts from this BB or another BB, but do not end in this BB
            changedLocalVarStartBB.put(basicBlocks.get(bbNum).id.value, currentBB.id.value);
            if (!splitsInSameBBList.isEmpty()) {
                // current unfinished BB is passed to the function and a new one is returned from it
                currentBB = generateSplitsInSameBB(function, bbNum, splitsInSameBBList, newlyAddedFunctions,
                        newBBNum, newBBList, startInsNum, currentBB, fromAttachedFunction);
                startInsNum = possibleSplits.get(splitNum - 1).lastIns + 1;
                newBBNum += splitsInSameBBList.size();
            }

            // handle if splits are over or if the next spilt is in another BB
            if ((splitNum >= possibleSplits.size()) || (possibleSplits.get(splitNum).startBBNum > bbNum)) {
                if (startInsNum < basicBlocks.get(bbNum).instructions.size()) {
                    currentBB.instructions.addAll(basicBlocks.get(bbNum).instructions.subList(startInsNum,
                            basicBlocks.get(bbNum).instructions.size()));
                }
                currentBB.terminator = basicBlocks.get(bbNum).terminator;
                newBBList.add(currentBB);
                changedLocalVarEndBB.put(basicBlocks.get(bbNum).id.value, currentBB.id.value);
                startInsNum = 0;
                bbNum += 1;
                currentBB = new BIRBasicBlock(new Name("bb" + getBbIdNum(basicBlocks, bbNum)));
                continue;
            }

            // now we have a split that go to another BB from this BB
            // need to add remaining instructions then create a split function
            if (startInsNum < possibleSplits.get(splitNum).firstIns) {
                currentBB.instructions.addAll(basicBlocks.get(bbNum).instructions.subList(startInsNum,
                        possibleSplits.get(splitNum).firstIns));
            }
            splitFuncNum += 1;
            String newFunctionName = "$split$method$" + splitFuncNum;
            Name newFuncName = new Name(newFunctionName);
            Split currSplit = possibleSplits.get(splitNum);
            splitNum += 1;
            BIRNonTerminator lastInstruction = basicBlocks.get(currSplit.endBBNum).instructions.get(currSplit.lastIns);
            BType newFuncReturnType = lastInstruction.lhsOp.variableDcl.type;
            BIROperand splitLastInsLhsOp = new BIROperand(lastInstruction.lhsOp.variableDcl);
            BIROperand splitFuncCallResultOp;
            if (currSplit.returnValAssigned) {
                splitFuncCallResultOp = generateTempLocalVariable(function, newFuncReturnType);
                newFuncReturnType = createErrorUnionReturnType(newFuncReturnType);
            } else {
                splitFuncCallResultOp = splitLastInsLhsOp;
            }
            BIRFunction newBIRFunc = createNewBIRFunctionAcrossBB(function, newFuncName, newFuncReturnType, currSplit,
                    newBBNum, fromAttachedFunction);
            newBBNum += 1;
            newlyAddedFunctions.add(newBIRFunc);
            if (currSplit.splitFurther) {
                newlyAddedFunctions.addAll(splitBIRFunction(newBIRFunc, fromAttachedFunction));
            }
            function.errorTable.removeAll(currSplit.errorTableEntries);
            startInsNum = currSplit.lastIns + 1;
            newBBNum += 1;
            BIRBasicBlock newBB = new BIRBasicBlock(new Name("bb" + newBBNum));
            List<BIROperand> args = new ArrayList<>();
            for (BIRVariableDcl funcArg : currSplit.funcArgs) {
                args.add(new BIROperand(funcArg));
            }
            currentBB.terminator = new BIRTerminator.Call(lastInstruction.pos, InstructionKind.CALL, false,
                    currentPackageId, newFuncName, args, splitFuncCallResultOp, newBB, Collections.emptyList(),
                    Collections.emptySet(), lastInstruction.scope);
            newBBList.add(currentBB);
            changedLocalVarEndBB.put(basicBlocks.get(bbNum).id.value, currentBB.id.value);
            currentBB = newBB;
            if (currSplit.returnValAssigned) {
                currentBB = handleNewFuncReturnVal(function, splitFuncCallResultOp, lastInstruction.scope, currentBB,
                        newBBNum, newBBList, splitLastInsLhsOp);
                newBBNum += 2;
            }
            bbNum = currSplit.endBBNum;
        }

        // set startBB and endBB for local vars in the function
        setLocalVarStartEndBB(function, newBBList, changedLocalVarStartBB, changedLocalVarEndBB);
        // newBBList is used as the original function's BB list
        function.basicBlocks = newBBList;
        // unused temp and synthetic vars in the original function are removed
        // and onlyUsedInSingleBB flag in BIRVariableDcl is changed in needed places
        removeUnusedVarsAndSetVarUsage(function);
    }

    private BType createErrorUnionReturnType(BType newFuncReturnType) {
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(2);
        memberTypes.add(newFuncReturnType);
        memberTypes.add(symbolTable.errorType);
        return new BUnionType(null, memberTypes, false, false);
    }

    private BIRBasicBlock handleNewFuncReturnVal(BIRFunction function, BIROperand splitFuncCallResultOp,
                                                 BirScope lastInsScope, BIRBasicBlock currentBB, int newBBNum,
                                                 List<BIRBasicBlock> newBBList, BIROperand splitLastInsLhsOp) {
        BIROperand isErrorResultOp = generateTempLocalVariable(function, symbolTable.booleanType);
        BIRNonTerminator errorTestIns = new BIRNonTerminator.TypeTest(null, symbolTable.errorType,
                isErrorResultOp, splitFuncCallResultOp);
        currentBB.instructions.add(errorTestIns);
        BIRBasicBlock trueBB = new BIRBasicBlock(new Name("bb" + ++newBBNum));
        BIRBasicBlock falseBB = new BIRBasicBlock(new Name("bb" + ++newBBNum));
        currentBB.terminator = new BIRTerminator.Branch(null, isErrorResultOp, trueBB, falseBB, lastInsScope);
        newBBList.add(currentBB);
        BIROperand castedErrorOp = generateTempLocalVariable(function, symbolTable.errorType);
        BIRNonTerminator typeCastErrIns = new BIRNonTerminator.TypeCast(null, castedErrorOp,
                splitFuncCallResultOp, symbolTable.errorType, false);
        trueBB.instructions.add(typeCastErrIns);
        BIRNonTerminator moveIns = new BIRNonTerminator.Move(null, castedErrorOp,
                new BIROperand(function.returnVariable));
        trueBB.instructions.add(moveIns);
        BIRBasicBlock returnBB = function.basicBlocks.get(function.basicBlocks.size() - 1);
        trueBB.terminator = new BIRTerminator.GOTO(null, returnBB, lastInsScope);
        newBBList.add(trueBB);
        BIRNonTerminator typeCastLhsOpIns = new BIRNonTerminator.TypeCast(null, splitLastInsLhsOp,
                splitFuncCallResultOp, splitLastInsLhsOp.variableDcl.type, false);
        falseBB.instructions.add(typeCastLhsOpIns);
        return falseBB;
    }

    private boolean isTempOrSyntheticVar(BIRVariableDcl variableDcl) {
        return (variableDcl.kind == VarKind.TEMP) || (variableDcl.kind == VarKind.SYNTHETIC);
    }

    private Set<BIRVariableDcl> findUsedVars(BIRBasicBlock basicBlock, Set<BIRVariableDcl> usedTempAndSyntheticVars,
                                             Set<BIRVariableDcl> allUsedVars, Set<BIRVariableDcl> multipleBBUsedVars) {
        Set<BIRVariableDcl> thisBBUsedAllVars = new HashSet<>();
        for (BIRNonTerminator instruction : basicBlock.instructions) {
            thisBBUsedAllVars.add(instruction.lhsOp.variableDcl);
            if (allUsedVars.contains(instruction.lhsOp.variableDcl)) {
                multipleBBUsedVars.add(instruction.lhsOp.variableDcl);
            }
            if (isTempOrSyntheticVar(instruction.lhsOp.variableDcl)) {
                usedTempAndSyntheticVars.add(instruction.lhsOp.variableDcl);
            }
            BIROperand[] rhsOperands = instruction.getRhsOperands();
            for (BIROperand rhsOperand : rhsOperands) {
                thisBBUsedAllVars.add(rhsOperand.variableDcl);
                if (allUsedVars.contains(rhsOperand.variableDcl)) {
                    multipleBBUsedVars.add(rhsOperand.variableDcl);
                }
                if (isTempOrSyntheticVar(rhsOperand.variableDcl)) {
                    usedTempAndSyntheticVars.add(rhsOperand.variableDcl);
                }
            }
        }
        if (basicBlock.terminator.lhsOp != null) {
            thisBBUsedAllVars.add(basicBlock.terminator.lhsOp.variableDcl);
            if (allUsedVars.contains(basicBlock.terminator.lhsOp.variableDcl)) {
                multipleBBUsedVars.add(basicBlock.terminator.lhsOp.variableDcl);
            }
            if (isTempOrSyntheticVar(basicBlock.terminator.lhsOp.variableDcl)) {
                usedTempAndSyntheticVars.add(basicBlock.terminator.lhsOp.variableDcl);
            }
        }
        BIROperand[] rhsOperands = basicBlock.terminator.getRhsOperands();
        for (BIROperand rhsOperand : rhsOperands) {
            thisBBUsedAllVars.add(rhsOperand.variableDcl);
            if (allUsedVars.contains(rhsOperand.variableDcl)) {
                multipleBBUsedVars.add(rhsOperand.variableDcl);
            }
            if (isTempOrSyntheticVar(rhsOperand.variableDcl)) {
                usedTempAndSyntheticVars.add(rhsOperand.variableDcl);
            }
        }
        return thisBBUsedAllVars;
    }

    private void removeUnusedVarsAndSetVarUsage(BIRFunction birFunction) {
        Set<BIRVariableDcl> usedTempAndSyntheticVars = new HashSet<>();
        Set<BIRVariableDcl> allUsedVars = new HashSet<>();
        Set<BIRVariableDcl> multipleBBUsedVars = new HashSet<>();
        for (BIRBasicBlock basicBlock : birFunction.basicBlocks) {
            allUsedVars.addAll(findUsedVars(basicBlock, usedTempAndSyntheticVars, allUsedVars, multipleBBUsedVars));
        }

        List<BIRVariableDcl> newLocalVars = new ArrayList<>();
        for (BIRVariableDcl localVar : birFunction.localVars) {
            if ((localVar.onlyUsedInSingleBB) && multipleBBUsedVars.contains(localVar)) {
                localVar.onlyUsedInSingleBB = false;
            }
            if (isTempOrSyntheticVar(localVar)) {
                if (usedTempAndSyntheticVars.contains(localVar)) {
                    newLocalVars.add(localVar);
                }
            } else {
                newLocalVars.add(localVar);
            }
        }
        birFunction.localVars = newLocalVars;
    }

    private void setLocalVarStartEndBB(BIRFunction birFunction, List<BIRBasicBlock> newBBList, Map<String, String>
            changedLocalVarStartBB, Map<String, String> changedLocalVarEndBB) {

        Map<String, BIRBasicBlock> newBBs = new HashMap<>();
        for (BIRBasicBlock newBB : newBBList) {
            newBBs.put(newBB.id.value, newBB);
        }
        for (BIRVariableDcl localVar : birFunction.localVars) {
            if (localVar.kind == VarKind.LOCAL) {
                if (localVar.startBB != null) {
                    if (changedLocalVarStartBB.containsKey(localVar.startBB.id.value)) {
                        localVar.startBB = newBBs.get(changedLocalVarStartBB.get(localVar.startBB.id.value));
                    } else {
                        localVar.startBB = newBBs.get(localVar.startBB.id.value);
                    }
                }
                if (localVar.endBB != null) {
                    if (changedLocalVarEndBB.containsKey(localVar.endBB.id.value)) {
                        localVar.endBB = newBBs.get(changedLocalVarEndBB.get(localVar.endBB.id.value));
                    } else {
                        localVar.endBB = newBBs.get(localVar.endBB.id.value);
                    }
                }
            }
        }
    }

    /**
     * Create a new BIR function which spans across available multiple BBs.
     *
     * @param parentFunc parent BIR function
     * @param funcName Name of the function to be created
     * @param currSplit ongoing split details
     * @param newBBNum last BB id num of the parent function
     * @param fromAttachedFunction flag which indicates an original attached function is being split
     * @return newly created BIR function
     */
    private BIRFunction createNewBIRFunctionAcrossBB(BIRFunction parentFunc, Name funcName, BType retType,
                                                     Split currSplit, int newBBNum, boolean fromAttachedFunction) {
        List<BIRBasicBlock> parentFuncBBs = parentFunc.basicBlocks;
        BIRNonTerminator lastIns = parentFuncBBs.get(currSplit.endBBNum).instructions.get(currSplit.lastIns);
        List<BType> paramTypes = new ArrayList<>();
        for (BIRVariableDcl funcArg : currSplit.funcArgs) {
            paramTypes.add(funcArg.type);
        }
        BInvokableType type = new BInvokableType(paramTypes, retType, null);

        BIRFunction birFunc = new BIRFunction(parentFunc.pos, funcName, funcName, parentFunc.flags, type,
                parentFunc.workerName, 0, parentFunc.origin);

        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        BIRVariableDcl selfVarDcl = null;
        for (BIRVariableDcl funcArg : currSplit.funcArgs) {
            Name argName = funcArg.name;
            birFunc.requiredParams.add(new BIRParameter(lastIns.pos, argName, 0));
            BIRFunctionParameter funcParameter = new BIRFunctionParameter(lastIns.pos, funcArg.type, argName,
                    VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false);
            functionParams.add(funcParameter);
            birFunc.parameters.add(funcParameter);
            if (funcArg.kind == VarKind.SELF) {
                selfVarDcl = new BIRVariableDcl(funcArg.pos, funcArg.type, funcArg.name, funcArg.originalName,
                        VarScope.FUNCTION, VarKind.ARG, funcArg.metaVarName);
            }
        }

        birFunc.argsCount = currSplit.funcArgs.size();
        if (fromAttachedFunction) {
            birFunc.returnVariable = new BIRVariableDcl(lastIns.pos, retType, new Name("%1"),
                    VarScope.FUNCTION, VarKind.RETURN, null);
        } else {
            birFunc.returnVariable = new BIRVariableDcl(lastIns.pos, retType, new Name("%0"),
                    VarScope.FUNCTION, VarKind.RETURN, null);
        }
        birFunc.localVars.add(0, birFunc.returnVariable);
        birFunc.localVars.addAll(functionParams);
        birFunc.localVars.addAll(currSplit.lhsVars);
        birFunc.errorTable.addAll(currSplit.errorTableEntries);

        // creates BBs
        BIRBasicBlock entryBB = new BIRBasicBlock(new Name("bb0"));
        if (currSplit.firstIns < parentFuncBBs.get(currSplit.startBBNum).instructions.size()) {
            entryBB.instructions.addAll(parentFuncBBs.get(currSplit.startBBNum).instructions.subList(
                    currSplit.firstIns, parentFuncBBs.get(currSplit.startBBNum).instructions.size()));
        }
        entryBB.terminator = parentFuncBBs.get(currSplit.startBBNum).terminator;
        birFunc.basicBlocks.add(entryBB);

        // copying BBs between first and last bb
        for (int bbNum = currSplit.startBBNum + 1; bbNum < currSplit.endBBNum; bbNum++) {
            birFunc.basicBlocks.add(parentFuncBBs.get(bbNum));
        }

        // create last BB and the return BB (exit BB)
        BIRBasicBlock lastBB = new BIRBasicBlock(new Name("bb" + getBbIdNum(parentFuncBBs, currSplit.endBBNum)));
        if (0 <= currSplit.lastIns) {
            lastBB.instructions.addAll(parentFuncBBs.get(currSplit.endBBNum).instructions.subList(
                    0, currSplit.lastIns + 1));
        }
        lastBB.instructions.get(currSplit.lastIns).lhsOp = new BIROperand(birFunc.returnVariable);

        // a new BB with id using newBBNum is used to include the return statement
        newBBNum += 1;
        BIRBasicBlock exitBB = new BIRBasicBlock(new Name("bb" + newBBNum));
        exitBB.terminator = new BIRTerminator.Return(null);
        lastBB.terminator = new BIRTerminator.GOTO(null, exitBB, lastIns.scope);
        birFunc.basicBlocks.add(lastBB);
        birFunc.basicBlocks.add(exitBB);
        rectifyVarKindsAndTerminators(birFunc, selfVarDcl, exitBB);
        return birFunc;
    }

    /**
     * Generate the given splits inside the same BB.
     *
     * @param function parent function
     * @param bbNum index of the ongoing BB in the BB list in the ongoing function in birPkg
     * @param possibleSplits list of possible splits
     * @param newlyAddedFunctions list to add the newly created functions
     * @param newBBNum last BB id num of the parent function
     * @param newBBList new BB list creating for the already available parent function
     * @param startInsNum index of the start instruction in the BB instruction list
     * @param currentBB BB at the start of the split
     * @param fromAttachedFunction flag which indicates an original attached function is being split
     * @return the next BB that will be in the parent function
     */
    private BIRBasicBlock generateSplitsInSameBB(BIRFunction function, int bbNum, List<Split> possibleSplits,
                                                 List<BIRFunction> newlyAddedFunctions, int newBBNum,
                                                 List<BIRBasicBlock> newBBList, int startInsNum,
                                                 BIRBasicBlock currentBB, boolean fromAttachedFunction) {
        List<BIRNonTerminator> instructionList = function.basicBlocks.get(bbNum).instructions;

        for (int splitNum = 0; splitNum < possibleSplits.size(); splitNum++) {
            splitFuncNum += 1;
            String newFunctionName = "$split$method$" + splitFuncNum;
            Name newFuncName = new Name(newFunctionName);
            BIROperand currentBBTerminatorLhsOp =
                    new BIROperand(instructionList.get(possibleSplits.get(splitNum).lastIns).lhsOp.variableDcl);
            BIRFunction newBIRFunc = createNewBIRFuncForSplitInBB(function, newFuncName,
                    instructionList.get(possibleSplits.get(splitNum).lastIns),
                    instructionList.subList(possibleSplits.get(splitNum).firstIns,
                            possibleSplits.get(splitNum).lastIns),
                    possibleSplits.get(splitNum).lhsVars, possibleSplits.get(splitNum).funcArgs, fromAttachedFunction);
            newlyAddedFunctions.add(newBIRFunc);
            if (possibleSplits.get(splitNum).splitFurther) {
                newlyAddedFunctions.addAll(splitBIRFunction(newBIRFunc, fromAttachedFunction));
            }
            currentBB.instructions.addAll(instructionList.subList(startInsNum, possibleSplits.get(splitNum).firstIns));
            startInsNum = possibleSplits.get(splitNum).lastIns + 1;
            newBBNum += 1;
            BIRBasicBlock newBB = new BIRBasicBlock(new Name("bb" + newBBNum));
            List<BIROperand> args = new ArrayList<>();
            for (BIRVariableDcl funcArg : possibleSplits.get(splitNum).funcArgs) {
                args.add(new BIROperand(funcArg));
            }
            currentBB.terminator = new BIRTerminator.Call(instructionList.get(possibleSplits.get(splitNum).lastIns).pos,
                    InstructionKind.CALL, false, currentPackageId, newFuncName, args, currentBBTerminatorLhsOp,
                    newBB, Collections.emptyList(), Collections.emptySet(),
                    instructionList.get(possibleSplits.get(splitNum).lastIns).scope);
            newBBList.add(currentBB);
            currentBB = newBB;
        }
        // need to handle the last created BB, this is done outside the function
        return currentBB;
    }

    /**
     * Generate a temporary function scope variable.
     *
     * @param func The BIR function to which the variable should be added
     * @param variableType The type of the variable
     * @return The generated operand for the variable declaration
     */
    private BIROperand generateTempLocalVariable(BIRFunction func, BType variableType) {
        Name variableName = new Name("$split$tempVar$" + splitTempVarNum++);
        BIRVariableDcl variableDcl = new BIRVariableDcl(variableType, variableName, VarScope.FUNCTION, VarKind.TEMP);
        func.localVars.add(variableDcl);
        return new BIROperand(variableDcl);
    }

    /**
     * Create a new BIR function for a split which starts and ends in the same BB.
     *
     * @param parentFunc parent BIR function
     * @param funcName Name of the function to be created
     * @param currentIns last instruction in the split
     * @param collectedIns other instructions in the split
     * @param lhsOperandList set of varDcl of the LHS operands
     * @param funcArgs list of varDcl for the new function arguments
     * @param fromAttachedFunction flag which indicates an original attached function is being split
     * @return newly created BIR function
     */
    private BIRFunction createNewBIRFuncForSplitInBB(BIRFunction parentFunc, Name funcName, BIRNonTerminator currentIns,
                                                     List<BIRNonTerminator> collectedIns,
                                                     Set<BIRVariableDcl> lhsOperandList, List<BIRVariableDcl> funcArgs,
                                                     boolean fromAttachedFunction) {
        BType retType = currentIns.lhsOp.variableDcl.type;
        List<BType> paramTypes = new ArrayList<>();
        for (BIRVariableDcl funcArg : funcArgs) {
            paramTypes.add(funcArg.type);
        }
        BInvokableType type = new BInvokableType(paramTypes, retType, null);

        BIRFunction birFunc = new BIRFunction(parentFunc.pos, funcName, funcName, parentFunc.flags, type,
                parentFunc.workerName, 0, parentFunc.origin);

        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        BIRVariableDcl selfVarDcl = null;
        for (BIRVariableDcl funcArg : funcArgs) {
            Name argName = funcArg.name;
            birFunc.requiredParams.add(new BIRParameter(currentIns.pos, argName, 0));
            BIRFunctionParameter funcParameter = new BIRFunctionParameter(currentIns.pos, funcArg.type, argName,
                    VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false);
            functionParams.add(funcParameter);
            birFunc.parameters.add(funcParameter);
            if (funcArg.kind == VarKind.SELF) {
                selfVarDcl = new BIRVariableDcl(funcArg.pos, funcArg.type, funcArg.name, funcArg.originalName,
                        VarScope.FUNCTION, VarKind.ARG, funcArg.metaVarName);
            }
        }

        birFunc.argsCount = funcArgs.size();
        if (fromAttachedFunction) {
            birFunc.returnVariable = new BIRVariableDcl(currentIns.pos, retType, new Name("%1"),
                    VarScope.FUNCTION, VarKind.RETURN, null);
        } else {
            birFunc.returnVariable = new BIRVariableDcl(currentIns.pos, retType, new Name("%0"),
                    VarScope.FUNCTION, VarKind.RETURN, null);
        }
        birFunc.localVars.add(0, birFunc.returnVariable);
        birFunc.localVars.addAll(functionParams);
        birFunc.localVars.addAll(lhsOperandList);

        // creates 2 bbs
        BIRBasicBlock entryBB = new BIRBasicBlock(new Name("bb0"));
        entryBB.instructions.addAll(collectedIns);
        currentIns.lhsOp = new BIROperand(birFunc.returnVariable);
        entryBB.instructions.add(currentIns);
        BIRBasicBlock exitBB = new BIRBasicBlock(new Name("bb1"));
        exitBB.terminator = new BIRTerminator.Return(null);
        entryBB.terminator = new BIRTerminator.GOTO(null, exitBB, currentIns.scope);
        birFunc.basicBlocks.add(entryBB);
        birFunc.basicBlocks.add(exitBB);
        rectifyVarKindsAndTerminators(birFunc, selfVarDcl, exitBB);
        return birFunc;
    }

    private BIRVariableDcl getArgVarKindVarDcl(BIRVariableDcl variableDcl) {
        return new BIRVariableDcl(variableDcl.pos, variableDcl.type, variableDcl.name, variableDcl.originalName,
                VarScope.FUNCTION, VarKind.ARG, variableDcl.metaVarName);
    }

    private void rectifyVarKindsAndTerminators(BIRFunction birFunction, BIRVariableDcl selfVarDcl,
                                               BIRBasicBlock returnBB) {
        for (BIRBasicBlock basicBlock : birFunction.basicBlocks) {
            for (BIRNonTerminator instruction : basicBlock.instructions) {
                if (instruction.lhsOp.variableDcl.kind == VarKind.SELF) {
                    instruction.lhsOp.variableDcl = selfVarDcl;
                } else if (instruction.lhsOp.variableDcl.kind == VarKind.RETURN) {
                    instruction.lhsOp.variableDcl = birFunction.returnVariable;
                    basicBlock.terminator = new BIRTerminator.GOTO(null, returnBB, basicBlock.terminator.scope);
                }
                BIROperand[] rhsOperands = instruction.getRhsOperands();
                for (BIROperand rhsOperand : rhsOperands) {
                    if (rhsOperand.variableDcl.kind == VarKind.SELF) {
                        rhsOperand.variableDcl = selfVarDcl;
                    } else if (rhsOperand.variableDcl.kind == VarKind.LOCAL) {
                        rhsOperand.variableDcl = getArgVarKindVarDcl(rhsOperand.variableDcl);
                    }
                }
            }
            if ((basicBlock.terminator.lhsOp != null) &&
                    (basicBlock.terminator.lhsOp.variableDcl.kind == VarKind.SELF)) {
                basicBlock.terminator.lhsOp.variableDcl = selfVarDcl;
            }
            BIROperand[] rhsOperands = basicBlock.terminator.getRhsOperands();
            for (BIROperand rhsOperand : rhsOperands) {
                if (rhsOperand.variableDcl.kind == VarKind.SELF) {
                    rhsOperand.variableDcl = selfVarDcl;
                } else if (rhsOperand.variableDcl.kind == VarKind.LOCAL) {
                    rhsOperand.variableDcl = getArgVarKindVarDcl(rhsOperand.variableDcl);
                }
            }
        }
    }

    static class Split {
        int firstIns;
        int lastIns;
        int startBBNum;
        int endBBNum;
        boolean splitFurther;
        boolean returnValAssigned;
        Set<BIRVariableDcl> lhsVars;
        List<BIRVariableDcl> funcArgs;
        List<BIRErrorEntry> errorTableEntries;

        private Split(int firstIns, int lastIns, int startBBNum, int endBBNum, Set<BIRVariableDcl> lhsVars,
                        List<BIRVariableDcl> funcArgs, List<BIRErrorEntry> errorTableEntries, boolean splitFurther,
                      boolean returnValAssigned) {
            this.firstIns = firstIns;
            this.lastIns = lastIns;
            this.startBBNum = startBBNum;
            this.endBBNum = endBBNum;
            this.splitFurther = splitFurther;
            this.returnValAssigned = returnValAssigned;
            this.lhsVars = lhsVars;
            this.funcArgs = funcArgs;
            this.errorTableEntries = errorTableEntries;
        }
    }
}
