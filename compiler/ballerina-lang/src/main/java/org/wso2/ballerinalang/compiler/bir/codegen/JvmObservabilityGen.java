/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import io.ballerina.identifier.Utils;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JIMethodCall;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JMethodCallInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ConstantLoad;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeCast;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeTest;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.AsyncCall;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Branch;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Call;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.GOTO;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Panic;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Return;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DISPLAY_ANNOTATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVABLE_ANNOTATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_CHECKPOINT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REPORT_ERROR_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_CALLABLE_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_RESOURCE_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STOP_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STOP_OBSERVATION_WITH_ERROR_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CHECKPOINT_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ERROR_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.START_CALLABLE_OBSERVATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.START_RESOURCE_OBSERVATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.STOP_OBSERVATION;

/**
 * BIR desugar to inject observations class.
 *
 * @since 2.0.0
 */
class JvmObservabilityGen {
    private static final String ENTRY_POINT_MAIN_METHOD_NAME = "main";
    private static final String NEW_BB_PREFIX = "observabilityDesugaredBB";
    private static final String INVOCATION_INSTRUMENTATION_TYPE = "invocation";
    private static final String FUNC_BODY_INSTRUMENTATION_TYPE = "funcBody";
    private static final String FILE_NAME_STRING = "fileName";
    private static final String START_LINE_STRING = "startLine";
    private static final String START_COLUMN_STRING = "startCol";
    private static final Location COMPILE_TIME_CONST_POS =
            new BLangDiagnosticLocation(null, -1, -1, -1, -1, 0, 0);

    private final PackageCache packageCache;
    private final SymbolTable symbolTable;
    private int lambdaIndex;
    private int desugaredBBIndex;
    private int localVarIndex;
    private int constantIndex;
    private int defaultServiceIndex;

    private final Map<Object, BIROperand> compileTimeConstants;
    private final Map<Name, String> svcAttachPoints;
    private final Map<String, BIROperand> tempLocalVarsMap;
    private final Map<BIRBasicBlock, List<BIRBasicBlock>> predecessorMap;

    JvmObservabilityGen(PackageCache packageCache, SymbolTable symbolTable) {
        this.compileTimeConstants = new HashMap<>();
        this.svcAttachPoints = new HashMap<>();
        this.tempLocalVarsMap = new HashMap<>();
        this.predecessorMap = new HashMap<>();
        this.packageCache = packageCache;
        this.symbolTable = symbolTable;
        this.lambdaIndex = 0;
        this.desugaredBBIndex = 0;
        this.constantIndex = 0;
        this.localVarIndex = 0;
        this.defaultServiceIndex = 0;
    }

    /**
     * Instrument the package by rewriting the BIR to add relevant Observability related instructions.
     *
     * @param pkg The package to instrument
     */
    public void instrumentPackage(BIRPackage pkg) {
        initializeTempLocalVariables();
        for (int i = 0; i < pkg.functions.size(); i++) {
            localVarIndex = 0;
            BIRFunction func = pkg.functions.get(i);

            if (ENTRY_POINT_MAIN_METHOD_NAME.equals(func.name.getValue())) {
                rewriteControlFlowInvocation(func, pkg);
            }
            rewriteAsyncInvocations(func, null, pkg);
            rewriteObservableFunctionInvocations(func, pkg);
            if (ENTRY_POINT_MAIN_METHOD_NAME.equals(func.name.getValue())) {
                rewriteObservableFunctionBody(func, pkg, null, func.name.getValue(), null, false, false, true,
                        false);
            } else if ((func.flags & Flags.WORKER) == Flags.WORKER) {   // Identifying lambdas generated for workers
                rewriteObservableFunctionBody(func, pkg, null, func.workerName.getValue(), null, false, false, false,
                        true);
            }
        }
        for (BIRNode.BIRServiceDeclaration serviceDecl : pkg.serviceDecls) {
            List<String> attachPoint = serviceDecl.attachPoint;
            String attachPointLiteral = serviceDecl.attachPointLiteral;
            if (attachPoint != null) {
                svcAttachPoints.put(serviceDecl.associatedClassName, "/" + String.join("/", attachPoint));
            } else if (attachPointLiteral != null) {
                svcAttachPoints.put(serviceDecl.associatedClassName, attachPointLiteral);
            }
        }
        for (BIRTypeDefinition typeDef : pkg.typeDefs) {
            BType bType = JvmCodeGenUtil.getReferredType(typeDef.type);
            if ((typeDef.flags & Flags.CLASS) != Flags.CLASS && bType.tag == TypeTags.OBJECT) {
                continue;
            }
            boolean isService = (bType.flags & Flags.SERVICE) == Flags.SERVICE;
            String serviceName = null;
            if (isService) {
                for (BIRNode.BIRAnnotationAttachment annotationAttachment : typeDef.annotAttachments) {
                    if (DISPLAY_ANNOTATION.equals(annotationAttachment.annotTagRef.getValue())) {
                        BIRNode.ConstValue annotValue =
                                ((BIRNode.BIRConstAnnotationAttachment) annotationAttachment).annotValue;
                        Map<String, BIRNode.ConstValue> annotationMap =
                                (Map<String, BIRNode.ConstValue>) annotValue.value;
                        serviceName = annotationMap.get("label").value.toString();
                        break;
                    }
                }
                if (serviceName == null) {
                    String basePath = this.svcAttachPoints.get(typeDef.name);
                    serviceName = basePath == null ? pkg.packageID.orgName.getValue() + "_" +
                            pkg.packageID.name.getValue() + "_svc_" + defaultServiceIndex++ :
                            Utils.unescapeBallerina(basePath);
                }
            }
            for (int i = 0; i < typeDef.attachedFuncs.size(); i++) {
                BIRFunction func = typeDef.attachedFuncs.get(i);
                localVarIndex = 0;
                if (isService && ((func.flags & Flags.RESOURCE) == Flags.RESOURCE ||
                        (func.flags & Flags.REMOTE) == Flags.REMOTE)) {
                    rewriteControlFlowInvocation(func, pkg);
                }
                rewriteAsyncInvocations(func, typeDef, pkg);
                rewriteObservableFunctionInvocations(func, pkg);
                if (isService) {
                    if ((func.flags & Flags.RESOURCE) == Flags.RESOURCE) {
                        rewriteObservableFunctionBody(func, pkg, typeDef, func.name.getValue(), serviceName,
                                                      true, false, false, false);
                    } else if ((func.flags & Flags.REMOTE) == Flags.REMOTE) {
                        rewriteObservableFunctionBody(func, pkg, typeDef, func.name.getValue(), serviceName,
                                                      false, true, false, false);
                    }
                }
            }
        }
        // Adding initializing instructions for all compile time known constants
        BIRFunction initFunc = pkg.functions.get(0);
        BIRBasicBlock constInitBB = initFunc.basicBlocks.get(0);
        for (Map.Entry<Object, BIROperand> entry : compileTimeConstants.entrySet()) {
            BIROperand operand = entry.getValue();
            ConstantLoad constLoadIns = new ConstantLoad(COMPILE_TIME_CONST_POS, entry.getKey(),
                    operand.variableDcl.type, operand);
            constInitBB.instructions.add(constLoadIns);
        }
    }

