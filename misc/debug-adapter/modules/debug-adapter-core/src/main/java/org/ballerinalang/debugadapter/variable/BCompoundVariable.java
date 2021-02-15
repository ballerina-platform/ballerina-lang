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

import static io.ballerina.runtime.api.utils.IdentifierUtils.decodeIdentifier;

/**
 * Base implementation for ballerina variable types with child variables.
 */
public abstract class BCompoundVariable implements BVariable {

    protected final SuspendedContext context;
    protected final String name;
    protected final BVariableType type;
    protected Value jvmValue;
    protected Variable dapVariable;

    public BCompoundVariable(SuspendedContext context, String varName, BVariableType bVariableType, Value jvmValue) {
        this.context = context;
        // all the runtime variable names should be decoded in order to support quoted identifiers.
        this.name = decodeIdentifier(varName);
        this.type = bVariableType;
        this.jvmValue = jvmValue;
        this.dapVariable = null;
    }

    /**
     * Returns child variable count, to be used for child variable paging.
     *
     * @return child variable count of the variable
     */
    public abstract int getChildrenCount();

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

    /**
     * Child variable types.
     * <p>
     * <ul>
     * <li> Indexed child variables - Variable types which can contain a large number of child variable entries and
     * hence should be lazy loaded (i.e. array elements, table entries, map entries, json elements, etc.)
     * <li> Named child variables - Variable types with a known/limited number of child elements. (i.e. error variable
     * fields, object fields, etc.)
     * </ul>
     */
    public enum ChildVariableKind {
        NAMED,
        INDEXED
    }
}
