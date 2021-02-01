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
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.utils.IdentifierUtils.decodeIdentifier;

/**
 * Base implementation for ballerina variable types with child variables.
 */
public abstract class BCompoundVariable implements BVariable {

    protected final SuspendedContext context;
    private final String name;
    private final BVariableType type;
    protected Value jvmValue;
    private Variable dapVariable;
    private Either<Map<String, Value>, List<Value>> childVariables;

    public BCompoundVariable(SuspendedContext context, String varName, BVariableType bVariableType, Value jvmValue) {
        this.context = context;
        // all the runtime variable names should be decoded in order to support quoted identifiers.
        this.name = decodeIdentifier(varName);
        this.type = bVariableType;
        this.jvmValue = jvmValue;
        this.dapVariable = null;
        this.childVariables = null;
    }

    /**
     * Returns JDI value representations of all the child variables. Child variables can either be
     * <ul>
     *   <li> a map of named child variables (map entries, json elements, etc.) </li>
     *   <li> a list of indexed child variables (array elements, table entries etc.) </li>
     * </ul>
     * <p>
     * Each compound variable type must have their own implementation to compute/fetch values.
     */
    protected abstract Either<Map<String, Value>, List<Value>> computeChildVariables();

    /**
     * Returns child variable count of the variable, which is used for child variable paging.
     *
     * @return child variable count of the variable
     */
    protected abstract Map.Entry<ChildVariableKind, Integer> getChildrenCount();

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
            Map.Entry<ChildVariableKind, Integer> childrenCount = getChildrenCount();
            if (childrenCount.getKey() == ChildVariableKind.INDEXED) {
                dapVariable.setIndexedVariables(childrenCount.getValue().longValue());
            } else {
                dapVariable.setNamedVariables(childrenCount.getValue().longValue());
            }
        }
        return dapVariable;
    }

    public Either<Map<String, Value>, List<Value>> getChildVariables() {
        if (childVariables == null) {
            childVariables = computeChildVariables();
        }
        return childVariables;
    }

    public Value getChildByName(String name) throws DebugVariableException {
        if (childVariables == null) {
            childVariables = computeChildVariables();
        }

        if (!childVariables.isLeft() || !childVariables.getLeft().containsKey(name)) {
            throw new DebugVariableException("No child variables found with name: '" + name + "'");
        }
        return childVariables.getLeft().get(name);
    }

    public Value getChildByIndex(int index) throws DebugVariableException {
        if (childVariables == null) {
            childVariables = computeChildVariables();
        }
        if (!childVariables.isRight() || childVariables.getRight().size() < index) {
            throw new DebugVariableException("No child variables found with index: '" + index + "'");
        }
        return childVariables.getRight().get(index);
    }

    /**
     * Child variable types.
     * <p>
     * Indexed child variables - array elements, table entries etc.
     * Named child variables - map entries, json elements, etc.
     */
    protected enum ChildVariableKind {
        NAMED, INDEXED
    }
}
