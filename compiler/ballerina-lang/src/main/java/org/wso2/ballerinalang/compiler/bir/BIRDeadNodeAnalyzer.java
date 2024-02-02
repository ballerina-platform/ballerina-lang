/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Detect the unused BIRFunctions, BIRTypeDefs and BIRConstants
 */
public class BIRDeadNodeAnalyzer extends BIRVisitor {

    private static final CompilerContext.Key<BIRDeadNodeAnalyzer> DEAD_BIR_NODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final HashSet<String> USED_FUNCTION_NAMES =
            new HashSet<>(Arrays.asList("main", ".<init>", ".<start>", ".<stop>"));
    private static final HashMap<String, HashSet<String>> INTEROP_DEPENDENCIES = new HashMap<>();
    private static final HashSet<InstructionKind> ANALYZED_INSTRUCTION_KINDS =
            new HashSet<>(Arrays.asList(InstructionKind.NEW_TYPEDESC, InstructionKind.NEW_INSTANCE,
                    InstructionKind.TYPE_CAST, InstructionKind.FP_LOAD, InstructionKind.TYPE_TEST));
    public PackageCache pkgCache;
    public TypeDefAnalyzer typeDefAnalyzer;
    public List<BIRNonTerminator> currentInstructionArr;
    public static BIRNode.BIRFunction currentParentFunction;
    public static InvocationData currentInvocationData;
    // We need to track whether function pointers are assigned to some other var after init
    // TODO Implement an instruction and var chain to track the fp assignments
    // If the FP is not invoked we can delete the path
    public static HashSet<BIRNode.BIRVariableDcl> localFpHolders = new HashSet<>();
    public final HashMap<PackageID, InvocationData> pkgWiseInvocationData = new HashMap<>();

    private BIRDeadNodeAnalyzer(CompilerContext context) {
        context.put(DEAD_BIR_NODE_ANALYZER_KEY, this);
        this.pkgCache = PackageCache.getInstance(context);
        this.typeDefAnalyzer = TypeDefAnalyzer.getInstance(context);
        initInteropDependencies();
    }

