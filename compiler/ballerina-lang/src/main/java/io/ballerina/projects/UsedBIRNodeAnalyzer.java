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
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.util.Constants.RECORD_DELIMITER;

public class UsedBIRNodeAnalyzer extends BIRVisitor {

    private static final CompilerContext.Key<UsedBIRNodeAnalyzer> USED_BIR_NODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final HashSet<String> USED_FUNCTION_NAMES =
            new HashSet<>(Arrays.asList("main", ".<init>", ".<start>", ".<stop>", "__execute__"));
    private static final HashMap<String, HashSet<String>> INTEROP_DEPENDENCIES = new HashMap<>();
    private static final HashSet<InstructionKind> ANALYZED_INSTRUCTION_KINDS = new HashSet<>(
            Arrays.asList(InstructionKind.NEW_TYPEDESC, InstructionKind.NEW_INSTANCE, InstructionKind.TYPE_CAST,
                    InstructionKind.FP_LOAD, InstructionKind.TYPE_TEST, InstructionKind.RECORD_DEFAULT_FP_LOAD,
                    InstructionKind.NEW_TABLE, InstructionKind.NEW_ARRAY, InstructionKind.MOVE,
                    InstructionKind.NEW_ERROR));
    private static final HashSet<InstructionKind> ANALYZED_TERMINATOR_KINDS =
            new HashSet<>(Arrays.asList(InstructionKind.CALL, InstructionKind.FP_CALL));
    private static final String EXTERNAL_METHOD_ANNOTATION_TAG = "Method";
    // To check whether a given FP is "USED" or not, we have to keep track of the existing FPs of a given scope.
    // Variable Declarations holding an FPs are needed to be tracked to achieve that.
    private HashMap<BIRNode.BIRVariableDcl, FunctionPointerData> localFpHolders = new HashMap<>();
    // pkgWiseInvocationData is used for debugging purposes
    public final Map<PackageID, UsedBIRNodeAnalyzer.InvocationData> pkgWiseInvocationData = new LinkedHashMap<>();
    protected UsedBIRNodeAnalyzer.InvocationData currentInvocationData;
    protected boolean isTestablePkgAnalysis = false;
    protected PackageID currentPkgID;
    private final PackageCache pkgCache;
    private final UsedTypeDefAnalyzer usedTypeDefAnalyzer;
    private List<BIRNonTerminator> currentInstructionArr;
    private BIRNode.BIRFunction currentParentFunction;

    private UsedBIRNodeAnalyzer(CompilerContext context) {
        context.put(USED_BIR_NODE_ANALYZER_KEY, this);
        this.pkgCache = PackageCache.getInstance(context);
        this.usedTypeDefAnalyzer = UsedTypeDefAnalyzer.getInstance(context);
        initInteropDependencies();
    }

    public static UsedBIRNodeAnalyzer getInstance(CompilerContext context) {
        UsedBIRNodeAnalyzer deadBIRNodeAnalyzer = context.get(USED_BIR_NODE_ANALYZER_KEY);
        if (deadBIRNodeAnalyzer == null) {
            deadBIRNodeAnalyzer = new UsedBIRNodeAnalyzer(context);
        }
        return deadBIRNodeAnalyzer;
    }

