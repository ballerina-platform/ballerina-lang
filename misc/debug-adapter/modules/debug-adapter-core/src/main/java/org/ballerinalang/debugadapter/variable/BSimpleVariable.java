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
import org.eclipse.lsp4j.debug.Variable;

/**
 * Base implementation for ballerina variable types with no child variables.
 */
public abstract class BSimpleVariable implements BVariable {

    protected final VariableContext context;
    protected final Value jvmValue;
    private final Variable dapVariable;

    public BSimpleVariable(VariableContext context, BVariableType bVariableType, Value jvmValue, Variable dapVar) {
        this.context = context;
        this.jvmValue = jvmValue;
        dapVar.setType(bVariableType.getString());
        dapVar.setValue(computeValue());
        this.dapVariable = dapVar;
    }

    @Override
    public VariableContext getContext() {
        return context;
    }

    @Override
    public Variable getDapVariable() {
        return dapVariable;
    }
}
