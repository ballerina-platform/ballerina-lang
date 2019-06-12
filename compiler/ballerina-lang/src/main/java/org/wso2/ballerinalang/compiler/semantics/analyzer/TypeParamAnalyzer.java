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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeParamType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        TypeParamAndBoundType result = new TypeParamAndBoundType();
        findTypeParamAndItsBoundType(expType, actualType, env, result);
        if (result.newEntry) {
            env.typeParamsEntries.add(new SymbolEnv.TypeParamEntry(result.typeParamType, result.actualBoundType));
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

    private static class TypeParamAndBoundType {

        BTypeParamType typeParamType;
        BType boundType;
        BType actualBoundType;
        boolean newEntry = false;
    }

    private void findTypeParamAndItsBoundType(BType expType, BType actualType, SymbolEnv env,
                                              TypeParamAndBoundType result) {

        // Finding TypePram and its bound type require, both has to be same structure.
        if (actualType.tag == TypeTags.SEMANTIC_ERROR) {
            // Do Nothing.
            return;
        } else if (expType.tag == TypeTags.TYPE_PARAM) {
            updateFindTypeParamAndBoundTypeResult(env, (BTypeParamType) expType, actualType, result);
            // Bound type is a structure. Visit recursively to find bound type.
        } else if (expType.tag == TypeTags.ARRAY) {
            if (actualType.tag == TypeTags.ARRAY) {
                findTypeParamAndItsBoundType(((BArrayType) expType).eType, ((BArrayType) actualType).eType, env,
                        result);
            }
        } else if (expType.tag == TypeTags.MAP) {
            if (actualType.tag == TypeTags.MAP) {
                findTypeParamAndItsBoundType(((BMapType) expType).constraint, ((BMapType) actualType).constraint, env,
                        result);
            }
        } else if (expType.tag == TypeTags.TUPLE) {
            BTupleType expectedTupleType = (BTupleType) expType;
            if (actualType.tag == TypeTags.TUPLE
                    && expectedTupleType.tupleTypes.size() == ((BTupleType) actualType).tupleTypes.size()) {
                BTupleType givenTupleType = (BTupleType) actualType;
                for (int i = 0; i < expectedTupleType.tupleTypes.size(); i++) {
                    findTypeParamAndItsBoundType(expectedTupleType.tupleTypes.get(i), givenTupleType.tupleTypes.get(i),
                            env, result);
                }
            }
        } else {

            // TODO: Add support for other types and remove BLangCompilerException from logic.
            throw new BLangCompilerException("Unsupported type param");
        }
    }

    private void updateFindTypeParamAndBoundTypeResult(SymbolEnv env, BTypeParamType typeParamType,
                                                       BType actualBoundType, TypeParamAndBoundType result) {

        Optional<SymbolEnv.TypeParamEntry> typeParamEntry = env.typeParamsEntries.stream().filter(entry ->
                entry.typeParam.tsymbol.pkgID.equals(typeParamType.tsymbol.pkgID) &&
                        entry.typeParam.name.equals(typeParamType.name)).findAny();
        result.newEntry = !typeParamEntry.isPresent();
        result.boundType = typeParamEntry.map(paramEntry -> paramEntry.boundType).orElse(actualBoundType);
        result.actualBoundType = actualBoundType;
        result.typeParamType = typeParamType;
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
            List<BType> tupleTypes = new ArrayList<>();
            ((BTupleType) expType).tupleTypes.forEach(type -> tupleTypes.add(getMatchingBoundType(type, env)));
            return new BTupleType(tupleTypes);
        } else {
            return expType;
        }
    }
}