    public void analyze(BLangPackage pkgNode) {
        CodeGenOptimizationReportEmitter.flipBirOptimizationTimer(pkgNode.packageID);

        currentInvocationData = pkgNode.symbol.invocationData;
        pkgWiseInvocationData.putIfAbsent(pkgNode.packageID, currentInvocationData);
        currentPkgID = pkgNode.packageID;

        if (!currentInvocationData.moduleIsUsed) {
            currentInvocationData.registerNodes(usedTypeDefAnalyzer, pkgNode.symbol.bir);
        }

        // All testablePkgs will be considered as root pkgs by default.
//        if (pkgNode.hasTestablePackage()) {
//            analyzeTestablePkg(pkgNode.getTestablePkg().symbol);
//        }

        for (BIRNode.BIRDocumentableNode node : currentInvocationData.startPointNodes) {
            visitNode(node);
        }

        CodeGenOptimizationReportEmitter.flipBirOptimizationTimer(pkgNode.packageID);
    }

//    private void analyzeTestablePkg(BPackageSymbol testableSymbol) {
//        isTestablePkgAnalysis = true;
//        // since the testablePkg can access the nodes of parent package, we can merge the invocationData of both
//        // testable and parent packages
//        currentInvocationData.testablePkgInvocationData = testableSymbol.invocationData;
//
//        testableSymbol.invocationData.registerNodes(usedTypeDefAnalyzer, testableSymbol.bir);
//        // Analyzing testablePkg should be done first because it is the root of the dependency graph
//        for (BIRNode.BIRDocumentableNode node : testableSymbol.invocationData.startPointNodes) {
//            visitNode(node);
//        }
//        isTestablePkgAnalysis = false;
//    }

    private void visitNode(BIRNode nodeToVisit) {
        BIRNode.BIRFunction prevParentFunction = currentParentFunction;
        List<BIRNonTerminator> prevInstructionArr = currentInstructionArr;

        nodeToVisit.accept(this);

        currentParentFunction = prevParentFunction;
        currentInstructionArr = prevInstructionArr;
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        if (birFunction.getUsedState() == UsedState.USED) {
            return;
        }

        birFunction.markAsUsed();
        currentInvocationData.addToUsedPool(birFunction);
        currentParentFunction = birFunction;

        if ((birFunction.flags & Flags.NATIVE) == Flags.NATIVE) {
            registerUsedNativeClassPaths(birFunction);
        }

        birFunction.basicBlocks.forEach(this::visitNode);

        birFunction.parameters.forEach(param -> usedTypeDefAnalyzer.analyzeTypeDefWithinScope(param.type, birFunction));
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(birFunction.returnVariable.type, birFunction);
    }

    @Override
    public void visit(BIRNode.BIRServiceDeclaration serviceDeclaration) {
        serviceDeclaration.listenerTypes.forEach(
                listenerType -> usedTypeDefAnalyzer.analyzeTypeDefWithinScope(listenerType, serviceDeclaration));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition typeDef) {
        if (typeDef.getUsedState() == UsedState.USED) {
            return;
        }

        typeDef.markAsUsed();
        currentInvocationData.addToUsedPool(typeDef);
        usedTypeDefAnalyzer.analyzeTypeDef(typeDef);

        typeDef.attachedFuncs.forEach(
                attachedFunc -> addDependentFunctionAndVisit(typeDef, attachedFunc, typeDef.type.tsymbol.pkgID));
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        currentInstructionArr = birBasicBlock.instructions;

        HashMap<BIRNode.BIRVariableDcl, FunctionPointerData> previousLocalFpHolders = localFpHolders;
        localFpHolders = new HashMap<>();
        localFpHolders.putAll(currentInvocationData.globalVarFPDataPool);
        birBasicBlock.instructions.forEach(instruction -> {
            if (ANALYZED_INSTRUCTION_KINDS.contains(instruction.getKind())) {
                visitNode(instruction);
            }

            // The current algorithm does not track all FP assignments.
            // If the FP is assigned inside the parent scope, the FP will have the same UsedState as the parent
            HashSet<BIROperand> rhsOperands = new HashSet<>(Arrays.asList(instruction.getRhsOperands()));
            HashSet<BIRNode.BIRVariableDcl> rhsVars = new HashSet<>();
            rhsOperands.forEach(birOperand -> rhsVars.add(birOperand.variableDcl));

            for (BIRNode.BIRVariableDcl rhsVar : rhsVars) {
                if (rhsVar instanceof BIRNode.BIRGlobalVariableDcl) {
                    rhsVar.markAsUsed();
                    currentParentFunction.childNodes.add(rhsVar);
                    rhsVar.parentNodes.add(currentParentFunction);

                    // TODO Refactor following logic
                    if (!rhsVar.isInSamePkg(currentPkgID) && !isTestablePkgAnalysis) {
                        getInvocationData(rhsVar.getPackageID())
                                .registerNodes(usedTypeDefAnalyzer, this.pkgCache.getBirPkg(rhsVar.getPackageID()));
                    } else if (isFunctionKindType(rhsVar.type)) {
                        visitNode(currentInvocationData.globalVarFPDataPool.get(rhsVar).lambdaFunction);
                    }
                }
            }
            rhsVars.retainAll(localFpHolders.keySet());

            if (!rhsVars.isEmpty() && instruction.kind != InstructionKind.RECORD_DEFAULT_FP_LOAD) {
                for (BIRNode.BIRVariableDcl var : rhsVars) {
                    FunctionPointerData fpData = currentInvocationData.getFPData(var);
                    if (fpData != null && fpData.lambdaFunction != null) {
                        visitNode(fpData.lambdaFunction);
                    }
                    currentParentFunction.addChildNode(var);
                }
            }
        });

        if (ANALYZED_TERMINATOR_KINDS.contains(birBasicBlock.terminator.getKind())) {
            visitNode(birBasicBlock.terminator);
        }

        localFpHolders = previousLocalFpHolders;
    }

