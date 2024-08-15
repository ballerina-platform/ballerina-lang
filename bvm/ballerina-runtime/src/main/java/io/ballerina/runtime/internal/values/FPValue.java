/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.BalRuntime;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * <p>
 * Ballerina runtime value representation of a function pointer.
 * </p>
 *
 * @since 0.995.0
 */
public class FPValue implements BFunctionPointer, RefValue {

    final Type type;
    private BTypedesc typedesc;
    public Function<Object[], Object> function;
    public boolean isIsolated;
    public String name;

    public FPValue(Function<Object[], Object> function, Type type, String name, boolean isIsolated) {
        this.function = function;
        this.type = type;
        this.name = name;
        this.isIsolated = isIsolated;
    }

    @Override
    public Object call(Runtime runtime, Object... t) {
        BalRuntime balRuntime = (BalRuntime) runtime;
        return balRuntime.scheduler.callFP(this, null, t);
    }

    @Override
    public String stringValue(BLink parent) {
        return "function " + type;
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public void freezeDirect() {
    }

    @Override
    public BTypedesc getTypedesc() {
        if (this.typedesc == null) {
            this.typedesc = new TypedescValueImpl(type);
        }
        return typedesc;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return RuntimeConstants.EMPTY;
    }

    @Override
    public SemType widenedType(Context cx) {
        return Builder.functionType();
    }

    @Override
    public Optional<SemType> shapeOf(Context cx) {
        return Optional.of(getType());
    }
}
