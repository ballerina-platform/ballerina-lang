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
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JavaClass;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.InitMethodGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.containsDefaultableField;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getReferredType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ZERO_VALUE_RECORDS__CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getFunctionWrapper;

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

    static void rewriteRecordInits(BIRNode.BIRPackage module, InitMethodGen initMethodGen, SymbolTable symbolTable,
                                   Map<String, BIRFunctionWrapper> birFunctionMap, String initClass,
                                   Map<String, JavaClass> jvmClassMapping) {
        for (BIRTypeDefinition typeDef : module.typeDefs) {
            BType recordType = getReferredType(typeDef.type);
            if (recordType.tag != TypeTags.RECORD) {
                continue;
            }
            if (containsDefaultableField((BRecordType) recordType)) {
                BIRFunction zeroValueFunction =
                        getRecordInitFunction((BRecordType) recordType, module, symbolTable, initMethodGen);
                module.functions.add(zeroValueFunction);
                String pkgName = JvmCodeGenUtil.getPackageName(module.packageID);
                String className = JvmCodeGenUtil.getModuleLevelClassName(module.packageID, MODULE_ZERO_VALUE_RECORDS__CLASS_NAME);
                birFunctionMap.put(pkgName + zeroValueFunction.name.value,
                        getFunctionWrapper(zeroValueFunction, module.packageID, className));
                JavaClass javaClass = jvmClassMapping.get(className);
                if (javaClass != null) {
                    javaClass.functions.add(zeroValueFunction);
                } else {
                    javaClass = new JavaClass(className);
                    javaClass.functions.add(zeroValueFunction);
                    jvmClassMapping.put(className, javaClass);
                }
            }
        }
    }

    private static BIRFunction getRecordInitFunction(BRecordType recordType, BIRNode.BIRPackage module,
                                                     SymbolTable symbolTable, InitMethodGen initMethodGen) {

        BIRNode.BIRVariableDcl retVar =
                new BIRNode.BIRVariableDcl(null, recordType, new Name("%returnVar"), VarScope.FUNCTION, VarKind.RETURN,
                        "");
        BIROperand retVarRef = new BIROperand(retVar);
        BIRNode.BIRVariableDcl recVar =
                new BIRNode.BIRVariableDcl(null, recordType, new Name("%recVar"), VarScope.FUNCTION, VarKind.LOCAL,
                        "");
        BIROperand recVarRef = new BIROperand(recVar);

        BTypedescType typedescType =
                new BTypedescType(BUnionType.create(null, symbolTable.anyType, symbolTable.errorType), null);
        BIRNode.BIRVariableDcl typeDescVar =
                new BIRNode.BIRVariableDcl(null, typedescType, new Name("%typeDescVar"), VarScope.FUNCTION,
                        VarKind.TEMP, "");
        BIROperand typeDescVarRef = new BIROperand(recVar);

        BInvokableType funcType = new BInvokableType(Collections.emptyList(), null, recordType, null);
        BIRNode.BIRFunction zeroValueFunc =
                new BIRNode.BIRFunction(null, new Name("$init_" + recordType.tsymbol.name.getValue()), 0,
                        funcType, null, 0, SymbolOrigin.VIRTUAL);
        zeroValueFunc.localVars.add(retVar);
        zeroValueFunc.localVars.add(recVar);
        zeroValueFunc.localVars.add(typeDescVar);
        BIRNode.BIRBasicBlock prevBB = initMethodGen.addAndGetNextBasicBlock(zeroValueFunc);
        prevBB.instructions.add(
                new BIRNonTerminator.NewTypeDesc(null, typeDescVarRef, recordType, Collections.emptyList()));
        Map<String, BInvokableSymbol> defaultValues = ((BRecordTypeSymbol) recordType.tsymbol).defaultValues;
        List<BIRNode.BIRMappingConstructorEntry> initialValues = new ArrayList<>();
        for (BField field : recordType.fields.values()) {
            if (field.symbol.isDefaultable) {
                BIRNode.BIRVariableDcl keyVar =
                        new BIRNode.BIRVariableDcl(null, symbolTable.stringType, new Name("%name_" + field.name),
                                VarScope.FUNCTION, VarKind.TEMP, "");
                BIROperand keyVarRef = new BIROperand(keyVar);
                prevBB.instructions.add(
                        new BIRNonTerminator.ConstantLoad(null, field.name.value, symbolTable.stringType,
                                keyVarRef));
                BIRNode.BIRVariableDcl fieldVar =
                        new BIRNode.BIRVariableDcl(null, field.type, new Name("%field_" + field.name),
                                VarScope.FUNCTION,
                                VarKind.TEMP, "");
                BIROperand fieldVarRef = new BIROperand(fieldVar);
                zeroValueFunc.localVars.add(keyVar);
                zeroValueFunc.localVars.add(fieldVar);

                BIRNode.BIRMappingConstructorKeyValueEntry initialValue =
                        new BIRNode.BIRMappingConstructorKeyValueEntry(keyVarRef, fieldVarRef);
                initialValues.add(initialValue);
                BInvokableSymbol defaultFunc = defaultValues.get(field.symbol.name.value);
//                BIRNode.BIRGlobalVariableDcl defaultFuncVar = getDefaultFuncFPGlobalVar(defaultFunc.name, module.globalVars);
//                BIROperand defaultFP = new BIROperand(defaultFuncVar);
//                boolean workerDerivative = Symbols.isFlagOn(defaultFunc.flags, Flags.WORKER);
                BIRNode.BIRBasicBlock nextBB = initMethodGen.addAndGetNextBasicBlock(zeroValueFunc);
                prevBB.terminator =
                        new BIRTerminator.Call(null, InstructionKind.CALL, false, module.packageID, defaultFunc.name,
                                Collections.emptyList(), fieldVarRef, nextBB, Collections.emptyList(),
                                defaultFunc.getFlags());

                prevBB = nextBB;
            }
        }
        BIRNonTerminator.NewStructure newStructure =
                new BIRNonTerminator.NewStructure(null, recVarRef, typeDescVarRef, initialValues);

        prevBB.instructions.add(newStructure);
        prevBB.instructions.add(new BIRNonTerminator.Move(null, recVarRef, retVarRef));
        prevBB.terminator = new BIRTerminator.Return(null);
        return zeroValueFunc;
    }

    private static BIRNode.BIRGlobalVariableDcl getDefaultFuncFPGlobalVar(Name name,
                                                                          List<BIRNode.BIRGlobalVariableDcl> globalVars) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            if (globalVar.name.value.equals(name.value)) {
                return globalVar;
            }
        }
        throw new BLangCompilerException("Cannot find global var for default value function: " + name);
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
                Map<String, BInvokableSymbol> defaultValues = ((BRecordTypeSymbol) recordType.tsymbol).defaultValues;
                for (BInvokableSymbol methodSymbol : defaultValues.values()) {
                    methodSymbol.name =
                            Names.fromString(encodeFunctionIdentifier(methodSymbol.name.value, encodedVsInitialIds));
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
            BType bType = getReferredType(typeDefinition.type);
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
                Map<String, BInvokableSymbol> defaultValues = ((BRecordTypeSymbol) recordType.tsymbol).defaultValues;
                for (BInvokableSymbol methodSymbol : defaultValues.values()) {
                    methodSymbol.name = getInitialIdString(methodSymbol.name, encodedVsInitialIds);
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
