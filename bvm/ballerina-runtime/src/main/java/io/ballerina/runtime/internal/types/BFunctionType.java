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

import io.ballerina.identifierutil.IdentifierUtils;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;

import java.util.Arrays;

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

    public BFunctionType() {
        super("function ()", null, Object.class);
        this.parameters = new Parameter[0];
        this.retType = PredefinedTypes.TYPE_NULL;
        this.flags = 0;
    }

    public BFunctionType(long flags) {
        super("function", null, Object.class);
        this.parameters = null;
        this.retType = null;
        this.flags = flags;
    }

    @Deprecated
    public BFunctionType(Type[] paramTypes, Type restType, Type retType, long flags) {
        super("function ()", null, Object.class);
        this.restType = restType;
        this.retType = retType;
        this.flags = flags;
    }

    public BFunctionType(Parameter[] parameters, Type restType, Type retType, long flags) {
        super("function ()", null, Object.class);
        this.parameters = parameters;
        this.restType = restType;
        this.retType = retType;
        this.flags = flags;
    }

    @Deprecated
    @Override
    public Type[] getParameterTypes() {
        Type[] types = new Type[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].type;
        }
        return types;
    }

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

    private static String getTypeListAsString(Parameter[] parameters) {
        StringBuffer br = new StringBuffer();
        int i = 0;
        for (Parameter parameter : parameters) {
            br.append(parameter.type.getName());
            if (++i < parameters.length) {
                br.append(",");
            }
        }
        return br.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BFunctionType)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BFunctionType that = (BFunctionType) o;

        boolean isSourceAnyFunction = SymbolFlags.isFlagOn(this.flags, SymbolFlags.ANY_FUNCTION);
        boolean isTargetAnyFunction = SymbolFlags.isFlagOn(that.flags, SymbolFlags.ANY_FUNCTION);

        if (isSourceAnyFunction && isTargetAnyFunction) {
            return true;
        }

        if (isSourceAnyFunction != isTargetAnyFunction) {
            return false;
        }

        if (SymbolFlags.isFlagOn(that.flags, SymbolFlags.ISOLATED) != SymbolFlags
                .isFlagOn(this.flags, SymbolFlags.ISOLATED)) {
            return false;
        }

        if (SymbolFlags.isFlagOn(that.flags, SymbolFlags.TRANSACTIONAL) != SymbolFlags
                .isFlagOn(this.flags, SymbolFlags.TRANSACTIONAL)) {
            return false;
        }

        if (!Arrays.equals(parameters, that.parameters)) {
            return false;
        }
        return retType.equals(that.retType);
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
        String stringRep;

        if (SymbolFlags.isFlagOn(this.flags, SymbolFlags.ANY_FUNCTION)) {
            stringRep = "function";
        } else {
            stringRep = "function (" + (parameters != null ? getTypeListAsString(parameters) : "") + ")" +
                    (retType != null ? " returns (" + retType + ")" : "");
        }

        if (SymbolFlags.isFlagOn(flags, SymbolFlags.ISOLATED)) {
            stringRep = "isolated ".concat(stringRep);
        }
        if (SymbolFlags.isFlagOn(flags, SymbolFlags.TRANSACTIONAL)) {
            "transactional ".concat(stringRep);
        }
        return stringRep;
    }

    @Override
    public String getAnnotationKey() {
        return IdentifierUtils.decodeIdentifier(this.typeName);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public Type getRestType() {
        return restType;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    public Type getReturnType() {
        return retType;
    }

    public long getFlags() {
        return flags;
    }
}