    @Override
    public void visit(BIRTerminator.Call call) {
        // If function pointers are passed as parameters, they will be bound to the parent function
        HashSet<BIRNode.BIRVariableDcl> argVars = new HashSet<>();
        call.args.forEach(birOperand -> argVars.add(birOperand.variableDcl));
        argVars.retainAll(localFpHolders.keySet());
        // TODO Refactor logic here to analyze typedefs of the fpCall parameter types
        if (!argVars.isEmpty()) {
            argVars.forEach(var -> {
                FunctionPointerData fpData = currentInvocationData.getFPData(var);
                if (fpData != null && fpData.lambdaFunction != null) {
                    visitNode(fpData.lambdaFunction);
                }
                currentParentFunction.addChildNode(var);
            });
        }

        BIRNode.BIRDocumentableNode childNode = lookupBirFunction(call.calleePkg, call.originalName.value);
        if (childNode == null) {
            return;
        }
        // Invoked function will also be visited
        addDependentFunctionAndVisit(currentParentFunction, childNode, call.calleePkg);
    }

    @Override
    public void visit(BIRTerminator.FPCall fpCall) {
        BIRNode.BIRVariableDcl fpPointer = fpCall.fp.variableDcl;

        fpPointer.markAsUsed();
        currentParentFunction.childNodes.add(fpPointer);
        fpPointer.parentNodes.add(currentParentFunction);

        FunctionPointerData fpData;

        // TODO Dont use instance of. Find a way to not use the type cast
        if (fpPointer instanceof BIRNode.BIRGlobalVariableDcl globalVariableDcl) {
            fpData = currentInvocationData.globalVarFPDataPool.get(globalVariableDcl);
        } else {
            fpData = currentInvocationData.getFPData(fpCall.fp.variableDcl);
        }

        if (fpData == null) {
            if (!fpPointer.isInSamePkg(currentPkgID) && !isTestablePkgAnalysis) {
                getInvocationData(fpPointer.getPackageID())
                        .registerNodes(usedTypeDefAnalyzer, this.pkgCache.getBirPkg(fpPointer.getPackageID()));
            }
        } else if (fpData.lambdaFunction != null) {
            visitNode(fpData.lambdaFunction);
            usedTypeDefAnalyzer.analyzeTypeDefWithinScope(fpData.lambdaPointerVar.type, currentParentFunction);
        }
    }

