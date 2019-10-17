/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.Name;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class consists of utility methods which operate on TypeParams (Parametric types).
 *
 * @since JB 1.0.0
 */
public class TypeParamAnalyzer {

    // How @typeParam works in 2019R2 spec.
    //
    // e.g. lang.array module.
    //
    // @typeParam
    // type Type1 any|error;
    //
    // public function getFirstAndSize(Type1[] array) returns [Type1,int] {
    //  return [array[0], array.length()];
    // }

    private static final CompilerContext.Key<TypeParamAnalyzer> TYPE_PARAM_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private Types types;
    private BLangDiagnosticLog dlog;

    public static TypeParamAnalyzer getInstance(CompilerContext context) {

        TypeParamAnalyzer types = context.get(TYPE_PARAM_ANALYZER_KEY);
        if (types == null) {
            types = new TypeParamAnalyzer(context);
        }

        return types;
    }

    private TypeParamAnalyzer(CompilerContext context) {

        context.put(TYPE_PARAM_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    static boolean isTypeParam(BType expType) {

        return Symbols.isFlagOn(expType.flags, Flags.TYPE_PARAM)
                || (expType.tsymbol != null && Symbols.isFlagOn(expType.tsymbol.flags, Flags.TYPE_PARAM));
    }

    public static boolean containsTypeParam(BType type) {

        return containsTypeParam(type, new HashSet<>());
    }

    void checkForTypeParamsInArg(BType actualType, SymbolEnv env, BType expType) {

        if (actualType == null) {
            // This is added to prevent compiler panic. Ideally every invocation node should have a type. But,
            // StreamTypeChecker skips some validation, which leads to actualType == null.
            // TODO: Fix this properly. issue #18363
            return;
        }
        // Not a langlib module invocation
        if (notRequireTypeParams(env)) {
            return;
        }

        FindTypeParamResult findTypeParamResult = new FindTypeParamResult();
        findTypeParam(expType, actualType, env, new HashSet<>(), findTypeParamResult);
    }

    boolean notRequireTypeParams(SymbolEnv env) {

        return env.typeParamsEntries == null;
    }

    BType getReturnTypeParams(SymbolEnv env, BType expType) {

        if (notRequireTypeParams(env) || env.typeParamsEntries.isEmpty()) {
            return expType;
        }
        return getMatchingBoundType(expType, env);
    }

    public BType getNominalType(BType type, Name name, int flag) {
        // Only type params has nominal behaviour for now.
        if (name == Names.EMPTY) {
            return type;
        }
        return createBuiltInType(type, name, flag);
    }

    BType createTypeParam(BType type, Name name) {

        int flag = type.flags | Flags.TYPE_PARAM;
        return createBuiltInType(type, name, flag);
    }

    BType getMatchingBoundType(BType expType, SymbolEnv env) {

        return getMatchingBoundType(expType, env, new HashSet<>());
    }

    // Private methods.

    private static boolean containsTypeParam(BType type, HashSet<BType> resolvedTypes) {

        if (isTypeParam(type)) {
            return true;
        }
        if (resolvedTypes.contains(type)) {
            return false;
        }
        resolvedTypes.add(type);
        switch (type.tag) {
            case TypeTags.ARRAY:
                return containsTypeParam(((BArrayType) type).eType, resolvedTypes);
            case TypeTags.TUPLE:
                BTupleType bTupleType = (BTupleType) type;
                for (BType member : bTupleType.tupleTypes) {
                    if (containsTypeParam(member, resolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.MAP:
                return containsTypeParam(((BMapType) type).constraint, resolvedTypes);
            case TypeTags.STREAM:
                return containsTypeParam(((BStreamType) type).constraint, resolvedTypes);
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) type;
                for (BField field : recordType.fields) {
                    BType bFieldType = field.getType();
                    if (containsTypeParam(bFieldType, resolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.INVOKABLE:
                BInvokableType invokableType = (BInvokableType) type;
                for (BType paramType : invokableType.paramTypes) {
                    if (containsTypeParam(paramType, resolvedTypes)) {
                        return true;
                    }
                }
                return containsTypeParam(invokableType.retType, resolvedTypes);
            case TypeTags.OBJECT:
                if (type instanceof BServiceType) {
                    return false;
                }

                BObjectType objectType = (BObjectType) type;
                for (BField field : objectType.fields) {
                    BType bFieldType = field.getType();
                    if (containsTypeParam(bFieldType, resolvedTypes)) {
                        return true;
                    }
                }
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) objectType.tsymbol;
                for (BAttachedFunction fuc : objectTypeSymbol.attachedFuncs) {
                    if (containsTypeParam(fuc.type, resolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) type;
                for (BType bType : unionType.getMemberTypes()) {
                    if (containsTypeParam(bType, resolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.ERROR:
                BErrorType errorType = (BErrorType) type;
                return containsTypeParam(errorType.reasonType, resolvedTypes)
                        || containsTypeParam(errorType.detailType, resolvedTypes);
            case TypeTags.TYPEDESC:
                return containsTypeParam(((BTypedescType) type).constraint, resolvedTypes);
            default:
                return false;
        }
    }

    private BType createBuiltInType(BType type, Name name, int flag) {
        // Handle built-in types.
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.BOOLEAN:
                return new BType(type.tag, null, name, flag);
            case TypeTags.ANY:
                return new BAnyType(type.tag, null, name, flag);
            case TypeTags.ANYDATA:
                return new BAnydataType(type.tag, null, name, flag);
        }
        // For others, we will use TSymbol.
        return type;
    }

    private void findTypeParam(BType expType, BType actualType, SymbolEnv env, HashSet<BType> resolvedTypes,
                               FindTypeParamResult result) {
        findTypeParam(expType, actualType, env, resolvedTypes, result, false);
    }

    private void findTypeParam(BType expType, BType actualType, SymbolEnv env, HashSet<BType> resolvedTypes,
                               FindTypeParamResult result, boolean checkContravariance) {

        if (resolvedTypes.contains(expType)) {
            return;
        }
        resolvedTypes.add(expType);
        // Finding TypePram and its bound type require, both has to be same structure.
        if (isTypeParam(expType)) {
            updateTypeParamAndBoundType(env, expType, actualType, result);

            // If type param discovered before, now type check with actual type. It has to be matched.

            if (checkContravariance) {
                types.checkType(env.node.pos, getMatchingBoundType(expType, env, new HashSet<>()), actualType,
                                DiagnosticCode.INCOMPATIBLE_TYPES);
            } else {
                types.checkType(env.node.pos, actualType, getMatchingBoundType(expType, env, new HashSet<>()),
                                DiagnosticCode.INCOMPATIBLE_TYPES);
            }
            return;
        }
        // Bound type is a structure. Visit recursively to find bound type.
        switch (expType.tag) {
            case TypeTags.ARRAY:
                if (actualType.tag == TypeTags.ARRAY) {
                    findTypeParam(((BArrayType) expType).eType, ((BArrayType) actualType).eType, env,
                            resolvedTypes, result);
                }
                if (actualType.tag == TypeTags.TUPLE) {
                    findTypeParamInTupleForArray((BArrayType) expType, (BTupleType) actualType, env, resolvedTypes,
                                                 result);
                }
                return;
            case TypeTags.MAP:
                if (actualType.tag == TypeTags.MAP) {
                    findTypeParam(((BMapType) expType).constraint, ((BMapType) actualType).constraint, env,
                            resolvedTypes, result);
                }
                if (actualType.tag == TypeTags.RECORD) {
                    findTypeParamInMapForRecord((BMapType) expType, (BRecordType) actualType, env, resolvedTypes,
                                                result);
                }
                return;
            case TypeTags.STREAM:
                if (actualType.tag == TypeTags.STREAM) {
                    findTypeParam(((BStreamType) expType).constraint, ((BStreamType) actualType).constraint, env,
                                  resolvedTypes, result);
                }
                return;
            case TypeTags.TUPLE:
                if (actualType.tag == TypeTags.TUPLE) {
                    findTypeParamInTuple((BTupleType) expType, (BTupleType) actualType, env, resolvedTypes, result);
                }
                return;
            case TypeTags.RECORD:
                if (actualType.tag == TypeTags.RECORD) {
                    findTypeParamInRecord((BRecordType) expType, (BRecordType) actualType, env, resolvedTypes, result);
                }
                return;
            case TypeTags.INVOKABLE:
                if (actualType.tag == TypeTags.INVOKABLE) {
                    findTypeParamInInvokableType((BInvokableType) expType, (BInvokableType) actualType, env,
                            resolvedTypes, result);
                }
                return;
            case TypeTags.OBJECT:
                if (actualType.tag == TypeTags.OBJECT && !(actualType instanceof BServiceType)) {
                    findTypeParamInObject((BObjectType) expType, (BObjectType) actualType, env, resolvedTypes, result);
                }
                return;
            case TypeTags.UNION:
                if (actualType.tag == TypeTags.UNION) {
                    findTypeParamInUnion((BUnionType) expType, (BUnionType) actualType, env, resolvedTypes, result);
                }
                return;
            case TypeTags.ERROR:
                if (actualType.tag == TypeTags.ERROR) {
                    findTypeParamInError((BErrorType) expType, (BErrorType) actualType, env, resolvedTypes, result);
                }
                if (actualType.tag == TypeTags.UNION && types.isSubTypeOfBaseType(actualType, TypeTags.ERROR)) {
                    findTypeParamInError((BErrorType) expType, symTable.errorType, env, resolvedTypes, result);
                }
                return;
            case TypeTags.TYPEDESC:
                if (actualType.tag == TypeTags.TYPEDESC) {
                    findTypeParam(((BTypedescType) expType).constraint, ((BTypedescType) actualType).constraint, env,
                            resolvedTypes, result);
                }
                return;
        }
    }

    private void updateTypeParamAndBoundType(SymbolEnv env, BType typeParamType, BType boundType,
                                             FindTypeParamResult result) {

        if (env.typeParamsEntries.stream()
                .noneMatch(entry -> entry.typeParam.tsymbol.pkgID.equals(typeParamType.tsymbol.pkgID)
                        && entry.typeParam.tsymbol.name.equals(typeParamType.tsymbol.name))) {
            if (boundType == symTable.noType) {
                dlog.error(env.node.pos, DiagnosticCode.CANNOT_INFER_TYPE);
                return;
            }
            env.typeParamsEntries.add(new SymbolEnv.TypeParamEntry(typeParamType, boundType));
        }
    }

    private void findTypeParamInTuple(BTupleType expType, BTupleType actualType, SymbolEnv env,
                                      HashSet<BType> resolvedTypes, FindTypeParamResult result) {

        for (int i = 0; i < expType.tupleTypes.size() && i < actualType.tupleTypes.size(); i++) {
            findTypeParam(expType.tupleTypes.get(i), actualType.tupleTypes.get(i), env, resolvedTypes, result);
        }
    }

    private void findTypeParamInTupleForArray(BArrayType expType, BTupleType actualType, SymbolEnv env,
                                              HashSet<BType> resolvedTypes, FindTypeParamResult result) {
        LinkedHashSet<BType> tupleTypes = new LinkedHashSet<>(actualType.tupleTypes);
        if (actualType.restType != null) {
            tupleTypes.add(actualType.restType);
        }
        BUnionType tupleElementType = BUnionType.create(null, tupleTypes);
        findTypeParam(expType.eType, tupleElementType, env, resolvedTypes, result);
    }

    private void findTypeParamInRecord(BRecordType expType, BRecordType actualType, SymbolEnv env,
                                       HashSet<BType> resolvedTypes, FindTypeParamResult result) {

        for (BField exField : expType.fields) {
            BType actualFieldType = actualType.fields.stream()
                    .filter(acField -> exField.name.equals(acField.name))
                    .findFirst()
                    .map(acField -> acField.type).orElse(null);
            if (actualFieldType == null) {
                // This is an error, which is logged already.
                continue;
            }
            findTypeParam(exField.type, actualFieldType, env, resolvedTypes, result);
        }
    }

    private void findTypeParamInMapForRecord(BMapType expType, BRecordType actualType, SymbolEnv env,
                                             HashSet<BType> resolvedTypes, FindTypeParamResult result) {
        LinkedHashSet<BType> fields = actualType.fields.stream().map(f -> f.type)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        LinkedHashSet<BType> reducedTypeSet;
        BType commonFieldType;

        if (actualType.restFieldType != symTable.noType) {
            reducedTypeSet = new LinkedHashSet<>();
            for (BType fType : fields) {
                if (!types.isAssignable(fType, actualType.restFieldType)) {
                    reducedTypeSet.add(fType);
                }
            }
            reducedTypeSet.add(actualType.restFieldType);
        } else {
            // TODO: 7/16/19 Handle cases where there may be multiple field types which are assignable to another
            //  field type: https://github.com/ballerina-platform/ballerina-lang/issues/16824
            reducedTypeSet = fields;
        }

        if (reducedTypeSet.size() == 1) {
            commonFieldType = reducedTypeSet.iterator().next();
        } else {
            commonFieldType = BUnionType.create(null, reducedTypeSet);
        }

        findTypeParam(expType.constraint, commonFieldType, env, resolvedTypes, result);
    }

    private void findTypeParamInInvokableType(BInvokableType expType, BInvokableType actualType, SymbolEnv env,
                                              HashSet<BType> resolvedTypes, FindTypeParamResult result) {

        for (int i = 0; i < expType.paramTypes.size() && i < actualType.paramTypes.size(); i++) {
            findTypeParam(expType.paramTypes.get(i), actualType.paramTypes.get(i), env, resolvedTypes, result, true);
        }
        findTypeParam(expType.retType, actualType.retType, env, resolvedTypes, result);
    }

    private void findTypeParamInObject(BObjectType expType, BObjectType actualType, SymbolEnv env,
                                       HashSet<BType> resolvedTypes, FindTypeParamResult result) {

        // Not needed now.
        for (BField exField : expType.fields) {
            BType actualFieldType = actualType.fields.stream()
                    .filter(acField -> exField.name.equals(acField.name))
                    .findFirst()
                    .map(acField -> acField.type).orElse(null);
            if (actualFieldType == null) {
                // This is an error, which is logged already.
                continue;
            }
            findTypeParam(exField.type, actualFieldType, env, resolvedTypes, result);
        }
        List<BAttachedFunction> expAttFunctions = ((BObjectTypeSymbol) expType.tsymbol).attachedFuncs;
        List<BAttachedFunction> actualAttFunctions = ((BObjectTypeSymbol) actualType.tsymbol).attachedFuncs;

        for (BAttachedFunction expFunc : expAttFunctions) {
            BInvokableType actFuncType = actualAttFunctions.stream()
                    .filter(actFunc -> actFunc.funcName.equals(expFunc.funcName))
                    .findFirst()
                    .map(actFunc -> actFunc.type).orElse(null);
            if (actFuncType == null) {
                continue;
            }
            findTypeParamInInvokableType(expFunc.type, actFuncType, env, resolvedTypes, result);
        }
    }

    private void findTypeParamInUnion(BUnionType expType, BUnionType actualType, SymbolEnv env,
                                      HashSet<BType> resolvedTypes, FindTypeParamResult result) {
        // Limitation : supports only optional types and depends to given order.
        if ((expType.getMemberTypes().size() != 2) || !expType.isNullable()
                || (actualType.getMemberTypes().size() != 2) || !actualType.isNullable()) {
            return;
        }
        BType exp = expType.getMemberTypes().stream()
                .filter(type -> type != symTable.nilType).findFirst().orElse(symTable.nilType);
        BType act = actualType.getMemberTypes().stream()
                .filter(type -> type != symTable.nilType).findFirst().orElse(symTable.nilType);
        findTypeParam(exp, act, env, resolvedTypes, result);
    }

    private void findTypeParamInError(BErrorType expType, BErrorType actualType, SymbolEnv env,
                                      HashSet<BType> resolvedTypes, FindTypeParamResult result) {

        if (expType == symTable.errorType) {
            return;
        }
        findTypeParam(expType.detailType, actualType.detailType, env, resolvedTypes, result);
        findTypeParam(expType.reasonType, actualType.reasonType, env, resolvedTypes, result);
    }

    private BType getMatchingBoundType(BType expType, SymbolEnv env, HashSet<BType> resolvedTypes) {
        if (isTypeParam(expType)) {
            return env.typeParamsEntries.stream().filter(typeParamEntry -> typeParamEntry.typeParam == expType)
                    .findFirst()
                    .map(typeParamEntry -> typeParamEntry.boundType)
                    // Else, this need to be inferred from the context.
                    .orElse(symTable.noType);
        }

        if (resolvedTypes.contains(expType)) {
            return expType;
        }
        resolvedTypes.add(expType);

        switch (expType.tag) {
            case TypeTags.ARRAY:
                BType elementType = ((BArrayType) expType).eType;
                return new BArrayType(getMatchingBoundType(elementType, env, resolvedTypes));
            case TypeTags.MAP:
                BType constraint = ((BMapType) expType).constraint;
                return new BMapType(TypeTags.MAP, getMatchingBoundType(constraint, env, resolvedTypes),
                        symTable.mapType.tsymbol);
            case TypeTags.STREAM:
                BType streamConstraint = ((BStreamType) expType).constraint;
                return new BStreamType(TypeTags.STREAM, getMatchingBoundType(streamConstraint, env, resolvedTypes),
                                       symTable.streamType.tsymbol);
            case TypeTags.TUPLE:
                return getMatchingTupleBoundType((BTupleType) expType, env, resolvedTypes);
            case TypeTags.RECORD:
                return getMatchingRecordBoundType((BRecordType) expType, env, resolvedTypes);
            case TypeTags.INVOKABLE:
                return getMatchingFunctionBoundType((BInvokableType) expType, env, resolvedTypes);
            case TypeTags.OBJECT:
                if (expType instanceof BServiceType) {
                    return expType;
                }
                return getMatchingObjectBoundType((BObjectType) expType, env, resolvedTypes);
            case TypeTags.UNION:
                return getMatchingOptionalBoundType((BUnionType) expType, env, resolvedTypes);
            case TypeTags.ERROR:
                return getMatchingErrorBoundType((BErrorType) expType, env, resolvedTypes);
            case TypeTags.TYPEDESC:
                constraint = ((BTypedescType) expType).constraint;
                return new BTypedescType(getMatchingBoundType(constraint, env, resolvedTypes),
                        symTable.typeDesc.tsymbol);
            default:
                return expType;
        }
    }

    private BTupleType getMatchingTupleBoundType(BTupleType expType, SymbolEnv env, HashSet<BType> resolvedTypes) {

        List<BType> tupleTypes = new ArrayList<>();
        expType.tupleTypes.forEach(type -> tupleTypes.add(getMatchingBoundType(type, env, resolvedTypes)));
        return new BTupleType(tupleTypes);
    }

    private BRecordType getMatchingRecordBoundType(BRecordType expType, SymbolEnv env, HashSet<BType> resolvedTypes) {

        BRecordTypeSymbol expTSymbol = (BRecordTypeSymbol) expType.tsymbol;
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(expTSymbol.flags, expTSymbol.name,
                expTSymbol.pkgID, null, expType.tsymbol.scope.owner);
        recordSymbol.scope = new Scope(recordSymbol);
        recordSymbol.initializerFunc = expTSymbol.initializerFunc;

        List<BField> fields = new ArrayList<>();
        for (BField expField : expType.fields) {
            BField field = new BField(expField.name, expField.pos,
                    new BVarSymbol(0, expField.name, env.enclPkg.packageID,
                            getMatchingBoundType(expField.type, env, resolvedTypes), env.scope.owner));
            fields.add(field);
            recordSymbol.scope.define(expField.name, field.symbol);
        }

        BRecordType bRecordType = new BRecordType(recordSymbol);
        bRecordType.fields = fields;
        recordSymbol.type = bRecordType;

        if (expType.sealed) {
            bRecordType.sealed = true;
        }
        bRecordType.restFieldType = getMatchingBoundType(expType.restFieldType, env, resolvedTypes);

        return bRecordType;
    }

    private BInvokableType getMatchingFunctionBoundType(BInvokableType expType, SymbolEnv env,
                                                        HashSet<BType> resolvedTypes) {

        List<BType> paramTypes = expType.paramTypes.stream()
                .map(type -> getMatchingBoundType(type, env, resolvedTypes))
                .collect(Collectors.toList());
        // TODO: 7/4/19 Set a type symbol for the below type. Otherwise it'll cause problems for functions returning
        //  a function pointer.
        return new BInvokableType(paramTypes, getMatchingBoundType(expType.retType, env, resolvedTypes), null);
    }

    private BType getMatchingObjectBoundType(BObjectType expType, SymbolEnv env, HashSet<BType> resolvedTypes) {

        BObjectTypeSymbol actObjectSymbol = (BObjectTypeSymbol) Symbols.createObjectSymbol(0, expType.tsymbol.name,
                                                                                           expType.tsymbol.pkgID, null,
                                                                                           expType.tsymbol.scope.owner);
        BObjectType objectType = new BObjectType(actObjectSymbol);
        actObjectSymbol.type = objectType;
        actObjectSymbol.scope = new Scope(actObjectSymbol);
        actObjectSymbol.methodScope = new Scope(actObjectSymbol);

        for (BField expField : expType.fields) {
            BField field = new BField(expField.name, expField.pos,
                    new BVarSymbol(expField.symbol.flags, expField.name, env.enclPkg.packageID,
                            getMatchingBoundType(expField.type, env, resolvedTypes), env.scope.owner));
            objectType.fields.add(field);
            objectType.tsymbol.scope.define(expField.name, field.symbol);
        }

        for (BAttachedFunction expFunc : ((BObjectTypeSymbol) expType.tsymbol).attachedFuncs) {
            BInvokableType matchType = getMatchingFunctionBoundType(expFunc.type, env, resolvedTypes);
            BInvokableSymbol invokableSymbol = new BInvokableSymbol(expFunc.symbol.tag, expFunc.symbol.flags,
                    expFunc.symbol.name, env.enclPkg.packageID, matchType, env.scope.owner);
            matchType.tsymbol = Symbols.createTypeSymbol(SymTag.FUNCTION_TYPE, invokableSymbol.flags, Names.EMPTY,
                                                         env.enclPkg.symbol.pkgID, invokableSymbol.type,
                                                         env.scope.owner);
            actObjectSymbol.attachedFuncs.add(new BAttachedFunction(expFunc.funcName, invokableSymbol, matchType));
            actObjectSymbol.methodScope.define(expFunc.funcName, invokableSymbol);
        }

        return objectType;
    }

    private BType getMatchingOptionalBoundType(BUnionType expType, SymbolEnv env, HashSet<BType> resolvedTypes) {

        if (!expType.isNullable() || expType.getMemberTypes().size() != 2) {
            return expType;
        }
        LinkedHashSet<BType> members = new LinkedHashSet<>();
        expType.getMemberTypes()
                .forEach(type -> members.add(getMatchingBoundType(type, env, resolvedTypes)));
        return BUnionType.create(null, members);
    }

    private BType getMatchingErrorBoundType(BErrorType expType, SymbolEnv env, HashSet<BType> resolvedTypes) {

        if (expType == symTable.errorType) {
            return expType;
        }
        BType reasonType = getMatchingBoundType(expType.reasonType, env, resolvedTypes);
        BType detailType = getMatchingBoundType(expType.detailType, env, resolvedTypes);
        BErrorTypeSymbol typeSymbol = new BErrorTypeSymbol(SymTag.ERROR,
                symTable.errorType.tsymbol.flags,
                symTable.errorType.tsymbol.name,
                symTable.errorType.tsymbol.pkgID,
                null, null);
        BErrorType errorType = new BErrorType(typeSymbol, reasonType, detailType);
        typeSymbol.type = errorType;
        return errorType;
    }

    /**
     * Data holder for FindTypeParamResult.
     *
     * @since jb 1.0.0
     */
    private static class FindTypeParamResult {

        boolean found = false;
        boolean isNew = false;
    }

}
