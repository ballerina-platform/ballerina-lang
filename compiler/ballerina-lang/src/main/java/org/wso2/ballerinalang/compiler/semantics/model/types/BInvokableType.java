/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.FunctionDefinition;
import io.ballerina.types.definition.FunctionQualifiers;
import io.ballerina.types.definition.ListDefinition;
import org.ballerinalang.model.types.InvokableType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BInvokableType extends BType implements InvokableType {

    public List<BType> paramTypes;
    // TODO: make these final
    public BType restType;
    public BType retType;
    private final Env env;
    private FunctionDefinition defn;

    public BInvokableType(Env env, List<BType> paramTypes, BType restType, BType retType, BTypeSymbol tsymbol) {
        super(TypeTags.INVOKABLE, tsymbol, Flags.READONLY);
        this.paramTypes = Collections.unmodifiableList(paramTypes);
        assert restType == null || restType instanceof BArrayType || restType.tag == TypeTags.SEMANTIC_ERROR;
        this.restType = restType;
        this.retType = retType;
        this.env = env;
    }

    public BInvokableType(Env env, List<BType> paramTypes, BType retType, BTypeSymbol tsymbol) {
        this(env, paramTypes, null, retType, tsymbol);
    }

    public BInvokableType(Env env, BTypeSymbol tSymbol) {
        super(TypeTags.INVOKABLE, tSymbol, Flags.READONLY);
        this.paramTypes = List.of();
        this.env = env;
    }

    public void addParamType(BType type) {
        List<BType> newParams = new ArrayList<>(paramTypes);
        newParams.add(type);
        paramTypes = Collections.unmodifiableList(newParams);
    }

    public void setParamTypes(List<BType> paramTypes) {
        this.paramTypes = Collections.unmodifiableList(paramTypes);
    }

    @Override
    public List<BType> getParameterTypes() {
        return paramTypes;
    }

    @Override
    public BType getReturnType() {
        return retType;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.FUNCTION;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        String flagStr = "";
        if (Symbols.isFlagOn(getFlags(), Flags.ISOLATED)) {
            flagStr = "isolated ";
        }
        if (Symbols.isFlagOn(getFlags(), Flags.TRANSACTIONAL)) {
            flagStr += "transactional ";
        }
        if (Symbols.isFlagOn(getFlags(), Flags.ANY_FUNCTION)) {
            return flagStr + "function";
        }
        return flagStr + "function " + getTypeSignature();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BInvokableType that)) {
            return false;
        }

        if (this.getFlags() != that.getFlags()) {
            return false;
        }

        if (paramTypes != null ? !paramTypes.equals(that.paramTypes) : that.paramTypes != null) {
            return false;
        }

        if (tsymbol != null ? !tsymbol.equals(that.tsymbol) : that.tsymbol != null) {
            return false;
        }

        if (retType != null ? !retType.equals(that.retType) : that.retType != null) {
            return false;
        }

        return restType != null ? restType.equals(that.restType) : that.restType == null;
    }

    @Override
    public int hashCode() {
        int result = paramTypes != null ? paramTypes.hashCode() : 0;
        result = 31 * result + (retType != null ? retType.hashCode() : 0);
        return result;
    }

    public String getTypeSignature() {
        String retTypeWithParam = "()";
        if (retType != null && retType.getKind() != TypeKind.NIL) {
            retTypeWithParam = "(" + retType.toString() + ")";
        }
        String restParam = "";
        if (restType != null && restType instanceof BArrayType bArrayType) {
            if (!paramTypes.isEmpty()) {
                restParam += ", ";
            }
            restParam += bArrayType.eType + "...";
        }
        return "(" + (!paramTypes.isEmpty() ? getBTypeListAsString(paramTypes) : "") + restParam + ")"
                + " returns " + retTypeWithParam;
    }

    private static String getBTypeListAsString(List<BType> typeNames) {
        StringBuilder br = new StringBuilder();
        int i = 0;
        for (BType type : typeNames) {
            br.append(type);
            if (++i < typeNames.size()) {
                br.append(",");
            }
        }
        return br.toString();
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    /**
     * When the type is mutated we need to reset the definition used for the semType.
     */
    @Override
    public void resetSemType() {
        defn = null;
    }

    @Override
    public SemType semType() {
        if (isFunctionTop()) {
            if (Symbols.isFlagOn(getFlags(), Flags.ISOLATED) || Symbols.isFlagOn(getFlags(), Flags.TRANSACTIONAL)) {
                FunctionQualifiers qualifiers =
                        FunctionQualifiers.from(env, Symbols.isFlagOn(getFlags(), Flags.ISOLATED),
                                Symbols.isFlagOn(getFlags(), Flags.TRANSACTIONAL));
                FunctionDefinition definition = new FunctionDefinition();
                return definition.define(env, PredefinedType.NEVER, PredefinedType.VAL, qualifiers);
            }
            return PredefinedType.FUNCTION;
        }
        if (defn != null) {
            return defn.getSemType(env);
        }
        FunctionDefinition fd = new FunctionDefinition();
        this.defn = fd;
        List<SemType> params = this.paramTypes.stream().map(BInvokableType::from).toList();
        return getSemTypeWithParams(params, fd);
    }

    public SemType getSemTypeWithParams(List<SemType> params, FunctionDefinition fd) {
        SemType rest;
        if (restType instanceof BArrayType arrayType) {
            rest = from(arrayType.eType);
        } else {
            // Is this correct even when type is semantic error?
            rest = PredefinedType.NEVER;
        }
        SemType returnType = resolveReturnType();
        ListDefinition paramListDefinition = new ListDefinition();
        SemType paramTypes = paramListDefinition.defineListTypeWrapped(env, params, params.size(), rest,
                CellAtomicType.CellMutability.CELL_MUT_NONE);
        // TODO: probably we need to move this method from Types.
        boolean isGeneric = Types.containsTypeParams(this);
        FunctionQualifiers qualifiers = FunctionQualifiers.from(env, Symbols.isFlagOn(getFlags(), Flags.ISOLATED),
                Symbols.isFlagOn(getFlags(), Flags.TRANSACTIONAL));
        if (isGeneric) {
            return fd.defineGeneric(env, paramTypes, returnType, qualifiers);
        }
        return fd.define(env, paramTypes, returnType, qualifiers);
    }

    private SemType resolveReturnType() {
        if (retType == null) {
            return PredefinedType.NIL;
        }
        SemType innerType = from(retType);
        ListDefinition ld = new ListDefinition();
        return ld.tupleTypeWrapped(env, isDependentlyTyped(retType,
                new HashSet<>()) ? SemTypes.booleanConst(true) : PredefinedType.BOOLEAN, innerType);
    }

    private static boolean isDependentlyTyped(BType returnType, Set<BType> visited) {
        if (visited.contains(returnType)) {
            return false;
        }

        visited.add(returnType);

        // it doesn't seem we actually have a flag to check this, may be the correct way to do this is to have a
        //  method in BType for this, but given this is a temporary thing, this should be enough.
        if (returnType instanceof BParameterizedType) {
            return true;
        }
        if (returnType instanceof BUnionType unionType) {
            return unionType.getMemberTypes().stream().anyMatch(returnType1 ->
                    isDependentlyTyped(returnType1, visited));
        }
        if (returnType instanceof BMapType mapType) {
            return isDependentlyTyped(mapType.constraint, visited);
        }
        if (returnType instanceof BRecordType recordType) {
            return recordType.fields.values().stream().anyMatch(field -> isDependentlyTyped(field.type, visited)) ||
                    isDependentlyTyped(recordType.restFieldType, visited);
        }
        if (returnType instanceof BArrayType arrayType) {
            return isDependentlyTyped(arrayType.eType, visited);
        }
        if (returnType instanceof BTupleType tupleType) {
            return tupleType.getTupleTypes().stream().anyMatch(returnType1 -> isDependentlyTyped(returnType1, visited));
        }
        if (returnType instanceof BInvokableType invokableType) {
            return invokableType.paramTypes.stream().anyMatch(returnType1 ->
                    isDependentlyTyped(returnType1, visited)) ||
                    isDependentlyTyped(invokableType.retType, visited) ||
                    isDependentlyTyped(invokableType.restType, visited);
        }
        if (returnType instanceof BFutureType futureType) {
            return isDependentlyTyped(futureType.constraint, visited);
        }
        if (returnType instanceof BTableType tableType) {
            return isDependentlyTyped(tableType.constraint, visited);
        }
        if (returnType instanceof BStreamType streamType) {
            return isDependentlyTyped(streamType.constraint, visited);
        }
        return false;
    }

    private static SemType from(BType type) {
        SemType semType = type.semType();
        if (semType == null) {
            semType = PredefinedType.NEVER;
        }
        return semType;
    }

    private boolean isFunctionTop() {
        return paramTypes.isEmpty() && restType == null && retType == null;
    }
}
