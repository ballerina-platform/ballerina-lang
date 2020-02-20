/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupFunctionName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunction;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunctionParam;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getVariableDcl;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextVarId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator.toNameString;
import static org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Branch;
import static org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.GOTO;


//import ballerina/bir;

public class JvmDesugarPhase {

    public static void addDefaultableBooleanVarsToSignature(@Nilable BIRFunction func) {
        BIRFunction currentFunc = getFunction(func);
        currentFunc.type = new BInvokableType(currentFunc.type.paramTypes, currentFunc.type.restType, currentFunc.type.retType, currentFunc.type.tsymbol);
        BInvokableType type = currentFunc.type;
        currentFunc.type.paramTypes = updateParamTypesWithDefaultableBooleanVar(currentFunc.type.paramTypes,
                type != null ? type.restType : type);
        int index = 0;
        @Nilable List<BIRVariableDcl> updatedVars = new ArrayList<>();
        @Nilable List<BIRVariableDcl> localVars = currentFunc.localVars;
        int nameIndex = 0;

        for (BIRVariableDcl localVar : localVars) {
            updatedVars.add(index, localVar);
            index += 1;
            if (localVar instanceof BIRFunctionParameter) {
                // An additional boolean arg is gen for each function parameter.
                String argName = "%syn" + nameIndex;
                nameIndex += 1;
                BIRFunctionParameter booleanVar = new BIRFunctionParameter(null, new BType(TypeTags.BOOLEAN, null),
                        new Name(argName), VarScope.FUNCTION, VarKind.ARG, "", false);
                updatedVars.add(index, booleanVar);
                index += 1;
            }
        }
        currentFunc.localVars = updatedVars;
    }

    public static void enrichWithDefaultableParamInits(BIRFunction currentFunc) {

        int k = 1;
        @Nilable List<BIRFunctionParameter> functionParams = new ArrayList<>();
        @Nilable List<BIRVariableDcl> localVars = currentFunc.localVars;
        while (k < localVars.size()) {
            BIRVariableDcl localVar = getVariableDcl(localVars.get(k));
            if (localVar instanceof BIRFunctionParameter) {
                functionParams.add((BIRFunctionParameter) localVar);
            }
            k += 1;
        }

        nextId = -1;
        nextVarId = -1;

        @Nilable List<BIRBasicBlock> basicBlocks = new ArrayList<>();

        BIRBasicBlock nextBB = insertAndGetNextBasicBlock(basicBlocks, "desugaredBB");

        int paramCounter = 0;
        int paramBBCounter = 0;
        DiagnosticPos pos = currentFunc.pos;
        while (paramCounter < functionParams.size()) {
            BIRFunctionParameter funcParam = functionParams.get(paramCounter);
            if (funcParam instanceof BIRFunctionParameter && funcParam.hasDefaultExpr) {
                int boolParam = paramCounter + 1;
                BIRFunctionParameter funcBooleanParam = getFunctionParam(functionParams.get(boolParam));
                BIROperand boolRef = new BIROperand(funcBooleanParam);
                UnaryOP notOp = new UnaryOP(pos, InstructionKind.NOT, boolRef, boolRef);
                nextBB.instructions.add(notOp);
                @Nilable List<BIRBasicBlock> bbArray = currentFunc.parameters.get(funcParam);
                BIRBasicBlock trueBB = getBasicBlock(bbArray.get(0));
                for (BIRBasicBlock defaultBB : bbArray) {
                    basicBlocks.add(getBasicBlock(defaultBB));
                }
                BIRBasicBlock falseBB = insertAndGetNextBasicBlock(basicBlocks, "desugaredBB");
                Branch branch = new Branch(pos, boolRef, trueBB, falseBB);
                nextBB.terminator = branch;

                BIRBasicBlock lastBB = getBasicBlock(bbArray.get(bbArray.size() - 1));
                GOTO gotoRet = new GOTO(pos, falseBB);
                lastBB.terminator = gotoRet;

                nextBB = falseBB;

                paramBBCounter += 1;
            }
            paramCounter += 2;
        }

        if (basicBlocks.size() == 1) {
            // this means only one block added, if there are default vars, there must be more than one block
            return;
        }
        if (currentFunc.basicBlocks.size() == 0) {
            currentFunc.basicBlocks = basicBlocks;
            return;
        }

        int pl = currentFunc.basicBlocks.size();
        BIRBasicBlock firstBB = getBasicBlock(currentFunc.basicBlocks.get(0));

        GOTO gotoRet = new GOTO(pos, firstBB);
        nextBB.terminator = gotoRet;
        for (BIRBasicBlock bb : currentFunc.basicBlocks) {
            basicBlocks.add(bb);
        }

        int nl = basicBlocks.size();
        currentFunc.basicBlocks = basicBlocks;
    }

