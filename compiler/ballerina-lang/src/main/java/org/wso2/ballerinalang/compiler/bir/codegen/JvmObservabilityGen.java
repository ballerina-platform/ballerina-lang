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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JIMethodCall;
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
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.FPCall;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.GOTO;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Panic;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Return;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVABLE_ANNOTATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REPORT_ERROR_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_CALLABLE_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_RESOURCE_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STOP_OBSERVATION_METHOD;

/**
 * BIR desugar to inject observations class.
 *
 * @since 2.0.0
 */
class JvmObservabilityGen {
    private static final String ENTRY_POINT_MAIN_METHOD_NAME = "main";
    private static final String NEW_BB_PREFIX = "observabilityDesugaredBB";
    private static final String SERVICE_IDENTIFIER = "$$service$";
    private static final String ANONYMOUS_SERVICE_IDENTIFIER = "$anonService$";
    private static final String INVOCATION_INSTRUMENTATION_TYPE = "invocation";
    private static final String FUNC_BODY_INSTRUMENTATION_TYPE = "funcBody";
    private static final DiagnosticPos COMPILE_TIME_CONST_POS = new DiagnosticPos(null, -1, -1, -1, -1);

    private final PackageCache packageCache;
    private final SymbolTable symbolTable;
    private int lambdaIndex;
    private int desugaredBBIndex;
    private int constantIndex;

    private Map<Object, BIROperand> compileTimeConstants;

    JvmObservabilityGen(JvmPackageGen pkgGen) {
        compileTimeConstants = new HashMap<>();
        packageCache = pkgGen.packageCache;
        symbolTable = pkgGen.symbolTable;
        lambdaIndex = 0;
        desugaredBBIndex = 0;
        constantIndex = 0;
    }

