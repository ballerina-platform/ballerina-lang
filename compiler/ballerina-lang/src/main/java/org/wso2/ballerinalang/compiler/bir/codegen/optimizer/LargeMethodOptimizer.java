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
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.bir.BIRGenUtils;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JInstruction;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JLargeArrayInstruction;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JLargeMapInstruction;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JMethodCallInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.BIRGenUtils.rearrangeBasicBlocks;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LARGE_STRUCTURE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_OBJECT_LONG_ARGS;

/**
 * Split large BIR functions into smaller methods.
 *
 * @since 2.0
 */
public class LargeMethodOptimizer {

    private static final Name DEFAULT_WORKER_NAME = new Name("function");
    private static final String OBJECT_INITIALIZATION_FUNCTION_NAME = "$init$";
    private static final String SPLIT_METHOD = "$split$method$_";
    private final SymbolTable symbolTable;
    // splits are done only if the original function has more instructions than the below number
    private static final int FUNCTION_INSTRUCTION_COUNT_THRESHOLD = 1000;
    // splits are done only if the newly created method will contain more instructions than the below number
    private static final int SPLIT_INSTRUCTION_COUNT_THRESHOLD = 25;
    // splits are done only if the newly created method will have less function arguments than the below number
    private static final int MAX_SPLIT_FUNCTION_ARG_COUNT = 125;
    // total least no. of terminators and non-terminators that should be there to make the periodic split
    private static final int INS_COUNT_PERIODIC_SPLIT_THRESHOLD = 500;
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
            List<BIRFunction> newBIRFunctions = splitBIRFunction(function, false, false, false);
            newlyAddedBIRFunctions.addAll(newBIRFunctions);
        }
        for (BIRTypeDefinition birTypeDef : birPkg.typeDefs) {
            for (BIRFunction function : birTypeDef.attachedFuncs) {
                if (hasLessInstructionCount(function)) {
                    continue;
                }
                List<BIRFunction> newBIRFunctions = splitBIRFunction(function, true, false, false);
                newlyAddedBIRFunctions.addAll(newBIRFunctions);
            }
        }
        birPkg.functions.addAll(newlyAddedBIRFunctions);
    }

    private boolean hasLessInstructionCount(BIRFunction birFunction) {
        int instructionCount = 0;
        for (BIRBasicBlock basicBlock : birFunction.basicBlocks) {
            instructionCount += (basicBlock.instructions.size() + 1);
        }
        return instructionCount < FUNCTION_INSTRUCTION_COUNT_THRESHOLD;
    }

    private List<BIRFunction> splitBIRFunction(BIRFunction birFunction, boolean fromAttachedFunction,
                                               boolean fromSplitFunction, boolean splitTypeArray) {
        final List<BIRFunction> newlyAddingFunctions = new ArrayList<>();
        List<Split> possibleSplits = getPossibleSplits(birFunction.basicBlocks, birFunction.errorTable,
                fromSplitFunction);
        // periodic splits will be done only if it is from a split function
        boolean splitFurther = fromSplitFunction;
        if (!possibleSplits.isEmpty()) {
            generateSplits(birFunction, possibleSplits, newlyAddingFunctions, fromAttachedFunction);
            rearrangeBasicBlocks(birFunction);
            // if now we have less instructions in the parent function no splits are done
            if (hasLessInstructionCount(birFunction)) {
                splitFurther = false;
            }
        }
        if (splitFurther) {
            periodicSplitFunction(birFunction, newlyAddingFunctions, fromAttachedFunction, splitTypeArray);
        }
        // Persisting the used state of the original function
        // TODO This wont be needed if we dont codegen 2 times with the same BIRPkg
        newlyAddingFunctions.forEach(newFunc -> {
            newFunc.usedState = birFunction.usedState;
            birFunction.addChildNode(newFunc);
        });
        return newlyAddingFunctions;
    }

    private void periodicSplitFunction(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                       boolean fromAttachedFunction, boolean splitTypeArray) {
        if (splitTypeArray) {
            periodicSplitArray(parentFunc, newlyAddingFunctions, fromAttachedFunction);
        } else {
            periodicSplitMap(parentFunc, newlyAddingFunctions, fromAttachedFunction);
        }
    }

    private void periodicSplitMap(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                    boolean fromAttachedFunction) {
        List<BIRBasicBlock> bbs = parentFunc.basicBlocks;
        int newMapInsBBNum = bbs.size() - 2;
        int newMapInsNumInRelevantBB = bbs.get(newMapInsBBNum).instructions.size() - 1;
        BIRNonTerminator.NewStructure mapIns = (BIRNonTerminator.NewStructure) bbs.get(newMapInsBBNum).instructions
                .get(newMapInsNumInRelevantBB);

        // creating necessary temp variables
        TempVarsForArraySplit parentFuncTempVars = getTempVarsForArraySplit();
        ParentFuncEnv parentFuncEnv = new ParentFuncEnv(bbs.get(newMapInsBBNum + 1));
        SplitFuncEnv splitFuncEnv = new SplitFuncEnv(getTempVarsForArraySplit(), fromAttachedFunction);

        parentFuncEnv.returnOperand = mapIns.lhsOp;
        BIRVariableDcl handleArray = new BIRVariableDcl(null, symbolTable.handleType, new Name("%mapEntryArray"),
                VarScope.FUNCTION, VarKind.TEMP, null);
        BIROperand handleArrayOperand = new BIROperand(handleArray);
        createAndAddNewHandleArrayForLargeMapIns(parentFuncEnv, mapIns, handleArray, handleArrayOperand);
        JLargeMapInstruction newLargeMapIns = new JLargeMapInstruction(mapIns.pos, mapIns.lhsOp, mapIns.rhsOp,
                handleArrayOperand);

        // we populate MappingConstructorEntry array elements at runtime using jMethodCalls
        // the below three are used when both key-value operands or expr operands are synthetic vars
        Map<BIROperand, BIRMappingConstructorEntryWithIndex> birOperands =  new HashMap<>();
        Set<BIROperand> mapValuesOperands = new HashSet<>();
        Map<BIROperand, NonTerminatorLocation> mapKeyOperandLocations = new HashMap<>();
        // the below two are used for other 3 cases - either key operand or value operand or both arg global/arg kind
        Map<BIROperand, BIRMappingConstructorEntryWithIndex> globalAndArgVarKeyOrValueLhsOperands = new HashMap<>();
        List<BIRNonTerminator> globalAndArgVarIns = getGlobalAndArgVarInsForMap(parentFuncTempVars, mapIns,
                handleArrayOperand, birOperands, mapValuesOperands, globalAndArgVarKeyOrValueLhsOperands,
                mapKeyOperandLocations);

        // add the constant load array size operand instruction
        parentFuncEnv.parentFuncNewInsList.add(bbs.get(0).instructions.get(0));
        parentFuncEnv.parentFuncLocalVarList.add(bbs.get(0).instructions.get(0).lhsOp.variableDcl);

        splitParentFuncForPeriodicMapSplits(parentFunc, newlyAddingFunctions, fromAttachedFunction,
                bbs, newMapInsBBNum, newMapInsNumInRelevantBB,
                handleArray, handleArrayOperand, birOperands, splitFuncEnv, parentFuncEnv, mapValuesOperands,
                globalAndArgVarKeyOrValueLhsOperands, mapKeyOperandLocations);

        setParentFuncDetails(parentFunc, bbs, newMapInsBBNum, parentFuncTempVars, parentFuncEnv,
                newLargeMapIns, globalAndArgVarIns);
    }

    private void setParentFuncDetails(BIRFunction parentFunc, List<BIRBasicBlock> bbs, int newMapOrArrayInsBBNum,
                                      TempVarsForArraySplit parentFuncTempVars, ParentFuncEnv parentFuncEnv,
                                      JInstruction jLargeStructureIns, List<BIRNonTerminator> globalAndArgVarIns) {
        parentFuncEnv.parentFuncNewInsList.addAll(globalAndArgVarIns);
        parentFuncEnv.parentFuncNewInsList.add(jLargeStructureIns);
        parentFuncEnv.parentFuncNewBB.instructions = parentFuncEnv.parentFuncNewInsList;
        parentFuncEnv.parentFuncNewBB.terminator = bbs.get(newMapOrArrayInsBBNum).terminator;
        parentFuncEnv.parentFuncNewBBList.add(parentFuncEnv.parentFuncNewBB);
        BIRBasicBlock parentFuncExitBB = bbs.get(newMapOrArrayInsBBNum + 1);
        BIRGenUtils.renumberBasicBlock(parentFuncEnv.parentFuncBBId++, parentFuncExitBB);
        parentFuncEnv.parentFuncNewBBList.add(parentFuncExitBB);
        parentFunc.basicBlocks = parentFuncEnv.parentFuncNewBBList;
        parentFunc.errorTable = new ArrayList<>();

        parentFunc.localVars = new ArrayList<>();
        parentFunc.localVars.add(parentFunc.returnVariable);
        parentFunc.localVars.addAll(parentFunc.parameters);
        parentFunc.localVars.addAll(parentFuncEnv.parentFuncLocalVarList);
        if (parentFuncTempVars.tempVarsUsed) {
            parentFunc.localVars.add(parentFuncTempVars.arrayIndexVarDcl);
            parentFunc.localVars.add(parentFuncTempVars.typeCastVarDcl);
        }
        // parent function would not have VarKind.LOCAL parentFunc.localVars.
        // Hence, no need to correct localVar.startBB and localVar.endBB
    }

    private void periodicSplitArray(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                    boolean fromAttachedFunction) {
        List<BIRBasicBlock> bbs = parentFunc.basicBlocks;
        int newArrayInsBBNum = bbs.size() - 2;
        int newArrayInsNumInRelevantBB = bbs.get(newArrayInsBBNum).instructions.size() - 1;
        BIRNonTerminator.NewArray arrayIns = (BIRNonTerminator.NewArray) bbs.get(newArrayInsBBNum).instructions
                .get(newArrayInsNumInRelevantBB);

        // creating necessary temp variables
        TempVarsForArraySplit parentFuncTempVars = getTempVarsForArraySplit();
        ParentFuncEnv parentFuncEnv = new ParentFuncEnv(bbs.get(newArrayInsBBNum + 1));
        SplitFuncEnv splitFuncEnv = new SplitFuncEnv(getTempVarsForArraySplit(), fromAttachedFunction);

        parentFuncEnv.returnOperand = arrayIns.lhsOp;
        BIRVariableDcl handleArray = new BIRVariableDcl(null, symbolTable.handleType, new Name("%listEntryArray"),
                VarScope.FUNCTION, VarKind.TEMP, null);
        BIROperand handleArrayOperand = new BIROperand(handleArray);
        createAndAddNewHandleArrayForLargeArrayIns(parentFuncEnv, arrayIns, handleArray, handleArrayOperand);
        JLargeArrayInstruction newLargeArrayIns = new JLargeArrayInstruction(arrayIns.pos,
                arrayIns.type, arrayIns.lhsOp, arrayIns.typedescOp, arrayIns.sizeOp, handleArrayOperand);

        // populating ListConstructorEntry array elements at runtime using jMethodCalls
        // creating method calls
        Map<BIROperand, BIRListConstructorEntryWithIndex> birOperands =  new HashMap<>();
        Set<BIROperand> arrayValuesOperands = new HashSet<>();
        List<BIRNonTerminator> globalAndArgVarIns = getGlobalAndArgVarInsForArray(parentFuncTempVars, arrayIns,
                handleArrayOperand, birOperands, arrayValuesOperands);

        // add the constant load array size operand instruction
        parentFuncEnv.parentFuncNewInsList.add(bbs.get(0).instructions.get(0));
        parentFuncEnv.parentFuncLocalVarList.add(bbs.get(0).instructions.get(0).lhsOp.variableDcl);

        splitParentFuncForPeriodicArraySplits(parentFunc, newlyAddingFunctions, fromAttachedFunction,
                bbs, newArrayInsBBNum, newArrayInsNumInRelevantBB,
                handleArray, handleArrayOperand, birOperands, splitFuncEnv, parentFuncEnv, arrayValuesOperands);

        setParentFuncDetails(parentFunc, bbs, newArrayInsBBNum, parentFuncTempVars, parentFuncEnv,
                newLargeArrayIns, globalAndArgVarIns);
    }

    private List<BIRNonTerminator> getGlobalAndArgVarInsForMap(TempVarsForArraySplit parentFuncTempVars,
                                                               BIRNonTerminator.NewStructure mapIns,
                                                               BIROperand handleArrayOperand,
                                                               Map<BIROperand, BIRMappingConstructorEntryWithIndex>
                                                                       birOperands,
                                                               Set<BIROperand> mapValuesOperands,
                                                               Map<BIROperand, BIRMappingConstructorEntryWithIndex>
                                                                       globalAndArgVarKeyOrValueLhsOperands,
                                                               Map<BIROperand, NonTerminatorLocation>
                                                                       mapKeyOperandLocations) {
        List<BIRNonTerminator> globalAndArgVarIns = new ArrayList<>();
        int arrIndex = 0;
        for (BIRNode.BIRMappingConstructorEntry value : mapIns.initialValues) {
            if (value.isKeyValuePair()) {
                BIRNode.BIRMappingConstructorKeyValueEntry keyValueEntry =
                        (BIRNode.BIRMappingConstructorKeyValueEntry) value;
                BIROperand valueOp = keyValueEntry.valueOp;
                BIRVariableDcl valueVarDcl = valueOp.variableDcl;
                BIROperand keyOp = keyValueEntry.keyOp;
                BIRVariableDcl keyVarDcl = keyOp.variableDcl;
                // there are 4 cases - key : value, and either temp/synthetic kind operand or global/arg kind operand
                if (isTempOrSyntheticVar(valueVarDcl)) {
                    // value is a temp/synthetic operand
                    // if the key is also a temp/synthetic operand, we need to populate it as
                    // soon as value is found if the key is populated before that --- (1)
                    if (isTempOrSyntheticVar(keyVarDcl)) {
                        birOperands.put(valueOp, new BIRMappingConstructorEntryWithIndex(
                                value, arrIndex));
                        mapValuesOperands.add(valueOp);
                    } else {
                        // if the key is a global/arg operand,
                        // we need to populate it as soon as the value operand is found --- (2)
                        globalAndArgVarKeyOrValueLhsOperands.put(
                                valueOp, new BIRMappingConstructorEntryWithIndex(value, arrIndex));
                    }
                } else {
                    // value is a global/arg operand
                    // if the key is a temp/synthetic operand, we need to populate it as soon as key is found --- (3)
                    if (isTempOrSyntheticVar(keyVarDcl)) {
                        globalAndArgVarKeyOrValueLhsOperands.put(
                                keyOp, new BIRMappingConstructorEntryWithIndex(value, arrIndex));
                    } else {
                        // if the key is also a global/arg operand, we can populate at the end --- (4)
                        setMapElement(globalAndArgVarIns, handleArrayOperand, valueOp, value, arrIndex,
                                parentFuncTempVars);
                    }
                }
                mapKeyOperandLocations.put(keyOp, null);
            } else {
                // this is a spread field entry. need to do same as arrays
                // global/arg operands will be populated at the end, temp/synthetic operands as they are found
                BIRNode.BIRMappingConstructorSpreadFieldEntry spreadFieldEntry =
                        (BIRNode.BIRMappingConstructorSpreadFieldEntry) value;
                BIROperand exprOp = spreadFieldEntry.exprOp;
                if (isTempOrSyntheticVar(exprOp.variableDcl)) {
                    // done same as (1)
                    birOperands.put(exprOp, new BIRMappingConstructorEntryWithIndex(value, arrIndex));
                    mapValuesOperands.add(exprOp);
                } else {
                    // done same as (4)
                    setMapElement(globalAndArgVarIns, handleArrayOperand, exprOp, value, arrIndex,
                            parentFuncTempVars);
                }
            }
            arrIndex++;
        }
        return globalAndArgVarIns;
    }

    private List<BIRNonTerminator> getGlobalAndArgVarInsForArray(TempVarsForArraySplit parentFuncTempVars,
                                                                 BIRNonTerminator.NewArray arrayIns,
                                                                 BIROperand handleArrayOperand,
                                                                 Map<BIROperand, BIRListConstructorEntryWithIndex>
                                                                 birOperands, Set<BIROperand> arrayValuesOperands) {
        List<BIRNonTerminator> globalAndArgVarIns = new ArrayList<>();
        int arrIndex = 0;
        for (BIRNode.BIRListConstructorEntry value : arrayIns.values) {
            BIRVariableDcl arrElementVarDcl = value.exprOp.variableDcl;
            if (isTempOrSyntheticVar(arrElementVarDcl)) {
                birOperands.put(value.exprOp, new BIRListConstructorEntryWithIndex(value, arrIndex));
                arrayValuesOperands.add(value.exprOp);
            } else {
                setArrayElement(globalAndArgVarIns, handleArrayOperand, value.exprOp, value, arrIndex,
                        parentFuncTempVars);
            }
            arrIndex++;
        }
        return globalAndArgVarIns;
    }

    private void createAndAddNewHandleArrayForLargeMapIns(ParentFuncEnv parentFuncEnv,
                                                          BIRNonTerminator.NewStructure mapIns,
                                                          BIRVariableDcl handleArray, BIROperand handleArrayOperand) {
        BIRVariableDcl arraySizeVarDcl = new BIRVariableDcl(null, symbolTable.intType, new Name("%mapEntryArraySize"),
                VarScope.FUNCTION, VarKind.TEMP, null);
        parentFuncEnv.parentFuncLocalVarList.add(arraySizeVarDcl);
        BIROperand arraySizeOperand = new BIROperand(arraySizeVarDcl);
        parentFuncEnv.parentFuncLocalVarList.add(handleArray);
        JMethodCallInstruction callHandleArray = new JMethodCallInstruction(null);
        callHandleArray.lhsOp = handleArrayOperand;
        callHandleArray.invocationType = INVOKESTATIC;
        callHandleArray.jClassName = LARGE_STRUCTURE_UTILS;
        callHandleArray.jMethodVMSig = "(J)" + GET_HANDLE_VALUE;
        callHandleArray.name = "getBMapInitialValueEntryArray";
        callHandleArray.args = Collections.singletonList(arraySizeOperand);
        BIRNonTerminator.ConstantLoad loadArraySize = new BIRNonTerminator.ConstantLoad(null,
                (long) mapIns.initialValues.size(), symbolTable.intType, arraySizeOperand);
        parentFuncEnv.parentFuncNewInsList.add(loadArraySize);
        parentFuncEnv.parentFuncNewInsList.add(callHandleArray);
    }

    private void createAndAddNewHandleArrayForLargeArrayIns(ParentFuncEnv parentFuncEnv,
                                                            BIRNonTerminator.NewArray arrayIns,
                                                            BIRVariableDcl handleArray, BIROperand handleArrayOperand) {
        BIRVariableDcl arraySizeVarDcl = new BIRVariableDcl(null, symbolTable.intType, new Name("%listEntryArraySize"),
                VarScope.FUNCTION, VarKind.TEMP, null);
        parentFuncEnv.parentFuncLocalVarList.add(arraySizeVarDcl);
        BIROperand arraySizeOperand = new BIROperand(arraySizeVarDcl);
        parentFuncEnv.parentFuncLocalVarList.add(handleArray);
        JMethodCallInstruction callHandleArray = new JMethodCallInstruction(null);
        callHandleArray.lhsOp = handleArrayOperand;
        callHandleArray.invocationType = INVOKESTATIC;
        callHandleArray.jClassName = LARGE_STRUCTURE_UTILS;
        callHandleArray.jMethodVMSig = "(J)" + GET_HANDLE_VALUE;
        callHandleArray.name = "getListInitialValueEntryArray";
        callHandleArray.args = Collections.singletonList(arraySizeOperand);
        BIRNonTerminator.ConstantLoad loadArraySize = new BIRNonTerminator.ConstantLoad(null,
                (long) arrayIns.values.size(), symbolTable.intType, arraySizeOperand);
        parentFuncEnv.parentFuncNewInsList.add(loadArraySize);
        parentFuncEnv.parentFuncNewInsList.add(callHandleArray);
    }

    private Map<BIRAbstractInstruction,
            SplitPointDetails> getSplitPointsForMap(List<BIRBasicBlock> bbs, Set<BIROperand> mapValueOperands,
                                                    Map<BIROperand, BIRMappingConstructorEntryWithIndex>
                                                            globalAndArgVarKeyOrValueLhsOperands,
                                                    Map<BIRAbstractInstruction, BIRMappingConstructorEntryWithIndex>
                                                                        globalAndArgVarKeyOrValueRelatedIns,
                                                    Map<BIROperand, NonTerminatorLocation> mapKeyOperandLocations) {

        // Here we also handle globalAndArgVarKeyOrValueLhsOperands and globalAndArgVarKeyOrValueRelatedIns
        // which is related to global or arg vars.

        // For a key-value entry given both key values are temp/synthetic,
        // we do not populate the map value if we do not have a key operand before
        // the value operand. We have to check that when processing split points. Hence, we have to find where
        // the map key operands are located. That is done in the same loop below.

        // a copy of arrayValueOperands is made to find first found map element operands from bottom
        Set<BIROperand> remainingArrayValueOperands = new HashSet<>(mapValueOperands);

        // Split points mean the places which are eligible for a split. i.e. we can split the parent function by
        // moving instructions and terminators until this one (including this) to a new function.
        // The criteria for choosing a split point are as follows. We go from bottom to top searching for map value
        // operands in both RHS and LHS. If we find one, that is a split point, unless we find that previously in the
        // same BB. A new flag is added to the split point to depict whether to split the function here (splitHere).
        // If it is false only map entry populating instructions are added, no split is done.
        Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints = new HashMap<>();
        for (int bbIndex = bbs.size() - 2; bbIndex >= 0; bbIndex--) {
            Set<BIROperand> operandsInSameBB = new HashSet<>();
            BIRBasicBlock bb = bbs.get(bbIndex);
            List<BIRNonTerminator> bbInstructions = bb.instructions;
            BIROperand terminatorLhsOp = bb.terminator.lhsOp;
            if (terminatorLhsOp != null) {
                populateGlobalAndArgVarKeyOrValueRelatedIns(globalAndArgVarKeyOrValueLhsOperands,
                        globalAndArgVarKeyOrValueRelatedIns, bb.terminator, terminatorLhsOp);
            }
            int lastInsNum = getLastInsNumAndHandleTerminator(bbs, mapValueOperands, remainingArrayValueOperands,
                    insSplitPoints, bbIndex, operandsInSameBB, bb, bbInstructions);
            for (int insIndex = lastInsNum; insIndex >= 0; insIndex--) {
                BIRNonTerminator currIns = bbInstructions.get(insIndex);
                BIROperand lhsOp = currIns.lhsOp;
                if (lhsOp != null) {
                    if (mapKeyOperandLocations.containsKey(lhsOp) && mapKeyOperandLocations.get(lhsOp) == null) {
                        mapKeyOperandLocations.put(lhsOp, new NonTerminatorLocation(bbIndex, insIndex));
                    }
                    populateGlobalAndArgVarKeyOrValueRelatedIns(globalAndArgVarKeyOrValueLhsOperands,
                            globalAndArgVarKeyOrValueRelatedIns, currIns, lhsOp);
                    addOperandToInsSplitPoints(mapValueOperands, insSplitPoints, currIns, lhsOp, operandsInSameBB,
                            remainingArrayValueOperands);
                }
            }
        }
        return insSplitPoints;
    }

    private void populateGlobalAndArgVarKeyOrValueRelatedIns(Map<BIROperand, BIRMappingConstructorEntryWithIndex>
                                                                     globalAndArgVarKeyOrValueLhsOperands,
                                                             Map<BIRAbstractInstruction,
                                                                     BIRMappingConstructorEntryWithIndex>
                                                                     globalAndArgVarKeyOrValueRelatedIns,
                                                             BIRAbstractInstruction currIns, BIROperand lhsOp) {
        if (globalAndArgVarKeyOrValueLhsOperands.containsKey(lhsOp)) {
            globalAndArgVarKeyOrValueRelatedIns.put(currIns, globalAndArgVarKeyOrValueLhsOperands.get(lhsOp));
            globalAndArgVarKeyOrValueLhsOperands.remove(lhsOp);
        }
    }

    private int getLastInsNumAndHandleTerminator(List<BIRBasicBlock> bbs, Set<BIROperand> arrayOrMapValueOperands,
                                                 Set<BIROperand> remainingArrayValueOperands,
                                                 Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints,
                                                 int bbIndex, Set<BIROperand> operandsInSameBB, BIRBasicBlock bb,
                                                 List<BIRNonTerminator> bbInstructions) {
        int lastInsNum;
        if (bbIndex == bbs.size() - 2) {
            lastInsNum = bbInstructions.size() - 2;
        } else {
            lastInsNum = bbInstructions.size() - 1;
            BIRTerminator bbTerminator = bb.terminator;
            if (bbTerminator.kind != InstructionKind.BRANCH) {
                // branch terminators are omitted from terminators because their boolean operands appear
                // in a preceding instruction before the terminator. There we can do the split
                populateInsSplitPoints(arrayOrMapValueOperands, insSplitPoints, bbTerminator, operandsInSameBB,
                        remainingArrayValueOperands);
            }
            if (bbTerminator.kind == InstructionKind.CALL) {
                // When arrays have new objects created inside them, the object is created in one instruction
                // and it is initialized/populated later by passing it as an argument to the $init$ function
                // hence split function should be created after such function call
                // only in this case we have to consider RHS operands (only function arguments)
                BIRTerminator.Call callIns = (BIRTerminator.Call) bbTerminator;
                if (callIns.name.value.contains(OBJECT_INITIALIZATION_FUNCTION_NAME)) {
                    for (BIROperand arg : callIns.args) {
                        addOperandToInsSplitPoints(arrayOrMapValueOperands, insSplitPoints, callIns, arg,
                                operandsInSameBB, remainingArrayValueOperands);
                    }
                }
            }
        }
        return lastInsNum;
    }

    private Map<BIRAbstractInstruction, SplitPointDetails> getSplitPointsForArray(List<BIRBasicBlock> bbs,
                                                                                  Set<BIROperand> arrayValuesOperands) {
        // a copy of arrayValueOperands is made to find first found array element operands from bottom
        Set<BIROperand> remainingArrayValueOperands = new HashSet<>(arrayValuesOperands);

        // Split points mean the places which are eligible for a split. i.e. we can split the parent function by
        // moving instructions and terminators until this one (including this) to a new function.
        // The criteria for choosing a split point are as follows. We go from bottom to top searching for array value
        // operands in both RHS and LHS. If we find one, that is a split point, unless we find that previously in the
        // same BB. A new flag is added to the split point to depict whether to split the function here (splitHere).
        // If it is false only array populating instructions are added.
        Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints = new HashMap<>();
        for (int bbIndex = bbs.size() - 2; bbIndex >= 0; bbIndex--) {
            Set<BIROperand> operandsInSameBB = new HashSet<>();
            BIRBasicBlock bb = bbs.get(bbIndex);
            List<BIRNonTerminator> bbInstructions = bb.instructions;
            int lastInsNum = getLastInsNumAndHandleTerminator(bbs, arrayValuesOperands, remainingArrayValueOperands,
                    insSplitPoints, bbIndex, operandsInSameBB, bb, bbInstructions);
            for (int insIndex = lastInsNum; insIndex >= 0; insIndex--) {
                populateInsSplitPoints(arrayValuesOperands, insSplitPoints, bbInstructions.get(insIndex),
                        operandsInSameBB, remainingArrayValueOperands);
            }
        }
        return insSplitPoints;
    }

    private void populateInsSplitPoints(Set<BIROperand> arrayValuesOperands,
                                        Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints,
                                        BIRAbstractInstruction ins, Set<BIROperand> operandsInSameBB,
                                        Set<BIROperand> remainingArrayValueOperands) {
        BIROperand insLhsOp = ins.lhsOp;
        if (insLhsOp != null) {
            addOperandToInsSplitPoints(arrayValuesOperands, insSplitPoints, ins, insLhsOp, operandsInSameBB,
                    remainingArrayValueOperands);
        }
    }

    private void addOperandToInsSplitPoints(Set<BIROperand> arrayValuesOperands,
                                            Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints,
                                            BIRAbstractInstruction ins, BIROperand insOperand,
                                            Set<BIROperand> operandsInSameBB,
                                            Set<BIROperand> remainingArrayValueOperands) {
        if (arrayValuesOperands.contains(insOperand) && !operandsInSameBB.contains(insOperand)) {
            operandsInSameBB.add(insOperand);
            if (insSplitPoints.containsKey(ins)) {
                SplitPointDetails splitPointDetails = insSplitPoints.get(ins);
                List<BIROperand> operandList = splitPointDetails.arrayElementsBIROperands;
                operandList.add(insOperand);
                if (remainingArrayValueOperands.contains(insOperand)) {
                    remainingArrayValueOperands.remove(insOperand);
                } else {
                    if (splitPointDetails.splitHere) {
                        splitPointDetails.splitHere = false;
                    }
                }
            } else {
                List<BIROperand> operandList = new ArrayList<>();
                operandList.add(insOperand);
                if (remainingArrayValueOperands.contains(insOperand)) {
                    remainingArrayValueOperands.remove(insOperand);
                    insSplitPoints.put(ins, new SplitPointDetails(operandList, true));
                } else {
                    insSplitPoints.put(ins, new SplitPointDetails(operandList, false));
                }
            }
        }
    }

    private Map<BIROperand, Integer> getErrorOperandsAndTargetBBs(List<BIRErrorEntry> errorTable) {
        Map<BIROperand, Integer> errorOperandsAndTargetBBs = new HashMap<>();
        for (BIRErrorEntry birErrorEntry : errorTable) {
            errorOperandsAndTargetBBs.put(birErrorEntry.errorOp, birErrorEntry.targetBB.number);
        }
        return errorOperandsAndTargetBBs;
    }

    private void splitParentFuncForPeriodicMapSplits(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                                     boolean fromAttachedFunction,
                                                     List<BIRBasicBlock> bbs,
                                                     int newArrayInsBBNum, int newArrayInsNumInRelevantBB,
                                                     BIRVariableDcl handleArray, BIROperand handleArrayOperand,
                                                     Map<BIROperand, BIRMappingConstructorEntryWithIndex> birOperands,
                                                     SplitFuncEnv splitFuncEnv,
                                                     ParentFuncEnv parentFuncEnv, Set<BIROperand> mapValuesOperands,
                                                     Map<BIROperand, BIRMappingConstructorEntryWithIndex>
                                                             globalAndArgVarKeyOrValueLhsOperands,
                                                     Map<BIROperand, NonTerminatorLocation> mapKeyOperandLocations) {
        Map<BIRAbstractInstruction, BIRMappingConstructorEntryWithIndex> globalAndArgVarKeyOrValueRelatedIns =
                new HashMap<>();
        Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints = getSplitPointsForMap(bbs, mapValuesOperands,
                globalAndArgVarKeyOrValueLhsOperands, globalAndArgVarKeyOrValueRelatedIns, mapKeyOperandLocations);
        Map<BIROperand, Integer> errorOperandsAndTargetBBs = getErrorOperandsAndTargetBBs(parentFunc.errorTable);
        // When an error table operand is found as an array element operand in the LHS of a non-terminator,
        // the array element populating instructions should be put in the targetBB for the try-catch to work correctly.
        List<BIRNonTerminator> nextBBPendingIns = new ArrayList<>();
        List<BIRNonTerminator> newBBInsList;
        boolean mapElementSet;
        for (int bbIndex = 0; bbIndex < bbs.size() - 1; bbIndex++) {
            BIRBasicBlock bb = bbs.get(bbIndex);
            handleBasicBlockStart(splitFuncEnv, nextBBPendingIns, bb);
            for (int insIndex = 0; insIndex < bb.instructions.size(); insIndex++) {
                if (bbIndex == 0 && insIndex == 0) { // map type ins
                    continue;
                } else if ((bbIndex == newArrayInsBBNum) && (insIndex == newArrayInsNumInRelevantBB)) {
                    createNewFuncForPeriodicSplit(parentFunc, newlyAddingFunctions, fromAttachedFunction,
                            handleArray, splitFuncEnv, parentFuncEnv, bb,
                            bb.instructions.get(newArrayInsNumInRelevantBB));
                    return;
                }

                BIRNonTerminator bbIns = bb.instructions.get(insIndex);
                BIROperand bbInsLhsOp = bbIns.lhsOp;
                handleReturnValAssignedInstruction(splitFuncEnv, bb, bbInsLhsOp);
                splitFuncEnv.splitFuncNewInsList.add(bbIns);
                splitFuncEnv.periodicSplitInsCount++;
                populateSplitFuncArgsAndLocalVarList(splitFuncEnv, bbIns);
                newBBInsList = new ArrayList<>();
                mapElementSet = setMapElementGivenOperand(birOperands, newBBInsList, handleArrayOperand,
                        splitFuncEnv.splitFuncTempVars, insSplitPoints, bbIns, splitFuncEnv, mapKeyOperandLocations,
                        bbIndex, insIndex, globalAndArgVarKeyOrValueRelatedIns);
                handleSplittingAtNonTerminator(parentFunc, newlyAddingFunctions, fromAttachedFunction, handleArray,
                        splitFuncEnv, parentFuncEnv, errorOperandsAndTargetBBs, nextBBPendingIns, newBBInsList,
                        mapElementSet, bb, bbIns, bbInsLhsOp);
            }
            handleBBInstructions(splitFuncEnv, bb);
            // Next consider LHS op in the BIR terminator, branch terms won't have an array element as LHS op
            // If we found a LHS op, we need to create a new BB and change the terminator
            populateSplitFuncArgsAndLocalVarList(splitFuncEnv, bb.terminator);
            splitFuncEnv.periodicSplitInsCount++;
            newBBInsList = new ArrayList<>();
            mapElementSet = setMapElementGivenOperand(birOperands, newBBInsList, handleArrayOperand,
                    splitFuncEnv.splitFuncTempVars, insSplitPoints, bb.terminator, splitFuncEnv, mapKeyOperandLocations,
                    bbIndex, -1, globalAndArgVarKeyOrValueRelatedIns);
            handleSplittingAtTerminator(parentFunc, newlyAddingFunctions, fromAttachedFunction, handleArray,
                    splitFuncEnv, parentFuncEnv, newBBInsList, mapElementSet, bb);
        }
    }

    private void handleSplittingAtTerminator(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                             boolean fromAttachedFunction, BIRVariableDcl handleArray,
                                             SplitFuncEnv splitFuncEnv, ParentFuncEnv parentFuncEnv,
                                             List<BIRNonTerminator> newBBInsList, boolean mapElementSet,
                                             BIRBasicBlock bb) {
        if (mapElementSet) {
            splitFuncEnv.splitFuncCorrectTerminatorBBs.add(splitFuncEnv.splitFuncBB);
            BIRBasicBlock newBB = new BIRBasicBlock(splitFuncEnv.splitFuncBBId++);
            splitFuncEnv.splitFuncBB = new BIRBasicBlock(splitFuncEnv.splitFuncBBId++);
            newBB.instructions.addAll(newBBInsList);
            newBB.terminator = new BIRTerminator.GOTO(null, bb.terminator.thenBB, bb.terminator.scope);
            bb.terminator.thenBB = newBB; // change the current BB's terminator to new BB where ins are put
            splitFuncEnv.splitFuncNewBBList.add(newBB);
        } else {
            splitFuncEnv.splitFuncBB = new BIRBasicBlock(splitFuncEnv.splitFuncBBId++);
        }

        if (bb.terminator.kind == InstructionKind.BRANCH) {
            splitFuncEnv.splitOkay = false;
            BIRTerminator.Branch branch = (BIRTerminator.Branch) bb.terminator;
            int higherBBNumber = Math.max(branch.trueBB.number, branch.falseBB.number);
            splitFuncEnv.doNotSplitTillThisBBNum = Math.max(splitFuncEnv.doNotSplitTillThisBBNum, higherBBNumber);
        }

        if (splitFuncEnv.periodicSplitInsCount > INS_COUNT_PERIODIC_SPLIT_THRESHOLD && splitFuncEnv.splitOkay
                && mapElementSet && splitFuncEnv.splitHere) {
            createNewFuncForPeriodicSplit(parentFunc, newlyAddingFunctions, fromAttachedFunction,
                    handleArray, splitFuncEnv, parentFuncEnv, bb, bb.terminator);
        }
    }

    private void handleSplittingAtNonTerminator(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                                boolean fromAttachedFunction, BIRVariableDcl handleArray,
                                                SplitFuncEnv splitFuncEnv, ParentFuncEnv parentFuncEnv,
                                                Map<BIROperand, Integer> errorOperandsAndTargetBBs,
                                                List<BIRNonTerminator> nextBBPendingIns,
                                                List<BIRNonTerminator> newBBInsList, boolean mapElementSet,
                                                BIRBasicBlock bb, BIRNonTerminator bbIns, BIROperand bbInsLhsOp) {
        if (bbInsLhsOp != null && errorOperandsAndTargetBBs.containsKey(bbInsLhsOp)) {
            nextBBPendingIns.addAll(newBBInsList);
            splitFuncEnv.splitOkay = false;
            splitFuncEnv.doNotSplitTillThisBBNum = Math.max(splitFuncEnv.doNotSplitTillThisBBNum,
                    errorOperandsAndTargetBBs.get(bbInsLhsOp));
        } else {
            splitFuncEnv.splitFuncNewInsList.addAll(newBBInsList);
        }

        // create split func if needed
        if (splitFuncEnv.periodicSplitInsCount > INS_COUNT_PERIODIC_SPLIT_THRESHOLD && splitFuncEnv.splitOkay
                && mapElementSet && splitFuncEnv.splitHere) {
            createNewFuncForPeriodicSplit(parentFunc, newlyAddingFunctions, fromAttachedFunction,
                    handleArray, splitFuncEnv, parentFuncEnv, bb, bbIns);
        }
    }

    private void handleReturnValAssignedInstruction(SplitFuncEnv splitFuncEnv, BIRBasicBlock bb,
                                                    BIROperand bbInsLhsOp) {
        if (!splitFuncEnv.returnValAssigned && bbInsLhsOp != null
                && bbInsLhsOp.variableDcl.kind == VarKind.RETURN) {
            splitFuncEnv.returnValAssigned = true;
            // when the return val is assigned, there are no more ins in the BB
            // and the terminator is of kind GOTO to the return BB
            bb.terminator = new BIRTerminator.GOTO(bb.terminator.pos, splitFuncEnv.returnBB,
                    bb.terminator.scope);
            splitFuncEnv.splitFuncCorrectTerminatorBBs.add(splitFuncEnv.splitFuncBB);
        }
    }

    private void handleBasicBlockStart(SplitFuncEnv splitFuncEnv, List<BIRNonTerminator> nextBBPendingIns,
                                       BIRBasicBlock bb) {
        if (!splitFuncEnv.splitOkay && splitFuncEnv.doNotSplitTillThisBBNum == bb.number) {
            splitFuncEnv.splitOkay = true;
            splitFuncEnv.periodicSplitInsCount -= 1;
        }
        splitFuncEnv.splitFuncChangedBBs.put(bb.number, splitFuncEnv.splitFuncBB);
        if (!nextBBPendingIns.isEmpty()) {
            splitFuncEnv.splitFuncNewInsList.addAll(nextBBPendingIns);
            nextBBPendingIns.clear();
        }
    }

    private void splitParentFuncForPeriodicArraySplits(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                                       boolean fromAttachedFunction,
                                                       List<BIRBasicBlock> bbs,
                                                       int newArrayInsBBNum, int newArrayInsNumInRelevantBB,
                                                       BIRVariableDcl handleArray, BIROperand handleArrayOperand,
                                                       Map<BIROperand, BIRListConstructorEntryWithIndex> birOperands,
                                                       SplitFuncEnv splitFuncEnv,
                                                       ParentFuncEnv parentFuncEnv,
                                                       Set<BIROperand> arrayValuesOperands) {
        Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints = getSplitPointsForArray(bbs,
                arrayValuesOperands);
        Map<BIROperand, Integer> errorOperandsAndTargetBBs = getErrorOperandsAndTargetBBs(parentFunc.errorTable);
        // When an error table operand is found as an array element operand in the LHS of a non-terminator,
        // The array element populating instructions should be put in the targetBB for the try-catch to work correctly.
        List<BIRNonTerminator> nextBBPendingIns = new ArrayList<>();
        List<BIRNonTerminator> newBBInsList;
        boolean arrayElementSet;
        for (int bbIndex = 0; bbIndex < bbs.size() - 1; bbIndex++) {
            BIRBasicBlock bb = bbs.get(bbIndex);
            handleBasicBlockStart(splitFuncEnv, nextBBPendingIns, bb);
            for (int insIndex = 0; insIndex < bb.instructions.size(); insIndex++) {
                if (bbIndex == 0 && insIndex == 0) { // array size ins
                    continue;
                } else if ((bbIndex == newArrayInsBBNum) && (insIndex == newArrayInsNumInRelevantBB)) {
                    createNewFuncForPeriodicSplit(parentFunc, newlyAddingFunctions, fromAttachedFunction,
                            handleArray, splitFuncEnv, parentFuncEnv, bb,
                            bb.instructions.get(newArrayInsNumInRelevantBB));
                    return;
                }

                BIRNonTerminator bbIns = bb.instructions.get(insIndex);
                BIROperand bbInsLhsOp = bbIns.lhsOp;
                handleReturnValAssignedInstruction(splitFuncEnv, bb, bbInsLhsOp);
                splitFuncEnv.splitFuncNewInsList.add(bbIns);
                splitFuncEnv.periodicSplitInsCount++;
                populateSplitFuncArgsAndLocalVarList(splitFuncEnv, bbIns);
                newBBInsList = new ArrayList<>();
                arrayElementSet = setArrayElementGivenOperand(birOperands, newBBInsList,
                        handleArrayOperand, splitFuncEnv.splitFuncTempVars, insSplitPoints, bbIns, splitFuncEnv);
                handleSplittingAtNonTerminator(parentFunc, newlyAddingFunctions, fromAttachedFunction, handleArray,
                        splitFuncEnv, parentFuncEnv, errorOperandsAndTargetBBs, nextBBPendingIns, newBBInsList,
                        arrayElementSet, bb, bbIns, bbInsLhsOp);
            }

            handleBBInstructions(splitFuncEnv, bb);

            // Next consider LHS op in the BIR terminator, branch terms won't have an array element as LHS op
            // If we found a LHS op, we need to create a new BB and change the terminator
            populateSplitFuncArgsAndLocalVarList(splitFuncEnv, bb.terminator);
            splitFuncEnv.periodicSplitInsCount++;
            newBBInsList = new ArrayList<>();
            arrayElementSet = setArrayElementGivenOperand(birOperands, newBBInsList, handleArrayOperand,
                    splitFuncEnv.splitFuncTempVars, insSplitPoints, bb.terminator, splitFuncEnv);
            handleSplittingAtTerminator(parentFunc, newlyAddingFunctions, fromAttachedFunction, handleArray,
                    splitFuncEnv, parentFuncEnv, newBBInsList, arrayElementSet, bb);
        }
    }

    private void handleBBInstructions(SplitFuncEnv splitFuncEnv, BIRBasicBlock bb) {
        splitFuncEnv.splitFuncBB.instructions = splitFuncEnv.splitFuncNewInsList;
        splitFuncEnv.splitFuncBB.terminator = bb.terminator;
        splitFuncEnv.splitFuncNewBBList.add(splitFuncEnv.splitFuncBB);
        splitFuncEnv.splitFuncNewInsList = new ArrayList<>();
    }

    private void createNewFuncForPeriodicSplit(BIRFunction parentFunc, List<BIRFunction> newlyAddingFunctions,
                                               boolean fromAttachedFunction, BIRVariableDcl handleArray,
                                               SplitFuncEnv splitFuncEnv, ParentFuncEnv parentFuncEnv,
                                               BIRBasicBlock bb, BIRAbstractInstruction bbIns) {
        splitFuncEnv.periodicSplitInsCount = 0;
        if (splitFuncEnv.splitFuncTempVars.tempVarsUsed) {
            splitFuncEnv.splitFuncArgs.add(handleArray);
        }

        // Create a new split BIRFunction
        List<BType> paramTypes = new ArrayList<>();
        for (BIRVariableDcl funcArg : splitFuncEnv.splitFuncArgs) {
            paramTypes.add(funcArg.type);
        }
        BType funcRetType = splitFuncEnv.returnValAssigned ? symbolTable.errorOrNilType : symbolTable.nilType;
        BInvokableType type =  new BInvokableType(paramTypes, funcRetType, null);
        splitFuncNum += 1;
        String splitFuncName = SPLIT_METHOD + splitFuncNum;
        Name newFuncName = new Name(splitFuncName);
        BIRFunction splitBirFunc = new BIRFunction(null, newFuncName, newFuncName, 0, type, DEFAULT_WORKER_NAME, 0,
                SymbolOrigin.VIRTUAL);
        populateSplitFuncLocalVarsAndErrorTable(parentFunc, splitFuncEnv, parentFuncEnv, bb, bbIns, funcRetType,
                splitBirFunc);

        // need to add BBs and () return var assignment and return BB
        BIRNonTerminator.ConstantLoad constantLoad = new BIRNonTerminator.ConstantLoad(bbIns.pos,
                new Name("()"), symbolTable.nilType, splitFuncEnv.returnOperand);
        splitFuncEnv.splitFuncNewInsList.add(constantLoad);
        splitFuncEnv.splitFuncBB.instructions.addAll(splitFuncEnv.splitFuncNewInsList);

        BIRBasicBlock exitBB = splitFuncEnv.returnBB;
        exitBB.terminator = new BIRTerminator.Return(null);
        splitFuncEnv.splitFuncBB.terminator = new BIRTerminator.GOTO(null, exitBB, bbIns.scope);
        splitFuncEnv.splitFuncCorrectTerminatorBBs.add(splitFuncEnv.splitFuncBB);
        splitFuncEnv.splitFuncNewBBList.add(splitFuncEnv.splitFuncBB);

        fixTerminatorBBsInPeriodicSplitFunc(splitFuncEnv);

        splitFuncEnv.splitFuncBBId = BIRGenUtils.renumberBasicBlock(splitFuncEnv.splitFuncBBId, exitBB);
        splitFuncEnv.splitFuncNewBBList.add(exitBB);
        splitBirFunc.basicBlocks = splitFuncEnv.splitFuncNewBBList;
        newlyAddingFunctions.add(splitBirFunc);

        fixErrorTableInPeriodicSplitFunc(splitFuncEnv, splitBirFunc);
        // split function would not have VarKind.LOCAL splitBirFunc.localVars.
        // Hence no need to correct localVar.startBB and localVar.endBB

        // now we need to call this function from parent func
        List<BIROperand> args = new ArrayList<>();
        for (BIRVariableDcl funcArg : splitFuncEnv.splitFuncArgs) {
            args.add(new BIROperand(funcArg));
        }

        BIRBasicBlock parentFuncNextBB = new BIRBasicBlock(parentFuncEnv.parentFuncBBId++);
        parentFuncEnv.parentFuncNewBB.instructions = parentFuncEnv.parentFuncNewInsList;

        BIROperand splitFuncCallResultOp = null;
        if (splitFuncEnv.returnValAssigned) {
            splitFuncCallResultOp = generateTempLocalVariable(symbolTable.errorOrNilType,
                    parentFuncEnv.parentFuncLocalVarList);
        }
        parentFuncEnv.parentFuncNewBB.terminator = new BIRTerminator.Call(bbIns.pos, InstructionKind.CALL,
                false, currentPackageId, newFuncName, args, splitFuncCallResultOp, parentFuncNextBB,
                Collections.emptyList(), Collections.emptySet(), bbIns.scope);

        parentFuncEnv.parentFuncNewInsList = new ArrayList<>();
        parentFuncEnv.parentFuncNewBBList.add(parentFuncEnv.parentFuncNewBB);
        parentFuncEnv.parentFuncNewBB = parentFuncNextBB;

        if (splitFuncEnv.returnValAssigned) {
            BIROperand isErrorResultOp = generateTempLocalVariable(symbolTable.booleanType,
                    parentFuncEnv.parentFuncLocalVarList);
            BIRNonTerminator errorTestIns = new BIRNonTerminator.TypeTest(null, symbolTable.errorType,
                    isErrorResultOp, splitFuncCallResultOp);
            parentFuncEnv.parentFuncNewInsList.add(errorTestIns);
            parentFuncEnv.parentFuncNewBB.instructions = parentFuncEnv.parentFuncNewInsList;
            BIRBasicBlock trueBB = new BIRBasicBlock(parentFuncEnv.parentFuncBBId++);
            BIRBasicBlock falseBB = new BIRBasicBlock(parentFuncEnv.parentFuncBBId++);
            parentFuncEnv.parentFuncNewBB.terminator = new BIRTerminator.Branch(null, isErrorResultOp, trueBB,
                    falseBB, bbIns.scope);
            parentFuncEnv.parentFuncNewBBList.add(parentFuncEnv.parentFuncNewBB);
            BIROperand castedErrorOp = generateTempLocalVariable(symbolTable.errorType,
                    parentFuncEnv.parentFuncLocalVarList);
            BIRNonTerminator typeCastErrIns = new BIRNonTerminator.TypeCast(null, castedErrorOp,
                    splitFuncCallResultOp, symbolTable.errorType, false);
            trueBB.instructions.add(typeCastErrIns);
            BIRNonTerminator moveIns = new BIRNonTerminator.Move(null, castedErrorOp,
                    parentFuncEnv.returnOperand);
            trueBB.instructions.add(moveIns);
            trueBB.terminator = new BIRTerminator.GOTO(null, parentFuncEnv.returnBB, bbIns.scope);
            parentFuncEnv.parentFuncNewBBList.add(trueBB);
            parentFuncEnv.parentFuncNewBB = falseBB;
            parentFuncEnv.parentFuncNewInsList = new ArrayList<>();
        }

        splitFuncEnv.reset(getTempVarsForArraySplit(), fromAttachedFunction);
    }

    private void populateSplitFuncLocalVarsAndErrorTable(BIRFunction parentFunc, SplitFuncEnv splitFuncEnv,
                                                         ParentFuncEnv parentFuncEnv, BIRBasicBlock bb,
                                                         BIRAbstractInstruction bbIns, BType funcRetType,
                                                         BIRFunction splitBirFunc) {
        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        for (BIRVariableDcl funcArg : splitFuncEnv.splitFuncArgs) {
            Name argName = funcArg.name;
            splitBirFunc.requiredParams.add(new BIRParameter(bbIns.pos, argName, 0));
            BIRFunctionParameter funcParameter = new BIRFunctionParameter(bbIns.pos, funcArg.type, argName,
                    VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false, false);
            functionParams.add(funcParameter);
            splitBirFunc.parameters.add(funcParameter);
        }
        splitBirFunc.argsCount = splitFuncEnv.splitFuncArgs.size();
        splitBirFunc.returnVariable = splitFuncEnv.returnVarDcl;
        splitFuncEnv.returnVarDcl.type = funcRetType;
        splitBirFunc.localVars.add(0, splitBirFunc.returnVariable);
        splitBirFunc.localVars.addAll(functionParams);
        splitBirFunc.localVars.addAll(splitFuncEnv.splitFuncLocalVarList);

        if (splitFuncEnv.splitFuncTempVars.tempVarsUsed) {
            splitBirFunc.localVars.add(splitFuncEnv.splitFuncTempVars.arrayIndexVarDcl);
            splitBirFunc.localVars.add(splitFuncEnv.splitFuncTempVars.typeCastVarDcl);
        }

        while (parentFuncEnv.errorTableIndex < parentFunc.errorTable.size()
                && parentFunc.errorTable.get(parentFuncEnv.errorTableIndex).trapBB.number <= bb.number) {
            splitFuncEnv.splitFuncErrorTable.add(parentFunc.errorTable.get(parentFuncEnv.errorTableIndex));
            parentFuncEnv.errorTableIndex++;
        }
        splitBirFunc.errorTable = splitFuncEnv.splitFuncErrorTable;
    }

    private void fixErrorTableInPeriodicSplitFunc(SplitFuncEnv splitFuncEnv, BIRFunction splitBirFunc) {
        Map<Integer, BIRBasicBlock> changedBBs = splitFuncEnv.splitFuncChangedBBs;
        for (BIRErrorEntry birErrorEntry : splitBirFunc.errorTable) {
            if (changedBBs.containsKey(birErrorEntry.trapBB.number)) {
                birErrorEntry.trapBB = changedBBs.get(birErrorEntry.trapBB.number);
            }
            if (changedBBs.containsKey(birErrorEntry.endBB.number)) {
                birErrorEntry.endBB = changedBBs.get(birErrorEntry.endBB.number);
            }
            if (changedBBs.containsKey(birErrorEntry.targetBB.number)) {
                birErrorEntry.targetBB = changedBBs.get(birErrorEntry.targetBB.number);
            }
        }
    }

    private void fixTerminatorBBsInPeriodicSplitFunc(SplitFuncEnv splitFuncEnv) {
        List<BIRBasicBlock> bbList = splitFuncEnv.splitFuncNewBBList;
        BIRBasicBlock beforeLastBB = null;
        for (BIRBasicBlock basicBlock : bbList) {
            if (!splitFuncEnv.splitFuncCorrectTerminatorBBs.contains(basicBlock)) {
                BIRTerminator terminator = basicBlock.terminator;
                Map<Integer, BIRBasicBlock> changedBBs = splitFuncEnv.splitFuncChangedBBs;
                if (terminator.thenBB != null) {
                    if (changedBBs.containsKey(terminator.thenBB.number)) {
                        terminator.thenBB = changedBBs.get(terminator.thenBB.number);
                    } else {
                        if (beforeLastBB == null) {
                            beforeLastBB = getBeforeLastBB(splitFuncEnv);
                        }
                        terminator.thenBB = beforeLastBB;
                    }
                }

                switch (terminator.getKind()) {
                    case GOTO -> {
                        if (changedBBs.containsKey(
                                ((BIRTerminator.GOTO) terminator).targetBB.number)) {
                            ((BIRTerminator.GOTO) terminator).targetBB = changedBBs.get(
                                    ((BIRTerminator.GOTO) terminator).targetBB.number);
                        } else {
                            if (beforeLastBB == null) {
                                beforeLastBB = getBeforeLastBB(splitFuncEnv);
                            }
                            ((BIRTerminator.GOTO) terminator).targetBB = beforeLastBB;
                        }
                    }
                    case BRANCH -> {
                        BIRTerminator.Branch branchTerminator = (BIRTerminator.Branch) terminator;
                        if (changedBBs.containsKey(branchTerminator.trueBB.number)) {
                            branchTerminator.trueBB = changedBBs.get(
                                    branchTerminator.trueBB.number);
                        }
                        if (changedBBs.containsKey(branchTerminator.falseBB.number)) {
                            branchTerminator.falseBB = changedBBs.get(
                                    branchTerminator.falseBB.number);
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        if (beforeLastBB != null) {
            bbList.add(beforeLastBB);
        }
    }

    private BIRBasicBlock getBeforeLastBB(SplitFuncEnv splitFuncEnv) {
        BIRBasicBlock beforeLastBB = new BIRBasicBlock(splitFuncEnv.splitFuncBBId++);
        BIRNonTerminator.ConstantLoad constantLoad = new BIRNonTerminator.ConstantLoad(
                splitFuncEnv.returnBB.terminator.pos, new Name("()"), symbolTable.nilType,
                splitFuncEnv.returnOperand);
        beforeLastBB.instructions.add(constantLoad);
        beforeLastBB.terminator = new BIRTerminator.GOTO(null, splitFuncEnv.returnBB,
                splitFuncEnv.returnBB.terminator.scope);
        return beforeLastBB;
    }

    private void populateSplitFuncArgsAndLocalVarList(SplitFuncEnv splitFuncEnv, BIRAbstractInstruction bbIns) {
        for (BIROperand rhsOperand : bbIns.getRhsOperands()) {
            if (!splitFuncEnv.splitAvailableOperands.contains(rhsOperand)
                    && needToPassRhsVarDclAsArg(rhsOperand)) {
                splitFuncEnv.splitFuncArgs.add(rhsOperand.variableDcl);
            }
        }
        if (bbIns.lhsOp != null) {
            splitFuncEnv.splitAvailableOperands.add(bbIns.lhsOp);
            if (needToPassLhsVarDclAsArg(bbIns.lhsOp)) {
                splitFuncEnv.splitFuncArgs.add(bbIns.lhsOp.variableDcl);
            }
            if (isTempOrSyntheticVar(bbIns.lhsOp.variableDcl)) {
                splitFuncEnv.splitFuncLocalVarList.add(bbIns.lhsOp.variableDcl);
            }
        }
    }

    private TempVarsForArraySplit getTempVarsForArraySplit() {
        BIRVariableDcl arrayIndexVarDcl = new BIRVariableDcl(null, symbolTable.intType,
                new Name("%arrIndex"), VarScope.FUNCTION, VarKind.TEMP, null);
        BIRVariableDcl typeCastVarDcl = new BIRVariableDcl(null, symbolTable.anyOrErrorType,
                new Name("%typeCast"), VarScope.FUNCTION, VarKind.TEMP, null);
        return new TempVarsForArraySplit(arrayIndexVarDcl, typeCastVarDcl);
    }

    private boolean setMapElementGivenOperand(Map<BIROperand, BIRMappingConstructorEntryWithIndex> birOperands,
                                              List<BIRNonTerminator> newInsList, BIROperand handleArrayOperand,
                                              TempVarsForArraySplit tempVars,
                                              Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints,
                                              BIRAbstractInstruction currIns, SplitFuncEnv splitFuncEnv,
                                              Map<BIROperand, NonTerminatorLocation> mapKeyOperandLocations,
                                              int bbIndex, int insIndex, Map<BIRAbstractInstruction,
            BIRMappingConstructorEntryWithIndex> globalAndArgVarKeyOrValueRelatedIns) {
        boolean splitPointsContainCurrIns = insSplitPoints.containsKey(currIns);
        if (splitPointsContainCurrIns) {
            SplitPointDetails splitPointDetails = insSplitPoints.get(currIns);
            List<BIROperand> currOperands = splitPointDetails.arrayElementsBIROperands;
            for (BIROperand currOperand : currOperands) {
                BIRMappingConstructorEntryWithIndex mappingConstructorEntryWithIndex = birOperands.get(currOperand);
                BIRNode.BIRMappingConstructorEntry mappingConstructorEntry = mappingConstructorEntryWithIndex
                        .mappingConstructorEntry;
                boolean isKeyValueEntry = mappingConstructorEntry instanceof BIRNode.BIRMappingConstructorKeyValueEntry;
                if (isKeyValueEntry && insIndex != -1) {
                    BIROperand keyOperand = ((BIRNode.BIRMappingConstructorKeyValueEntry) mappingConstructorEntry)
                            .keyOp;
                    if (mapKeyOperandLocations.containsKey(keyOperand)) {
                        NonTerminatorLocation nonTerminatorLocation = mapKeyOperandLocations.get(keyOperand);
                        if (nonTerminatorLocation != null && (nonTerminatorLocation.bbNum > bbIndex ||
                                (nonTerminatorLocation.bbNum == bbIndex && nonTerminatorLocation.insNum > insIndex))) {
                            splitPointDetails.splitHere = false;
                            continue;
                        }
                    }
                }
                int arrayIndex = mappingConstructorEntryWithIndex.index;
                setMapElement(newInsList, handleArrayOperand, currOperand, mappingConstructorEntry, arrayIndex,
                        tempVars);
            }
            // no need to set in else part as it will not affect since splitNow is considered along with this
            splitFuncEnv.splitHere = splitPointDetails.splitHere;
        }
        boolean globalAndArgVarKeyInsContainsCurrIns = globalAndArgVarKeyOrValueRelatedIns.containsKey(currIns);
        if (globalAndArgVarKeyInsContainsCurrIns) {
            // no need to set in else part as it will not affect since splitNow is considered along with this
            if (!splitPointsContainCurrIns) {
                splitFuncEnv.splitHere = true;
            }
            BIRMappingConstructorEntryWithIndex mappingConstructorEntryWithIndex =
                    globalAndArgVarKeyOrValueRelatedIns.get(currIns);
            BIRNode.BIRMappingConstructorKeyValueEntry keyValueEntry =
                    (BIRNode.BIRMappingConstructorKeyValueEntry) mappingConstructorEntryWithIndex
                    .mappingConstructorEntry;
            int arrayIndex = mappingConstructorEntryWithIndex.index;
            BIROperand valueOperand = keyValueEntry.valueOp;
            setMapElement(newInsList, handleArrayOperand, valueOperand, keyValueEntry, arrayIndex,
                    tempVars);
            BIRVariableDcl valueVarDcl = valueOperand.variableDcl;
            addToSplitFuncArgsIfVarDclIsArg(splitFuncEnv, valueVarDcl);
            BIRVariableDcl keyVarDcl = keyValueEntry.keyOp.variableDcl;
            addToSplitFuncArgsIfVarDclIsArg(splitFuncEnv, keyVarDcl);
        }
        return splitPointsContainCurrIns || globalAndArgVarKeyInsContainsCurrIns;
    }

    private void addToSplitFuncArgsIfVarDclIsArg(SplitFuncEnv splitFuncEnv, BIRVariableDcl variableDcl) {
        if (variableDcl.kind == VarKind.ARG) {
            splitFuncEnv.splitFuncArgs.add(variableDcl);
        }
    }

    private boolean setArrayElementGivenOperand(Map<BIROperand, BIRListConstructorEntryWithIndex> birOperands,
                                                List<BIRNonTerminator> newInsList, BIROperand handleArrayOperand,
                                                TempVarsForArraySplit tempVars,
                                                Map<BIRAbstractInstruction, SplitPointDetails> insSplitPoints,
                                                BIRAbstractInstruction currIns, SplitFuncEnv splitFuncEnv) {
        if (insSplitPoints.containsKey(currIns)) {
            SplitPointDetails splitPointDetails = insSplitPoints.get(currIns);
            List<BIROperand> currOperands = splitPointDetails.arrayElementsBIROperands;
            for (BIROperand currOperand : currOperands) {
                BIRListConstructorEntryWithIndex listConstructorEntryWithIndex = birOperands.get(currOperand);
                BIRNode.BIRListConstructorEntry listConstructorEntry = listConstructorEntryWithIndex
                        .listConstructorEntry;
                int arrayIndex = listConstructorEntryWithIndex.index;
                setArrayElement(newInsList, handleArrayOperand, currOperand, listConstructorEntry, arrayIndex,
                        tempVars);
            }
            // no need to set the below in else part as it will not affect since splitNow is considered along with this
            splitFuncEnv.splitHere = splitPointDetails.splitHere;
            return true;
        }
        return false;
    }

    private void setMapElement(List<BIRNonTerminator> newInsList,
                                 BIROperand handleArrayOperand, BIROperand valueOrExprOperand,
                                 BIRNode.BIRMappingConstructorEntry mappingConstructorEntry, int arrayIndex,
                                 TempVarsForArraySplit tempVars) {
        JMethodCallInstruction callSetEntry = new JMethodCallInstruction(null);
        callSetEntry.invocationType = INVOKESTATIC;
        callSetEntry.jClassName = LARGE_STRUCTURE_UTILS;
        tempVars.tempVarsUsed = true;
        BIRNonTerminator.ConstantLoad loadArrayIndex = new BIRNonTerminator.ConstantLoad(null,
                (long) arrayIndex, symbolTable.intType, tempVars.arrayIndexOperand);
        newInsList.add(loadArrayIndex);

        BIRNonTerminator.TypeCast typeCastInstruction = new BIRNonTerminator.TypeCast(null,
                tempVars.typeCastOperand, valueOrExprOperand, symbolTable.anyOrErrorType, true);
        newInsList.add(typeCastInstruction);

        if (mappingConstructorEntry instanceof BIRNode.BIRMappingConstructorKeyValueEntry) {
            callSetEntry.jMethodVMSig = "(" + GET_HANDLE_VALUE + GET_OBJECT + GET_OBJECT + "J)V";
            callSetEntry.args = new ArrayList<>(Arrays.asList(handleArrayOperand,
                    ((BIRNode.BIRMappingConstructorKeyValueEntry) mappingConstructorEntry).keyOp,
                    tempVars.typeCastOperand, tempVars.arrayIndexOperand));
            callSetEntry.name = "setKeyValueEntry";
        } else {
            callSetEntry.jMethodVMSig = HANDLE_OBJECT_LONG_ARGS;
            callSetEntry.args = new ArrayList<>(Arrays.asList(handleArrayOperand, tempVars.typeCastOperand,
                    tempVars.arrayIndexOperand));
            callSetEntry.name = "setSpreadFieldEntry";
        }
        newInsList.add(callSetEntry);
    }

    private void setArrayElement(List<BIRNonTerminator> newInsList,
                                 BIROperand handleArrayOperand, BIROperand insLhsOp,
                                 BIRNode.BIRListConstructorEntry listConstructorEntry, int arrayIndex,
                                 TempVarsForArraySplit tempVars) {
        JMethodCallInstruction callSetEntry = new JMethodCallInstruction(null);
        callSetEntry.invocationType = INVOKESTATIC;
        callSetEntry.jClassName = LARGE_STRUCTURE_UTILS;
        callSetEntry.jMethodVMSig = HANDLE_OBJECT_LONG_ARGS;
        tempVars.tempVarsUsed = true;
        BIRNonTerminator.ConstantLoad loadArrayIndex = new BIRNonTerminator.ConstantLoad(null,
                (long) arrayIndex, symbolTable.intType, tempVars.arrayIndexOperand);
        newInsList.add(loadArrayIndex);

        BIRNonTerminator.TypeCast typeCastInstruction = new BIRNonTerminator.TypeCast(null,
                tempVars.typeCastOperand, insLhsOp, symbolTable.anyOrErrorType, true);
        newInsList.add(typeCastInstruction);
        callSetEntry.args = new ArrayList<>(Arrays.asList(handleArrayOperand, tempVars.typeCastOperand,
                tempVars.arrayIndexOperand));

        if (listConstructorEntry instanceof BIRNode.BIRListConstructorExprEntry) {
            callSetEntry.name = "setExpressionEntry";
        } else {
            callSetEntry.name = "setSpreadEntry";
        }
        newInsList.add(callSetEntry);
    }

    /**
     * Obtain a list of possible splits that can be done inside a function due to large no. of instructions.
     *
     * @param basicBlocks available basic block list of the function
     * @param errorTableEntries available error table entries of the function
     * @param fromSplitFunction whether the parent function is a split function
     * @return a list of possible splits
     */
    private List<Split> getPossibleSplits(List<BIRBasicBlock> basicBlocks, List<BIRErrorEntry> errorTableEntries,
                                          boolean fromSplitFunction) {
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
                            trapBBNum = errorTableEntries.get(errTableEntryIndex).trapBB.number;
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
                        possibleSplits.add(new Split(insNum, splitEndInsIndex, bbNum, splitEndBBIndex, lhsOperandList,
                                newFuncArgs, splitErrorTableEntries, splitAgain, splitTypeArray, returnValAssigned));
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
                                (basicBlocks.get(0).instructions.get(0).lhsOp == splitStartOperand)
                                && fromSplitFunction) {
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
        return bbList.get(bbIndex).number;
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
        BIRBasicBlock currentBB  = possibleSplits.get(splitNum).startBBNum == 0
                ? replaceExistingBBAndGetCurrentBB(basicBlocks, bbNum) : null;
        Map<String, String> changedLocalVarStartBB = new HashMap<>(); // key: oldBBId, value: newBBId
        Map<String, String> changedLocalVarEndBB = new HashMap<>(); // key: oldBBId, value: newBBId
        Map<Integer, BIRBasicBlock> changedErrorTableEndBB = new HashMap<>(); // key: oldBB number, value: new BB

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
                    currentBB = replaceExistingBBAndGetCurrentBB(basicBlocks, bbNum);
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
            if (currentBB == null) {
                throw new IllegalStateException("currentBB cannot be null");
            }
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
                if (splitNum < possibleSplits.size() && (possibleSplits.get(splitNum).startBBNum == bbNum)) {
                    currentBB = replaceExistingBBAndGetCurrentBB(basicBlocks, bbNum);
                } else {
                    currentBB = null;
                }
                continue;
            }

            // now we have a split that go to another BB from this BB
            // need to add remaining instructions then create a split function
            if (startInsNum < possibleSplits.get(splitNum).firstIns) {
                currentBB.instructions.addAll(basicBlocks.get(bbNum).instructions.subList(startInsNum,
                        possibleSplits.get(splitNum).firstIns));
            }
            splitFuncNum += 1;
            String newFunctionName = SPLIT_METHOD + splitFuncNum;
            Name newFuncName = new Name(newFunctionName);
            Split currSplit = possibleSplits.get(splitNum);
            splitNum += 1;
            BIRNonTerminator lastInstruction = basicBlocks.get(currSplit.endBBNum).instructions.get(currSplit.lastIns);
            BType newFuncReturnType = lastInstruction.lhsOp.variableDcl.type;
            BIROperand splitLastInsLhsOp = new BIROperand(lastInstruction.lhsOp.variableDcl);
            BIROperand splitFuncCallResultOp;
            if (currSplit.returnValAssigned) {
                splitFuncCallResultOp = generateTempLocalVariable(newFuncReturnType, function.localVars);
                newFuncReturnType = createErrorUnionReturnType(newFuncReturnType);
            } else {
                splitFuncCallResultOp = splitLastInsLhsOp;
            }
            // extra +1 for BB incremented in createNewBIRFunctionAcrossBB function, hence BB number is newBBNum + 2
            BIRBasicBlock parentFuncNewBB = new BIRBasicBlock(newBBNum + 2);
            BIRFunction newBIRFunc = createNewBIRFunctionAcrossBB(function, newFuncName, newFuncReturnType, currSplit,
                    newBBNum, fromAttachedFunction, changedErrorTableEndBB, parentFuncNewBB);
            newBBNum += 2;
            newlyAddedFunctions.add(newBIRFunc);
            if (currSplit.splitFurther) {
                newlyAddedFunctions.addAll(splitBIRFunction(newBIRFunc, fromAttachedFunction, true,
                        currSplit.splitTypeArray));
            }
            function.errorTable.removeAll(currSplit.errorTableEntries);
            startInsNum = currSplit.lastIns + 1;

            List<BIROperand> args = new ArrayList<>();
            for (BIRVariableDcl funcArg : currSplit.funcArgs) {
                args.add(new BIROperand(funcArg));
            }
            currentBB.terminator = new BIRTerminator.Call(lastInstruction.pos, InstructionKind.CALL, false,
                    currentPackageId, newFuncName, args, splitFuncCallResultOp, parentFuncNewBB,
                    Collections.emptyList(), Collections.emptySet(), lastInstruction.scope);
            newBBList.add(currentBB);
            changedLocalVarEndBB.put(basicBlocks.get(bbNum).id.value, currentBB.id.value);
            currentBB = parentFuncNewBB;
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
        // renumbering BBs is not necessary here as it will be done later
        // fixing the error table does not require BBs to be properly ordered
        fixErrorTableEndBBs(function.errorTable, changedErrorTableEndBB);
    }

    private void fixErrorTableEndBBs(List<BIRErrorEntry> errorTable,
                                     Map<Integer, BIRBasicBlock> changedErrorTableEndBB) {
        // set error table changed BBs
        for (BIRErrorEntry birErrorEntry : errorTable) {
            if (changedErrorTableEndBB.containsKey(birErrorEntry.endBB.number)) {
                birErrorEntry.endBB = changedErrorTableEndBB.get(birErrorEntry.endBB.number);
            }
        }
    }

    private BIRBasicBlock replaceExistingBBAndGetCurrentBB(List<BIRBasicBlock> basicBlocks, int bbNum) {
        BIRBasicBlock currentBB = basicBlocks.get(bbNum);
        BIRBasicBlock newCurrentBB = new BIRBasicBlock(getBbIdNum(basicBlocks, bbNum));
        newCurrentBB.instructions = currentBB.instructions;
        newCurrentBB.terminator = currentBB.terminator;
        basicBlocks.set(bbNum, newCurrentBB);
        currentBB.terminator = null;
        currentBB.instructions = new ArrayList<>();
        return currentBB;
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
        BIROperand isErrorResultOp = generateTempLocalVariable(symbolTable.booleanType, function.localVars);
        BIRNonTerminator errorTestIns = new BIRNonTerminator.TypeTest(null, symbolTable.errorType,
                isErrorResultOp, splitFuncCallResultOp);
        currentBB.instructions.add(errorTestIns);
        BIRBasicBlock trueBB = new BIRBasicBlock(++newBBNum);
        BIRBasicBlock falseBB = new BIRBasicBlock(++newBBNum);
        currentBB.terminator = new BIRTerminator.Branch(null, isErrorResultOp, trueBB, falseBB, lastInsScope);
        newBBList.add(currentBB);
        BIROperand castedErrorOp = generateTempLocalVariable(symbolTable.errorType, function.localVars);
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
     * @param changedErrorTableEndBB changed error table end basic block map
     * @param parentFuncNewBB parent function new BB
     * @return newly created BIR function
     */
    private BIRFunction createNewBIRFunctionAcrossBB(BIRFunction parentFunc, Name funcName, BType retType,
                                                     Split currSplit, int newBBNum, boolean fromAttachedFunction,
                                                     Map<Integer, BIRBasicBlock> changedErrorTableEndBB,
                                                     BIRBasicBlock parentFuncNewBB) {
        List<BIRBasicBlock> parentFuncBBs = parentFunc.basicBlocks;
        BIRNonTerminator lastIns = parentFuncBBs.get(currSplit.endBBNum).instructions.get(currSplit.lastIns);
        List<BType> paramTypes = new ArrayList<>();
        for (BIRVariableDcl funcArg : currSplit.funcArgs) {
            paramTypes.add(funcArg.type);
        }
        BInvokableType type = new BInvokableType(paramTypes, retType, null);
        BIRFunction birFunc = new BIRFunction(null, funcName, funcName, 0, type, DEFAULT_WORKER_NAME, 0,
                SymbolOrigin.VIRTUAL);

        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        BIRVariableDcl selfVarDcl = null;
        for (BIRVariableDcl funcArg : currSplit.funcArgs) {
            Name argName = funcArg.name;
            birFunc.requiredParams.add(new BIRParameter(lastIns.pos, argName, 0));
            BIRFunctionParameter funcParameter = new BIRFunctionParameter(lastIns.pos, funcArg.type, argName,
                    VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false, false);
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
        BIRBasicBlock entryBB = new BIRBasicBlock(0);
        if (currSplit.firstIns < parentFuncBBs.get(currSplit.startBBNum).instructions.size()) {
            entryBB.instructions.addAll(parentFuncBBs.get(currSplit.startBBNum).instructions.subList(
                    currSplit.firstIns, parentFuncBBs.get(currSplit.startBBNum).instructions.size()));
        }
        entryBB.terminator = parentFuncBBs.get(currSplit.startBBNum).terminator;
        List<BIRBasicBlock> basicBlocks = birFunc.basicBlocks;
        basicBlocks.add(entryBB);

        // copying BBs between first and last bb
        for (int bbNum = currSplit.startBBNum + 1; bbNum < currSplit.endBBNum; bbNum++) {
            BIRBasicBlock bb = parentFuncBBs.get(bbNum);
            basicBlocks.add(bb);
            changedErrorTableEndBB.put(bb.number, parentFuncNewBB);
        }

        // create last BB and the return BB (exit BB)
        int lastBBIdNum = getBbIdNum(parentFuncBBs, currSplit.endBBNum);
        BIRBasicBlock lastBB = new BIRBasicBlock(lastBBIdNum);
        changedErrorTableEndBB.put(lastBBIdNum, parentFuncNewBB);
        if (0 <= currSplit.lastIns) {
            lastBB.instructions.addAll(parentFuncBBs.get(currSplit.endBBNum).instructions.subList(
                    0, currSplit.lastIns + 1));
        }
        lastBB.instructions.get(currSplit.lastIns).lhsOp = new BIROperand(birFunc.returnVariable);

        // a new BB with id using newBBNum is used to include the return statement
        newBBNum += 1;
        BIRBasicBlock exitBB = new BIRBasicBlock(newBBNum);
        exitBB.terminator = new BIRTerminator.Return(null);
        lastBB.terminator = new BIRTerminator.GOTO(null, exitBB, lastIns.scope);
        for (BIRBasicBlock basicBlock : basicBlocks) {
            fixTerminatorBBs(lastBBIdNum, lastBB, basicBlock.terminator);
        }
        basicBlocks.add(lastBB);
        basicBlocks.add(exitBB);
        rectifyVarKindsAndTerminators(birFunc, selfVarDcl, exitBB);
        rearrangeBasicBlocks(birFunc);
        return birFunc;
    }

    private void fixTerminatorBBs(int lastBBIdNum, BIRBasicBlock lastBB, BIRTerminator terminator) {
        if (terminator.thenBB != null && terminator.thenBB.number == lastBBIdNum) {
            terminator.thenBB = lastBB;
        }

        switch (terminator.getKind()) {
            case GOTO -> {
                if (((BIRTerminator.GOTO) terminator).targetBB.number == lastBBIdNum) {
                    ((BIRTerminator.GOTO) terminator).targetBB = lastBB;
                }
            }
            case BRANCH -> {
                BIRTerminator.Branch branchTerminator = (BIRTerminator.Branch) terminator;
                if (branchTerminator.trueBB.number == lastBBIdNum) {
                    branchTerminator.trueBB = lastBB;
                }
                if (branchTerminator.falseBB.number == lastBBIdNum) {
                    branchTerminator.falseBB = lastBB;
                }
            }
            default -> {
            }
        }
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
        for (Split possibleSplit : possibleSplits) {
            splitFuncNum += 1;
            String newFunctionName = SPLIT_METHOD + splitFuncNum;
            Name newFuncName = new Name(newFunctionName);
            BIROperand currentBBTerminatorLhsOp =
                    new BIROperand(instructionList.get(possibleSplit.lastIns).lhsOp.variableDcl);
            BIRFunction newBIRFunc = createNewBIRFuncForSplitInBB(newFuncName,
                    instructionList.get(possibleSplit.lastIns),
                    instructionList.subList(possibleSplit.firstIns, possibleSplit.lastIns),
                    possibleSplit.lhsVars, possibleSplit.funcArgs, fromAttachedFunction);
            newlyAddedFunctions.add(newBIRFunc);
            if (possibleSplit.splitFurther) {
                newlyAddedFunctions.addAll(splitBIRFunction(newBIRFunc, fromAttachedFunction, true,
                        possibleSplit.splitTypeArray));
            }
            currentBB.instructions.addAll(instructionList.subList(startInsNum, possibleSplit.firstIns));
            startInsNum = possibleSplit.lastIns + 1;
            newBBNum += 1;
            BIRBasicBlock newBB = new BIRBasicBlock(newBBNum);
            List<BIROperand> args = new ArrayList<>();
            for (BIRVariableDcl funcArg : possibleSplit.funcArgs) {
                args.add(new BIROperand(funcArg));
            }
            currentBB.terminator = new BIRTerminator.Call(instructionList.get(possibleSplit.lastIns).pos,
                    InstructionKind.CALL, false, currentPackageId, newFuncName, args, currentBBTerminatorLhsOp,
                    newBB, Collections.emptyList(), Collections.emptySet(),
                    instructionList.get(possibleSplit.lastIns).scope);
            newBBList.add(currentBB);
            currentBB = newBB;
        }
        // need to handle the last created BB, this is done outside the function
        return currentBB;
    }

    /**
     * Generate a temporary function scope variable.
     *
     * @param variableType The type of the variable
     * @param funcLocalVarList The BIR function local var list to which the variable should be added
     * @return The generated operand for the variable declaration
     */
    private BIROperand generateTempLocalVariable(BType variableType, List<BIRVariableDcl> funcLocalVarList) {
        BIRVariableDcl variableDcl = getSplitTempVariableDcl(variableType);
        funcLocalVarList.add(variableDcl);
        return new BIROperand(variableDcl);
    }

    private BIRVariableDcl getSplitTempVariableDcl(BType variableType) {
        Name variableName = new Name("$split$tempVar$_" + splitTempVarNum++);
        return new BIRVariableDcl(variableType, variableName, VarScope.FUNCTION, VarKind.TEMP);
    }

    /**
     * Generate a temporary function scope variable.
     *
     * @param variableType The type of the variable
     * @param funcLocalVarList The BIR function local var set to which the variable should be added
     * @return The generated operand for the variable declaration
     */
    private BIROperand generateTempLocalVariable(BType variableType, Set<BIRVariableDcl> funcLocalVarList) {
        BIRVariableDcl variableDcl = getSplitTempVariableDcl(variableType);
        funcLocalVarList.add(variableDcl);
        return new BIROperand(variableDcl);
    }

    /**
     * Create a new BIR function for a split which starts and ends in the same BB.
     *
     * @param funcName Name of the function to be created
     * @param currentIns last instruction in the split
     * @param collectedIns other instructions in the split
     * @param lhsOperandList set of varDcl of the LHS operands
     * @param funcArgs list of varDcl for the new function arguments
     * @param fromAttachedFunction flag which indicates an original attached function is being split
     * @return newly created BIR function
     */
    private BIRFunction createNewBIRFuncForSplitInBB(Name funcName, BIRNonTerminator currentIns,
                                                     List<BIRNonTerminator> collectedIns,
                                                     Set<BIRVariableDcl> lhsOperandList, List<BIRVariableDcl> funcArgs,
                                                     boolean fromAttachedFunction) {
        BType retType = currentIns.lhsOp.variableDcl.type;
        List<BType> paramTypes = new ArrayList<>();
        for (BIRVariableDcl funcArg : funcArgs) {
            paramTypes.add(funcArg.type);
        }
        BInvokableType type = new BInvokableType(paramTypes, retType, null);
        BIRFunction birFunc = new BIRFunction(null, funcName, funcName, 0, type, DEFAULT_WORKER_NAME, 0,
                SymbolOrigin.VIRTUAL);

        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        BIRVariableDcl selfVarDcl = null;
        for (BIRVariableDcl funcArg : funcArgs) {
            Name argName = funcArg.name;
            birFunc.requiredParams.add(new BIRParameter(currentIns.pos, argName, 0));
            BIRFunctionParameter funcParameter = new BIRFunctionParameter(currentIns.pos, funcArg.type, argName,
                    VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false, false);
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
        BIRBasicBlock entryBB = new BIRBasicBlock(0);
        entryBB.instructions.addAll(collectedIns);
        currentIns.lhsOp = new BIROperand(birFunc.returnVariable);
        entryBB.instructions.add(currentIns);
        BIRBasicBlock exitBB = new BIRBasicBlock(1);
        exitBB.terminator = new BIRTerminator.Return(null);
        entryBB.terminator = new BIRTerminator.GOTO(null, exitBB, currentIns.scope);
        birFunc.basicBlocks.add(entryBB);
        birFunc.basicBlocks.add(exitBB);
        rectifyVarKindsAndTerminators(birFunc, selfVarDcl, exitBB);
        return birFunc;
    }

    private void rectifyVarKindsAndTerminators(BIRFunction birFunction, BIRVariableDcl selfVarDcl,
                                               BIRBasicBlock returnBB) {
        Map<Name, BIRVariableDcl> funcArgsWithName = new HashMap<>();
        for (BIRFunctionParameter parameter : birFunction.parameters) {
            funcArgsWithName.put(parameter.name, parameter);
        }
        Map<BIRVariableDcl, BIROperand> newRhsOperands = new HashMap<>();
        for (BIRBasicBlock basicBlock : birFunction.basicBlocks) {
            for (BIRNonTerminator instruction : basicBlock.instructions) {
                if (instruction.lhsOp.variableDcl.kind == VarKind.SELF) {
                    instruction.lhsOp.variableDcl = selfVarDcl;
                } else if (instruction.lhsOp.variableDcl.kind == VarKind.RETURN) {
                    instruction.lhsOp.variableDcl = birFunction.returnVariable;
                    basicBlock.terminator = new BIRTerminator.GOTO(null, returnBB, basicBlock.terminator.scope);
                }
                setInsRhsOperands(selfVarDcl, funcArgsWithName, instruction, newRhsOperands);
            }
            if ((basicBlock.terminator.lhsOp != null) &&
                    (basicBlock.terminator.lhsOp.variableDcl.kind == VarKind.SELF)) {
                basicBlock.terminator.lhsOp.variableDcl = selfVarDcl;
            }
            setInsRhsOperands(selfVarDcl, funcArgsWithName, basicBlock.terminator, newRhsOperands);
        }
    }

    private void setInsRhsOperands(BIRVariableDcl selfVarDcl, Map<Name, BIRVariableDcl> funcArgsWithName,
                                   BIRAbstractInstruction instruction, Map<BIRVariableDcl, BIROperand> newRhsOperands) {
        if (selfVarDcl == null && funcArgsWithName.isEmpty()) {
            return;
        }
        List<BIROperand> operandList = new ArrayList<>();
        boolean foundArgs = false;
        BIROperand[] rhsOperands = instruction.getRhsOperands();
        for (BIROperand rhsOperand : rhsOperands) {
            if (rhsOperand.variableDcl.kind == VarKind.SELF) {
                foundArgs = true;
                populateNewRHSOperands(selfVarDcl, operandList, newRhsOperands, rhsOperand);
            } else if (funcArgsWithName.containsKey(rhsOperand.variableDcl.name)) {
                foundArgs = true;
                populateNewRHSOperands(funcArgsWithName.get(rhsOperand.variableDcl.name), operandList, newRhsOperands,
                        rhsOperand);
            } else {
                operandList.add(rhsOperand);
            }
        }
        if (foundArgs) {
            instruction.setRhsOperands(operandList.toArray(new BIROperand[0]));
        }
    }

    private static void populateNewRHSOperands(BIRVariableDcl variableDcl, List<BIROperand> operandList,
                                               Map<BIRVariableDcl, BIROperand> newRhsOperands, BIROperand rhsOperand) {
        if (!newRhsOperands.containsKey(rhsOperand.variableDcl)) {
            BIROperand newOperand = new BIROperand(variableDcl);
            operandList.add(newOperand);
            newRhsOperands.put(rhsOperand.variableDcl, newOperand);
        } else {
            operandList.add(newRhsOperands.get(rhsOperand.variableDcl));
        }
    }

    static class Split {
        int firstIns;
        int lastIns;
        int startBBNum;
        int endBBNum;
        boolean splitFurther;
        boolean splitTypeArray;
        boolean returnValAssigned;
        Set<BIRVariableDcl> lhsVars;
        List<BIRVariableDcl> funcArgs;
        List<BIRErrorEntry> errorTableEntries;

        private Split(int firstIns, int lastIns, int startBBNum, int endBBNum, Set<BIRVariableDcl> lhsVars,
                      List<BIRVariableDcl> funcArgs, List<BIRErrorEntry> errorTableEntries, boolean splitFurther,
                      boolean splitTypeArray, boolean returnValAssigned) {
            this.firstIns = firstIns;
            this.lastIns = lastIns;
            this.startBBNum = startBBNum;
            this.endBBNum = endBBNum;
            this.splitFurther = splitFurther;
            this.splitTypeArray = splitTypeArray;
            this.returnValAssigned = returnValAssigned;
            this.lhsVars = lhsVars;
            this.funcArgs = funcArgs;
            this.errorTableEntries = errorTableEntries;
        }
    }

    static class SplitPointDetails {
        List<BIROperand> arrayElementsBIROperands; // BIROperands which are the array elements
        // whether to split to a function here. This is made true if only, all the array element operands
        // in this instruction are first found when going from bottom to top
        boolean splitHere;

        private SplitPointDetails(List<BIROperand> arrayElementsBIROperands, boolean splitHere) {
            this.arrayElementsBIROperands = arrayElementsBIROperands;
            this.splitHere = splitHere;
        }
    }

    static class BIRListConstructorEntryWithIndex {
        BIRNode.BIRListConstructorEntry listConstructorEntry;
        int index;

        private BIRListConstructorEntryWithIndex(BIRNode.BIRListConstructorEntry listConstructorEntry, int index) {
            this.listConstructorEntry = listConstructorEntry;
            this.index = index;
        }
    }

    static class BIRMappingConstructorEntryWithIndex {
        BIRNode.BIRMappingConstructorEntry mappingConstructorEntry;
        int index;

        private BIRMappingConstructorEntryWithIndex(BIRNode.BIRMappingConstructorEntry mappingConstructorEntry,
                                                    int index) {
            this.mappingConstructorEntry = mappingConstructorEntry;
            this.index = index;
        }
    }

    static class NonTerminatorLocation {
        int bbNum;
        int insNum;

        private NonTerminatorLocation(int bbNum, int insNum) {
            this.bbNum = bbNum;
            this.insNum = insNum;
        }
    }

    static class TempVarsForArraySplit {
        BIRVariableDcl arrayIndexVarDcl;
        BIRVariableDcl typeCastVarDcl;
        BIROperand arrayIndexOperand;
        BIROperand typeCastOperand;
        boolean tempVarsUsed = false;

        private TempVarsForArraySplit(BIRVariableDcl arrayIndexVarDcl, BIRVariableDcl typeCastVarDcl) {
            this.arrayIndexVarDcl = arrayIndexVarDcl;
            this.typeCastVarDcl = typeCastVarDcl;
            this.arrayIndexOperand = new BIROperand(arrayIndexVarDcl);
            this.typeCastOperand = new BIROperand(typeCastVarDcl);
        }
    }

    static class SplitFuncEnv {
        List<BIRBasicBlock> splitFuncNewBBList = new ArrayList<>();
        List<BIRNonTerminator> splitFuncNewInsList =  new ArrayList<>();
        Set<BIRVariableDcl> splitFuncLocalVarList = new HashSet<>();
        Set<BIROperand> splitAvailableOperands = new HashSet<>();
        Set<BIRVariableDcl> splitFuncArgs = new LinkedHashSet<>(); // see hashset is possible
        List<BIRErrorEntry> splitFuncErrorTable = new ArrayList<>();
        //for split functions we need to always create BBs, change terminators BB, local var end BBs, and error table
        Map<Integer, BIRBasicBlock> splitFuncChangedBBs = new HashMap<>();
        Set<BIRBasicBlock> splitFuncCorrectTerminatorBBs = new HashSet<>();
        int periodicSplitInsCount = 0;
        boolean splitOkay = true; // this is made false when there is branch terminator
        int doNotSplitTillThisBBNum = 0;
        boolean returnValAssigned = false;
        int splitFuncBBId = 0;
        BIRBasicBlock splitFuncBB = new BIRBasicBlock(splitFuncBBId++);
        TempVarsForArraySplit splitFuncTempVars;
        BIRBasicBlock returnBB = new BIRBasicBlock(-1);
        BIRVariableDcl returnVarDcl;
        BIROperand returnOperand;
        boolean splitHere = false; // this is set from SplitPointDetails splitHere field

        private SplitFuncEnv(TempVarsForArraySplit splitFuncTempVars, boolean fromAttachedFunction) {
            this.splitFuncTempVars = splitFuncTempVars;
            if (fromAttachedFunction) {
                this.returnVarDcl = new BIRVariableDcl(null, null, new Name("%1"),
                        VarScope.FUNCTION, VarKind.RETURN, null);
            } else {
                this.returnVarDcl = new BIRVariableDcl(null, null, new Name("%0"),
                        VarScope.FUNCTION, VarKind.RETURN, null);
            }
            this.returnOperand = new BIROperand(this.returnVarDcl);
        }

        void reset(TempVarsForArraySplit tempVars, boolean fromAttachedFunc) {
            this.splitFuncNewBBList = new ArrayList<>();
            this.splitFuncNewInsList =  new ArrayList<>();
            this.splitFuncLocalVarList = new HashSet<>();
            this.splitAvailableOperands = new HashSet<>();
            this.splitFuncArgs = new LinkedHashSet<>(); // see hashset is possible
            this.splitFuncErrorTable = new ArrayList<>();
            this.splitFuncChangedBBs = new HashMap<>();
            this.splitFuncCorrectTerminatorBBs = new HashSet<>();
            this.splitFuncTempVars = tempVars;
            this.splitFuncBBId = 0;
            this.splitFuncBB = new BIRBasicBlock(this.splitFuncBBId++);
            this.splitOkay = true;
            this.returnValAssigned = false;
            if (fromAttachedFunc) {
                this.returnVarDcl = new BIRVariableDcl(null, null, new Name("%1"),
                        VarScope.FUNCTION, VarKind.RETURN, null);
            } else {
                this.returnVarDcl = new BIRVariableDcl(null, null, new Name("%0"),
                        VarScope.FUNCTION, VarKind.RETURN, null);
            }
            this.returnOperand = new BIROperand(this.returnVarDcl);
            this.returnBB = new BIRBasicBlock(-1);
        }
    }

    static class ParentFuncEnv {
        List<BIRBasicBlock> parentFuncNewBBList = new ArrayList<>();
        List<BIRNonTerminator> parentFuncNewInsList =  new ArrayList<>();
        Set<BIRVariableDcl> parentFuncLocalVarList = new HashSet<>();
        int errorTableIndex = 0;
        int parentFuncBBId = 0;
        BIRBasicBlock parentFuncNewBB = new BIRBasicBlock(parentFuncBBId++);
        BIRBasicBlock returnBB;
        BIROperand returnOperand;

        private ParentFuncEnv(BIRBasicBlock returnBB) {
            this.returnBB = returnBB;
        }
    }
}
