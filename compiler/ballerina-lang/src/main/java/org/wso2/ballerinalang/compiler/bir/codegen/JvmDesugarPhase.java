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

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.InitMethodGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WRAPPER_GEN_BB_ID_NAME;

/**
 * BIR desugar phase related methods at JVM code generation.
 *
 * @since 1.2.0
 */
public class JvmDesugarPhase {

    private JvmDesugarPhase() {}

    public static void addDefaultableBooleanVarsToSignature(BIRFunction func) {
        func.type = new BInvokableType(func.type.paramTypes, func.type.restType, func.type.retType, func.type.tsymbol);
        BInvokableType type = func.type;
        func.type.paramTypes = updateParamTypesWithDefaultableBooleanVar(func.type.paramTypes, type.restType);
    }

    public static void enrichWithDefaultableParamInits(BIRFunction currentFunc, InitMethodGen initMethodGen) {
        int k = 1;
        // Contents of collection 'functionParams' are updated, but never queried
        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        List<BIRVariableDcl> localVars = currentFunc.localVars;
        while (k < localVars.size()) {
            BIRVariableDcl localVar = localVars.get(k);
            if (localVar instanceof BIRFunctionParameter) {
                functionParams.add((BIRFunctionParameter) localVar);
            }
            k += 1;
        }
        initMethodGen.resetIds();
    }

    public static BIRBasicBlock insertAndGetNextBasicBlock(List<BIRBasicBlock> basicBlocks,
                                                           InitMethodGen initMethodGen) {
        BIRBasicBlock nextbb = new BIRBasicBlock(WRAPPER_GEN_BB_ID_NAME, getNextDesugarBBId(initMethodGen));
        basicBlocks.add(nextbb);
        return nextbb;
    }

    public static int getNextDesugarBBId(InitMethodGen initMethodGen) {
        return initMethodGen.incrementAndGetNextId();
    }

    private static List<BType> updateParamTypesWithDefaultableBooleanVar(List<BType> funcParams, BType restType) {
        List<BType> paramTypes = new ArrayList<>();
        int counter = 0;
        int size = funcParams == null ? 0 : funcParams.size();
        while (counter < size) {
            paramTypes.add(counter, funcParams.get(counter));
            counter += 1;
        }
        if (restType != null) {
            paramTypes.add(counter, restType);
        }
        return paramTypes;
    }

    static void rewriteRecordInits(List<BIRTypeDefinition> typeDefs) {
        for (BIRTypeDefinition typeDef : typeDefs) {
            BType recordType = JvmCodeGenUtil.getImpliedType(typeDef.type);
            if (recordType.tag != TypeTags.RECORD) {
                continue;
            }
            List<BIRFunction> attachFuncs = typeDef.attachedFuncs;
            for (BIRFunction func : attachFuncs) {
                rewriteRecordInitFunction(func, (BRecordType) recordType);
            }
        }
    }

    private static void rewriteRecordInitFunction(BIRFunction func, BRecordType recordType) {

        BIRVariableDcl receiver = func.receiver;

        // Rename the function name by appending the record name to it.
        // This done to avoid frame class name overlapping.
        func.name = new Name(toNameString(recordType) + func.name.value);

        // change the kind of receiver to 'ARG'
        receiver.kind = VarKind.ARG;

        // Update the name of the reciever. Then any instruction that was refering to the receiver will
        // now refer to the injected parameter.
        String paramName = "$_" + receiver.name.value;
        receiver.name = new Name(paramName);

        // Inject an additional parameter to accept the self-record value into the init function
        BIRFunctionParameter selfParam = new BIRFunctionParameter(null, receiver.type, receiver.name,
                receiver.scope, VarKind.ARG, paramName, false, false);

        List<BType> updatedParamTypes = Lists.of(receiver.type);
        updatedParamTypes.addAll(func.type.paramTypes);
        func.type = new BInvokableType(updatedParamTypes, func.type.restType, func.type.retType, null);

        List<BIRVariableDcl> localVars = func.localVars;
        List<BIRVariableDcl> updatedLocalVars = new ArrayList<>();
        updatedLocalVars.add(localVars.get(0));
        updatedLocalVars.add(selfParam);
        int index = 1;
        while (index < localVars.size()) {
            updatedLocalVars.add(localVars.get(index));
            index += 1;
        }
        func.localVars = updatedLocalVars;
    }

    static void encodeModuleIdentifiers(BIRNode.BIRPackage module) {
        encodePackageIdentifiers(module.packageID);
        encodeGlobalVariableIdentifiers(module.globalVars);
        encodeImportedGlobalVariableIdentifiers(module.importedGlobalVarsDummyVarDcls);
        encodeFunctionIdentifiers(module.functions);
        encodeTypeDefIdentifiers(module.typeDefs);
    }

