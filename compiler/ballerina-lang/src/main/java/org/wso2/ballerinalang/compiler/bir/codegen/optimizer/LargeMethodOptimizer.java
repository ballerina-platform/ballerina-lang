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

    public LargeMethodOptimizer() {
    }

    public void splitLargeBIRFunctions(BIRPackage birPkg) {
        int functionInstructionThreshold = 1;

        for (int funcNum = 0; funcNum < birPkg.functions.size(); funcNum++) {
            BIRFunction function = birPkg.functions.get(funcNum);
            int instructionCount = 0;
            for (BIRBasicBlock basicBlock : function.basicBlocks) {
                instructionCount += basicBlock.instructions.size();
            }
            if (instructionCount < functionInstructionThreshold) {
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

    private int getBbIdNum(List<BIRBasicBlock> bbList, int bbIndex) {
        try {
            return Integer.parseInt(bbList.get(bbIndex).toString().substring(2));
        } catch (NumberFormatException e) {
            return bbList.size() - 1;
        }
    }

    private void generateSplits(BIRPackage birPkg, int funcNum, List<Split> possibleSplits,
                                List<BIRFunction> newlyAddedFunctions) {

        int splitNum = 0; //nextsplit or the current split in

        BIRFunction function = birPkg.functions.get(funcNum);
        String funcName = function.name.toString();
        List<BIRBasicBlock> basicBlocks = function.basicBlocks;
        List<BIRBasicBlock> newBBList = new ArrayList<>();
        int startInsNum = 0;
        int bbNum = 0;
        // newBBNum have last created BB number so always add 1 and use (for safety)
        int newBBNum = getBbIdNum(basicBlocks, basicBlocks.size() - 1);
        int newFuncNum = 0; // hence newFuncNum are as 1,2,3,..
        BIRBasicBlock currentBB  = new BIRBasicBlock(new Name("bb" + getBbIdNum(basicBlocks, bbNum)));;

        // i also want to check whether any splits are remaining and if no just fill the ins
        while (bbNum < basicBlocks.size()) {
            // so at the loop beginning it is either the start of a new BB or after jumping from a BB
            if (startInsNum == 0) {
                if (splitNum >= possibleSplits.size()) {
                    // if there are no more splits just copy the rem BBs and break
                    for (; bbNum < basicBlocks.size(); bbNum++) {
                        newBBList.add(basicBlocks.get(bbNum));
                    }
                    break;
                } else if (bbNum < possibleSplits.get(splitNum).startBBNum) {
                    // can just copy the bbs till a bb where a split starts
                    for (; bbNum < possibleSplits.get(splitNum).startBBNum; bbNum++) {
                        newBBList.add(basicBlocks.get(bbNum));
                    }
                    currentBB = new BIRBasicBlock(new Name("bb" + getBbIdNum(basicBlocks, bbNum)));
                    continue;
                }
            } else if (splitNum >= possibleSplits.size()) {
                //came from a jump - if there are no more splits can do the rem instructions in the bb and
                // continue to next bb
                // put remaning ins in the current BB
                if (startInsNum < basicBlocks.get(bbNum).instructions.size()) {
                    currentBB.instructions.addAll(basicBlocks.get(bbNum).instructions.subList(startInsNum,
                            basicBlocks.get(bbNum).instructions.size()));
                }
                currentBB.terminator = basicBlocks.get(bbNum).terminator;
                newBBList.add(currentBB);
                startInsNum = 0;
                bbNum += 1;
                continue;
                // do not need to create new BB because after BBs will be just copied

            }

            // now there a split in the BB : either after a jump or from the beginning

            //now handle the splits in the same BB
            // here I get a list of splits starting from this BB and ends in this BB
            // so after creating splits have to handle the last ins in the BB/ a jump to another BB
            List<Split> splitsInSameBBList = new ArrayList<>();
            while (splitNum < possibleSplits.size()) {
                if (possibleSplits.get(splitNum).endBBNum == bbNum) {
                    splitsInSameBBList.add(possibleSplits.get(splitNum));
                    splitNum++;
                } else {
                    break;
                }
            }
            // so the next splitNum contains a split which may start from this BB or another BB, but do not end inThisBB
            if (!splitsInSameBBList.isEmpty()) {
                // need to pass current unfinished BB
                currentBB = generateSplitsInSameBB(birPkg, funcNum, bbNum, splitsInSameBBList,
                        newlyAddedFunctions, newBBNum, newBBList, newFuncNum, startInsNum, currentBB);
                startInsNum = possibleSplits.get(splitNum - 1).lastIns + 1;
                newFuncNum += splitsInSameBBList.size();
                newBBNum += splitsInSameBBList.size();
            }

            // here i handle if splits are over or the next spilt is in another BB
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

            // now we have a split that go to another function
            // now need to add ins then create a function and but next bbNum as its terminator
            if (startInsNum < possibleSplits.get(splitNum).firstIns) {
                currentBB.instructions.addAll(basicBlocks.get(bbNum).instructions.subList(startInsNum,
                        possibleSplits.get(splitNum).firstIns));
            }

            // this BB terminator is a func call pointing to end BB of the split

            newFuncNum += 1;
            Name newFuncName = new Name("$split$" + funcName + "$" + newFuncNum);
            Split currSplit = possibleSplits.get(splitNum);
            splitNum += 1;
            BIRNonTerminator lastInstruction = basicBlocks.get(currSplit.endBBNum).instructions.get(currSplit.lastIns);
            BIROperand newCurrentBBTerminatorLhsOp = new BIROperand(lastInstruction.lhsOp.variableDcl);
            BIRFunction newBIRFunc = createNewBIRFuncAcrossBB(birPkg, funcNum, newFuncName, currSplit, newBBNum);
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
        function.basicBlocks = newBBList;
    }

    private BIRFunction createNewBIRFuncAcrossBB(BIRPackage birPkg, int funcNum, Name funcName, Split currSplit,
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

        //creates bbs
        // first bb
        BIRBasicBlock entryBB = new BIRBasicBlock(new Name("bb0"));
        if (currSplit.firstIns < parentFuncBBs.get(currSplit.startBBNum).instructions.size()) {
            entryBB.instructions.addAll(parentFuncBBs.get(currSplit.startBBNum).instructions.subList(
                    currSplit.firstIns, parentFuncBBs.get(currSplit.startBBNum).instructions.size()));
        }
        entryBB.terminator = parentFuncBBs.get(currSplit.startBBNum).terminator;
        birFunc.basicBlocks.add(entryBB);

        // copying bbs between first and last bb
        for (int bbNum = currSplit.startBBNum + 1; bbNum < currSplit.endBBNum; bbNum++) {
            birFunc.basicBlocks.add(parentFuncBBs.get(bbNum));
        }

        // now need to create last bb and return bb (exit bb)
        BIRBasicBlock lastBB = new BIRBasicBlock(new Name("bb" + getBbIdNum(parentFuncBBs, currSplit.endBBNum)));
        if (0 <= currSplit.lastIns) {
            lastBB.instructions.addAll(parentFuncBBs.get(currSplit.endBBNum).instructions.subList(
                    0, currSplit.lastIns + 1));
        }
        lastBB.instructions.get(currSplit.lastIns).lhsOp = new BIROperand(birFunc.returnVariable);

        // newBBNum is used to create the return statement
        newBBNum += 1;
        BIRBasicBlock exitBB = new BIRBasicBlock(new Name("bb" + newBBNum));
        exitBB.terminator = new BIRTerminator.Return(null);
        lastBB.terminator = new BIRTerminator.GOTO(null, exitBB, lastIns.scope);
        birFunc.basicBlocks.add(lastBB);
        birFunc.basicBlocks.add(exitBB);
        return birFunc;
    }

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
            newlyAddedFunctions.add(createNewBIRFunctionForSameBB(birPkg, funcNum, newFuncName,
                    instructionList.get(possibleSplits.get(splitNum).lastIns),
                    instructionList.subList(possibleSplits.get(splitNum).firstIns,
                            possibleSplits.get(splitNum).lastIns), possibleSplits.get(splitNum).lhsVars,
                    possibleSplits.get(splitNum).funcArgs));
//            birPkg.functions.get(funcNum).localVars.removeAll(possibleSplits.get(splitNum).lhsVars); // cuz after opt
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
        // need to handle the last created BB done outside the dunction
        return currentBB;
    }

    private boolean needToPassVarDclAsArg(BIROperand rhsOp) {
        return (!rhsOp.variableDcl.ignoreVariable) && (rhsOp.variableDcl.kind != VarKind.GLOBAL) &&
                (rhsOp.variableDcl.kind != VarKind.CONSTANT);
    }

    private List<Split> getPossibleSplits(List<BIRBasicBlock> basicBlocks, List<BIRErrorEntry> errorTableEntries) {
        List<Split> possibleSplits = new ArrayList<>();
        List<BIRVariableDcl> newFuncArgs;
        int maxFuncArgs = 250;
        int splitInstructionThreshold = 1;
        int splitEndBBIndex = basicBlocks.size() - 1;
        int splitEndInsIndex = basicBlocks.get(splitEndBBIndex).instructions.size() - 1;
        int errTableEntryIndex = errorTableEntries.size() - 1; // goes from end to beginning
        boolean splitStarted = false;
        boolean splitTypeArray = true;
        Set<BIRVariableDcl> neededOperandsVarDcl = new HashSet<>();
        List<BIRVariableDcl> lhsOperandList = new ArrayList<>();
        BIROperand splitStartOperand = null;

        for (int bbNum = basicBlocks.size() - 1; bbNum >= 0; bbNum--) {
            BIRBasicBlock basicBlock = basicBlocks.get(bbNum);
            BIRTerminator bbTerminator = basicBlock.terminator;
            if (splitStarted) {
                if (bbTerminator.lhsOp != null) {
                    neededOperandsVarDcl.remove(bbTerminator.lhsOp.variableDcl);
                    lhsOperandList.add(bbTerminator.lhsOp.variableDcl);
                    if (bbTerminator.lhsOp.variableDcl.kind == VarKind.RETURN) {
                        splitStarted = false;
                    }
                }
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
                    // if the return var is assigned value inside split, it is not valid
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
                    // now check for termination of split, ie: split start
                    if (splitTypeArray) {
                        if (currIns.lhsOp == splitStartOperand) {
                            if ((neededOperandsVarDcl.size() > maxFuncArgs) ||
                                    (lhsOperandList.size() < splitInstructionThreshold)) {
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

                            possibleSplits.add(new Split(insNum, splitEndInsIndex, bbNum, splitEndBBIndex,
                                    lhsOperandList, newFuncArgs, splitErrorTableEntries));
                            splitStarted = false;
                        }

                    } else {
                        // write for new structure - both looks same remove this if cond
                        // but if you need the type in split add it here
                        if (currIns.lhsOp == splitStartOperand) {
                            if ((neededOperandsVarDcl.size() > maxFuncArgs) ||
                                    (lhsOperandList.size() < splitInstructionThreshold)) {
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

                            possibleSplits.add(new Split(insNum, splitEndInsIndex, bbNum, splitEndBBIndex,
                                    lhsOperandList, newFuncArgs, splitErrorTableEntries));
                            splitStarted = false;
                        }
                    }
                } else {
                    if (currIns.kind == InstructionKind.NEW_ARRAY) {
                        BIRNonTerminator.NewArray arrayIns = (BIRNonTerminator.NewArray) currIns;
                        splitStartOperand = arrayIns.sizeOp;
                        if ((bbNum == basicBlocks.size() - 2) && (!basicBlocks.get(0).instructions.isEmpty()) &&
                                (basicBlocks.get(0).instructions.get(0).lhsOp == splitStartOperand)) {
                            continue;
                        }
                        splitStarted = true;
                        splitTypeArray = true;
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
                    } else if (currIns.kind == InstructionKind.NEW_STRUCTURE) {
                        BIRNonTerminator.NewStructure structureIns = (BIRNonTerminator.NewStructure) currIns;
                        splitStartOperand = structureIns.rhsOp;
                        if ((bbNum == basicBlocks.size() - 2) && (!basicBlocks.get(0).instructions.isEmpty()) &&
                                (basicBlocks.get(0).instructions.get(0).lhsOp == splitStartOperand)) {
                            continue;
                        }
                        splitStarted = true;
                        splitTypeArray = false;
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
                    } else {
                        continue;
                    }
                }

            }
        }
        Collections.reverse(possibleSplits);
        return possibleSplits;
    }

    private BIRFunction createNewBIRFunctionForSameBB(BIRPackage birPkg, int funcNum, Name funcName,
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

        //creates 2 bbs
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

    class Split {
        int firstIns;
        int lastIns;
        int startBBNum;
        int endBBNum;
        List<BIRVariableDcl> lhsVars;
        List<BIRVariableDcl> funcArgs;
        List<BIRErrorEntry> errorTableEntries;

        public Split(int firstIns, int lastIns, int startBBNum, int endBBNum, List<BIRVariableDcl> lhsVars,
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
}