    public static BIRDeadNodeAnalyzer getInstance(CompilerContext context) {
        BIRDeadNodeAnalyzer BIRDeadNodeAnalyzer = context.get(DEAD_BIR_NODE_ANALYZER_KEY);
        if (BIRDeadNodeAnalyzer == null) {
            BIRDeadNodeAnalyzer = new BIRDeadNodeAnalyzer(context);
        }
        return BIRDeadNodeAnalyzer;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        currentInvocationData = pkgNode.symbol.invocationData_Deprecated;
        pkgWiseInvocationData.putIfAbsent(pkgNode.packageID, currentInvocationData);
        currentInvocationData.registerNodes(pkgNode.symbol.bir);

        // TODO Move populate typeDefPool into register nodes
        typeDefAnalyzer.populateTypeDefPool(pkgNode.symbol.bir);
        pkgNode.symbol.bir.typeDefs.forEach(typeDefAnalyzer::analyzeTypeDef);

        pkgNode.symbol.bir.serviceDecls.forEach(typeDefAnalyzer::analyzeServiceDecl);
        visit(pkgNode.symbol.bir);

        // TODO Make this happen only once after all the modules are analyzed
        pkgWiseInvocationData.values().forEach(InvocationData::updateInvocationData);
        return pkgNode;
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
        // Examining TypeDef Attached Functions
        birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
        birPackage.functions.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        currentParentFunction = birFunction;
        localFpHolders.clear();

        if (USED_FUNCTION_NAMES.contains(birFunction.originalName.value)) {
            currentInvocationData.dependencyMap.add(birFunction);
            birFunction.markSelfAndChildrenAsUsed();
        }

        // TODO Apply this logic to everywhere instead of unmask().contains()
        if ((birFunction.flags & Flags.RESOURCE) == Flags.RESOURCE) {
            birFunction.markSelfAndChildrenAsUsed();
        }
        // TODO Check whether we need to analyze parameters
        birFunction.basicBlocks.forEach(bb -> bb.accept(this));

        // Function parameters and return types are required for the function to run
        typeDefAnalyzer.addParamTypeDefsAsChildren(birFunction);
        typeDefAnalyzer.analyzeFunctionLevelTypeDef(birFunction.returnVariable.type, birFunction);

        // When there is external code callings, the compiler is blind to which functions will be invoked from the parameters
        // Therefore it is safer to connect all the attached functions of parameters to the parent external function

        if ((birFunction.flags & Flags.NATIVE) == Flags.NATIVE) {
            HashSet<BIRNode.BIRFunction> allAttachedFuncs = new HashSet<>();
            birFunction.childNodes.forEach(childNode -> {
                BIRNode.BIRTypeDefinition childTypeDef = ((BIRNode.BIRTypeDefinition) childNode);
                allAttachedFuncs.addAll(childTypeDef.attachedFuncs);
            });
            allAttachedFuncs.forEach(birFunction::addChildNode);
        }
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition typeDef) {
        typeDef.attachedFuncs.forEach(attachedFunc -> {
            //TODO use a constant for finding the init function of the type def
            if (attachedFunc.name.toString().contains("init")) {
                typeDef.addChildNode(attachedFunc);
            }

            if ((typeDef.flags & Flags.SERVICE) == Flags.SERVICE) {
                typeDef.addChildNode(attachedFunc);
            }

            // TODO This is done because of the risk of methods being overridden. Change it one day hopefully.
            typeDef.addChildNode(attachedFunc);

            attachedFunc.accept(this);
        });

        HashSet<String> interopDependencies = INTEROP_DEPENDENCIES.get(typeDef.type.tsymbol.pkgID.toString());
        if (interopDependencies == null) {
            return;
        }

        if (interopDependencies.contains(typeDef.internalName.toString())) {
            typeDef.attachedFuncs.forEach(typeDef::addChildNode);
            typeDef.markSelfAndChildrenAsUsed();
        }
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        currentInstructionArr = birBasicBlock.instructions;

        birBasicBlock.instructions.forEach(instruction -> {
            if (ANALYZED_INSTRUCTION_KINDS.contains(instruction.getKind())) {
                instruction.accept(this);
            }

            // The current algorithm does not track all FP assignments. It binds all the FP to their parent function,
            // if the FP is assigned to another Variable.
            // TODO change this logic to use Operands instead of VariableDeclarations
            HashSet<BIROperand> rhsOperands = new HashSet<>(Arrays.asList(instruction.getRhsOperands()));
            HashSet<BIRNode.BIRVariableDcl> rhsVars = new HashSet<>();
            rhsOperands.forEach(birOperand -> rhsVars.add(birOperand.variableDcl));
            rhsVars.retainAll(localFpHolders);

            if (!rhsVars.isEmpty()) {
                rhsVars.forEach(var -> currentParentFunction.addChildNode(var));
            }
        });

        // TODO Merge the following two iterations if possible
        // TODO Use terminator.accept(this) instead for following code
        if (birBasicBlock.terminator.getKind() == InstructionKind.CALL) {
            birBasicBlock.terminator.accept(this);
//            currentParentFunction.addChildNode(lookupBirFunction((BIRTerminator.Call) birBasicBlock.terminator));
        }

        if (birBasicBlock.terminator.getKind() == InstructionKind.FP_CALL) {
            currentParentFunction.addChildNode(((BIRTerminator.FPCall) birBasicBlock.terminator).fp.variableDcl);
        }
    }

    @Override
    public void visit(BIRTerminator.Call call) {
        currentParentFunction.addChildNode(lookupBirFunction(call));
        HashSet<BIROperand> argOperands = new HashSet<>(call.args);
        HashSet<BIRNode.BIRVariableDcl> argVars = new HashSet<>();
        argOperands.forEach(birOperand -> argVars.add(birOperand.variableDcl));
        argVars.retainAll(localFpHolders);

        if (!argVars.isEmpty()) {
            argVars.forEach(var -> currentParentFunction.addChildNode(var));
        }
    }