    @Override
    public void visit(BIRNonTerminator.FPLoad fpLoadInstruction) {
        BIRNode.BIRFunction pointedFunction =
                lookupBirFunction(fpLoadInstruction.pkgId, fpLoadInstruction.funcName.value);

        FunctionPointerData fpData = new FunctionPointerData(fpLoadInstruction, currentInstructionArr, pointedFunction);
        initializeFpData(fpLoadInstruction.lhsOp.variableDcl, pointedFunction, fpData);

        // Used to detect,
        // 1. Record default fields containing function pointers
        // 2. Testerina related TestRegistrars (These global function pointers are called from testerina side)
        if (currentParentFunction.name.value.contains(RECORD_DELIMITER) ||
                currentParentFunction.name.value.startsWith("executeTestRegistrar")) {
            fpData.lambdaPointerVar.markAsUsed();
        }

        if (fpData.lambdaPointerVar instanceof BIRNode.BIRGlobalVariableDcl globalVariableDcl) {
            getInvocationData(currentPkgID).globalVarFPDataPool.put(globalVariableDcl, fpData);
        }

        if (fpData.lambdaPointerVar.getUsedState() == UsedState.USED && pointedFunction != null) {
            visitNode(pointedFunction);

            // TODO Move this into a visit function for BIRGlobalVarDcl
            usedTypeDefAnalyzer.analyzeTypeDefWithinScope(fpData.lambdaPointerVar.type, currentParentFunction);
        }

        localFpHolders.put(fpLoadInstruction.lhsOp.variableDcl, fpData);
        getInvocationData(currentPkgID).functionPointerDataPool.put(fpData.lambdaPointerVar, fpData);
    }

    private void initializeFpData(BIRNode.BIRVariableDcl lambdaPointerVar, BIRNode.BIRFunction lambdaFunction,
                                  FunctionPointerData fpData) {
        if (lambdaPointerVar.getUsedState() == UsedState.UNEXPOLORED) {
            lambdaPointerVar.markSelfAsUnused();
        }
        localFpHolders.put(lambdaPointerVar, fpData);

        lambdaPointerVar.childNodes.add(lambdaFunction);
        if (lambdaFunction != null) {
            lambdaFunction.parentNodes.add(lambdaPointerVar);
        }
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
            fpData.lambdaPointerVar.markAsUsed();
            visitNode(fpData.lambdaFunction);
        }

