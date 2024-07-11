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
import io.ballerina.types.definition.FunctionDefinition;
import io.ballerina.types.definition.ListDefinition;
import org.ballerinalang.model.types.InvokableType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 0.94
 */
public class BInvokableType extends BType implements InvokableType {

    public List<BType> paramTypes;
    // TODO: make these final
    public BType restType;
    public BType retType;
    public final Env env;
    private SoftReference<FunctionDefinition> defn = new SoftReference<>(null);

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
        if (!(o instanceof BInvokableType)) {
            return false;
        }
        BInvokableType that = (BInvokableType) o;

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
        if (restType != null && restType instanceof BArrayType) {
            if (!paramTypes.isEmpty()) {
                restParam += ", ";
            }
            restParam += ((BArrayType) restType).eType + "...";
        }
        return "(" + (!paramTypes.isEmpty() ? getBTypeListAsString(paramTypes) : "") + restParam + ")"
                + " returns " + retTypeWithParam;
    }

    private static String getBTypeListAsString(List<BType> typeNames) {
        StringBuffer br = new StringBuffer();
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

    @Override
    public SemType semType() {
        if (isFunctionTop()) {
            return PredefinedType.FUNCTION;
        }
        FunctionDefinition cachedDefn = defn.get();
        if (cachedDefn != null) {
            return cachedDefn.getSemType(env);
        }
        FunctionDefinition fd = new FunctionDefinition();
        this.defn = new SoftReference<>(fd);
        List<SemType> params = this.paramTypes.stream().map(BInvokableType::from).toList();
        SemType rest;
        if (restType instanceof BArrayType arrayType) {
            rest = from(arrayType.eType);
        } else {
            // Is this correct even when type is semantic error?
            rest = PredefinedType.NEVER;
        }
        SemType returnType = retType != null ? from(retType) : PredefinedType.NIL;
        ListDefinition paramListDefinition = new ListDefinition();
        SemType paramTypes = paramListDefinition.defineListTypeWrapped(env, params, params.size(), rest,
                CellAtomicType.CellMutability.CELL_MUT_NONE);
        // TODO: probably we need to move this method from Types.
        boolean isGeneric = Types.containsTypeParams(this);
        if (isGeneric) {
            return fd.defineGeneric(env, paramTypes, returnType);
        }
        return fd.define(env, paramTypes, returnType);
    }

    @Override
    public void semType(SemType semtype) {

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