    public void visit(BIRNonTerminator.FPLoad fpLoadInstruction) {
        LambdaPointerData fpData = new LambdaPointerData(fpLoadInstruction, currentInstructionArr);

        pkgCache.getInvocationData_Deprecated(fpLoadInstruction.pkgId).fpDataPool.add(fpData);
    }

    @Override
    public void visit(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        if (newTypeDesc.type.tsymbol == null) {
            return;
        }
        typeDefAnalyzer.analyzeFunctionLevelTypeDef(newTypeDesc.type, currentParentFunction);
    }

    @Override
    public void visit(BIRNonTerminator.NewInstance newInstance) {
        typeDefAnalyzer.analyzeFunctionLevelTypeDef(newInstance.expectedType, currentParentFunction);
    }

    @Override
    public void visit(BIRNonTerminator.TypeCast typeCast) {
        typeDefAnalyzer.analyzeFunctionLevelTypeDef(typeCast.type, currentParentFunction);
        typeDefAnalyzer.analyzeFunctionLevelTypeDef(typeCast.lhsOp.variableDcl.type, currentParentFunction);
        typeDefAnalyzer.analyzeFunctionLevelTypeDef(typeCast.rhsOp.variableDcl.type, currentParentFunction);
    }

    @Override
    public void visit(BIRNonTerminator.TypeTest typeTest) {
        typeDefAnalyzer.analyzeFunctionLevelTypeDef(typeTest.type, currentParentFunction);
    }

    public BIRNode.BIRFunction lookupBirFunction(BIRTerminator.Call terminatorCall) {
        InvocationData invocationData = pkgCache.getInvocationData_Deprecated(terminatorCall.calleePkg);
        return invocationData.functionPool.get(terminatorCall.originalName.value);
    }