        currentInvocationData.recordDefTypeWiseFPDataPool.putIfAbsent(recordDefaultFPLoad.enclosedType,
                new HashSet<>());
        currentInvocationData.recordDefTypeWiseFPDataPool.get(recordDefaultFPLoad.enclosedType).add(fpData);
    }

    @Override
    public void visit(BIRNonTerminator.NewError newError) {
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(newError.type, currentParentFunction);
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
    public void visit(BIRNonTerminator.NewArray newArray) {
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(newArray.type, currentParentFunction);
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

    @Override
    public void visit(BIRNonTerminator.NewTable newTable) {
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(newTable.type, currentParentFunction);
    }

    @Override
    public void visit(BIRNonTerminator.Move move) {
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(move.lhsOp.variableDcl.type, currentParentFunction);
        usedTypeDefAnalyzer.analyzeTypeDefWithinScope(move.rhsOp.variableDcl.type, currentParentFunction);
    }

    private boolean isFunctionKindType(BType bType) {
        if (bType.getKind() == TypeKind.TYPEREFDESC) {
            if (bType instanceof BTypeReferenceType referenceType) {
                return referenceType.referredType.getKind() == TypeKind.FUNCTION;
            }
        }
        return bType.getKind() == TypeKind.FUNCTION;
    }

    private BIRNode.BIRFunction lookupBirFunction(PackageID pkgId, String funcName) {
        UsedBIRNodeAnalyzer.InvocationData invocationData = getInvocationData(pkgId);
        if (!invocationData.moduleIsUsed && !isTestablePkgAnalysis) {
            invocationData.registerNodes(usedTypeDefAnalyzer, pkgCache.getBirPkg(pkgId));
        }
        return invocationData.functionPool.get(funcName);
    }

    public InvocationData getInvocationData(PackageID pkgId) {
        if (currentPkgID.equals(pkgId)) {
            return pkgId.isTestPkg ? currentInvocationData.testablePkgInvocationData : currentInvocationData;
        }
        return pkgCache.getInvocationData(pkgId);
    }

    private void initInteropDependencies() {
        Properties prop = new Properties();

        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("interop-dependencies.properties");
            prop.load(stream);
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                HashSet<String> usedRecordNames = new HashSet<>(Arrays.asList(entry.getValue().toString().split(",")));
                INTEROP_DEPENDENCIES.putIfAbsent(entry.getKey().toString(), usedRecordNames);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load interop-dependencies : ", e);
        }
    }

    private void registerUsedNativeClassPaths(BIRNode.BIRFunction birFunction) {
        birFunction.annotAttachments.forEach(annot -> {
            if (annot.annotTagRef.value.equals(EXTERNAL_METHOD_ANNOTATION_TAG)) {
                BIRNode.ConstValue constValue = ((BIRNode.BIRConstAnnotationAttachment) annot).annotValue;
                String filePath =
                        ((HashMap<String, BIRNode.ConstValue>) constValue.value).get("class").value.toString();
                currentInvocationData.usedNativeClassPaths.add(filePath.replace(".", "/") + ".class");
            }
        });
    }

    private void addDependentFunctionAndVisit(BIRNode.BIRDocumentableNode parentNode,
                                              BIRNode.BIRDocumentableNode childFunction, PackageID childPkgId) {
        parentNode.childNodes.add(childFunction);
        childFunction.parentNodes.add(parentNode);
        if (childPkgId.equals(currentPkgID)) {
            visitNode(childFunction);
            return;
        }

        // Testable pkg dependencies should not be analysed because the unoptimized duplicates will be referenced.
        if (isTestablePkgAnalysis) {
            return;
        }

        UsedBIRNodeAnalyzer.InvocationData childInvocationData = getInvocationData(childPkgId);

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

        // Following Sets are used to whitelist the bal types called through ValueCreator in native dependencies
        private static final HashSet<String> WHITELSITED_FILE_NAMES =
                new HashSet<>(Arrays.asList("types.bal", "error.bal", "stream_types.bal"));
        private static final HashSet<String> PKGS_WITH_WHITELSITED_FILES = new HashSet<>(
                Arrays.asList("ballerinax/mysql", "ballerina/sql", "ballerinax/persist.sql"));
        private static final String BALLERINA_TEST_PKG_NAME = "ballerina/test:0.0.0";
        private static final String MOCK_FUNCTION_PREFIX = "$MOCK_";
        protected final Set<BIRNode.BIRFunction> usedFunctions = new LinkedHashSet<>();
        protected final Set<BIRNode.BIRFunction> unusedFunctions = new LinkedHashSet<>();
        protected final Set<BIRNode.BIRTypeDefinition> usedTypeDefs = new LinkedHashSet<>();
        protected final Set<BIRNode.BIRTypeDefinition> unusedTypeDefs = new LinkedHashSet<>();
        protected final ArrayList<BIRNode.BIRDocumentableNode> startPointNodes = new ArrayList<>();
        private final HashMap<String, BIRNode.BIRFunction> functionPool = new HashMap<>();
        private final Map<BIRNode.BIRVariableDcl, FunctionPointerData> functionPointerDataPool =
                new IdentityHashMap<>();
        private final HashMap<BIRNode.BIRGlobalVariableDcl, FunctionPointerData> globalVarFPDataPool =
                new HashMap<>();
        private final HashMap<BType, HashSet<FunctionPointerData>> recordDefTypeWiseFPDataPool = new HashMap<>();
        private final HashSet<PackageID> childPackages = new HashSet<>();
        protected HashSet<String> usedNativeClassPaths = new HashSet<>();
        protected boolean moduleIsUsed = false;
        private HashSet<String> interopDependencies = new HashSet<>();
        private InvocationData testablePkgInvocationData;

        private static boolean isResourceFunction(BIRNode.BIRFunction birFunction) {
            return (birFunction.flags & Flags.RESOURCE) == Flags.RESOURCE;
        }

        private static boolean isTestFunction(BIRNode.BIRFunction birFunction) {
            if (birFunction.annotAttachments == null) {
                return false;
            }
            return birFunction.annotAttachments.stream()
                    .anyMatch(annAttach -> annAttach.annotPkgId.toString().equals(BALLERINA_TEST_PKG_NAME));
        }

        private static boolean isGeneratedMockFunction(BIRNode.BIRFunction birFunction) {
            return birFunction.name.value.startsWith(MOCK_FUNCTION_PREFIX);
        }

        private static boolean isExternalDependencyBIRNode(BIRNode.BIRFunction birFunction) {
            if (birFunction.annotAttachments == null) {
                return false;
            }

            for (BIRNode.BIRAnnotationAttachment annotAttachment : birFunction.annotAttachments) {
                if (annotAttachment.annotPkgId.toString().equals("ballerina/jballerina.java:0.0.0") &&
                        annotAttachment.annotTagRef.toString().equals("ExternalDependency")) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isExternalDependencyBIRNode(BIRNode.BIRTypeDefinition birTypeDefinition) {
            if (birTypeDefinition.annotAttachments == null) {
                return false;
            }

            for (BIRNode.BIRAnnotationAttachment annotAttachment : birTypeDefinition.annotAttachments) {
                if (annotAttachment.annotPkgId.toString().equals("ballerina/jballerina.java:0.0.0") &&
                        annotAttachment.annotTagRef.toString().equals("ExternalDependency")) {
                    return true;
                }
            }
            return false;
        }

        protected void registerNodes(UsedTypeDefAnalyzer typeDefAnalyzer, BIRNode.BIRPackage birPackage) {
            // If the birPackage is from a langlib it will be null.
            if (birPackage == null) {
                return;
            }

            birPackage.functions.forEach(this::initializeFunction);
            this.interopDependencies = INTEROP_DEPENDENCIES.get(birPackage.packageID.getPackageNameWithOrg());
            typeDefAnalyzer.populateTypeDefPool(birPackage, interopDependencies);
            birPackage.typeDefs.forEach(typeDef -> {
                unusedTypeDefs.add(typeDef);
                typeDef.markSelfAsUnused();
                typeDef.attachedFuncs.forEach(birFunction -> initializeAttachedFunction(typeDef, birFunction));

                if (PKGS_WITH_WHITELSITED_FILES.contains(birPackage.packageID.getPackageNameWithOrg())) {
                    if (isInWhiteListedBalFile(typeDef)) {
                        this.startPointNodes.add(typeDef);
                    }
                }

                if (isExternalDependencyBIRNode(typeDef)) {
                    this.startPointNodes.add(typeDef);
                }
            });
            this.startPointNodes.addAll(birPackage.serviceDecls);
            this.moduleIsUsed = true;
        }

        private boolean isInWhiteListedBalFile(BIRNode.BIRTypeDefinition typedef) {
            return typedef.pos != null && WHITELSITED_FILE_NAMES.contains(typedef.pos.lineRange().fileName());
        }

        private void initializeFunction(BIRNode.BIRFunction birFunction) {
            birFunction.markSelfAsUnused();
            this.unusedFunctions.add(birFunction);
            functionPool.putIfAbsent(birFunction.originalName.value, birFunction);

            if (USED_FUNCTION_NAMES.contains(birFunction.name.value) || isResourceFunction(birFunction) ||
                    isTestFunction(birFunction) || isGeneratedMockFunction(birFunction) ||
                    isExternalDependencyBIRNode(birFunction)) {
                this.startPointNodes.add(birFunction);
            }
        }

        private void initializeAttachedFunction(BIRNode.BIRTypeDefinition parentTypeDef,
                                                BIRNode.BIRFunction attachedFunc) {
            attachedFunc.markSelfAsUnused();
            this.unusedFunctions.add(attachedFunc);
            functionPool.putIfAbsent(parentTypeDef.internalName.value + "." + attachedFunc.name.value, attachedFunc);
        }

        protected void addToUsedPool(BIRNode.BIRFunction birFunction) {
            if (this.unusedFunctions.remove(birFunction)) {
                this.usedFunctions.add(birFunction);
            } else if (this.testablePkgInvocationData != null) {
                this.testablePkgInvocationData.unusedFunctions.remove(birFunction);
                this.testablePkgInvocationData.usedFunctions.add(birFunction);
            }
        }

        protected void addToUsedPool(BIRNode.BIRTypeDefinition birTypeDef) {
            if (this.unusedTypeDefs.remove(birTypeDef)) {
                this.usedTypeDefs.add(birTypeDef);
            } else if (this.testablePkgInvocationData != null) {
                this.testablePkgInvocationData.unusedTypeDefs.remove(birTypeDef);
                this.testablePkgInvocationData.usedTypeDefs.add(birTypeDef);
            }
        }

        private FunctionPointerData getFPData(BIRNode.BIRVariableDcl variableDcl) {
            return functionPointerDataPool.get(variableDcl);
        }

        protected HashSet<FunctionPointerData> getFpData(BType bType) {
            return this.recordDefTypeWiseFPDataPool.get(bType);
        }

        protected Collection<FunctionPointerData> getFpDataPool() {
            return this.functionPointerDataPool.values();
        }

        public void emitInvocationData(BufferedWriter out) throws IOException {
            out.newLine();
            out.write("/".repeat(60));
            out.write("/".repeat(60));

            out.newLine();
            out.write("Used Functions");
            out.newLine();

            for (BIRNode.BIRFunction function : usedFunctions) {
                out.write(function.originalName.value);
                out.newLine();
            }

            out.newLine();
            out.write("-".repeat(60));
            out.newLine();
            out.write("Deleted functions");
            for (BIRNode.BIRFunction function : unusedFunctions) {
                out.write(function.originalName.value);
                out.newLine();
            }

            out.newLine();
            out.write("-".repeat(60));
            out.newLine();
            out.write("Used Type definitions");
            for (BIRNode.BIRTypeDefinition typeDef : usedTypeDefs) {
                out.write(typeDef.originalName.value);
                out.newLine();
            }

            out.newLine();
            out.write("-".repeat(60));
            out.newLine();
            out.write("Deleted Type definitions");
            for (BIRNode.BIRTypeDefinition typeDef : unusedTypeDefs) {
                out.write(typeDef.originalName.value);
                out.newLine();
            }
        }
    }

    protected static class FunctionPointerData {

        // Can be deleted from the BirFunctions of the enclosing module
        protected BIRNode.BIRFunction lambdaFunction;
        // Can be deleted from the localVar or global var list
        protected BIRNode.BIRVariableDcl lambdaPointerVar;
        // Can be deleted from the instruction array of either the parent function or init<> function
        private final BIRNonTerminator.FPLoad fpLoadInstruction;
        private final List<BIRNonTerminator> instructionArray;
        // Holds a recordDefaultFPLoad instruction if the respective fpLoadInstruction has one
        private BIRNonTerminator.RecordDefaultFPLoad recordDefaultFPLoad;

        protected FunctionPointerData(BIRNonTerminator.FPLoad fpLoadInstruction,
                                      List<BIRNonTerminator> instructionArray, BIRNode.BIRFunction lambdaFunction) {
            this.fpLoadInstruction = fpLoadInstruction;
            this.instructionArray = instructionArray;
            this.lambdaPointerVar = fpLoadInstruction.lhsOp.variableDcl;
            this.lambdaFunction = lambdaFunction;
        }

        // functions with default parameters will desugar into a new lambda function that gets invoked through an
        // FP_CALL inside the init function.
        // We need to delete those if the lambda function is not used.
        // If the FP_LOAD instruction is not removed its references will be used for CodeGen.
        // We need to prevent this from happening to fully clean the UNUSED functions.
        protected void deleteIfUnused() {
            // if the parent record is used, instructions should not be deleted
            if (recordDefaultFPLoad != null && recordDefaultFPLoad.enclosedType.isUsed) {
                return;
            }
            if (lambdaPointerVar.getUsedState() == UsedState.UNUSED) {
                instructionArray.remove(fpLoadInstruction);
                instructionArray.remove(recordDefaultFPLoad);
            }
        }
    }
}
