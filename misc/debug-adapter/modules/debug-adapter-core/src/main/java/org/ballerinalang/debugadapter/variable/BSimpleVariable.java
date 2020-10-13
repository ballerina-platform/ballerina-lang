/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.variable;

import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.eclipse.lsp4j.debug.Variable;

import static io.ballerina.runtime.IdentifierUtils.decodeIdentifier;

/**
 * Base implementation for ballerina variable types with no child variables.
 */
public abstract class BSimpleVariable implements BVariable {

    protected final SuspendedContext context;
    private final String name;
    private final BVariableType type;
    protected final Value jvmValue;
    private Variable dapVariable;

    public BSimpleVariable(SuspendedContext context, String varName, BVariableType bVariableType, Value jvmValue) {
        this.context = context;
        // all the runtime variable names should be decoded in order to support quoted identifiers.
        this.name = decodeIdentifier(varName);
        this.type = bVariableType;
        this.jvmValue = jvmValue;
        this.dapVariable = null;
    }

    @Override
    public SuspendedContext getContext() {
        return context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BVariableType getBType() {
        return type;
    }

    @Override
    public Value getJvmValue() {
        return jvmValue;
    }

    @Override
    public Variable getDapVariable() {
        if (dapVariable == null) {
            dapVariable = new Variable();
            dapVariable.setName(this.name);
            dapVariable.setType(this.type.getString());
            dapVariable.setValue(computeValue());
        }
        return dapVariable;
    }
}
