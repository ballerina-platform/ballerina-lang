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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.types;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;
import io.ballerina.runtime.internal.types.semtype.FunctionDefinition;
import io.ballerina.runtime.internal.types.semtype.FunctionQualifiers;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * {@code {@link BFunctionType }} represents a function type in ballerina.
 *
 * @since 0.995.0
 */
public class BFunctionType extends BAnnotatableType implements FunctionType {

    public Type restType;
    public Type retType;
    public long flags;
    public Parameter[] parameters;

    private final DefinitionContainer<FunctionDefinition> defn = new DefinitionContainer<>();

    public BFunctionType(Module pkg) {
        super("function ()", pkg, Object.class);
        this.parameters = new Parameter[0];
        this.retType = PredefinedTypes.TYPE_NULL;
        this.flags = 0;
    }

    public BFunctionType(Module pkg, long flags) {
        super("function", pkg, Object.class);
        this.parameters = null;
        this.retType = null;
        this.flags = flags;
    }

    @Deprecated
    public BFunctionType(Module pkg, Type[] paramTypes, Type restType, Type retType, long flags) {
        super("function ()", pkg, Object.class);
        this.restType = restType;
        this.retType = retType;
        this.flags = flags;
    }


    public BFunctionType(Module pkg, Parameter[] parameters, Type restType, Type retType, long flags, String name) {
        super(name, pkg, Object.class);
        this.parameters = parameters;
        this.restType = restType;
        this.retType = retType;
        this.flags = flags;
    }

    public Type[] getParameterTypes() {
        Type[] types = new Type[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].type;
        }
        return types;
    }

    @Override
    public Type getReturnParameterType() {
        return retType;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.FUNCTION_POINTER_TAG;
    }

    private static void addParamListToString(Parameter[] parameters, StringBuilder stringRep) {
        String prefix = "";
        for (Parameter parameter : parameters) {
            stringRep.append(prefix);
            prefix = ",";
            stringRep.append(parameter.type.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BFunctionType that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        if (this.flags != that.flags) {
            return false;
        }

        if (!Arrays.equals(parameters, that.parameters)) {
            return false;
        }
        return Objects.equals(retType, that.retType) && Objects.equals(restType, that.restType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if (SymbolFlags.isFlagOn(this.flags, SymbolFlags.ANY_FUNCTION)) {
            return result;
        }
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + retType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringRep = new StringBuilder();

        if (SymbolFlags.isFlagOn(flags, SymbolFlags.TRANSACTIONAL)) {
            stringRep.append("transactional ");
        }
        if (SymbolFlags.isFlagOn(flags, SymbolFlags.ISOLATED)) {
            stringRep.append("isolated ");
        }

        if (SymbolFlags.isFlagOn(this.flags, SymbolFlags.ANY_FUNCTION)) {
            stringRep.append("function");
        } else {
            stringRep.append("function (");
            if (parameters != null) {
                addParamListToString(parameters, stringRep);
            }
            if (restType instanceof BArrayType bArrayType) {
                stringRep.append(",");
                stringRep.append(bArrayType.getElementType().toString());
                stringRep.append("...");
            }
            stringRep.append(")");
            if (retType != null) {
                stringRep.append(" returns (").append(retType).append(")");
            }
        }
        return stringRep.toString();
    }

    @Override
    public String getAnnotationKey() {
        return Utils.decodeIdentifier(this.typeName);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Type getRestType() {
        return restType;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Type getReturnType() {
        return retType;
    }

    @Override
    public long getFlags() {
        return flags;
    }

    private static SemType createIsolatedTop(Env env) {
        FunctionDefinition fd = new FunctionDefinition();
        SemType ret = Builder.getValType();
        return fd.define(env, Builder.getNeverType(), ret, FunctionQualifiers.create(true, false));
    }

    @Override
    public SemType createSemType(Context cx) {
        if (isFunctionTop()) {
            return getTopType(cx);
        }
        Env env = cx.env;
        if (defn.isDefinitionReady()) {
            return defn.getSemType(env);
        }
        var result = defn.trySetDefinition(FunctionDefinition::new);
        if (!result.updated()) {
            return defn.getSemType(env);
        }
        FunctionDefinition fd = result.definition();
        SemType[] params = new SemType[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            params[i] = getSemType(cx, parameters[i].type);
        }
        SemType rest;
        if (restType instanceof BArrayType arrayType) {
            rest = getSemType(cx, arrayType.getElementType());
        } else {
            rest = Builder.getNeverType();
        }

        SemType returnType = resolveReturnType(cx);
        ListDefinition paramListDefinition = new ListDefinition();
        SemType paramType = paramListDefinition.defineListTypeWrapped(env, params, params.length, rest,
                CellAtomicType.CellMutability.CELL_MUT_NONE);
        return fd.define(env, paramType, returnType, getQualifiers());
    }

    private SemType getTopType(Context cx) {
        if (SymbolFlags.isFlagOn(flags, SymbolFlags.ISOLATED)) {
            return createIsolatedTop(cx.env);
        }
        return Builder.getFunctionType();
    }

    FunctionQualifiers getQualifiers() {
        return FunctionQualifiers.create(SymbolFlags.isFlagOn(flags, SymbolFlags.ISOLATED),
                SymbolFlags.isFlagOn(flags, SymbolFlags.TRANSACTIONAL));
    }

    private SemType getSemType(Context cx, Type type) {
        return tryInto(cx, type);
    }

    private boolean isFunctionTop() {
        return parameters == null && restType == null && retType == null;
    }

    @Override
    public synchronized void resetSemType() {
        defn.clear();
        super.resetSemType();
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return (restType instanceof BType rest && rest.isDependentlyTyped(visited)) ||
                (retType instanceof BType ret && ret.isDependentlyTyped(visited)) ||
                isDependentlyTypeParameters(visited);
    }

    private boolean isDependentlyTypeParameters(Set<MayBeDependentType> visited) {
        if (parameters == null) {
            return false;
        }
        return Arrays.stream(parameters).map(each -> each.type).filter(each -> each instanceof MayBeDependentType)
                .anyMatch(each -> ((MayBeDependentType) each).isDependentlyTyped(visited));
    }

    private SemType resolveReturnType(Context cx) {
        if (retType == null) {
            return Builder.getNilType();
        }
        MayBeDependentType retBType = (MayBeDependentType) retType;
        SemType returnType = getSemType(cx, retType);
        ListDefinition ld = new ListDefinition();
        SemType dependentlyTypedBit =
                retBType.isDependentlyTyped() ? Builder.getBooleanConst(true) : Builder.getBooleanType();
        SemType[] innerType = new SemType[]{dependentlyTypedBit, returnType};
        return ld.defineListTypeWrapped(cx.env, innerType, 2, Builder.getNeverType(),
                CellAtomicType.CellMutability.CELL_MUT_NONE);
    }
}