    private static void encodePackageIdentifiers(PackageID packageID) {
        packageID.orgName = encodeNonFunctionIdentifier(packageID.orgName.value);
        packageID.name = encodeNonFunctionIdentifier(packageID.name.value);
    }

    private static void encodeTypeDefIdentifiers(List<BIRTypeDefinition> typeDefs) {
        for (BIRTypeDefinition typeDefinition : typeDefs) {
            Name encodedName = encodeNonFunctionIdentifier(typeDefinition.type.tsymbol.name.value);
            if (typeDefinition.type.tsymbol.name.value.equals(typeDefinition.internalName.value)) {
                typeDefinition.internalName = encodedName;
            } else {
                typeDefinition.internalName =
                        encodeNonFunctionIdentifier(typeDefinition.internalName.value);
            }
            typeDefinition.type.tsymbol.name = encodedName;
            if (typeDefinition.referenceType != null) {
                typeDefinition.referenceType.tsymbol.name = encodeNonFunctionIdentifier(
                        typeDefinition.referenceType.tsymbol.name.value);
            }
            encodeFunctionIdentifiers(typeDefinition.attachedFuncs);
            BType bType = JvmCodeGenUtil.getImpliedType(typeDefinition.type);
            if (bType.tag == TypeTags.OBJECT) {
                BObjectType objectType = (BObjectType) bType;
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bType.tsymbol;
                if (objectTypeSymbol.attachedFuncs != null) {
                    encodeAttachedFunctionIdentifiers(objectTypeSymbol.attachedFuncs);
                }
                for (BField field : objectType.fields.values()) {
                    field.name = encodeNonFunctionIdentifier(field.name.value);
                }
            }
            if (bType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) bType;
                for (BField field : recordType.fields.values()) {
                    field.name = encodeNonFunctionIdentifier(field.name.value);
                }
            }
        }
    }

    private static void encodeFunctionIdentifiers(List<BIRFunction> functions) {
        for (BIRFunction function : functions) {
            function.name = encodeFunctionIdentifier(function.name.value);
            for (BIRNode.BIRVariableDcl localVar : function.localVars) {
                if (localVar.metaVarName == null) {
                    continue;
                }
                localVar.metaVarName = Utils.encodeNonFunctionIdentifier(localVar.metaVarName);
            }
            for (BIRNode.BIRParameter parameter : function.requiredParams) {
                parameter.name = encodeNonFunctionIdentifier(parameter.name.value);
            }
            if (function.type.tsymbol != null) {
                for (BVarSymbol parameter : ((BInvokableTypeSymbol) function.type.tsymbol).params) {
                    parameter.name = encodeNonFunctionIdentifier(parameter.name.value);
                }
            }
            encodeDefaultFunctionName(function.type);
            encodeWorkerName(function);
        }
    }

    private static void encodeDefaultFunctionName(BInvokableType type) {
        BInvokableTypeSymbol typeSymbol = (BInvokableTypeSymbol) type.tsymbol;
        if (typeSymbol == null) {
            return;
        }
        for (BInvokableSymbol defaultFunc : typeSymbol.defaultValues.values()) {
            defaultFunc.name = encodeFunctionIdentifier(defaultFunc.name.value);
        }
    }

    private static void encodeWorkerName(BIRFunction function) {
        if (function.workerName != null) {
            function.workerName = encodeNonFunctionIdentifier(function.workerName.value);
        }
    }

    private static void encodeAttachedFunctionIdentifiers(List<BAttachedFunction> functions) {
        for (BAttachedFunction function : functions) {
            function.funcName = encodeFunctionIdentifier(function.funcName.value);
        }
    }

    private static void encodeGlobalVariableIdentifiers(List<BIRNode.BIRGlobalVariableDcl> globalVars) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            // globalVar should not be null, check and remove this
            if (globalVar == null) {
                continue;
            }
            globalVar.name = encodeNonFunctionIdentifier(globalVar.name.value);
        }
    }

    private static void encodeImportedGlobalVariableIdentifiers(Set<BIRNode.BIRGlobalVariableDcl> globalVars) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            globalVar.name = encodeNonFunctionIdentifier(globalVar.name.value);
        }
    }

    private static Name encodeFunctionIdentifier(String identifier) {
        return Names.fromString(Utils.encodeFunctionIdentifier(identifier));
    }

    private static Name encodeNonFunctionIdentifier(String identifier) {
        return Names.fromString(Utils.encodeNonFunctionIdentifier(identifier));
    }
}
