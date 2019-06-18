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

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeParamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
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
    }

    void checkForTypeParams(BType actualType, SymbolEnv env, BType expType) {
        // Not a langlib module invocation
        if (env.typeParamsEntries == null) {
            return;
        }
        boolean foundAnyTypeParam = findTypeParamAndBoundType(expType, actualType, env);
        if (!foundAnyTypeParam) {
            return;
        }
        // Now construct matching type and type check with that.
        types.checkType(env.node.pos, getMatchingBoundType(expType, env), actualType,
                DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    BType getReturnTypeParams(SymbolEnv env, BType expType) {

        if (env.typeParamsEntries == null) {
            return expType;
        }
        return getMatchingBoundType(expType, env);
    }

    // Private methods.

    private boolean findTypeParamAndBoundType(BType expType, BType actualType, SymbolEnv env) {

        // Finding TypePram and its bound type require, both has to be same structure.
        if (expType.tag == TypeTags.TYPE_PARAM) {
            updateTypeParamAndBoundType(env, (BTypeParamType) expType, actualType);
            return true;
            // Bound type is a structure. Visit recursively to find bound type.
        } else if (expType.tag == TypeTags.ARRAY && actualType.tag == TypeTags.ARRAY) {
            return findTypeParamAndBoundType(((BArrayType) expType).eType, ((BArrayType) actualType).eType, env);
        } else if (expType.tag == TypeTags.MAP && actualType.tag == TypeTags.MAP) {
            return findTypeParamAndBoundType(((BMapType) expType).constraint, ((BMapType) actualType).constraint, env);
        } else if (expType.tag == TypeTags.TUPLE && actualType.tag == TypeTags.TUPLE) {
            return findTypeParamInTupleMembers((BTupleType) expType, (BTupleType) actualType, env);
        } else if (expType.tag == TypeTags.RECORD && actualType.tag == TypeTags.RECORD) {
            return findTypeParamInRecordField((BRecordType) expType, (BRecordType) actualType, env);
        } else if (expType.tag == TypeTags.INVOKABLE && actualType.tag == TypeTags.INVOKABLE) {
            return findTypeParamInFunctionSignature((BInvokableType) expType, (BInvokableType) actualType, env);
        } else if (expType.tag == TypeTags.OBJECT && actualType.tag == TypeTags.OBJECT) {
            return findTypeParamInObject((BObjectType) expType, (BObjectType) actualType, env);
        } else if (expType.tag == TypeTags.UNION && actualType.tag == TypeTags.UNION) {
            return findTypeParamInUnion((BUnionType) expType, (BUnionType) actualType, env);
        }
        return false;
    }

    private void updateTypeParamAndBoundType(SymbolEnv env, BTypeParamType typeParamType, BType boundType) {

        if (env.typeParamsEntries.stream()
                .noneMatch(entry -> entry.typeParam.tsymbol.pkgID.equals(typeParamType.tsymbol.pkgID)
                        && entry.typeParam.name.equals(typeParamType.name))) {
            env.typeParamsEntries.add(new SymbolEnv.TypeParamEntry(typeParamType, boundType));
        }
    }

    private boolean findTypeParamInTupleMembers(BTupleType expType, BTupleType actualType, SymbolEnv env) {

        boolean found = false;
        for (int i = 0; i < expType.tupleTypes.size() && i < actualType.tupleTypes.size(); i++) {
            found |= findTypeParamAndBoundType(expType.tupleTypes.get(i), actualType.tupleTypes.get(i), env);
        }
        return found;
    }

    private boolean findTypeParamInRecordField(BRecordType expType, BRecordType actualType, SymbolEnv env) {

        boolean found = false;
        for (BField exField : expType.fields) {
            BType actualFieldType = actualType.fields.stream()
                    .filter(acField -> exField.name.equals(acField.name))
                    .findFirst()
                    .map(acField -> acField.type).orElse(null);
            if (actualFieldType == null) {
                // This is an error, which is logged already.
                continue;
            }
            found |= findTypeParamAndBoundType(exField.type, actualFieldType, env);
        }
        return found;
    }

    private boolean findTypeParamInFunctionSignature(BInvokableType expType, BInvokableType actualType, SymbolEnv env) {

        boolean found = false;
        for (int i = 0; i < expType.paramTypes.size() && i < actualType.paramTypes.size(); i++) {
            found |= findTypeParamAndBoundType(expType.paramTypes.get(i), actualType.paramTypes.get(i), env);
        }
        found |= findTypeParamAndBoundType(expType.retType, actualType.retType, env);
        return found;
    }

    private boolean findTypeParamInObject(BObjectType expType, BObjectType actualType, SymbolEnv env) {

        boolean found = false;
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
            found |= findTypeParamAndBoundType(exField.type, actualFieldType, env);
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
            found |= findTypeParamInFunctionSignature(expFunc.type, actFuncType, env);
        }
        return found;
    }

    private boolean findTypeParamInUnion(BUnionType expType, BUnionType actualType, SymbolEnv env) {
        // Limitation : supports only optional types and depends to given order.
        if ((expType.getMemberTypes().size() != 2) || !expType.isNullable()
                || (actualType.getMemberTypes().size() != 2) || !actualType.isNullable()) {
            return false;
        }
        BType exp = expType.getMemberTypes().stream()
                .filter(type -> type != symTable.nilType).findFirst().orElse(symTable.nilType);
        BType act = actualType.getMemberTypes().stream()
                .filter(type -> type != symTable.nilType).findFirst().orElse(symTable.nilType);
        return findTypeParamAndBoundType(exp, act, env);
    }

    private BType getMatchingBoundType(BType expType, SymbolEnv env) {

        if (expType.tag == TypeTags.TYPE_PARAM) {
            return env.typeParamsEntries.stream().filter(typeParamEntry -> typeParamEntry.typeParam == expType)
                    .findFirst()
                    .map(typeParamEntry -> typeParamEntry.boundType)
                    .orElse(expType);
        } else if (expType.tag == TypeTags.ARRAY) {
            BType elementType = ((BArrayType) expType).eType;
            return new BArrayType(getMatchingBoundType(elementType, env));
        } else if (expType.tag == TypeTags.MAP) {
            BType constraint = ((BMapType) expType).constraint;
            return new BMapType(TypeTags.MAP, getMatchingBoundType(constraint, env), symTable.mapType.tsymbol);
        } else if (expType.tag == TypeTags.TUPLE) {
            return getMatchBoundTypeForTuple((BTupleType) expType, env);
        } else if (expType.tag == TypeTags.RECORD) {
            return getMatchingBoundTypeForRecord((BRecordType) expType, env);
        } else if (expType.tag == TypeTags.INVOKABLE) {
            return getMatchingBoundTypeForFunctions((BInvokableType) expType, env);
        } else if (expType.tag == TypeTags.OBJECT) {
            return getMatchingBoundTypeForObject((BObjectType) expType, env);
        } else if (expType.tag == TypeTags.UNION) {
            return getMatchingBoundTypeForOptional((BUnionType) expType, env);
        }
        return expType;
    }

    private BTupleType getMatchBoundTypeForTuple(BTupleType expType, SymbolEnv env) {

        List<BType> tupleTypes = new ArrayList<>();
        expType.tupleTypes.forEach(type -> tupleTypes.add(getMatchingBoundType(type, env)));
        return new BTupleType(tupleTypes);
    }

    private BRecordType getMatchingBoundTypeForRecord(BRecordType expType, SymbolEnv env) {

        BRecordTypeSymbol expTSymbol = (BRecordTypeSymbol) expType.tsymbol;
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(expTSymbol.flags, expTSymbol.name,
                env.enclPkg.packageID, null, expType.tsymbol.scope.owner);
        recordSymbol.scope = new Scope(recordSymbol);
        recordSymbol.initializerFunc = expTSymbol.initializerFunc;

        List<BField> fields = new ArrayList<>();
        for (BField expField : expType.fields) {
            BField field = new BField(expField.name, expField.pos,
                    new BVarSymbol(0, expField.name, env.enclPkg.packageID,
                            getMatchingBoundType(expField.type, env), env.scope.owner));
            fields.add(field);
            recordSymbol.scope.define(expField.name, field.symbol);
        }

        BRecordType bRecordType = new BRecordType(recordSymbol);
        bRecordType.fields = fields;
        recordSymbol.type = bRecordType;

        if (expType.sealed) {
            bRecordType.sealed = true;
        }
        bRecordType.restFieldType = getMatchingBoundType(expType.restFieldType, env);

        return bRecordType;
    }

    private BInvokableType getMatchingBoundTypeForFunctions(BInvokableType expType, SymbolEnv env) {

        List<BType> paramTypes = expType.paramTypes.stream()
                .map(type -> getMatchingBoundType(type, env))
                .collect(Collectors.toList());
        return new BInvokableType(paramTypes, getMatchingBoundType(expType.retType, env), null);
    }

    private BType getMatchingBoundTypeForObject(BObjectType expType, SymbolEnv env) {

        BObjectTypeSymbol actObjectSymbol = (BObjectTypeSymbol) Symbols.createObjectSymbol(0, expType.tsymbol.name,
                env.enclPkg.packageID, null, expType.tsymbol.scope.owner);
        BObjectType objectType = new BObjectType(actObjectSymbol);
        actObjectSymbol.type = objectType;
        actObjectSymbol.scope = new Scope(actObjectSymbol);
        actObjectSymbol.methodScope = new Scope(actObjectSymbol);

        for (BField expField : expType.fields) {
            BField field = new BField(expField.name, expField.pos,
                    new BVarSymbol(expField.symbol.flags, expField.name, env.enclPkg.packageID,
                            getMatchingBoundType(expField.type, env), env.scope.owner));
            objectType.fields.add(field);
            objectType.tsymbol.scope.define(expField.name, field.symbol);
        }

        for (BAttachedFunction expFunc : ((BObjectTypeSymbol) expType.tsymbol).attachedFuncs) {
            BInvokableType matchType = getMatchingBoundTypeForFunctions(expFunc.type, env);
            BInvokableSymbol invokableSymbol = new BInvokableSymbol(expFunc.symbol.tag, expFunc.symbol.flags,
                    expFunc.symbol.name, env.enclPkg.packageID, matchType, env.scope.owner);
            actObjectSymbol.attachedFuncs.add(new BAttachedFunction(expFunc.funcName, invokableSymbol, matchType));
            actObjectSymbol.methodScope.define(expFunc.funcName, invokableSymbol);
        }

        return objectType;
    }

    private BType getMatchingBoundTypeForOptional(BUnionType expType, SymbolEnv env) {

        if (!expType.isNullable() && expType.getMemberTypes().size() != 2) {
            return expType;
        }
        LinkedHashSet<BType> members = new LinkedHashSet<>();
        expType.getMemberTypes()
                .forEach(type -> members.add(getMatchingBoundType(type, env)));
        return BUnionType.create(null, members);
    }
}
