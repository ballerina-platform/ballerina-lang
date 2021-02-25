/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.shell.invoker.classload.visitors;

import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;

/**
 * Transforms the types to a type. Visits all the type symbols.
 * The transformation state saving logic should be implemented in child classes.
 *
 * @param <T> Type to transform.
 * @since 2.0.0
 */
public abstract class TypeSymbolTransformer<T> extends TypeSymbolVisitor {
    private T state;

    public TypeSymbolTransformer() {
        resetState();
    }

    public T transformType(TypeSymbol typeSymbol) {
        resetState();
        visitType(typeSymbol);
        return state;
    }

    protected T transformParameter(ParameterSymbol parameterSymbol) {
        resetState();
        visitParameter(parameterSymbol);
        return state;
    }

    protected T transformField(ObjectFieldSymbol fieldSymbol) {
        resetState();
        visitField(fieldSymbol);
        return state;
    }

    protected T transformField(RecordFieldSymbol fieldSymbol) {
        resetState();
        visitField(fieldSymbol);
        return state;
    }

    protected T transformMethod(MethodSymbol methodSymbol) {
        resetState();
        visitMethod(methodSymbol);
        return state;
    }

    /**
     * Set the transformer state.
     * State should be an immutable value.
     *
     * @param state State to set.
     */
    protected void setState(T state) {
        this.state = state;
    }

    /**
     * Resets the transformer state.
     * Should use the set state method to set the new state.
     */
    protected abstract void resetState();
}