    public static BIRBasicBlock insertAndGetNextBasicBlock(@Nilable List<BIRBasicBlock> basicBlocks, String prefix /* = "desugaredBB" */) {
        BIRBasicBlock nextbb = new BIRBasicBlock(getNextDesugarBBId(prefix));
        basicBlocks.add(nextbb);
        return nextbb;
    }

    public static Name getNextDesugarBBId(String prefix) {
        String bbIdPrefix = prefix;
        nextId += 1;
        return new Name(bbIdPrefix + nextId);
    }

    static @Nilable
    List<BType> updateParamTypesWithDefaultableBooleanVar(@Nilable List<BType> funcParams, @Nilable BType restType) {
        @Nilable List<BType> paramTypes = new ArrayList<>();

        int counter = 0;
        int index = 0;
        // Update the param types to add boolean variables to indicate if the previous variable contains a user given value
        int size = funcParams == null ? 0 :funcParams.size();
        while (true) {
            if (!(counter < size)) break;
            paramTypes.add(index, funcParams.get(counter));
            paramTypes.add(index + 1, new BType(TypeTags.BOOLEAN, null));
            index += 2;
            counter += 1;
        }
        if (!(restType == null)) {
            paramTypes.add(index, restType);
            paramTypes.add(index + 1, new BType(TypeTags.BOOLEAN, null));
        }
        return paramTypes;
    }

    public static void rewriteRecordInits(@Nilable List<BIRTypeDefinition> typeDefs) {
        for (@Nilable BIRTypeDefinition typeDef : typeDefs) {
            @Nilable BType recordType = typeDef.type;
            if (recordType.tag == TypeTags.RECORD) {
                @Nilable List<BIRFunction> attachFuncs = typeDef.attachedFuncs;
                for (BIRFunction func : attachFuncs) {
                    rewriteRecordInitFunction(func, (BRecordType) recordType);
                }
            }
        }
    }

    static void rewriteRecordInitFunction(BIRFunction func, BRecordType recordType) {
        BIRVariableDcl receiver = (BIRVariableDcl) func.receiver;

        // Rename the function name by appending the record name to it.
        // This done to avoid frame class name overlappings.
        func.name = new Name(cleanupFunctionName(toNameString(recordType) + func.name.value));

        // change the kind of receiver to 'ARG'
        receiver.kind = VarKind.ARG;

        // Update the name of the reciever. Then any instruction that was refering to the receiver will
        // now refer to the injected parameter.
        String paramName = "$_" + receiver.name.value;
        receiver.name = new Name(paramName);

        // Inject an additional parameter to accept the self-record value into the init function
        BIRFunctionParameter selfParam = new BIRFunctionParameter(null, receiver.type, receiver.name,
                receiver.scope, VarKind.ARG, paramName, false);

        func.type = new BInvokableType(Collections.singletonList(receiver.type), func.type.restType, func.type.retType
                , null);
        func.type.paramTypes = Collections.singletonList(receiver.type);

        @Nilable List<BIRVariableDcl> localVars = func.localVars;
        @Nilable List<BIRVariableDcl> updatedLocalVars = new ArrayList<>();
        updatedLocalVars.add(localVars.get(0));
        updatedLocalVars.add(selfParam);
        int index = 1;
        while (index < localVars.size()) {
            updatedLocalVars.add(localVars.get(index));
            index += 1;
        }
        func.localVars = updatedLocalVars;
    }
}