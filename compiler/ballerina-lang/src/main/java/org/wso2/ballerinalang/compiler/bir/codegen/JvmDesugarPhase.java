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
import org.ballerinalang.compiler.BLangCompilerException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;

/**
 * BIR desugar phase related methods at JVM code generation.
 *
 * @since 1.2.0
 */
public class JvmDesugarPhase {

    public static void addDefaultableBooleanVarsToSignature(BIRFunction func, BType booleanType) {

        func.type = new BInvokableType(func.type.paramTypes, func.type.restType,
                                       func.type.retType, func.type.tsymbol);
        BInvokableType type = func.type;
        func.type.paramTypes = updateParamTypesWithDefaultableBooleanVar(func.type.paramTypes,
                                                                         type.restType, booleanType);
    }

    public static void enrichWithDefaultableParamInits(BIRFunction currentFunc, InitMethodGen initMethodGen) {
        int k = 1;
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
                                                           String prefix, InitMethodGen initMethodGen) {
        BIRBasicBlock nextbb = new BIRBasicBlock(getNextDesugarBBId(prefix, initMethodGen));
        basicBlocks.add(nextbb);
        return nextbb;
    }

    public static Name getNextDesugarBBId(String prefix, InitMethodGen initMethodGen) {
        int nextId = initMethodGen.incrementAndGetNextId();
        return new Name(prefix + nextId);
    }

    private static List<BType> updateParamTypesWithDefaultableBooleanVar(List<BType> funcParams, BType restType,
                                                                         BType booleanType) {

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
            BType recordType = JvmCodeGenUtil.getReferredType(typeDef.type);
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
                                                                  receiver.scope, VarKind.ARG, paramName, false);

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

    private static BIRFunctionParameter getFunctionParam(BIRFunctionParameter localVar) {
        if (localVar == null) {
            throw new BLangCompilerException("Invalid function parameter");
        }

        return localVar;
    }

    private JvmDesugarPhase() {
    }

    static HashMap<String, String> encodeModuleIdentifiers(BIRNode.BIRPackage module) {
        HashMap<String, String> encodedVsInitialIds = new HashMap<>();
        encodePackageIdentifiers(module.packageID, encodedVsInitialIds);
        encodeGlobalVariableIdentifiers(module.globalVars, encodedVsInitialIds);
        encodeImportedGlobalVariableIdentifiers(module.importedGlobalVarsDummyVarDcls, encodedVsInitialIds);
        encodeFunctionIdentifiers(module.functions, encodedVsInitialIds);
        encodeTypeDefIdentifiers(module.typeDefs, encodedVsInitialIds);
        return encodedVsInitialIds;
    }

    private static void encodePackageIdentifiers(PackageID packageID, HashMap<String, String> encodedVsInitialIds) {
        packageID.orgName = Names.fromString(encodeNonFunctionIdentifier(packageID.orgName.value,
                                                                         encodedVsInitialIds));
        packageID.name = Names.fromString(encodeNonFunctionIdentifier(packageID.name.value, encodedVsInitialIds));
    }