    /**
     * Rewrite all observable functions in a package.
     *
     * @param pkg The package to instrument
     */
    void rewriteObservableFunctions(BIRPackage pkg) {
        for (int i = 0; i < pkg.functions.size(); i++) {
            BIRFunction func = pkg.functions.get(i);
            rewriteAsyncInvocations(func, null, pkg);
            rewriteObservableFunctionInvocations(func, pkg);
            if (ENTRY_POINT_MAIN_METHOD_NAME.equals(func.name.value)) {
                rewriteObservableFunctionBody(func, pkg, false, true, false,
                        StringUtils.EMPTY, func.name.value);
            } else if ((func.flags & Flags.WORKER) == Flags.WORKER) {   // Identifying lambdas generated for workers
                rewriteObservableFunctionBody(func, pkg, false, false, true,
                        StringUtils.EMPTY, func.workerName.value);
            }
        }
        for (BIRTypeDefinition typeDef : pkg.typeDefs) {
            if ((typeDef.flags & Flags.CLASS) != Flags.CLASS && typeDef.type.tag == TypeTags.OBJECT) {
                continue;
            }
            boolean isService = typeDef.type instanceof BServiceType;
            for (int i = 0; i < typeDef.attachedFuncs.size(); i++) {
                BIRFunction func = typeDef.attachedFuncs.get(i);
                rewriteAsyncInvocations(func, typeDef, pkg);
                rewriteObservableFunctionInvocations(func, pkg);
                if (isService && (func.flags & Flags.RESOURCE) == Flags.RESOURCE) {
                    rewriteObservableFunctionBody(func, pkg, true, false, false,
                            cleanUpServiceName(typeDef.name.value), func.name.value);
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
     * Rewrite the invocations in the function bodies to call a lambda asynchronously which in turn calls the
     * actual function synchronously. This is done so that the actual invocation can be observed accurately.
     *
     * Without this wrapper, the start and end time recorded would only reflect the time it took to give the the async
     * invocation to the scheduler. However, we require the actual time it took for the invocation.
     *
     * @param func The function of which the instructions in the body should be rewritten
     * @param attachedTypeDef The type definition to which the function was attached to or null
     * @param pkg The package containing the function
     */
    private void rewriteAsyncInvocations(BIRFunction func, BIRTypeDefinition attachedTypeDef, BIRPackage pkg) {
        PackageID currentPkgId = new PackageID(pkg.org, pkg.name, pkg.version);
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
            List<BType> argTypes = asyncCallIns.args.stream()
                    .map(arg -> arg.variableDcl.type)
                    .collect(Collectors.toList());
            Name lambdaName = new Name(String.format("$lambda$observability%d$%s", lambdaIndex++,
                    asyncCallIns.name.value.replace(".", "_")));
            BInvokableType bInvokableType = new BInvokableType(argTypes, null,
                                                               returnType, null);
            BIRFunction desugaredFunc = new BIRFunction(asyncCallIns.pos, lambdaName, 0, bInvokableType,
                                                        func.workerName, 0, null, VIRTUAL);
            desugaredFunc.receiver = func.receiver;
            scopeFunctionsList.add(desugaredFunc);

            // Creating the return variable
            BIRVariableDcl funcReturnVariableDcl = new BIRVariableDcl(returnType,
                    new Name(String.format("$%s$retVal", lambdaName.value)), VarScope.FUNCTION, VarKind.RETURN);
            BIROperand funcReturnOperand = new BIROperand(funcReturnVariableDcl);
            desugaredFunc.localVars.add(funcReturnVariableDcl);
            desugaredFunc.returnVariable = funcReturnVariableDcl;

            // Creating and adding invokable symbol to the relevant scope
            BInvokableSymbol invokableSymbol = new BInvokableSymbol(SymTag.FUNCTION, 0, lambdaName,
                                                                    currentPkgId, bInvokableType, functionOwner,
                                                                    desugaredFunc.pos, VIRTUAL);
            invokableSymbol.retType = funcReturnVariableDcl.type;
            invokableSymbol.kind = SymbolKind.FUNCTION;
            invokableSymbol.params = asyncCallIns.args.stream()
                    .map(arg -> new BVarSymbol(0, arg.variableDcl.name, currentPkgId, arg.variableDcl.type,
                                               invokableSymbol, arg.pos, VIRTUAL))
                    .collect(Collectors.toList());
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
                            VarScope.FUNCTION, VarKind.SELF, selfArgName.value, false);
                } else {
                    Name argName = new Name(String.format("$funcParam%d", i));
                    funcParam = new BIRFunctionParameter(asyncCallIns.pos, arg.variableDcl.type,
                            argName, VarScope.FUNCTION, VarKind.ARG, argName.value, false);
                    desugaredFunc.localVars.add(funcParam);
                    desugaredFunc.parameters.put(funcParam, Collections.emptyList());
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
                asyncCallIns.args.add(0, new BIROperand(new BIRVariableDcl(attachedTypeDef.type,
                        selfArgName, VarScope.FUNCTION, VarKind.SELF)));
            }
        }
    }

    /**
     * Rewrite a function so that the internal body will be observed. This adds the relevant start and stop calls at
     * the beginning and return basic blocks of the function.
     *
     * This is only to be used in service resource functions, workers and main method.
     *
     * This method expects that Observable invocations had already been instrumented properly before this method is
     * called. This is because the uncaught panics thrown from such observable invocations are reported to the
     * observation covering the function body by using the re-panic terminators which gets added in
     * rewriteObservableFunctionInvocations method.
     *
     * @param func The function to instrument
     * @param pkg The package which contains the function
     * @param isResource True if the function is a service resource
     * @param isMainEntryPoint True if the function is the main entry point
     * @param isWorker True if the function was a worker
     * @param serviceName The name of the service
     * @param resourceOrAction The name of the resource or action which will be observed
     */
    private void rewriteObservableFunctionBody(BIRFunction func, BIRPackage pkg, boolean isResource,
                                               boolean isMainEntryPoint, boolean isWorker, String serviceName,
                                               String resourceOrAction) {
        // Injecting observe start call at the start of the function body
        {
            BIRBasicBlock startBB = func.basicBlocks.get(0);    // Every non-abstract function should have function body
            BIRBasicBlock newStartBB = insertBasicBlock(func, 1);
            swapBasicBlockContent(startBB, newStartBB);

            if (isResource) {
                injectStartResourceObservationCall(startBB, serviceName, resourceOrAction, pkg, func.pos);
            } else {
                BIROperand objectTypeOperand = generateGlobalConstantOperand(pkg, symbolTable.nilType, null);
                injectStartCallableObservationCall(startBB, null, false, isMainEntryPoint, isWorker,
                        objectTypeOperand, resourceOrAction, pkg, func.pos);
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
                    swapBasicBlockTerminator(currentBB, newCurrentBB);

                    injectCheckErrorCalls(currentBB, errorReportBB, observeEndBB, func.localVars, null,
                            returnValOperand, FUNC_BODY_INSTRUMENTATION_TYPE);
                    injectReportErrorCall(errorReportBB, func.localVars, null, returnValOperand,
                            FUNC_BODY_INSTRUMENTATION_TYPE);
                    injectStopObservationCall(observeEndBB, null);

                    // Fix the Basic Blocks links
                    observeEndBB.terminator.thenBB = newCurrentBB;
                    errorReportBB.terminator.thenBB = observeEndBB;
                    i += 3; // Number of inserted BBs
                } else {
                    BIRBasicBlock newCurrentBB = insertBasicBlock(func, i + 1);
                    swapBasicBlockTerminator(currentBB, newCurrentBB);

                    injectStopObservationCall(currentBB, null);

                    // Fix the Basic Blocks links
                    currentBB.terminator.thenBB = newCurrentBB;
                    i += 1; // Number of inserted BBs
                }
            } else if (currentBB.terminator.kind == InstructionKind.PANIC) {
                Panic panicCall = (Panic) currentBB.terminator;
                BIRBasicBlock observeEndBB = insertBasicBlock(func, i + 1);
                BIRBasicBlock newCurrentBB = insertBasicBlock(func, i + 2);
                swapBasicBlockTerminator(currentBB, newCurrentBB);

                injectReportErrorCall(currentBB, func.localVars, newCurrentBB.terminator.pos, panicCall.errorOp,
                        FUNC_BODY_INSTRUMENTATION_TYPE);
                injectStopObservationCall(observeEndBB, newCurrentBB.terminator.pos);

                // Fix the Basic Blocks links
                currentBB.terminator.thenBB = observeEndBB;
                observeEndBB.terminator.thenBB = newCurrentBB;
                i += 2; // Number of inserted BBs
            } else if (currentBB.terminator.kind == InstructionKind.CALL
                    || (currentBB.terminator.kind == InstructionKind.FP_CALL
                    && !((FPCall) currentBB.terminator).isAsync)) {
                /*
                 * Traps for errors needs to be injected for each call and fp call separately to avoid messing up the
                 * line numbers in the stack trace shown when a panic is thrown.
                 *
                 * These panic traps are different from the traps added in rewriteObservableFunctionInvocations method,
                 * in the sense that these report the error to the Observation covering the current function this body
                 * belongs to. Also these do not cover the observable calls and fp calls (they are handled using the
                 * panic terminator handling logic)
                 */

                // If a panic is captured, it does not need to be reported
                Optional<BIRErrorEntry> existingEE = func.errorTable.stream()
                        .filter(errorEntry -> isBBCoveredInErrorEntry(errorEntry, func.basicBlocks, currentBB))
                        .findAny();
                if (!existingEE.isPresent()) {
                    BIRBasicBlock errorCheckBB = insertBasicBlock(func, i + 1);
                    BIRBasicBlock errorReportBB = insertBasicBlock(func, i + 2);
                    BIRBasicBlock observeEndBB = insertBasicBlock(func, i + 3);
                    BIRBasicBlock rePanicBB = insertBasicBlock(func, i + 4);

                    BIRVariableDcl trappedErrorVariableDcl = new BIRVariableDcl(symbolTable.errorType,
                            new Name(String.format("$%s$trappedError", currentBB.id.value)), VarScope.FUNCTION,
                            VarKind.TEMP);
                    func.localVars.add(trappedErrorVariableDcl);
                    BIROperand trappedErrorOperand = new BIROperand(trappedErrorVariableDcl);

                    injectCheckErrorCalls(errorCheckBB, errorReportBB, currentBB.terminator.thenBB, func.localVars,
                            currentBB.terminator.pos, trappedErrorOperand, FUNC_BODY_INSTRUMENTATION_TYPE);
                    injectReportErrorCall(errorReportBB, func.localVars, currentBB.terminator.pos, trappedErrorOperand,
                            FUNC_BODY_INSTRUMENTATION_TYPE);
                    injectStopObservationCall(observeEndBB, currentBB.terminator.pos);
                    rePanicBB.terminator = new Panic(currentBB.terminator.pos, trappedErrorOperand);

                    BIRErrorEntry errorEntry = new BIRErrorEntry(currentBB, currentBB, trappedErrorOperand,
                            errorCheckBB);
                    func.errorTable.add(errorEntry);

                    // Fix the Basic Blocks links
                    currentBB.terminator.thenBB = errorCheckBB;
                    errorReportBB.terminator.thenBB = observeEndBB;
                    observeEndBB.terminator.thenBB = rePanicBB;
                    i += 4; // Number of inserted BBs
                }
            }
            i++;
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
                DiagnosticPos desugaredInsPosition = callIns.pos;
                BIRBasicBlock observeStartBB = insertBasicBlock(func, i + 1);
                int newCurrentIndex = i + 2;
                BIRBasicBlock newCurrentBB = insertBasicBlock(func, newCurrentIndex);
                swapBasicBlockTerminator(currentBB, newCurrentBB);
                {   // Injecting the instrumentation points for invocations
                    BIROperand objectTypeOperand;
                    String action;
                    if (callIns.isVirtual) {
                        // Every virtual call instruction has self as the first argument
                        objectTypeOperand = callIns.args.get(0);
                        if (callIns.name.value.contains(".")) {
                            String[] split = callIns.name.value.split("\\.");
                            action = split[1];
                        } else {
                            action = callIns.name.value;
                        }
                    } else {
                        objectTypeOperand = generateGlobalConstantOperand(pkg, symbolTable.nilType, null);
                        action = callIns.name.value;
                    }
                    currentBB.terminator = new GOTO(desugaredInsPosition, observeStartBB);

                    BIRBasicBlock observeEndBB;
                    boolean isRemote = callIns.calleeFlags.contains(Flag.REMOTE);
                    DiagnosticPos originalInsPos = callIns.pos;
                    if (isErrorAssignable(callIns.lhsOp.variableDcl)) {
                        BIRBasicBlock errorCheckBB = insertBasicBlock(func, i + 3);
                        BIRBasicBlock errorReportBB = insertBasicBlock(func, i + 4);
                        observeEndBB = insertBasicBlock(func, i + 5);

                        injectStartCallableObservationCall(observeStartBB, desugaredInsPosition,
                                isRemote, false, false, objectTypeOperand, action, pkg,
                                originalInsPos);
                        injectCheckErrorCalls(errorCheckBB, errorReportBB, observeEndBB, func.localVars,
                                desugaredInsPosition, callIns.lhsOp, INVOCATION_INSTRUMENTATION_TYPE);
                        injectReportErrorCall(errorReportBB, func.localVars, desugaredInsPosition, callIns.lhsOp,
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

                        injectStartCallableObservationCall(observeStartBB, desugaredInsPosition,
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
                     * the invocation.
                     */
                    Optional<BIRErrorEntry> existingEE = func.errorTable.stream()
                            .filter(errorEntry -> isBBCoveredInErrorEntry(errorEntry, func.basicBlocks, newCurrentBB))
                            .findAny();
                    DiagnosticPos desugaredInsPos = callIns.pos;
                    if (existingEE.isPresent()) {
                        BIRErrorEntry errorEntry = existingEE.get();
                        int eeTargetIndex = func.basicBlocks.indexOf(errorEntry.targetBB);
                        if (eeTargetIndex == -1) {
                            throw new BLangCompilerException("Invalid Error Entry pointing to non-existent " +
                                    "target Basic Block " + errorEntry.targetBB.id);
                        }

                        BIRBasicBlock errorReportBB = insertBasicBlock(func, eeTargetIndex + 1);
                        BIRBasicBlock observeEndBB = insertBasicBlock(func, eeTargetIndex + 2);
                        BIRBasicBlock newTargetBB = insertBasicBlock(func, eeTargetIndex + 3);
                        swapBasicBlockContent(errorEntry.targetBB, newTargetBB);

                        injectCheckErrorCalls(errorEntry.targetBB, errorReportBB, newTargetBB, func.localVars,
                                desugaredInsPos, errorEntry.errorOp, INVOCATION_INSTRUMENTATION_TYPE);
                        injectReportErrorCall(errorReportBB, func.localVars, desugaredInsPos, errorEntry.errorOp,
                                INVOCATION_INSTRUMENTATION_TYPE);
                        injectStopObservationCall(observeEndBB, desugaredInsPos);

                        // Fix the Basic Blocks links
                        errorReportBB.terminator.thenBB = observeEndBB;
                        observeEndBB.terminator.thenBB = newTargetBB;
                        fixErrorTable(func, errorEntry.targetBB, newTargetBB);
                    } else {
                        BIRBasicBlock errorCheckBB = insertBasicBlock(func, newCurrentIndex + 1);
                        BIRBasicBlock errorReportBB = insertBasicBlock(func, newCurrentIndex + 2);
                        BIRBasicBlock observeEndBB = insertBasicBlock(func, newCurrentIndex + 3);
                        BIRBasicBlock rePanicBB = insertBasicBlock(func, newCurrentIndex + 4);

                        BIRVariableDcl trappedErrorVariableDcl = new BIRVariableDcl(symbolTable.errorType,
                                new Name(String.format("$%s$trappedError", newCurrentBB.id.value)),
                                VarScope.FUNCTION, VarKind.TEMP);
                        func.localVars.add(trappedErrorVariableDcl);
                        BIROperand trappedErrorOperand = new BIROperand(trappedErrorVariableDcl);

                        injectCheckErrorCalls(errorCheckBB, errorReportBB, newCurrentBB.terminator.thenBB,
                                func.localVars, newCurrentBB.terminator.pos, trappedErrorOperand,
                                INVOCATION_INSTRUMENTATION_TYPE);
                        injectReportErrorCall(errorReportBB, func.localVars, newCurrentBB.terminator.pos,
                                trappedErrorOperand, INVOCATION_INSTRUMENTATION_TYPE);
                        injectStopObservationCall(observeEndBB, newCurrentBB.terminator.pos);
                        rePanicBB.terminator = new Panic(newCurrentBB.terminator.pos, trappedErrorOperand);

                        BIRErrorEntry errorEntry = new BIRErrorEntry(newCurrentBB, newCurrentBB,
                                trappedErrorOperand, errorCheckBB);
                        func.errorTable.add(errorEntry);

                        // Fix the Basic Blocks links
                        newCurrentBB.terminator.thenBB = errorCheckBB;
                        errorReportBB.terminator.thenBB = observeEndBB;
                        observeEndBB.terminator.thenBB = rePanicBB;
                        i += 4; // Number of inserted BBs
                    }
                }
            }
            i += 1;
        }
    }

    /**
     * Inject start observation call to a basic block.
     * @param observeStartBB The basic block to which the start observation call should be injected
     * @param serviceName The service to which the instruction was attached to
     * @param resource The name of the resource which will be observed
     * @param pkg The package the invocation belongs to
     * @param originalInsPosition The source code position of the invocation
     */
    private void injectStartResourceObservationCall(BIRBasicBlock observeStartBB, String serviceName, String resource,
                                                    BIRPackage pkg, DiagnosticPos originalInsPosition) {
        String pkgId = generatePackageId(pkg);
        String position = generatePositionId(originalInsPosition);

        BIROperand serviceNameOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, serviceName);
        BIROperand resourceOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, resource);
        BIROperand pkgOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, pkgId);
        BIROperand originalInsPosOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, position);

        JIMethodCall observeStartCallTerminator = new JIMethodCall(null);
        observeStartCallTerminator.invocationType = INVOKESTATIC;
        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
        observeStartCallTerminator.jMethodVMSig = String.format("(L%s;L%s;L%s;L%s;)V", B_STRING_VALUE, B_STRING_VALUE,
                                                                B_STRING_VALUE, B_STRING_VALUE);
        observeStartCallTerminator.name = START_RESOURCE_OBSERVATION_METHOD;
        observeStartCallTerminator.args = Arrays.asList(serviceNameOperand, resourceOperand, pkgOperand,
                originalInsPosOperand);
        observeStartBB.terminator = observeStartCallTerminator;
    }

    /**
     * Inject start observation call to a basic block.
     *
     * @param observeStartBB The basic block to which the start observation call should be injected
     * @param desugaredInsPos The position of all instructions, variables declarations, terminators to be generated
     * @param isRemote True if a remote function will be observed by the observation
     * @param isMainEntryPoint True if the main function will be observed by the observation
     * @param isWorker True if a worker function will be observed by the observation
     * @param objectOperand The object the function was attached to
     * @param action The name of the action which will be observed
     * @param pkg The package the invocation belongs to
     * @param originalInsPosition The source code position of the invocation
     */
    private void injectStartCallableObservationCall(BIRBasicBlock observeStartBB, DiagnosticPos desugaredInsPos,
                                                    boolean isRemote, boolean isMainEntryPoint, boolean isWorker,
                                                    BIROperand objectOperand, String action, BIRPackage pkg,
                                                    DiagnosticPos originalInsPosition) {
        String pkgId = generatePackageId(pkg);
        String position = generatePositionId(originalInsPosition);

        BIROperand isRemoteOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType, isRemote);
        BIROperand isMainEntryPointOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType,
                isMainEntryPoint);
        BIROperand isWorkerOperand = generateGlobalConstantOperand(pkg, symbolTable.booleanType, isWorker);
        BIROperand pkgOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, pkgId);
        BIROperand originalInsPosOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, position);
        BIROperand actionOperand = generateGlobalConstantOperand(pkg, symbolTable.stringType, action);

        JIMethodCall observeStartCallTerminator = new JIMethodCall(desugaredInsPos);
        observeStartCallTerminator.invocationType = INVOKESTATIC;
        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
        observeStartCallTerminator.jMethodVMSig = String.format("(ZZZL%s;L%s;L%s;L%s;)V", B_OBJECT, B_STRING_VALUE,
                                                                B_STRING_VALUE, B_STRING_VALUE);
        observeStartCallTerminator.name = START_CALLABLE_OBSERVATION_METHOD;
        observeStartCallTerminator.args = Arrays.asList(isRemoteOperand, isMainEntryPointOperand, isWorkerOperand,
                objectOperand, actionOperand, pkgOperand, originalInsPosOperand);
        observeStartBB.terminator = observeStartCallTerminator;
    }

    /**
     * Inject branch condition for checking if a value is an error.
     *
     * @param errorCheckBB The basic block to which the error check should be injected
     * @param isErrorBB The basic block to which errors should go to
     * @param noErrorBB The basic block to which no errors should go to
     * @param scopeVarList The variables list in the scope
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param valueOperand Operand for passing the value which should be checked if it is an error
     * @param uniqueId A unique ID to identify the check error call
     */
    private void injectCheckErrorCalls(BIRBasicBlock errorCheckBB, BIRBasicBlock isErrorBB, BIRBasicBlock noErrorBB,
                                       Collection<BIRVariableDcl> scopeVarList, DiagnosticPos pos,
                                       BIROperand valueOperand, String uniqueId) {
        BIRVariableDcl isErrorVariableDcl = new BIRVariableDcl(symbolTable.booleanType,
                new Name(String.format("$%s$%s$isError", uniqueId, errorCheckBB.id.value)), VarScope.FUNCTION,
                VarKind.TEMP);
        scopeVarList.add(isErrorVariableDcl);
        BIROperand isErrorOperand = new BIROperand(isErrorVariableDcl);
        TypeTest errorTypeTestInstruction = new TypeTest(pos, symbolTable.errorType, isErrorOperand, valueOperand);
        errorCheckBB.instructions.add(errorTypeTestInstruction);
        errorCheckBB.terminator = new Branch(pos, isErrorOperand, isErrorBB, noErrorBB);
    }

    /**
     * Inject report error call.
     *
     * @param errorReportBB The basic block to which the report error call should be injected
     * @param scopeVarList The variables list in the scope
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param errorOperand Operand for passing the error
     * @param uniqueId A unique ID to identify the check error call
     */
    private void injectReportErrorCall(BIRBasicBlock errorReportBB, Collection<BIRVariableDcl> scopeVarList,
                                       DiagnosticPos pos, BIROperand errorOperand, String uniqueId) {
        BIRVariableDcl castedErrorVariableDcl = new BIRVariableDcl(symbolTable.errorType,
                new Name(String.format("$%s$%s$castedError", uniqueId, errorReportBB.id.value)), VarScope.FUNCTION,
                VarKind.TEMP);
        scopeVarList.add(castedErrorVariableDcl);
        BIROperand castedErrorOperand = new BIROperand(castedErrorVariableDcl);
        TypeCast errorCastInstruction = new TypeCast(pos, castedErrorOperand, errorOperand, symbolTable.errorType,
                false);
        errorReportBB.instructions.add(errorCastInstruction);

        JIMethodCall reportErrorCallTerminator = new JIMethodCall(pos);
        reportErrorCallTerminator.invocationType = INVOKESTATIC;
        reportErrorCallTerminator.jClassName = OBSERVE_UTILS;
        reportErrorCallTerminator.jMethodVMSig = String.format("(L%s;)V", ERROR_VALUE);
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
    private void injectStopObservationCall(BIRBasicBlock observeEndBB, DiagnosticPos pos) {
        JIMethodCall observeEndCallTerminator = new JIMethodCall(pos);
        observeEndCallTerminator.invocationType = INVOKESTATIC;
        observeEndCallTerminator.jClassName = OBSERVE_UTILS;
        observeEndCallTerminator.jMethodVMSig = "()V";
        observeEndCallTerminator.name = STOP_OBSERVATION_METHOD;
        observeEndCallTerminator.args = Collections.emptyList();
        observeEndBB.terminator = observeEndCallTerminator;
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
            PackageID pkgId = new PackageID(pkg.org, pkg.name, pkg.version);
            BIRGlobalVariableDcl constLoadVariableDcl =
                    new BIRGlobalVariableDcl(COMPILE_TIME_CONST_POS, 0,
                                             constantType, pkgId, new Name("$observabilityConst" + constantIndex++),
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
     * @param firstBB The first BB of which content should end up in second BB
     * @param secondBB The second BB of which content should end up in first BB
     */
    private void swapBasicBlockContent(BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        List<BIRNonTerminator> firstBBInstructions = firstBB.instructions;
        firstBB.instructions = secondBB.instructions;
        secondBB.instructions = firstBBInstructions;
        swapBasicBlockTerminator(firstBB, secondBB);
    }

    /**
     * Swap the terminators of two basic blocks.
     *
     * @param firstBB The first BB of which terminator should end up in second BB
     * @param secondBB The second BB of which terminator should end up in first BB
     */
    private void swapBasicBlockTerminator(BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        BIRTerminator firstBBTerminator = firstBB.terminator;
        firstBB.terminator = secondBB.terminator;
        secondBB.terminator = firstBBTerminator;
    }

    /**
     * Fix the ending BB of error entries in the error table of a function.
     *
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
                    JvmCodeGenUtil.getPackageName(annot.packageID.orgName, annot.packageID.name, Names.EMPTY) +
                            annot.annotTagRef.value)) {
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
            isErrorAssignable = returnUnionType.getMemberTypes().stream()
                    .anyMatch(type -> type instanceof BErrorType);
        } else if (variableDcl.type instanceof BErrorType) {
            isErrorAssignable = true;
        }
        return isErrorAssignable;
    }

    /**
     * Check if a basic block is covered in a error entry.
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
             * Traverse in the same way JvmMethodGen.generateBasicBlocks traverses through basic blocks to generate
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
     * Remove the additional prefixes and postfixes added by the compiler.
     * This is done to get the original name used by the developer.
     *
     * @param serviceName The service name to be cleaned up
     * @return The cleaned up service name which should be equal to the name given by the developer
     */
    private String cleanUpServiceName(String serviceName) {
        if (serviceName.contains(SERVICE_IDENTIFIER)) {
            if (serviceName.contains(ANONYMOUS_SERVICE_IDENTIFIER)) {
                return serviceName.replace(SERVICE_IDENTIFIER, "_");
            } else {
                return serviceName.substring(0, serviceName.lastIndexOf(SERVICE_IDENTIFIER));
            }
        }
        return serviceName;
    }

    /**
     * Generate a ID for a source code position.
     *
     * @param pos The position for which the ID should be generated
     * @return The generated ID
     */
    private String generatePositionId(DiagnosticPos pos) {
        return String.format("%s:%d:%d", pos.src.cUnitName, pos.sLine + 1, pos.sCol + 1);
    }

    /**
     * Generate a ID for a ballerina module.
     *
     * @param pkg The module for which the ID should be generated
     * @return The generated ID
     */
    private String generatePackageId(BIRPackage pkg) {
        return String.format("%s/%s:%s", pkg.org.value, pkg.name.value, pkg.version.value);
    }
}
