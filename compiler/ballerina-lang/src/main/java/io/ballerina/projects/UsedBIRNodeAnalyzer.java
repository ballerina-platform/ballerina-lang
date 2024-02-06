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
package io.ballerina.projects;

import org.ballerinalang.model.elements.PackageID;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UsedBIRNodeAnalyzer extends BIRVisitor {

    private static final CompilerContext.Key<UsedBIRNodeAnalyzer> DEAD_BIR_NODE_ANALYZER_2_KEY =
            new CompilerContext.Key<>();
    private static final HashSet<String> USED_FUNCTION_NAMES =
            new HashSet<>(Arrays.asList("main", ".<init>", ".<start>", ".<stop>"));
    private static final HashMap<String, HashSet<String>> INTEROP_DEPENDENCIES = new HashMap<>();
    private static final HashSet<InstructionKind> ANALYZED_INSTRUCTION_KINDS =
            new HashSet<>(Arrays.asList(InstructionKind.NEW_TYPEDESC, InstructionKind.NEW_INSTANCE,
                    InstructionKind.TYPE_CAST, InstructionKind.FP_LOAD, InstructionKind.TYPE_TEST,
                    InstructionKind.RECORD_DEFAULT_FP_LOAD));
    private static final String EXTERNAL_METHOD_ANNOTATION_TAG = "Method";
    public PackageCache pkgCache;
    public UsedTypeDefAnalyzer usedTypeDefAnalyzer;
    public List<BIRNonTerminator> currentInstructionArr;
    private BIRNode.BIRFunction currentParentFunction;
    public UsedBIRNodeAnalyzer.InvocationData currentInvocationData;
    // We need to track whether function pointers are assigned to some other var after init
    // TODO Implement an instruction and var chain to track the fp assignments
    // If the FP is not invoked we can delete the path
    public static HashSet<BIRNode.BIRVariableDcl> localFpHolders = new HashSet<>();
    public final HashMap<PackageID, UsedBIRNodeAnalyzer.InvocationData> pkgWiseInvocationData = new HashMap<>();
    public PackageID currentPkgID;

    private UsedBIRNodeAnalyzer(CompilerContext context) {
        context.put(DEAD_BIR_NODE_ANALYZER_2_KEY, this);
        this.pkgCache = PackageCache.getInstance(context);
        this.usedTypeDefAnalyzer = UsedTypeDefAnalyzer.getInstance(context);
        initInteropDependencies();
    }

    public static UsedBIRNodeAnalyzer getInstance(CompilerContext context) {
        UsedBIRNodeAnalyzer deadBIRNodeAnalyzer = context.get(DEAD_BIR_NODE_ANALYZER_2_KEY);
        if (deadBIRNodeAnalyzer == null) {
            deadBIRNodeAnalyzer = new UsedBIRNodeAnalyzer(context);
        }
        return deadBIRNodeAnalyzer;
    }

    public void analyze(BLangPackage pkgNode) {
        currentInvocationData = pkgNode.symbol.invocationData;
        pkgWiseInvocationData.putIfAbsent(pkgNode.packageID, currentInvocationData);
        currentPkgID = pkgNode.packageID;

        if (!currentInvocationData.moduleIsUsed) {
            currentInvocationData.registerNodes(usedTypeDefAnalyzer, pkgNode.symbol.bir);
        }

        for (BIRNode.BIRDocumentableNode node : currentInvocationData.startPointNodes) {
            visitNode(node);
        }
    }

    private void visitNode(BIRNode nodeToVisit) {
        BIRNode.BIRFunction prevParentFunction = currentParentFunction;
        List<BIRNonTerminator> prevInstructionArr = currentInstructionArr;

        nodeToVisit.accept(this);

        currentParentFunction = prevParentFunction;
        currentInstructionArr = prevInstructionArr;
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        if (birFunction.usedState == UsedState.USED) {
            return;
        } else {
            birFunction.usedState = UsedState.USED;
        }

        currentParentFunction = birFunction;
        currentInvocationData.addToUsedPool(birFunction);

        if ((birFunction.flags & Flags.NATIVE) == Flags.NATIVE) {
            registerUsedNativeClassPaths(birFunction);
        }

        HashSet<BIRNode.BIRVariableDcl> prevLocalFpHolders = new HashSet<>(localFpHolders);
        localFpHolders.clear();

        birFunction.basicBlocks.forEach(this::visitNode);

        // TODO Check whether we need to analyze parameters. It might not be needed because they are used for invoking anyways
        // Function parameters and return types are required for the function to run
        birFunction.parameters.forEach(param -> usedTypeDefAnalyzer.analyzeTypeDefWithinScope(param.type, birFunction));
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(birFunction.returnVariable.type, birFunction);
        localFpHolders = prevLocalFpHolders;
    }

    @Override
    public void visit(BIRNode.BIRServiceDeclaration serviceDeclaration) {
        serviceDeclaration.listenerTypes.forEach(
                listenerType -> usedTypeDefAnalyzer.analyzeTypeDefWithinScope(listenerType, serviceDeclaration));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition typeDef) {
        if (typeDef.usedState != UsedState.USED) {
            typeDef.usedState = UsedState.USED;
        } else {
            return;
        }
        currentInvocationData.addToUsedPool(typeDef);   // TODO Check whether we need this
        usedTypeDefAnalyzer.analyzeTypeDef(typeDef);

        typeDef.attachedFuncs.forEach(attachedFunc -> {
            // TODO This is done because of the risk of methods being overridden. Change it one day hopefully.
            addDependentFunctionAndVisit(typeDef, attachedFunc, typeDef.type.tsymbol.pkgID);
        });
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        currentInstructionArr = birBasicBlock.instructions;

        birBasicBlock.instructions.forEach(instruction -> {
            if (ANALYZED_INSTRUCTION_KINDS.contains(instruction.getKind())) {
                visitNode(instruction);
            }

            // The current algorithm does not track all FP assignments. It binds all the FP to their parent function,
            // if the FP is assigned to another Variable.
            // TODO change this logic to use Operands instead of VariableDeclarations
            HashSet<BIROperand> rhsOperands = new HashSet<>(Arrays.asList(instruction.getRhsOperands()));
            HashSet<BIRNode.BIRVariableDcl> rhsVars = new HashSet<>();
            rhsOperands.forEach(birOperand -> rhsVars.add(birOperand.variableDcl));
            rhsVars.retainAll(localFpHolders);

            if (!rhsVars.isEmpty() && instruction.kind != InstructionKind.RECORD_DEFAULT_FP_LOAD) {
                rhsVars.forEach(var -> {
                    FunctionPointerData fpData = currentInvocationData.getFPData(var);

                    if (fpData != null && fpData.lambdaFunction != null) {
                        visitNode(fpData.lambdaFunction);
                    }
                    currentParentFunction.addChildNode(var);
                });
            }
        });

        // TODO Merge the following two iterations if possible
        // TODO Use terminator.accept(this) instead for following code
        if (birBasicBlock.terminator.getKind() == InstructionKind.CALL) {
            visitNode(birBasicBlock.terminator);
        }

        if (birBasicBlock.terminator.getKind() == InstructionKind.FP_CALL) {
            BIRNode.BIRVariableDcl fpPointer = ((BIRTerminator.FPCall) birBasicBlock.terminator).fp.variableDcl;

            fpPointer.usedState = UsedState.USED;
            currentParentFunction.childNodes.add(fpPointer);
            fpPointer.parentNodes.add(currentParentFunction);

            FunctionPointerData fpData =
                    currentInvocationData.getFPData(((BIRTerminator.FPCall) birBasicBlock.terminator).fp.variableDcl);
            if (fpData != null && fpData.lambdaFunction != null) {
                visitNode(fpData.lambdaFunction);
            }
        }
    }

    @Override
    public void visit(BIRTerminator.Call call) {
        // If function pointers are passed as parameters, they will be bound to the parent function
        HashSet<BIRNode.BIRVariableDcl> argVars = new HashSet<>();
        call.args.forEach(birOperand -> argVars.add(birOperand.variableDcl));
        argVars.retainAll(localFpHolders);
        if (!argVars.isEmpty()) {
            argVars.forEach(var -> {
                FunctionPointerData fpData = currentInvocationData.getFPData(var);
                if (fpData != null && fpData.lambdaFunction != null) {
                    visitNode(fpData.lambdaFunction);
                }
                currentParentFunction.addChildNode(var);
            });
        }

        BIRNode.BIRDocumentableNode childNode = lookupBirFunction(call);
        if (childNode == null) {
            return;
        }
        // Invoked function will also be visited
        addDependentFunctionAndVisit(currentParentFunction, childNode, call.calleePkg);
    }

    @Override
    public void visit(BIRNonTerminator.FPLoad fpLoadInstruction) {
        BIRNode.BIRFunction pointedFunction =
                lookupBirFunction(fpLoadInstruction.pkgId, fpLoadInstruction.funcName.value);

        FunctionPointerData fpData =
                new FunctionPointerData(fpLoadInstruction, currentInstructionArr,
                        pointedFunction);

        if (fpData.lambdaPointerVar.usedState == UsedState.USED && pointedFunction != null) {
            visitNode(pointedFunction);
        }
        pkgCache.getInvocationData(currentPkgID).varDeclWiseFPDataPool.put(
                fpLoadInstruction.lhsOp.variableDcl, fpData);

    }

    /*
     recordDefaultFPLoad instructions can be "USED" without any reference to the "lhsOp".
     Make sure to check weather the enclosed type is used before deleting the instruction.
     */
    @Override
    public void visit(BIRNonTerminator.RecordDefaultFPLoad recordDefaultFPLoad) {
        FunctionPointerData fpData = currentInvocationData.getFPData(recordDefaultFPLoad.lhsOp.variableDcl);

        fpData.recordDefaultFPLoad = recordDefaultFPLoad;
        if (fpData.recordDefaultFPLoad.enclosedType.isUsed) {
            fpData.lambdaPointerVar.usedState = UsedState.USED;
            visitNode(fpData.lambdaFunction);
        }

        currentInvocationData.recordDefTypeWiseFPDataPool.putIfAbsent(recordDefaultFPLoad.enclosedType,
                new HashSet<>());
        currentInvocationData.recordDefTypeWiseFPDataPool.get(recordDefaultFPLoad.enclosedType).add(fpData);
    }

    @Override
    public void visit(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        if (newTypeDesc.type.tsymbol == null) {
            return;
        }
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(newTypeDesc.type, currentParentFunction);
    }

    @Override
    public void visit(BIRNonTerminator.NewInstance newInstance) {
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(newInstance.expectedType, currentParentFunction);
        if (newInstance.def != null) {
            visitNode(newInstance.def);
        }
    }

    @Override
    public void visit(BIRNonTerminator.TypeCast typeCast) {
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(typeCast.type, currentParentFunction);
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(typeCast.lhsOp.variableDcl.type, currentParentFunction);
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(typeCast.rhsOp.variableDcl.type, currentParentFunction);
    }

    @Override
    public void visit(BIRNonTerminator.TypeTest typeTest) {
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(typeTest.type, currentParentFunction);
    }

    public BIRNode.BIRFunction lookupBirFunction(BIRTerminator.Call terminatorCall) {
        UsedBIRNodeAnalyzer.InvocationData invocationData = pkgCache.getInvocationData(terminatorCall.calleePkg);
        if (!invocationData.moduleIsUsed) {
            //TODO find a better way to do this. It is ugly...
            invocationData.registerNodes(usedTypeDefAnalyzer, pkgCache.getBirPkg(terminatorCall.calleePkg));
        }

        return invocationData.functionPool.get(terminatorCall.originalName.value);
    }

    private BIRNode.BIRFunction lookupBirFunction(PackageID pkgId, String funcName) {
        UsedBIRNodeAnalyzer.InvocationData invocationData = pkgCache.getInvocationData(pkgId);
        if (!invocationData.moduleIsUsed) {
            //TODO find a better way to do this. It is ugly...
            invocationData.registerNodes(usedTypeDefAnalyzer, pkgCache.getBirPkg(pkgId));
        }

        return invocationData.functionPool.get(funcName);
    }

    private void initInteropDependencies() {
        Properties prop = new Properties();

        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("interop-dependencies.properties");
            prop.load(stream);
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                HashSet<String> usedRecordNames =
                        new HashSet<>(Arrays.asList(entry.getValue().toString().split(",")));
                INTEROP_DEPENDENCIES.putIfAbsent(entry.getKey().toString(), usedRecordNames);
            }

        } catch (IOException e) {
            System.out.println("Failed to load interop-dependencies : " + e);
        }
    }

    private void registerUsedNativeClassPaths(BIRNode.BIRFunction birFunction) {
        birFunction.annotAttachments.forEach(annot -> {
            if (annot.annotTagRef.value.equals(EXTERNAL_METHOD_ANNOTATION_TAG)) {
                BIRNode.ConstValue constValue = ((BIRNode.BIRConstAnnotationAttachment) annot).annotValue;
                String filePath =
                        ((HashMap<String, BIRNode.ConstValue>) constValue.value).get("class").value.toString();
                currentInvocationData.usedNativeClassPaths.add(filePath.replace(".","/")+ ".class");
            }
        });
    }

    private void addDependentFunctionAndVisit(BIRNode.BIRDocumentableNode parentNode, BIRNode.BIRDocumentableNode childFunction,
                                              PackageID childPkgId) {
        parentNode.childNodes.add(childFunction);
        childFunction.parentNodes.add(parentNode);
        if (childPkgId.equals(currentPkgID)) {
            visitNode(childFunction);
            return;
        }

        UsedBIRNodeAnalyzer.InvocationData childInvocationData = pkgCache.getInvocationData(childPkgId);

        // Handling langLibs for now
        if (childInvocationData == null) {
            return;
        }

        // Child is an external one
        childInvocationData.moduleIsUsed = true;
        childInvocationData.startPointNodes.add(childFunction);
        currentInvocationData.childPackages.add(childFunction.getPackageID());
    }

    public static class InvocationData {

        // TODO Change the nodePool structure to HashMap<BType, BIRNode>
        public final HashMap<String, BIRNode.BIRFunction> functionPool = new HashMap<>();
        public final HashSet<BIRNode.BIRFunction> usedFunctions = new HashSet<>();
        public final HashMap<BType, BIRNode.BIRTypeDefinition> typeDefPool = new HashMap<>();
        public final HashSet<BIRNode.BIRTypeDefinition> usedTypeDefs = new HashSet<>();
        private final HashMap<BIRNode.BIRVariableDcl, FunctionPointerData> varDeclWiseFPDataPool = new HashMap<>();
        private final HashMap<BType, HashSet<FunctionPointerData>> recordDefTypeWiseFPDataPool = new HashMap<>();
        public final ArrayList<BIRNode.BIRDocumentableNode> startPointNodes = new ArrayList<>();
        public final HashSet<PackageID> childPackages = new HashSet<>();
        public HashSet<String> interopDependencies = new HashSet<>();
        public HashSet<String> usedNativeClassPaths = new HashSet<>();
        public  PackageID pkgID;
        public boolean moduleIsUsed = false;

        // Following Sets are used to whitelist the bal types called through ValueCreator in native dependencies
        private static final HashSet<String> WHITELSITED_FILE_NAMES = new HashSet<>(Arrays.asList("types.bal", "error.bal", "stream_types.bal"));
        private static final HashSet<String> PKGS_WITH_WHITELSITED_FILES = new HashSet<>(Arrays.asList("ballerinax/mysql:1.11.0", "ballerina/sql:1.11.1", "ballerinax/persist.sql:1.2.1"));

        // TODO Don't use the original name. Use name instead.
        // TODO Parent class of attached functions is the first argument of all attached function calls
        public void registerNodes(UsedTypeDefAnalyzer typeDefAnalyzer, BIRNode.BIRPackage birPackage) {
            // TODO Remove this when we can get the BIRs of all modules. One day we can get the langlibs
            if (birPackage == null) {
                return;
            }

            birPackage.functions.forEach(this::initializeFunction);
            this.interopDependencies = INTEROP_DEPENDENCIES.get(birPackage.packageID.toString());
            typeDefAnalyzer.populateTypeDefPool(birPackage, interopDependencies);
            // TODO check why we omitted the FINITE typeDefs
            // TODO migrating the typeDef UsedState initialization to TypeDefAnalyzer. Clean up here if its permanent
            birPackage.typeDefs.forEach(typeDef -> {
                typeDef.usedState = UsedState.UNUSED;
                typeDefPool.putIfAbsent(typeDef.type, typeDef);
                typeDef.attachedFuncs.forEach(birFunction -> initializeAttachedFunction(typeDef, birFunction));

                // TODO use annotation for whitelisting instead
                if (PKGS_WITH_WHITELSITED_FILES.contains(birPackage.packageID.toString())) {
                    if (isInWhiteListedBalFile(typeDef)) {
                        this.startPointNodes.add(typeDef);
                    }
                }
            });
            this.startPointNodes.addAll(birPackage.serviceDecls);
            this.pkgID = birPackage.packageID;
            this.moduleIsUsed = true;
        }

        public boolean isInWhiteListedBalFile(BIRNode.BIRTypeDefinition typedef) {
            if (typedef.pos != null && WHITELSITED_FILE_NAMES.contains(typedef.pos.lineRange().fileName())) {
                return true;
            }
            return false;
        }

        // TODO Merge initializeFunction and initializeAttachedFunction
        public void initializeFunction(BIRNode.BIRFunction birFunction) {
            birFunction.usedState = UsedState.UNUSED;
            functionPool.putIfAbsent(birFunction.originalName.value, birFunction);

            if (USED_FUNCTION_NAMES.contains(birFunction.name.value) ||
                    (birFunction.flags & Flags.RESOURCE) == Flags.RESOURCE) {
                this.startPointNodes.add(birFunction);
            }
        }

        public void initializeAttachedFunction(BIRNode.BIRTypeDefinition parentTypeDef,
                                               BIRNode.BIRFunction attachedFunc) {
            attachedFunc.usedState = UsedState.UNUSED;
            functionPool.putIfAbsent(parentTypeDef.internalName.value + "." + attachedFunc.name.value, attachedFunc);
        }

        public void addToUsedPool(BIRNode.BIRDocumentableNode node) {
            if (node instanceof BIRNode.BIRFunction) {
                this.usedFunctions.add((BIRNode.BIRFunction) node);
            }
            else if (node instanceof BIRNode.BIRTypeDefinition) {
                this.usedTypeDefs.add((BIRNode.BIRTypeDefinition) node);
            }
        }

        public FunctionPointerData getFPData(BIRNode.BIRVariableDcl variableDcl) {
            return this.varDeclWiseFPDataPool.get(variableDcl);
        }

        public HashSet<FunctionPointerData> getFpData(BType bType) {
            return this.recordDefTypeWiseFPDataPool.get(bType);
        }

        public Collection<FunctionPointerData> getFpDataPool() {
            return this.varDeclWiseFPDataPool.values();
        }
    }

    public static class FunctionPointerData {

        // Can be deleted from the BirFunctions of the enclosing module
        public BIRNode.BIRFunction lambdaFunction;
        // Can be deleted from the localVar or global var list
        public BIRNode.BIRVariableDcl lambdaPointerVar;
        // Can be deleted from the instruction array of either the parent function or init<> function
        public BIRNonTerminator.FPLoad fpLoadInstruction;
        public List<BIRNonTerminator> instructionArray;
        // Holds a recordDefaultFPLoad instruction if the respective fpLoadInstruction has one
        public BIRNonTerminator.RecordDefaultFPLoad recordDefaultFPLoad;

        public FunctionPointerData(BIRNonTerminator.FPLoad fpLoadInstruction,
                                   List<BIRNonTerminator> instructionArray,
                                   UsedBIRNodeAnalyzer.InvocationData invocationData) {
            this.fpLoadInstruction = fpLoadInstruction;
            this.instructionArray = instructionArray;
            this.lambdaPointerVar = fpLoadInstruction.lhsOp.variableDcl;
            this.lambdaFunction = invocationData.functionPool.get(fpLoadInstruction.funcName.toString());

            initializeFpData();
        }


        public FunctionPointerData(BIRNonTerminator.FPLoad fpLoadInstruction,
                                   List<BIRNonTerminator> instructionArray,
                                   BIRNode.BIRFunction lambdaFunction) {
            this.fpLoadInstruction = fpLoadInstruction;
            this.instructionArray = instructionArray;
            this.lambdaPointerVar = fpLoadInstruction.lhsOp.variableDcl;
            this.lambdaFunction = lambdaFunction;

            initializeFpData();
        }

        private void initializeFpData() {
            if (lambdaPointerVar.usedState == UsedState.UNEXPOLORED) {
                lambdaPointerVar.usedState = UsedState.UNUSED;
            }
            localFpHolders.add(lambdaPointerVar);

            lambdaPointerVar.childNodes.add(lambdaFunction);
            if (lambdaFunction != null) {
                lambdaFunction.parentNodes.add(lambdaPointerVar);
            }
        }

        // functions with default parameters will desugar into a new lambda function that gets invoked through an FP_CALL inside the init function
        // We need to delete those if the lambda function is not used
        // If the FP_LOAD instruction is not removed its references will be used for CodeGen.
        // We need to prevent this from happening to fully clean the UNUSED functions.
        public void deleteIfUnused() {
            // if the parent record is used, instructions should not be deleted
            if (recordDefaultFPLoad != null && recordDefaultFPLoad.enclosedType.isUsed) {
                return;
            }
            if (lambdaPointerVar.usedState == UsedState.UNUSED) {
                instructionArray.remove(fpLoadInstruction);
                instructionArray.remove(recordDefaultFPLoad);
            }
        }
    }
}