    private void initInteropDependencies() {
        Properties prop = new Properties();

        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("interop-dependencies.properties");
            prop.load(stream);
            for (Entry<Object, Object> entry : prop.entrySet()) {
                HashSet<String> usedRecordNames =
                        new HashSet<>(Arrays.asList(entry.getValue().toString().split(",")));
                INTEROP_DEPENDENCIES.putIfAbsent(entry.getKey().toString(), usedRecordNames);
            }

        } catch (IOException e) {
            System.out.println("Failed to load interop-dependencies : " + e);
        }
    }

    public static class InvocationData {

        // TODO Change the nodePool structure to HashMap<BType, BIRNode>
        public final HashMap<String, BIRNode.BIRFunction> functionPool = new HashMap<>();
        public final HashSet<BIRNode.BIRFunction> usedFunctions = new HashSet<>();
        public final HashSet<BIRNode.BIRFunction> deadFunctions = new HashSet<>();
        public final HashMap<BType, BIRNode.BIRTypeDefinition> typeDefPool = new HashMap<>();
        public final HashSet<BIRNode.BIRTypeDefinition> usedTypeDefs = new HashSet<>();
        public final HashSet<BIRNode.BIRTypeDefinition> deadTypeDefs = new HashSet<>();
        public final HashSet<LambdaPointerData> fpDataPool = new HashSet<>();
        public final HashSet<BIRNode.BIRDocumentableNode> dependencyMap = new HashSet<>();
        public final HashSet<BIRNode.BIRFunction> usedSourceFunctions = new HashSet<>();
        public final HashSet<BIRNode.BIRFunction> deadSourceFunctions = new HashSet<>();
        public boolean packageIsUsed = false;

        // TODO Remove the "remove from hashset" part later. It wont be needed
        public void updateInvocationData() {
            functionPool.values().forEach(birFunction -> {
                if (birFunction.usedState == UsedState.UNUSED) {
                    deadFunctions.add(birFunction);
                    usedFunctions.remove(birFunction);
                    if (birFunction.origin == SymbolOrigin.COMPILED_SOURCE) {
                        deadSourceFunctions.add(birFunction);
                        usedSourceFunctions.remove(birFunction);
                    }
                } else {
                    usedFunctions.add(birFunction);
                    deadFunctions.remove(birFunction);
                    if (birFunction.origin == SymbolOrigin.COMPILED_SOURCE) {
                        usedSourceFunctions.add(birFunction);
                        deadSourceFunctions.remove(birFunction);
                    }
                }
            });

            typeDefPool.values().forEach(birTypeDef -> {
                if (birTypeDef.usedState == UsedState.UNUSED) {
                    deadTypeDefs.add(birTypeDef);
                    usedTypeDefs.remove(birTypeDef);
                } else {
                    usedTypeDefs.add(birTypeDef);
                    deadTypeDefs.remove(birTypeDef);
                }
            });
        }

        // TODO Dont use the original name. Use name instead.
        // TODO Parent class of attached functions is the first argument of all attached function calls
        public void registerNodes(BIRNode.BIRPackage birPackage) {
            birPackage.functions.forEach(this::initializeFunction);
            // TODO check why we omitted the FINITE typeDefs
            // TODO migrating the typeDef UsedState initialization to TypeDefAnalyzer. Clean up here if its permanent
            birPackage.typeDefs.forEach(typeDef -> {
                typeDefPool.putIfAbsent(typeDef.type, typeDef);
                typeDef.attachedFuncs.forEach(birFunction -> initializeAttachedFunction(typeDef, birFunction));
            });
        }

        public void initializeFunction(BIRNode.BIRFunction birFunction) {
            birFunction.usedState = UsedState.UNUSED;
            functionPool.putIfAbsent(birFunction.originalName.value, birFunction);
        }

        public void initializeAttachedFunction(BIRNode.BIRTypeDefinition parentTypeDef,
                                               BIRNode.BIRFunction attachedFunc) {
            attachedFunc.usedState = UsedState.UNUSED;
            functionPool.putIfAbsent(parentTypeDef.internalName.value + "." + attachedFunc.name.value, attachedFunc);
        }
    }

    public static class LambdaPointerData {

        // Can be deleted from the BirFunctions of the enclosing module
        public BIRNode.BIRFunction lambdaFunction;
        // Can be deleted from the localVar or global var list
        public BIRNode.BIRVariableDcl lambdaPointerVar;
        // Can be deleted from the instruction array of either the parent function or init<> function
        public BIRNonTerminator.FPLoad fpLoadInstruction;
        public List<BIRNonTerminator> instructionArray;

        public LambdaPointerData(BIRNonTerminator.FPLoad fpLoadInstruction,
                                 List<BIRNonTerminator> instructionArray) {
            this.fpLoadInstruction = fpLoadInstruction;
            this.instructionArray = instructionArray;
            this.lambdaPointerVar = fpLoadInstruction.lhsOp.variableDcl;

            lambdaFunction = currentInvocationData.functionPool.get(fpLoadInstruction.funcName.toString());
            initializeFpData();
        }

        private void initializeFpData() {
            if (lambdaPointerVar.usedState == UsedState.UNEXPOLORED) {
                lambdaPointerVar.usedState = UsedState.UNUSED;
            }
            localFpHolders.add(lambdaPointerVar);
            lambdaPointerVar.addChildNode(lambdaFunction);
        }

        // functions with default parameters will desugar into a new lambda function that gets invoked through a FP_CALL inside the init function
        // We need to delete those if the lambda function is not used
        // If the FP_LOAD instruction is not removed its references will be used for CodeGen.
        // We need to prevent this from happening to fully clean the UNUSED functions.
        public void deleteIfUnused() {
            if (lambdaPointerVar.usedState == UsedState.UNUSED) {
                instructionArray.remove(fpLoadInstruction);
            }
        }
    }
}