    private static void encodeTypeDefIdentifiers(List<BIRTypeDefinition> typeDefs,
                                                 HashMap<String, String> encodedVsInitialIds) {
        for (BIRTypeDefinition typeDefinition : typeDefs) {
            typeDefinition.type.tsymbol.name = Names.fromString(encodeNonFunctionIdentifier(
                    typeDefinition.type.tsymbol.name.value, encodedVsInitialIds));
            typeDefinition.internalName =
                    Names.fromString(encodeNonFunctionIdentifier(typeDefinition.internalName.value,
                            encodedVsInitialIds));
            if (typeDefinition.referenceType != null) {
                typeDefinition.referenceType.tsymbol.name = Names.fromString(encodeNonFunctionIdentifier(
                        typeDefinition.referenceType.tsymbol.name.value, encodedVsInitialIds));
            }

            encodeFunctionIdentifiers(typeDefinition.attachedFuncs, encodedVsInitialIds);
            BType bType = JvmCodeGenUtil.getReferredType(typeDefinition.type);
            if (bType.tag == TypeTags.OBJECT) {
                BObjectType objectType = (BObjectType) bType;
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bType.tsymbol;
                if (objectTypeSymbol.attachedFuncs != null) {
                    encodeAttachedFunctionIdentifiers(objectTypeSymbol.attachedFuncs, encodedVsInitialIds);
                }
                for (BField field : objectType.fields.values()) {
                    field.name = Names.fromString(encodeNonFunctionIdentifier(field.name.value, encodedVsInitialIds));
                }
            }
            if (bType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) bType;
                for (BField field : recordType.fields.values()) {
                    field.name = Names.fromString(encodeNonFunctionIdentifier(field.name.value, encodedVsInitialIds));
                }
            }
        }
    }

    private static void encodeFunctionIdentifiers(List<BIRFunction> functions,
                                                  HashMap<String, String> encodedVsInitialIds) {
        for (BIRFunction function : functions) {
            function.name = Names.fromString(encodeFunctionIdentifier(function.name.value, encodedVsInitialIds));
            for (BIRNode.BIRVariableDcl localVar : function.localVars) {
                if (localVar.metaVarName == null) {
                    continue;
                }
                localVar.metaVarName = encodeNonFunctionIdentifier(localVar.metaVarName, encodedVsInitialIds);
            }
            for (BIRNode.BIRParameter parameter : function.requiredParams) {
                if (parameter.name == null) {
                    continue;
                }
                parameter.name = Names.fromString(encodeNonFunctionIdentifier(parameter.name.value,
                                                                              encodedVsInitialIds));
            }
            encodeDefaultFunctionName(function.type, encodedVsInitialIds);
            encodeWorkerName(function, encodedVsInitialIds);
        }
    }

    private static void encodeDefaultFunctionName(BInvokableType type, HashMap<String, String> encodedVsInitialIds) {
        BInvokableTypeSymbol typeSymbol = (BInvokableTypeSymbol) type.tsymbol;
        if (typeSymbol == null) {
            return;
        }
        for (BInvokableSymbol defaultFunc : typeSymbol.defaultValues.values()) {
            defaultFunc.name = Names.fromString(encodeFunctionIdentifier(defaultFunc.name.value,
                    encodedVsInitialIds));
        }
    }

    private static void encodeWorkerName(BIRFunction function,
                                         HashMap<String, String> encodedVsInitialIds) {
        if (function.workerName != null) {
            function.workerName = Names.fromString(encodeNonFunctionIdentifier(function.workerName.value,
                                                                               encodedVsInitialIds));
        }
    }

    private static void encodeAttachedFunctionIdentifiers(List<BAttachedFunction> functions,
                                                          HashMap<String, String> encodedVsInitialIds) {
        for (BAttachedFunction function : functions) {
            function.funcName = Names.fromString(encodeFunctionIdentifier(function.funcName.value,
                                                                          encodedVsInitialIds));
        }
    }

    private static void encodeGlobalVariableIdentifiers(List<BIRNode.BIRGlobalVariableDcl> globalVars,
                                                        HashMap<String, String> encodedVsInitialIds) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            if (globalVar == null) {
                continue;
            }
            globalVar.name = Names.fromString(encodeNonFunctionIdentifier(globalVar.name.value, encodedVsInitialIds));
        }
    }

    private static void encodeImportedGlobalVariableIdentifiers(Set<BIRNode.BIRGlobalVariableDcl> globalVars,
                                                                HashMap<String, String> encodedVsInitialIds) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            globalVar.name = Names.fromString(encodeNonFunctionIdentifier(globalVar.name.value, encodedVsInitialIds));
        }
    }

    // Replace encoding identifiers
    static void replaceEncodedModuleIdentifiers(BIRNode.BIRPackage module,
                                                HashMap<String, String> encodedVsInitialIds) {
        replaceEncodedPackageIdentifiers(module.packageID, encodedVsInitialIds);
        replaceEncodedGlobalVariableIdentifiers(module.globalVars, encodedVsInitialIds);
        replaceEncodedImportedGlobalVariableIdentifiers(module.importedGlobalVarsDummyVarDcls, encodedVsInitialIds);
        replaceEncodedFunctionIdentifiers(module.functions, encodedVsInitialIds);
        replaceEncodedTypeDefIdentifiers(module.typeDefs, encodedVsInitialIds);
    }

    private static void replaceEncodedPackageIdentifiers(PackageID packageID,
                                                         HashMap<String, String> encodedVsInitialIds) {
        packageID.orgName = getInitialIdString(packageID.orgName, encodedVsInitialIds);
        packageID.name = getInitialIdString(packageID.name, encodedVsInitialIds);
    }

    private static void replaceEncodedTypeDefIdentifiers(List<BIRTypeDefinition> typeDefs,
                                                         HashMap<String, String> encodedVsInitialIds) {
        for (BIRTypeDefinition typeDefinition : typeDefs) {
            typeDefinition.type.tsymbol.name = getInitialIdString(typeDefinition.type.tsymbol.name,
                    encodedVsInitialIds);
            typeDefinition.internalName = getInitialIdString(typeDefinition.internalName, encodedVsInitialIds);
            replaceEncodedFunctionIdentifiers(typeDefinition.attachedFuncs, encodedVsInitialIds);
            BType bType = JvmCodeGenUtil.getReferredType(typeDefinition.type);
            if (bType.tag == TypeTags.OBJECT) {
                BObjectType objectType = (BObjectType) bType;
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bType.tsymbol;
                if (objectTypeSymbol.attachedFuncs != null) {
                    replaceEncodedAttachedFunctionIdentifiers(objectTypeSymbol.attachedFuncs, encodedVsInitialIds);
                }
                for (BField field : objectType.fields.values()) {
                    field.name = getInitialIdString(field.name, encodedVsInitialIds);
                }
            }
            if (bType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) bType;
                for (BField field : recordType.fields.values()) {
                    field.name = getInitialIdString(field.name, encodedVsInitialIds);
                }
            }
        }
    }

    private static void replaceEncodedFunctionIdentifiers(List<BIRFunction> functions,
                                                          HashMap<String, String> encodedVsInitialIds) {
        for (BIRFunction function : functions) {
            function.name = getInitialIdString(function.name, encodedVsInitialIds);
            for (BIRNode.BIRVariableDcl localVar : function.localVars) {
                if (localVar.metaVarName == null) {
                    continue;
                }
                localVar.metaVarName = getInitialIdString(localVar.metaVarName, encodedVsInitialIds);
            }
            for (BIRNode.BIRParameter parameter : function.requiredParams) {
                if (parameter.name == null) {
                    continue;
                }
                parameter.name = getInitialIdString(parameter.name, encodedVsInitialIds);
            }
            replaceEncodedDefaultFunctionName(function.type, encodedVsInitialIds);
            replaceEncodedWorkerName(function, encodedVsInitialIds);
        }
    }

    private static void replaceEncodedDefaultFunctionName(BInvokableType type,
                                                          HashMap<String, String> encodedVsInitialIds) {
        BInvokableTypeSymbol typeSymbol = (BInvokableTypeSymbol) type.tsymbol;
        if (typeSymbol == null) {
            return;
        }
        for (BInvokableSymbol defaultFunc : typeSymbol.defaultValues.values()) {
            defaultFunc.name = getInitialIdString(defaultFunc.name, encodedVsInitialIds);
        }
    }

    private static void replaceEncodedWorkerName(BIRFunction function,
                                                 HashMap<String, String> encodedVsInitialIds) {
        if (function.workerName != null) {
            function.workerName = getInitialIdString(function.workerName, encodedVsInitialIds);
        }
    }

    private static void replaceEncodedAttachedFunctionIdentifiers(List<BAttachedFunction> functions,
                                                                  HashMap<String, String> encodedVsInitialIds) {
        for (BAttachedFunction function : functions) {
            function.funcName = getInitialIdString(function.funcName, encodedVsInitialIds);
        }
    }

    private static void replaceEncodedGlobalVariableIdentifiers(List<BIRNode.BIRGlobalVariableDcl> globalVars,
                                                                HashMap<String, String> encodedVsInitialIds) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            if (globalVar == null) {
                continue;
            }
            globalVar.name = getInitialIdString(globalVar.name, encodedVsInitialIds);
        }
    }

    private static void replaceEncodedImportedGlobalVariableIdentifiers(Set<BIRNode.BIRGlobalVariableDcl> globalVars,
                                                                        HashMap<String, String> encodedVsInitialIds) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            globalVar.name = getInitialIdString(globalVar.name, encodedVsInitialIds);
        }
    }

    private static String encodeFunctionIdentifier(String identifier, HashMap<String, String> encodedVsInitialIds) {
        if (encodedVsInitialIds.containsKey(identifier)) {
            return identifier;
        }
        String encodedString = Utils.encodeFunctionIdentifier(identifier);
        encodedVsInitialIds.putIfAbsent(encodedString, identifier);
        return encodedString;
    }

    private static String encodeNonFunctionIdentifier(String identifier, HashMap<String, String> encodedVsInitialIds) {
        if (encodedVsInitialIds.containsKey(identifier)) {
            return identifier;
        }
        String encodedString = Utils.encodeNonFunctionIdentifier(identifier);
        encodedVsInitialIds.putIfAbsent(encodedString, identifier);
        return encodedString;
    }

    private static String getInitialIdString(String encodedIdString, HashMap<String, String> encodedVsInitialIds) {
        String initialString = encodedVsInitialIds.get(encodedIdString);
        if (initialString != null) {
            return initialString;
        }
        return encodedIdString;
    }

    private static Name getInitialIdString(Name encodedIdString, HashMap<String, String> encodedVsInitialIds) {
        String initialString = encodedVsInitialIds.get(encodedIdString.value);
        if (initialString != null) {
            return Names.fromString(initialString);
        }
        return encodedIdString;
    }
}