    /**
     * Adding Java Interop calls to basic blocks.
     * Here the JI calls are added for all kinds of terminators.
     * First we check if there are position details for instructions, if present we add the JI calls with those
     * positions else, we consider the terminator position to create the JI call.
     *
     * @param func The function of which the instructions should be rewritten
     * @param pkg The package containing the function
     */
    private void rewriteControlFlowInvocation(BIRFunction func, BIRPackage pkg) {
        populatePredecessorMap(func.basicBlocks);
        for (Map.Entry<BIRBasicBlock, List<BIRBasicBlock>> entry : this.predecessorMap.entrySet()) {
            BIRBasicBlock currentBB = entry.getKey();
            Location desugaredPos = getDesugaredPosition(currentBB);

            if (desugaredPos != null && desugaredPos.lineRange().startLine().line() >= 0) {
                List<BIRBasicBlock> predecessors = entry.getValue();
                int callInsOffset = 0;
                if (!desugaredPosAlreadyLoaded(desugaredPos, predecessors)) {
                    updatePositionArgsConstLoadIns(desugaredPos, currentBB);
                    callInsOffset = 2;
                }
                injectCheckpointCall(currentBB, pkg, callInsOffset);
            }
        }
    }

    private boolean desugaredPosAlreadyLoaded(Location desugaredPos, List<BIRBasicBlock> predecessors) {
        for (BIRBasicBlock bb : predecessors) {
            Location predecessorDesugaredPos = getDesugaredPosition(bb);
            if (predecessorDesugaredPos != null && predecessorDesugaredPos.equals(desugaredPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inject checkpoint JI method call to a basic block.
     *
     * @param currentBB The basic block to which the checkpoint call should be injected
     * @param pkg The package the invocation belongs to
     * @param offset The recordCheckPointCall instruction offset
     */
    private void injectCheckpointCall(BIRBasicBlock currentBB, BIRPackage pkg, int offset) {
        BIROperand pkgOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType,
                generatePackageId(pkg.packageID));
        BIROperand fileNameOperand = tempLocalVarsMap.get(FILE_NAME_STRING);
        BIROperand startLineOperand = tempLocalVarsMap.get(START_LINE_STRING);
        BIROperand startColOperand = tempLocalVarsMap.get(START_COLUMN_STRING);
        JMethodCallInstruction recordCheckPointCallIns = new JMethodCallInstruction(null);
        recordCheckPointCallIns.invocationType = INVOKESTATIC;
        recordCheckPointCallIns.jClassName = OBSERVE_UTILS;
        recordCheckPointCallIns.jMethodVMSig = CHECKPOINT_CALL;
        recordCheckPointCallIns.name = RECORD_CHECKPOINT_METHOD;
        recordCheckPointCallIns.args = new ArrayList<>(Arrays.asList(pkgOperand, fileNameOperand,
                startLineOperand, startColOperand));
        currentBB.instructions.add(offset, recordCheckPointCallIns);
    }

    private Location getDesugaredPosition(BIRBasicBlock basicBlock) {
        // First we give the priority to Instructions,
        // If no instructions are found, then we get the Terminator position
        for (BIRNonTerminator instruction : basicBlock.instructions) {
            if (instruction.pos != null) {
                return instruction.pos;
            }
        }
        return basicBlock.terminator.pos;
    }

    private void populatePredecessorMap(List<BIRBasicBlock> basicBlocks) {
        this.predecessorMap.clear();
        for (BIRBasicBlock basicBlock : basicBlocks) {
            this.predecessorMap.computeIfAbsent(basicBlock, k -> new ArrayList<>());
            for (BIRBasicBlock bb : basicBlock.terminator.getNextBasicBlocks()) {
                this.predecessorMap.computeIfAbsent(bb, k -> new ArrayList<>()).add(basicBlock);
            }
        }
    }

    /**
     * Rewrite the invocations in the function bodies to call a lambda asynchronously which in turn calls the
     * actual function synchronously. This is done so that the actual invocation can be observed accurately.
     * Without this wrapper, the start and end time recorded would only reflect the time it took to give the async
     * invocation to the scheduler. However, we require the actual time it took for the invocation.
     *
     * @param func The function of which the instructions in the body should be rewritten
     * @param attachedTypeDef The type definition to which the function was attached to or null
     * @param pkg The package containing the function
     */
    private void rewriteAsyncInvocations(BIRFunction func, BIRTypeDefinition attachedTypeDef, BIRPackage pkg) {
        PackageID packageID = pkg.packageID;
        Name org = new Name(Utils.decodeIdentifier(packageID.orgName.getValue()));
        Name module = new Name(Utils.decodeIdentifier(packageID.name.getValue()));
        PackageID currentPkgId = new PackageID(org, module, module, packageID.version, packageID.sourceFileName,
                packageID.isTestPkg);
        BSymbol functionOwner;
        List<BIRFunction> scopeFunctionsList;
        if (attachedTypeDef == null) {
            functionOwner = packageCache.getSymbol(currentPkgId);
            scopeFunctionsList = pkg.functions;
        } else {
            functionOwner = attachedTypeDef.type.tsymbol;
            scopeFunctionsList = attachedTypeDef.attachedFuncs;
        }
        for (BIRBasicBlock currentBB : func.basicBlocks) {
            if (currentBB.terminator.kind != InstructionKind.ASYNC_CALL
                    || !isObservable((AsyncCall) currentBB.terminator)) {
                continue;
            }

            AsyncCall asyncCallIns = (AsyncCall) currentBB.terminator;
            /*
             * The wrapper function generated below invokes the actual function synchronously, allowing the
             * instrumentation to record the actual start and end times of the function. The wrapper function
             * is invoked asynchronously preserving the asynchronous behaviour.
             */

            // Creating the lambda for this async call
            BType returnType = ((BFutureType) asyncCallIns.lhsOp.variableDcl.type).constraint;
            List<BType> argTypes = new ArrayList<>();
            for (BIROperand arg : asyncCallIns.args) {
                BType type = arg.variableDcl.type;
                argTypes.add(type);
            }
            Name lambdaName = new Name("$lambda$observability" + lambdaIndex++ + "$" +
                    asyncCallIns.name.getValue().replace(".", "_"));
            BInvokableType bInvokableType = new BInvokableType(argTypes, null,
                    returnType, null);
            BIRFunction desugaredFunc = new BIRFunction(asyncCallIns.pos, lambdaName, 0, bInvokableType,
                    func.workerName, 0, VIRTUAL);
            desugaredFunc.receiver = func.receiver;
            scopeFunctionsList.add(desugaredFunc);

            // Creating the return variable
            BIRVariableDcl funcReturnVariableDcl = new BIRVariableDcl(returnType,
                    new Name("$" + lambdaName.getValue() + "$retVal"), VarScope.FUNCTION, VarKind.RETURN);
            BIROperand funcReturnOperand = new BIROperand(funcReturnVariableDcl);
            desugaredFunc.localVars.add(funcReturnVariableDcl);
            desugaredFunc.returnVariable = funcReturnVariableDcl;

            // Creating and adding invokable symbol to the relevant scope
            BInvokableSymbol invokableSymbol = new BInvokableSymbol(SymTag.FUNCTION, 0, lambdaName,
                    currentPkgId, bInvokableType, functionOwner,
                    desugaredFunc.pos, VIRTUAL);
            invokableSymbol.retType = funcReturnVariableDcl.type;
            invokableSymbol.kind = SymbolKind.FUNCTION;
            List<BVarSymbol> list = new ArrayList<>();
            for (BIROperand arg : asyncCallIns.args) {
                BVarSymbol bVarSymbol = new BVarSymbol(0, arg.variableDcl.name, currentPkgId, arg.variableDcl.type,
                                                       invokableSymbol, arg.pos, VIRTUAL);
                list.add(bVarSymbol);
            }
            invokableSymbol.params = list;
            invokableSymbol.scope = new Scope(invokableSymbol);
            invokableSymbol.params.forEach(param -> invokableSymbol.scope.define(param.name, param));
            if (attachedTypeDef == null) {
                functionOwner.scope.define(lambdaName, invokableSymbol);
            }

            // Creating and adding function parameters
            List<BIROperand> funcParamOperands = new ArrayList<>();
            Name selfArgName = new Name("%self");
            for (int i = 0; i < asyncCallIns.args.size(); i++) {
                BIROperand arg = asyncCallIns.args.get(i);
                BIRFunctionParameter funcParam;
                if (arg.variableDcl.kind == VarKind.SELF) {
                    funcParam = new BIRFunctionParameter(asyncCallIns.pos, arg.variableDcl.type, selfArgName,
                            VarScope.FUNCTION, VarKind.SELF, selfArgName.getValue(), false);
                } else {
                    Name argName = new Name("$funcParam%d" + i);
                    funcParam = new BIRFunctionParameter(asyncCallIns.pos, arg.variableDcl.type,
                            argName, VarScope.FUNCTION, VarKind.ARG, argName.getValue(), false);
                    desugaredFunc.localVars.add(funcParam);
                    desugaredFunc.parameters.add(funcParam);
                    desugaredFunc.requiredParams.add(new BIRParameter(asyncCallIns.pos, argName, 0));
                    desugaredFunc.argsCount++;
                }
                funcParamOperands.add(new BIROperand(funcParam));
            }

            // Generating function body
            BIRBasicBlock callInsBB = insertBasicBlock(desugaredFunc, 0);
            BIRBasicBlock returnInsBB = insertBasicBlock(desugaredFunc, 1);
            callInsBB.terminator = new Call(asyncCallIns.pos, InstructionKind.CALL, asyncCallIns.isVirtual,
                    asyncCallIns.calleePkg, asyncCallIns.name, funcParamOperands, funcReturnOperand,
                    returnInsBB, asyncCallIns.calleeAnnotAttachments, asyncCallIns.calleeFlags);
            returnInsBB.terminator = new Return(asyncCallIns.pos);

            // Updating terminator to call the generated lambda asynchronously
            asyncCallIns.name = lambdaName;
            asyncCallIns.calleePkg = currentPkgId;
            asyncCallIns.isVirtual = attachedTypeDef != null;
            if (attachedTypeDef != null) {
                asyncCallIns.args.add(0, new BIROperand(new BIRVariableDcl(attachedTypeDef.type, selfArgName,
                                                                           VarScope.FUNCTION, VarKind.SELF)));
            }
        }
    }

    /**
     * Rewrite a function so that the internal body will be observed. This adds the relevant start and stop calls at
     * the beginning and return basic blocks of the function.
     * This is only to be used in service resource functions, workers and main method.
     * This method expects that Observable invocations had already been instrumented properly before this method is
     * called. This is because the uncaught panics thrown from such observable invocations are reported to the
     * observation covering the function body by using the re-panic terminators which gets added in
     * rewriteObservableFunctionInvocations method.
     *
     * @param func The function to instrument
     * @param pkg The package which contains the function
     * @param attachedTypeDef The type definition the function is attached to
     * @param functionName The name of the function which will be observed
     * @param isResource True if the function is a resource function
     * @param isRemote True if the function is a remote function
     * @param isMainEntryPoint True if the function is the main entry point
     * @param isWorker True if the function was a worker
     */
    private void rewriteObservableFunctionBody(BIRFunction func, BIRPackage pkg, BIRTypeDefinition attachedTypeDef,
                                               String functionName, String serviceName, boolean isResource,
                                               boolean isRemote, boolean isMainEntryPoint, boolean isWorker) {
        // Injecting observe start call at the start of the function body
        {
            BIRBasicBlock startBB = func.basicBlocks.get(0);    // Every non-abstract function should have function body
            BIRBasicBlock newStartBB = insertBasicBlock(func, 1);
            swapBasicBlockContent(func, startBB, newStartBB);

            if (isResource || isRemote) {
                String resourcePathOrFunction = functionName;
                String resourceAccessor = null;
                if (isResource) {
                    for (BAttachedFunction attachedFunc : ((BClassSymbol) attachedTypeDef.type.tsymbol).attachedFuncs) {
                        if (Objects.equals(attachedFunc.funcName.getValue(), functionName)) {
                            BResourceFunction resourceFunction = (BResourceFunction) attachedFunc;
                            StringBuilder resourcePathOrFunctionBuilder = new StringBuilder();
                            for (Name name : resourceFunction.resourcePath) {
                                resourcePathOrFunctionBuilder.append("/").append(
                                        Utils.unescapeBallerina(name.getValue()));
                            }
                            resourcePathOrFunction = resourcePathOrFunctionBuilder.toString();
                            resourceAccessor = resourceFunction.accessor.getValue();
                            break;
                        }
                    }
                }
                injectStartResourceObservationCall(func, startBB, serviceName, resourcePathOrFunction, resourceAccessor,
                        isResource, isRemote, pkg, func.pos);
            } else {
                BIROperand objectTypeOperand = generateGlobalConstantOperand(pkg, symbolTable.nilType, null);
                injectStartCallableObservationCall(func, startBB, null, false, isMainEntryPoint, isWorker,
                        objectTypeOperand, functionName, pkg, func.pos);
            }

            // Fix the Basic Blocks links
            startBB.terminator.thenBB = newStartBB;
        }
        // Injecting error report call and observe end call just before the return statements
        boolean isErrorCheckRequired = isErrorAssignable(func.returnVariable);
        BIROperand returnValOperand = new BIROperand(func.returnVariable);
        int i = 1;  // Since the first block is now the start observation call
        while (i < func.basicBlocks.size()) {
            BIRBasicBlock currentBB = func.basicBlocks.get(i);
            if (currentBB.terminator.kind == InstructionKind.RETURN) {
                if (isErrorCheckRequired) {
                    BIRBasicBlock errorReportBB = insertBasicBlock(func, i + 1);
                    BIRBasicBlock observeEndBB = insertBasicBlock(func, i + 2);
                    BIRBasicBlock newCurrentBB = insertBasicBlock(func, i + 3);
                    swapBasicBlockTerminator(func, currentBB, newCurrentBB);

                    injectCheckErrorCalls(func, currentBB, errorReportBB, observeEndBB, null,
                            returnValOperand, FUNC_BODY_INSTRUMENTATION_TYPE);
                    injectReportErrorCall(func, errorReportBB, null, returnValOperand,
                            FUNC_BODY_INSTRUMENTATION_TYPE);
                    injectStopObservationCall(observeEndBB, null);

                    // Fix the Basic Blocks links
                    observeEndBB.terminator.thenBB = newCurrentBB;
                    errorReportBB.terminator.thenBB = observeEndBB;
                    i += 3; // Number of inserted BBs
                } else {
                    BIRBasicBlock newCurrentBB = insertBasicBlock(func, i + 1);
                    swapBasicBlockTerminator(func, currentBB, newCurrentBB);

                    injectStopObservationCall(currentBB, null);

                    // Fix the Basic Blocks links
                    currentBB.terminator.thenBB = newCurrentBB;
                    i += 1; // Number of inserted BBs
                }
            } else if (currentBB.terminator.kind == InstructionKind.PANIC) {
                Panic panicCall = (Panic) currentBB.terminator;
                BIRBasicBlock newCurrentBB = insertBasicBlock(func, i + 1);
                swapBasicBlockTerminator(func, currentBB, newCurrentBB);

                injectStopObservationWithErrorCall(func, currentBB, newCurrentBB.terminator.pos,
                        panicCall.errorOp, FUNC_BODY_INSTRUMENTATION_TYPE);

                // Fix the Basic Blocks links
                currentBB.terminator.thenBB = newCurrentBB;
                i += 1; // Number of inserted BBs
            }
            i++;
        }
        // Add error entry for the entire function
        {
            int initialBBCount = func.basicBlocks.size();
            BIRBasicBlock startBB = func.basicBlocks.get(0);
            BIRBasicBlock endBB = func.basicBlocks.get(initialBBCount - 1);
            BIRBasicBlock observeEndBB = insertBasicBlock(func, initialBBCount);
            BIRBasicBlock rePanicBB = insertBasicBlock(func, initialBBCount + 1);

            BIROperand trappedErrorOperand = generateTempLocalVariable(func, "functionTrappedError",
                    symbolTable.errorOrNilType);

            injectStopObservationWithErrorCall(func, observeEndBB, null, trappedErrorOperand,
                    FUNC_BODY_INSTRUMENTATION_TYPE);
            rePanicBB.terminator = new Panic(null, trappedErrorOperand);

            BIRErrorEntry errorEntry = new BIRErrorEntry(startBB, endBB, trappedErrorOperand, observeEndBB);
            func.errorTable.add(errorEntry);

            // Fix the Basic Blocks links
            observeEndBB.terminator.thenBB = rePanicBB;
        }
    }

    /**
     * Re-write the relevant basic blocks in the list of basic blocks to observe function invocations.
     *
     * @param func The function of which the instructions in the body should be instrumented
     * @param pkg The package which contains the instruction which will be observed
     */
    private void rewriteObservableFunctionInvocations(BIRFunction func, BIRPackage pkg) {
        int i = 0;
        while (i < func.basicBlocks.size()) {
            BIRBasicBlock currentBB = func.basicBlocks.get(i);
            if (currentBB.terminator.kind == InstructionKind.CALL && isObservable((Call) currentBB.terminator)) {
                Call callIns = (Call) currentBB.terminator;
                Location desugaredInsPosition = callIns.pos;
                BIRBasicBlock observeStartBB = insertBasicBlock(func, i + 1);
                int newCurrentIndex = i + 2;
                BIRBasicBlock newCurrentBB = insertBasicBlock(func, newCurrentIndex);
                swapBasicBlockTerminator(func, currentBB, newCurrentBB);
                {   // Injecting the instrumentation points for invocations
                    BIROperand objectTypeOperand;
                    String action;
                    if (callIns.isVirtual) {
                        // Every virtual call instruction has self as the first argument
                        objectTypeOperand = callIns.args.get(0);
                        if (callIns.name.getValue().contains(".")) {
                            String[] split = callIns.name.getValue().split("\\.");
                            action = split[1];
                        } else {
                            action = callIns.name.getValue();
                        }
                    } else {
                        objectTypeOperand = generateGlobalConstantOperand(pkg, symbolTable.nilType, null);
                        action = callIns.name.getValue();
                    }
                    currentBB.terminator = new GOTO(desugaredInsPosition, observeStartBB);

                    BIRBasicBlock observeEndBB;
                    boolean isRemote = callIns.calleeFlags.contains(Flag.REMOTE);
                    Location originalInsPos = callIns.pos;
                    if (isErrorAssignable(callIns.lhsOp.variableDcl)) {
                        BIRBasicBlock errorCheckBB = insertBasicBlock(func, i + 3);
                        BIRBasicBlock errorReportBB = insertBasicBlock(func, i + 4);
                        observeEndBB = insertBasicBlock(func, i + 5);

                        injectStartCallableObservationCall(func, observeStartBB, desugaredInsPosition,
                                isRemote, false, false, objectTypeOperand, action, pkg,
                                originalInsPos);
                        injectCheckErrorCalls(func, errorCheckBB, errorReportBB, observeEndBB,
                                desugaredInsPosition, callIns.lhsOp, INVOCATION_INSTRUMENTATION_TYPE);
                        injectReportErrorCall(func, errorReportBB, desugaredInsPosition, callIns.lhsOp,
                                INVOCATION_INSTRUMENTATION_TYPE);
                        injectStopObservationCall(observeEndBB, desugaredInsPosition);

                        // Fix the Basic Blocks links
                        observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                        errorReportBB.terminator.thenBB = observeEndBB;
                        newCurrentBB.terminator.thenBB = errorCheckBB;
                        observeStartBB.terminator.thenBB = newCurrentBB;
                        i += 5; // Number of inserted BBs
                    } else {
                        observeEndBB = insertBasicBlock(func, i + 3);

                        injectStartCallableObservationCall(func, observeStartBB, desugaredInsPosition,
                                isRemote, false, false, objectTypeOperand, action, pkg,
                                originalInsPos);
                        injectStopObservationCall(observeEndBB, desugaredInsPosition);

                        // Fix the Basic Blocks links
                        observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                        newCurrentBB.terminator.thenBB = observeEndBB;
                        observeStartBB.terminator.thenBB = newCurrentBB;
                        i += 3; // Number of inserted BBs
                    }
                    fixErrorTable(func, currentBB, observeEndBB);
                }
                {
                    /*
                     * Adding panic traps for the invocations. These report the error to the Observation covering
                     * the invocation. In the above instrumentation, only errors returned by functions are
                     * considered.
                     */
                    Optional<BIRErrorEntry> existingEE = Optional.empty();
                    for (BIRErrorEntry birErrorEntry : func.errorTable) {
                        if (isBBCoveredInErrorEntry(birErrorEntry, func.basicBlocks, newCurrentBB)) {
                            existingEE = Optional.of(birErrorEntry);
                            break;
                        }
                    }
                    Location desugaredInsPos = callIns.pos;
                    if (existingEE.isPresent()) {
                        BIRErrorEntry errorEntry = existingEE.get();
                        int eeTargetIndex = func.basicBlocks.indexOf(errorEntry.targetBB);
                        if (eeTargetIndex == -1) {
                            throw new BLangCompilerException("Invalid Error Entry pointing to non-existent " +
                                    "target Basic Block " + errorEntry.targetBB.id);
                        }

                        BIRBasicBlock observeEndBB = insertBasicBlock(func, eeTargetIndex + 1);
                        BIRBasicBlock newTargetBB = insertBasicBlock(func, eeTargetIndex + 2);
                        swapBasicBlockContent(func, errorEntry.targetBB, newTargetBB);

                        injectCheckErrorCalls(func, errorEntry.targetBB, observeEndBB, newTargetBB,
                                desugaredInsPos, errorEntry.errorOp, INVOCATION_INSTRUMENTATION_TYPE);
                        injectStopObservationWithErrorCall(func, observeEndBB, desugaredInsPos,
                                errorEntry.errorOp, INVOCATION_INSTRUMENTATION_TYPE);

                        // Fix the Basic Blocks links
                        observeEndBB.terminator.thenBB = newTargetBB;
                        fixErrorTable(func, errorEntry.targetBB, newTargetBB);
                    } else {
                        BIRBasicBlock errorCheckBB = insertBasicBlock(func, newCurrentIndex + 1);
                        BIRBasicBlock observeEndBB = insertBasicBlock(func, newCurrentIndex + 2);
                        BIRBasicBlock rePanicBB = insertBasicBlock(func, newCurrentIndex + 3);

                        BIROperand trappedErrorOperand = generateTempLocalVariable(func, "trappedError",
                                symbolTable.errorOrNilType);

                        injectCheckErrorCalls(func, errorCheckBB, observeEndBB, newCurrentBB.terminator.thenBB,
                                newCurrentBB.terminator.pos, trappedErrorOperand,
                                INVOCATION_INSTRUMENTATION_TYPE);
                        injectStopObservationWithErrorCall(func, observeEndBB, newCurrentBB.terminator.pos,
                                trappedErrorOperand, INVOCATION_INSTRUMENTATION_TYPE);
                        rePanicBB.terminator = new Panic(newCurrentBB.terminator.pos, trappedErrorOperand);

                        BIRErrorEntry errorEntry = new BIRErrorEntry(newCurrentBB, newCurrentBB,
                                trappedErrorOperand, errorCheckBB);
                        func.errorTable.add(errorEntry);

                        // Fix the Basic Blocks links
                        newCurrentBB.terminator.thenBB = errorCheckBB;
                        observeEndBB.terminator.thenBB = rePanicBB;
                        i += 3; // Number of inserted BBs
                    }
                }
            }
            i += 1;
        }
    }

    /**
     * Inject start observation call to a basic block.
     * @param func Bir Function
     * @param observeStartBB The basic block to which the start observation call should be injected
     * @param serviceName The service to which the instruction was attached to
     * @param resourcePathOrFunction The resource path or function name
     * @param resourceAccessor The resource accessor if this is a resource
     * @param isResource True if the function is a resource
     * @param isRemote True if the function is a remote
     * @param pkg The package the invocation belongs to
     * @param originalInsPosition The source code position of the invocation
     */
    private void injectStartResourceObservationCall(BIRFunction func, BIRBasicBlock observeStartBB, String serviceName,
                                                    String resourcePathOrFunction, String resourceAccessor,
                                                    boolean isResource, boolean isRemote, BIRPackage pkg,
                                                    Location originalInsPosition) {
        BIROperand serviceNameOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, serviceName);
        BIROperand resourcePathOrFunctionOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType,
                                                                                 resourcePathOrFunction);
        BIROperand resourceAccessorOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType,
                resourceAccessor);
        BIROperand isResourceOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType, isResource);
        BIROperand isRemoteOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType, isRemote);

        JIMethodCall observeStartCallTerminator = new JIMethodCall(null);
        observeStartCallTerminator.invocationType = INVOKESTATIC;
        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
        observeStartCallTerminator.jMethodVMSig = START_RESOURCE_OBSERVATION;
        observeStartCallTerminator.name = START_RESOURCE_OBSERVATION_METHOD;
        List<BIROperand> positionOperands = generatePositionArgs(pkg, func, observeStartBB, originalInsPosition);
        List<BIROperand> otherOperands = Arrays.asList(serviceNameOperand, resourcePathOrFunctionOperand,
                resourceAccessorOperand, isResourceOperand, isRemoteOperand);
        positionOperands.addAll(otherOperands);
        observeStartCallTerminator.args = positionOperands;
        observeStartBB.terminator = observeStartCallTerminator;
    }

    /**
     * Inject start observation call to a basic block.
     *
     * @param func Bir Function
     * @param observeStartBB The basic block to which the start observation call should be injected
     * @param desugaredInsLocation The position of all instructions, variables declarations, terminators to be generated
     * @param isRemote True if a remote function will be observed by the observation
     * @param isMainEntryPoint True if the main function will be observed by the observation
     * @param isWorker True if a worker function will be observed by the observation
     * @param objectOperand The object the function was attached to
     * @param action The name of the action which will be observed
     * @param pkg The package the invocation belongs to
     * @param originalInsPosition The source code position of the invocation
     */
    private void injectStartCallableObservationCall(BIRFunction func, BIRBasicBlock observeStartBB,
                                                    Location desugaredInsLocation, boolean isRemote,
                                                    boolean isMainEntryPoint, boolean isWorker,
                                                    BIROperand objectOperand, String action,
                                                    BIRPackage pkg, Location originalInsPosition) {
        BIROperand actionOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, action);
        BIROperand isMainEntryPointOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType,
                isMainEntryPoint);
        BIROperand isRemoteOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType, isRemote);
        BIROperand isWorkerOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType, isWorker);

        JIMethodCall observeStartCallTerminator = new JIMethodCall(desugaredInsLocation);
        observeStartCallTerminator.invocationType = INVOKESTATIC;
        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
        observeStartCallTerminator.jMethodVMSig = START_CALLABLE_OBSERVATION;
        observeStartCallTerminator.name = START_CALLABLE_OBSERVATION_METHOD;
        List<BIROperand> positionOperands = generatePositionArgs(pkg, func, observeStartBB, originalInsPosition);
        List<BIROperand> otherOperands = Arrays.asList(objectOperand, actionOperand, isMainEntryPointOperand,
                isRemoteOperand, isWorkerOperand);
        positionOperands.addAll(otherOperands);
        observeStartCallTerminator.args = positionOperands;
        observeStartBB.terminator = observeStartCallTerminator;
    }

    /**
     * Inject branch condition for checking if a value is an error.
     *
     * @param func The BIR function in which the call is injected
     * @param errorCheckBB The basic block to which the error check should be injected
     * @param isErrorBB The basic block to which errors should go to
     * @param noErrorBB The basic block to which no errors should go to
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param valueOperand Operand for passing the value which should be checked if it is an error
     * @param uniqueId A unique ID to identify the check error call
     */
    private void injectCheckErrorCalls(BIRFunction func, BIRBasicBlock errorCheckBB, BIRBasicBlock isErrorBB,
                                       BIRBasicBlock noErrorBB, Location pos, BIROperand valueOperand,
                                       String uniqueId) {
        BIROperand isErrorOperand = tempLocalVarsMap.get(uniqueId + "$isError");
        addLocalVarIfAbsent(func, isErrorOperand.variableDcl);
        TypeTest errorTypeTestInstruction = new TypeTest(pos, symbolTable.errorType, isErrorOperand, valueOperand);
        errorCheckBB.instructions.add(errorTypeTestInstruction);
        errorCheckBB.terminator = new Branch(pos, isErrorOperand, isErrorBB, noErrorBB);
    }

    /**
     * Inject report error call.
     *
     * @param func The BIR function in which the call is injected
     * @param errorReportBB The basic block to which the report error call should be injected
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param errorOperand Operand for passing the error
     * @param uniqueId A unique ID to identify the check error call
     */
    private void injectReportErrorCall(BIRFunction func, BIRBasicBlock errorReportBB, Location pos,
                                       BIROperand errorOperand, String uniqueId) {
        BIROperand castedErrorOperand = tempLocalVarsMap.get(uniqueId + "$castedError");
        addLocalVarIfAbsent(func, castedErrorOperand.variableDcl);
        TypeCast errorCastInstruction = new TypeCast(pos, castedErrorOperand, errorOperand, symbolTable.errorType,
                false);
        errorReportBB.instructions.add(errorCastInstruction);

        JIMethodCall reportErrorCallTerminator = new JIMethodCall(pos);
        reportErrorCallTerminator.invocationType = INVOKESTATIC;
        reportErrorCallTerminator.jClassName = OBSERVE_UTILS;
        reportErrorCallTerminator.jMethodVMSig = ERROR_CALL;
        reportErrorCallTerminator.name = REPORT_ERROR_METHOD;
        reportErrorCallTerminator.args = Collections.singletonList(castedErrorOperand);
        errorReportBB.terminator = reportErrorCallTerminator;
    }

    /**
     * Inject a stop observation call to a basic block.
     *
     * @param observeEndBB The basic block to which the stop observation call should be injected
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     */
    private void injectStopObservationCall(BIRBasicBlock observeEndBB, Location pos) {
        JIMethodCall observeEndCallTerminator = new JIMethodCall(pos);
        observeEndCallTerminator.invocationType = INVOKESTATIC;
        observeEndCallTerminator.jClassName = OBSERVE_UTILS;
        observeEndCallTerminator.jMethodVMSig = STOP_OBSERVATION;
        observeEndCallTerminator.name = STOP_OBSERVATION_METHOD;
        observeEndCallTerminator.args = Collections.emptyList();
        observeEndBB.terminator = observeEndCallTerminator;
    }

    /**
     * Inject stop observation with an error call.
     *
     * @param func The BIR function in which the call is injected
     * @param observeEndBB The basic block to which the stop observation call should be injected
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param errorOperand Operand for passing the error
     * @param uniqueId A unique ID to identify the check error call
     */
    private void injectStopObservationWithErrorCall(BIRFunction func, BIRBasicBlock observeEndBB, Location pos,
                                                    BIROperand errorOperand, String uniqueId) {
        BIROperand castedErrorOperand = tempLocalVarsMap.get(uniqueId + "$castedError");
        addLocalVarIfAbsent(func, castedErrorOperand.variableDcl);
        TypeCast errorCastInstruction = new TypeCast(pos, castedErrorOperand, errorOperand, symbolTable.errorType,
                false);
        observeEndBB.instructions.add(errorCastInstruction);

        JIMethodCall observeEndBBCallTerminator = new JIMethodCall(pos);
        observeEndBBCallTerminator.invocationType = INVOKESTATIC;
        observeEndBBCallTerminator.jClassName = OBSERVE_UTILS;
        observeEndBBCallTerminator.jMethodVMSig = ERROR_CALL;
        observeEndBBCallTerminator.name = STOP_OBSERVATION_WITH_ERROR_METHOD;
        observeEndBBCallTerminator.args = Collections.singletonList(castedErrorOperand);
        observeEndBB.terminator = observeEndBBCallTerminator;
    }

    /**
     * Generate a constant operand from a compile-time known value.
     *
     * @param pkg The package which should contain the constant
     * @param constantType The type of the constant
     * @param constantValue The constant value which should end up being passed in the operand
     * @return The generated operand which will pass the constant
     */
    private BIROperand generateGlobalConstantOperand(BIRPackage pkg, BType constantType, Object constantValue) {
        return compileTimeConstants.computeIfAbsent(constantValue, k -> {
            PackageID pkgId = pkg.packageID;
            Name name = new Name("$observabilityConst" + constantIndex++);
            BIRGlobalVariableDcl constLoadVariableDcl =
                    new BIRGlobalVariableDcl(COMPILE_TIME_CONST_POS, 0,
                            constantType, pkgId, name, name,
                            VarScope.GLOBAL, VarKind.CONSTANT, "", VIRTUAL);
            pkg.globalVars.add(constLoadVariableDcl);
            return new BIROperand(constLoadVariableDcl);
        });
    }

    /**
     * Create and insert a new basic block into a function in the specified index.
     *
     * @param func The function to which the basic block should be injected
     * @param insertIndex The index at which the basic block should be injected
     * @return The injected new BB
     */
    private BIRBasicBlock insertBasicBlock(BIRFunction func, int insertIndex) {
        BIRBasicBlock newBB = new BIRBasicBlock(new Name(NEW_BB_PREFIX + desugaredBBIndex++));
        func.basicBlocks.add(insertIndex, newBB);
        return newBB;
    }

    /**
     * Swap the effective content of two basic blocks.
     *
     * @param func The BIR function
     * @param firstBB The first BB of which content should end up in second BB
     * @param secondBB The second BB of which content should end up in first BB
     */
    private void swapBasicBlockContent(BIRFunction func, BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        List<BIRNonTerminator> firstBBInstructions = firstBB.instructions;
        firstBB.instructions = secondBB.instructions;
        secondBB.instructions = firstBBInstructions;
        resetEndBasicBlock(func, firstBB, secondBB);
        swapBasicBlockTerminator(func, firstBB, secondBB);
    }

    /**
     * Swap the terminators of two basic blocks.
     *
     * @param func The BIR function
     * @param firstBB The first BB of which terminator should end up in second BB
     * @param secondBB The second BB of which terminator should end up in first BB
     */
    private void swapBasicBlockTerminator(BIRFunction func, BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        BIRTerminator firstBBTerminator = firstBB.terminator;
        firstBB.terminator = secondBB.terminator;
        secondBB.terminator = firstBBTerminator;
        resetEndBasicBlock(func, firstBB, secondBB);
    }

    /**
     * Reset endBBs of local variables after swapping basic blocks content.
     *
     * @param func The BIR function
     * @param firstBB The first BB of which content should end up in second BB
     * @param secondBB The second BB of which content should end up in first BB
     */
    private void resetEndBasicBlock(BIRFunction func, BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        for (BIRVariableDcl localVar : func.localVars) {
            if (localVar.endBB == firstBB) {
                localVar.endBB = secondBB;
            }
        }
    }

    /**
     * Fix the ending BB of error entries in the error table of a function.
     * When desugar instructions were added after the original BB,
     * where the original BB is a trap ending BB, the new trap ending BBs changes.
     * This needs to be adjusted properly.
     *
     * @param func The function of which the error table should be fixed
     * @param oldBB The old ending BB of error entries to be fixed
     * @param newBB The new ending BB which should be updated to in the error entries to be fixed
     */
    private void fixErrorTable(BIRFunction func, BIRBasicBlock oldBB, BIRBasicBlock newBB) {
        for (BIRErrorEntry errorEntry : func.errorTable) {
            if (errorEntry.endBB == oldBB) {
                errorEntry.endBB = newBB;
            }
        }
    }

    /**
     * Check if a call instruction is observable.
     *
     * @param callIns The call instruction to check
     * @return True if the call instruction is observable
     */
    private boolean isObservable(Call callIns) {
        boolean isRemote = callIns.calleeFlags.contains(Flag.REMOTE);
        boolean isObservableAnnotationPresent = false;
        for (BIRAnnotationAttachment annot : callIns.calleeAnnotAttachments) {
            if (OBSERVABLE_ANNOTATION.equals(
                    JvmCodeGenUtil.getPackageName(
                            new PackageID(annot.annotPkgId.orgName, annot.annotPkgId.name, Names.EMPTY)) +
                            annot.annotTagRef.getValue())) {
                isObservableAnnotationPresent = true;
                break;
            }
        }
        return isRemote || isObservableAnnotationPresent;
    }

    /**
     * Check is an error is assignable to a variable declaration.
     *
     * @param variableDcl The variable declaration which should be checked.
     * @return True if an error can be assigned and false otherwise
     */
    private boolean isErrorAssignable(BIRVariableDcl variableDcl) {
        boolean isErrorAssignable = false;
        if (variableDcl.type instanceof BUnionType) {
            BUnionType returnUnionType = (BUnionType) variableDcl.type;
            boolean b = false;
            for (BType type : returnUnionType.getMemberTypes()) {
                if (type instanceof BErrorType) {
                    b = true;
                    break;
                }
            }
            isErrorAssignable = b;
        } else if (variableDcl.type instanceof BErrorType) {
            isErrorAssignable = true;
        }
        return isErrorAssignable;
    }

    /**
     * Check if a basic block is covered into an error entry.
     *
     * @param errorEntry The error entry from the error table
     * @param basicBlocksList The basic blocks list which contains the basic block to be checked for
     * @param basicBlock The basic block which should be checked for
     * @return True if the basic block is covered in the error entry
     */
    private boolean isBBCoveredInErrorEntry(BIRErrorEntry errorEntry, List<BIRBasicBlock> basicBlocksList,
                                            BIRBasicBlock basicBlock) {
        boolean isCovered = Objects.equals(basicBlock, errorEntry.trapBB)
                || Objects.equals(basicBlock, errorEntry.endBB);
        if (!isCovered) {
            /*
             * Traverse in the same way MethodGen.generateBasicBlocks traverses through basic blocks to generate
             * method body to check if the basic block is covered in the error entry.
             */
            int i = 0;
            for (; i < basicBlocksList.size(); i++) {
                BIRBasicBlock currentBB = basicBlocksList.get(i);
                if (currentBB == errorEntry.trapBB) {
                    break;
                }
            }
            for (; i < basicBlocksList.size(); i++) {
                BIRBasicBlock currentBB = basicBlocksList.get(i);
                if (currentBB == basicBlock) {
                    isCovered = true;
                    break;
                }
                if (currentBB == errorEntry.endBB) {
                    break;
                }
            }
        }
        return isCovered;
    }

    /**
     * Generate a ID for a ballerina module.
     *
     * @param pkg The module for which the ID should be generated
     * @return The generated ID
     */
    private String generatePackageId(PackageID pkg) {
        return pkg.orgName.getValue() + "/" + pkg.name.getValue() + ":" + pkg.version.getValue();
    }

    /**
     * Generate operands for location.
     *
     * @param pkg Bir package
     * @param func Bir Function
     * @param observeStartBB Observe start basic block
     * @param pos Location
     * @return List of operands for source file name, position start line and start column
     */
    private List<BIROperand> generatePositionArgs(BIRPackage pkg, BIRFunction func, BIRBasicBlock observeStartBB,
                                                  Location pos) {
        BIROperand pkgOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType,
                generatePackageId(pkg.packageID));
        BIROperand fileNameOperand = getTempLocalVariable(FILE_NAME_STRING, pos, pos.lineRange().filePath(),
                symbolTable.stringType, observeStartBB);
        addLocalVarIfAbsent(func, fileNameOperand.variableDcl);
        BIROperand startLineOperand = getTempLocalVariable(START_LINE_STRING, pos,
                pos.lineRange().startLine().line() + 1, symbolTable.intType, observeStartBB);
        addLocalVarIfAbsent(func, startLineOperand.variableDcl);
        BIROperand startColOperand = getTempLocalVariable(START_COLUMN_STRING, pos,
                pos.lineRange().startLine().offset() + 1, symbolTable.intType, observeStartBB);
        addLocalVarIfAbsent(func, startColOperand.variableDcl);
        return new ArrayList<>(Arrays.asList(pkgOperand, fileNameOperand, startLineOperand, startColOperand));
    }

    private BIROperand getTempLocalVariable(String name, Location pos, Object value, BType variableType,
                                            BIRBasicBlock currentBB) {
        BIROperand birOperand = tempLocalVarsMap.get(name);
        addConstantLoadIns(pos, value, variableType, birOperand, currentBB);
        return birOperand;
    }

    private void updatePositionArgsConstLoadIns(Location pos, BIRBasicBlock currentBB) {
        addConstantLoadIns(pos, pos.lineRange().startLine().line() + 1, symbolTable.intType,
                tempLocalVarsMap.get(START_LINE_STRING), currentBB, 0);
        addConstantLoadIns(pos, pos.lineRange().startLine().offset() + 1, symbolTable.intType,
                tempLocalVarsMap.get(START_COLUMN_STRING), currentBB, 1);
    }

    private void addConstantLoadIns(Location pos, Object value, BType variableType, BIROperand birOperand,
                                    BIRBasicBlock currentBB) {
        ConstantLoad constantLoad = new ConstantLoad(pos, value, variableType, birOperand);
        currentBB.instructions.add(constantLoad);
    }

    private void addConstantLoadIns(Location pos, Object value, BType variableType, BIROperand birOperand,
                                    BIRBasicBlock currentBB, int index) {
        ConstantLoad constantLoad = new ConstantLoad(pos, value, variableType, birOperand);
        currentBB.instructions.add(index, constantLoad);
    }

    private void addLocalVarIfAbsent(BIRFunction func, BIRVariableDcl variableDcl) {
        if (!func.localVars.contains(variableDcl)) {
            func.localVars.add(variableDcl);
        }
    }

    /**
     * Initializes the temporary local variables which can be reused.
     *
     */
    private void initializeTempLocalVariables() {
        // Initialize temporary variables for position arguments
        generateTempLocalVariable(FILE_NAME_STRING, symbolTable.stringType);
        generateTempLocalVariable(START_LINE_STRING, symbolTable.intType);
        generateTempLocalVariable(START_COLUMN_STRING, symbolTable.intType);
        // Initialize temporary variables for error casting
        generateTempLocalVariable(FUNC_BODY_INSTRUMENTATION_TYPE + "$castedError", symbolTable.errorType);
        generateTempLocalVariable(INVOCATION_INSTRUMENTATION_TYPE + "$castedError", symbolTable.errorType);
        // Initialize temporary variables for isError check
        generateTempLocalVariable(FUNC_BODY_INSTRUMENTATION_TYPE + "$isError", symbolTable.booleanType);
        generateTempLocalVariable(INVOCATION_INSTRUMENTATION_TYPE + "$isError", symbolTable.booleanType);
    }

    private void generateTempLocalVariable(String name, BType variableType) {
        // Create a temporary variable
        BIRVariableDcl variableDcl = new BIRVariableDcl(variableType, new Name("$observability$" + name),
                VarScope.FUNCTION, VarKind.TEMP);
        // Create a birOperand for the tempVar and store it in the map `tempLocalVarsMap`
        BIROperand birOperand = new BIROperand(variableDcl);
        tempLocalVarsMap.put(name, birOperand);
    }

    /**
     * Generate a temporary function scope variable.
     *
     * @param func The BIR function to which the variable should be added
     * @param name The name of the variable
     * @param variableType The type of the variable
     * @return The generated operand for the variable declaration
     */
    private BIROperand generateTempLocalVariable(BIRFunction func, String name, BType variableType) {
        Name variableName = new Name("$observability$" + name + "$" + localVarIndex++);
        BIRVariableDcl variableDcl = new BIRVariableDcl(variableType, variableName, VarScope.FUNCTION, VarKind.TEMP);
        func.localVars.add(variableDcl);
        return new BIROperand(variableDcl);
    }
}
