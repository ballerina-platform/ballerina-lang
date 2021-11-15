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

import org.wso2.ballerinalang.compiler.bir.model.ArgumentState;
import org.wso2.ballerinalang.compiler.bir.model.BIRArgument;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Split large BIR functions into smaller methods.
 *
 * @since 2.0
 */
public class LargeMethodOptimizer {

    // splits are done only if the original function has more instructions than the below number
    private static final int FUNCTION_INSTRUCTION_COUNT_THRESHOLD = 1;
    // splits are done only if the newly created method will contain more instructions than the below number
    private static final int SPLIT_INSTRUCTION_COUNT_THRESHOLD = 1;
    // splits are done only if the newly created method will have less function arguments than the below number
    private static final int MAX_SPLIT_FUNCTION_ARG_COUNT = 250;

    public LargeMethodOptimizer() {
    }

    public void splitLargeBIRFunctions(BIRPackage birPkg) {
        for (int funcNum = 0; funcNum < birPkg.functions.size(); funcNum++) {
            BIRFunction function = birPkg.functions.get(funcNum);
            int instructionCount = 0;
            for (BIRBasicBlock basicBlock : function.basicBlocks) {
                instructionCount += basicBlock.instructions.size();
            }
            if (instructionCount < FUNCTION_INSTRUCTION_COUNT_THRESHOLD) {
                continue;
            }

            List<Split> possibleSplits = getPossibleSplits(function.basicBlocks, function.errorTable);

            if (!possibleSplits.isEmpty()) {
                final List<BIRFunction> newlyAddedFunctions = new ArrayList<>();
                generateSplits(birPkg, funcNum, possibleSplits, newlyAddedFunctions);
                birPkg.functions.addAll(newlyAddedFunctions);
            }
        }
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
        boolean splitTypeArray = true; // will be used to mark the split caused from either NewArray or NewStructure
        Set<BIRVariableDcl> neededOperandsVarDcl = new HashSet<>(); // that will need to be passed as function args
        List<BIRVariableDcl> lhsOperandList = new ArrayList<>(); // that will need as localVars in the new function
        BIROperand splitStartOperand = null;

        for (int bbNum = basicBlocks.size() - 1; bbNum >= 0; bbNum--) {
            BIRBasicBlock basicBlock = basicBlocks.get(bbNum);
            BIRTerminator bbTerminator = basicBlock.terminator;
            if (splitStarted) {
                if (bbTerminator.lhsOp != null) {
                    // as the varDcl is available inside the split no need to pass as function arg
                    neededOperandsVarDcl.remove(bbTerminator.lhsOp.variableDcl);
                    lhsOperandList.add(bbTerminator.lhsOp.variableDcl);
                    if (bbTerminator.lhsOp.variableDcl.kind == VarKind.RETURN) {
                        // if the return var is assigned value inside the split, it is not split
                        splitStarted = false;
                    }
                }
                // RHS operands varDcl will be needed to pass as function args if they won't be available inside split
                BIROperand[] rhsOperands = bbTerminator.getRhsOperands();
                for (BIROperand rhsOperand : rhsOperands) {
                    if (needToPassVarDclAsArg(rhsOperand)) {
                        neededOperandsVarDcl.add(rhsOperand.variableDcl);
                    }
                }
            }
            List<BIRNonTerminator> instructions = basicBlock.instructions;
            for (int insNum = instructions.size() - 1; insNum >= 0; insNum--) {
                BIRNonTerminator currIns = instructions.get(insNum);
                if (splitStarted && (currIns.lhsOp.variableDcl.kind == VarKind.RETURN)) {
                    // if the return var is assigned value inside the split, it is not split
                    splitStarted = false;
                }
                if (splitStarted) {
                    neededOperandsVarDcl.remove(currIns.lhsOp.variableDcl);
                    BIROperand[] rhsOperands = currIns.getRhsOperands();
                    lhsOperandList.add(currIns.lhsOp.variableDcl);
                    for (BIROperand rhsOperand : rhsOperands) {
                        if (needToPassVarDclAsArg(rhsOperand)) {
                            neededOperandsVarDcl.add(rhsOperand.variableDcl);
                        }
                    }
                    // now check for the termination of split, i.e. split start
                    if (currIns.lhsOp == splitStartOperand) {
                        if ((neededOperandsVarDcl.size() > MAX_SPLIT_FUNCTION_ARG_COUNT) ||
                                (lhsOperandList.size() < SPLIT_INSTRUCTION_COUNT_THRESHOLD)) {
                            splitStarted = false;
                            continue;
                        }
                        newFuncArgs = new ArrayList<>();
                        for (BIRVariableDcl funcArgVarDcl : neededOperandsVarDcl) {
                            newFuncArgs.add(funcArgVarDcl);
                        }
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

                        // splitTypeArray variable can be used here if that information is needed later
                        possibleSplits.add(new Split(insNum, splitEndInsIndex, bbNum, splitEndBBIndex,
                                lhsOperandList, newFuncArgs, splitErrorTableEntries));
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
                        neededOperandsVarDcl = new HashSet<>();
                        BIROperand[] initialRhsOperands = currIns.getRhsOperands();
                        for (BIROperand rhsOperand : initialRhsOperands) {
                            if (needToPassVarDclAsArg(rhsOperand)) {
                                neededOperandsVarDcl.add(rhsOperand.variableDcl);
                            }
                        }
                        lhsOperandList = new ArrayList<>();
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
        try {
            return Integer.parseInt(bbList.get(bbIndex).toString().substring(2));
        } catch (NumberFormatException e) {
            return bbList.size() - 1;
        }
    }

    private boolean needToPassVarDclAsArg(BIROperand rhsOp) {
        return (!rhsOp.variableDcl.ignoreVariable) && (rhsOp.variableDcl.kind != VarKind.GLOBAL) &&
                (rhsOp.variableDcl.kind != VarKind.CONSTANT);
    }

    /**
     * Generate the given possible splits.
     *
     * @param birPkg available BIRPackage
     * @param funcNum index of the ongoing function in the function list in birPkg
     * @param possibleSplits list of possible splits
     * @param newlyAddedFunctions list to add the newly created functions
     */
    private void generateSplits(BIRPackage birPkg, int funcNum, List<Split> possibleSplits,
                                List<BIRFunction> newlyAddedFunctions) {
        int splitNum = 0; // next split to be done or the ongoing split index
        BIRFunction function = birPkg.functions.get(funcNum);
        String funcName = function.name.toString();
        List<BIRBasicBlock> basicBlocks = function.basicBlocks;
        List<BIRBasicBlock> newBBList = new ArrayList<>();
        int startInsNum = 0; // start instruction index
        int bbNum = 0; // ongoing BB index
        int newBBNum = getBbIdNum(basicBlocks, basicBlocks.size() - 1); // used for newly created BB's id
        int newFuncNum = 0; // to number the splits made using a particular function
        BIRBasicBlock currentBB  = new BIRBasicBlock(new Name("bb" + getBbIdNum(basicBlocks, bbNum)));;

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

            if (!splitsInSameBBList.isEmpty()) {
                // current unfinished BB is passed to the function and a new one is returned from it
                currentBB = generateSplitsInSameBB(birPkg, funcNum, bbNum, splitsInSameBBList,
                        newlyAddedFunctions, newBBNum, newBBList, newFuncNum, startInsNum, currentBB);
                startInsNum = possibleSplits.get(splitNum - 1).lastIns + 1;
                newFuncNum += splitsInSameBBList.size();
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
            newFuncNum += 1;
            Name newFuncName = new Name("$split$" + funcName + "$" + newFuncNum);
            Split currSplit = possibleSplits.get(splitNum);
            splitNum += 1;
            BIRNonTerminator lastInstruction = basicBlocks.get(currSplit.endBBNum).instructions.get(currSplit.lastIns);
            BIROperand newCurrentBBTerminatorLhsOp = new BIROperand(lastInstruction.lhsOp.variableDcl);
            BIRFunction newBIRFunc = createNewBIRFunctionAcrossBB(birPkg, funcNum, newFuncName, currSplit, newBBNum);
            newBBNum += 1;
            newlyAddedFunctions.add(newBIRFunc);
            function.errorTable.removeAll(currSplit.errorTableEntries);
            startInsNum = currSplit.lastIns + 1;
            newBBNum += 1;
            BIRBasicBlock newBB = new BIRBasicBlock(new Name("bb" + newBBNum));
            List<BIRArgument> args = new ArrayList<>();
            for (BIRVariableDcl funcArg : currSplit.funcArgs) {
                args.add(new BIRArgument(ArgumentState.PROVIDED, funcArg));
            }
            currentBB.terminator = new BIRTerminator.Call(lastInstruction.pos, InstructionKind.CALL, false,
                    birPkg.packageID, newFuncName, args, newCurrentBBTerminatorLhsOp, newBB, Collections.emptyList(),
                    Collections.emptySet(), lastInstruction.scope);
            newBBList.add(currentBB);
            currentBB = newBB;
            bbNum = currSplit.endBBNum;
        }

        // newBBList is used as the original function's BB list
        function.basicBlocks = newBBList;
    }

    /**
     * Create a new BIR function which spans across available multiple BBs.
     *
     * @param birPkg available BIRPackage
     * @param funcNum index of the ongoing parent function in the function list in birPkg
     * @param funcName Name of the function to be created
     * @param currSplit ongoing split details
     * @param newBBNum last BB id num of the parent function
     * @return newly created BIR function
     */
    private BIRFunction createNewBIRFunctionAcrossBB(BIRPackage birPkg, int funcNum, Name funcName, Split currSplit,
                                                 int newBBNum) {
        BIRFunction parentFunc = birPkg.functions.get(funcNum);
        List<BIRBasicBlock> parentFuncBBs = parentFunc.basicBlocks;
        BIRNonTerminator lastIns = parentFuncBBs.get(currSplit.endBBNum).instructions.get(currSplit.lastIns);
        BType retType = lastIns.lhsOp.variableDcl.type;
        List<BType> paramTypes = new ArrayList<>();

        for (BIRVariableDcl funcArg : currSplit.funcArgs) {
            paramTypes.add(funcArg.type);
        }
        BInvokableType type = new BInvokableType(paramTypes, retType, null);

        BIRFunction birFunc = new BIRFunction(parentFunc.pos, funcName, funcName, parentFunc.flags, type,
                parentFunc.workerName, 0, parentFunc.origin);

        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        for (BIRVariableDcl funcArg : currSplit.funcArgs) {
            Name argName = funcArg.name;
            birFunc.requiredParams.add(new BIRParameter(lastIns.pos, argName, 0));
            BIRFunctionParameter funcParameter = new BIRFunctionParameter(lastIns.pos, funcArg.type, argName,
                    VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false);
            functionParams.add(funcParameter);
            birFunc.parameters.put(funcParameter, new ArrayList<>());
        }

        birFunc.argsCount = currSplit.funcArgs.size();
        birFunc.returnVariable = new BIRVariableDcl(lastIns.pos, retType, new Name("%0"),
                VarScope.FUNCTION, VarKind.RETURN, null);
        birFunc.localVars.add(0, birFunc.returnVariable);
        birFunc.localVars.addAll(functionParams);
        birFunc.localVars.addAll(new HashSet<>(currSplit.lhsVars));
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
        return birFunc;
    }

    /**
     * Generate the given splits inside the same BB.
     *
     * @param birPkg available BIRPackage
     * @param funcNum index of the ongoing function in the function list in birPkg
     * @param bbNum index of the ongoing BB in the BB list in the ongoing function in birPkg
     * @param possibleSplits list of possible splits
     * @param newlyAddedFunctions list to add the newly created functions
     * @param newBBNum last BB id num of the parent function
     * @param newBBList new BB list creating for the already available parent function
     * @param newFuncNum number to distinguish the newly created functions
     * @param startInsNum index of the start instruction in the BB instruction list
     * @param currentBB BB at the start of the split
     * @return the next BB that will be in the parent function
     */
    private BIRBasicBlock generateSplitsInSameBB(BIRPackage birPkg, int funcNum, int bbNum, List<Split> possibleSplits,
                                                 List<BIRFunction> newlyAddedFunctions, int newBBNum,
                                                 List<BIRBasicBlock> newBBList, int newFuncNum, int startInsNum,
                                                 BIRBasicBlock currentBB) {
        List<BIRNonTerminator> instructionList = birPkg.functions.get(funcNum).basicBlocks.get(bbNum).instructions;
        String funcName = birPkg.functions.get(funcNum).name.toString();

        for (int splitNum = 0; splitNum < possibleSplits.size(); splitNum++) {
            newFuncNum += 1;
            Name newFuncName = new Name("$split$" + funcName + "$" + newFuncNum);
            BIROperand currentBBTerminatorLhsOp =
                    new BIROperand(instructionList.get(possibleSplits.get(splitNum).lastIns).lhsOp.variableDcl);
            newlyAddedFunctions.add(createNewBIRFunctionInBB(birPkg, funcNum, newFuncName,
                    instructionList.get(possibleSplits.get(splitNum).lastIns),
                    instructionList.subList(possibleSplits.get(splitNum).firstIns,
                            possibleSplits.get(splitNum).lastIns), possibleSplits.get(splitNum).lhsVars,
                    possibleSplits.get(splitNum).funcArgs));
            currentBB.instructions.addAll(instructionList.subList(startInsNum, possibleSplits.get(splitNum).firstIns));
            startInsNum = possibleSplits.get(splitNum).lastIns + 1;
            newBBNum += 1;
            BIRBasicBlock newBB = new BIRBasicBlock(new Name("bb" + newBBNum));
            List<BIRArgument> args = new ArrayList<>();
            for (BIRVariableDcl funcArg : possibleSplits.get(splitNum).funcArgs) {
                args.add(new BIRArgument(ArgumentState.PROVIDED, funcArg));
            }
            currentBB.terminator = new BIRTerminator.Call(instructionList.get(possibleSplits.get(splitNum).lastIns).pos,
                    InstructionKind.CALL, false, birPkg.packageID, newFuncName, args, currentBBTerminatorLhsOp,
                    newBB, Collections.emptyList(), Collections.emptySet(),
                    instructionList.get(possibleSplits.get(splitNum).lastIns).scope);
            newBBList.add(currentBB);
            currentBB = newBB;
        }
        // need to handle the last created BB, this is done outside the function
        return currentBB;
    }

    /**
     * Create a new BIR function for a split which starts and ends in the same BB.
     *
     * @param birPkg available BIRPackage
     * @param funcNum index of the ongoing function in the function list in birPkg
     * @param funcName Name of the function to be created
     * @param currentIns last instruction in the split
     * @param collectedIns other instructions in the split
     * @param lhsOperandList list of varDcl of the LHS operands
     * @param funcArgs list of varDcl for the new function arguments
     * @return newly created BIR function
     */
    private BIRFunction createNewBIRFunctionInBB(BIRPackage birPkg, int funcNum, Name funcName,
                                                      BIRNonTerminator currentIns, List<BIRNonTerminator> collectedIns,
                                                      List<BIRVariableDcl> lhsOperandList,
                                                      List<BIRVariableDcl> funcArgs) {
        BIRFunction parentFunc = birPkg.functions.get(funcNum);
        BType retType = currentIns.lhsOp.variableDcl.type;
        List<BType> paramTypes = new ArrayList<>();

        for (BIRVariableDcl funcArg : funcArgs) {
            paramTypes.add(funcArg.type);
        }
        BInvokableType type = new BInvokableType(paramTypes, retType, null);

        BIRFunction birFunc = new BIRFunction(parentFunc.pos, funcName, funcName, parentFunc.flags, type,
                parentFunc.workerName, 0, parentFunc.origin);

        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        for (BIRVariableDcl funcArg : funcArgs) {
            Name argName = funcArg.name;
            birFunc.requiredParams.add(new BIRParameter(currentIns.pos, argName, 0));
            BIRFunctionParameter funcParameter = new BIRFunctionParameter(currentIns.pos, funcArg.type, argName,
                    VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false);
            functionParams.add(funcParameter);
            birFunc.parameters.put(funcParameter, new ArrayList<>());
        }

        birFunc.argsCount = funcArgs.size();
        birFunc.returnVariable = new BIRVariableDcl(currentIns.pos, retType, new Name("%0"),
                VarScope.FUNCTION, VarKind.RETURN, null);
        birFunc.localVars.add(0, birFunc.returnVariable);
        birFunc.localVars.addAll(functionParams);
        birFunc.localVars.addAll(new HashSet<>(lhsOperandList));

        // creates 2 bbs
        BIRBasicBlock entryBB = new BIRBasicBlock(new Name("bb0"));
        entryBB.instructions.addAll(collectedIns);
        BIRNonTerminator lastInstruction = currentIns;
        lastInstruction.lhsOp = new BIROperand(birFunc.returnVariable);
        entryBB.instructions.add(lastInstruction);
        BIRBasicBlock exitBB = new BIRBasicBlock(new Name("bb1"));
        exitBB.terminator = new BIRTerminator.Return(null);
        entryBB.terminator = new BIRTerminator.GOTO(null, exitBB, currentIns.scope);
        birFunc.basicBlocks.add(entryBB);
        birFunc.basicBlocks.add(exitBB);
        return birFunc;
    }
}

class Split {
    int firstIns;
    int lastIns;
    int startBBNum;
    int endBBNum;
    List<BIRVariableDcl> lhsVars;
    List<BIRVariableDcl> funcArgs;
    List<BIRErrorEntry> errorTableEntries;

    protected Split(int firstIns, int lastIns, int startBBNum, int endBBNum, List<BIRVariableDcl> lhsVars,
                 List<BIRVariableDcl> funcArgs, List<BIRErrorEntry> errorTableEntries) {
        this.firstIns = firstIns;
        this.lastIns = lastIns;
        this.startBBNum = startBBNum;
        this.endBBNum = endBBNum;
        this.lhsVars = lhsVars;
        this.funcArgs = funcArgs;
        this.errorTableEntries = errorTableEntries;
    }
}
