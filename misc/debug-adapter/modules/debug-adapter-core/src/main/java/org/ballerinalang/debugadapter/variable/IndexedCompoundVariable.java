/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

/**
 * Implementation for ballerina variable types which can contain a large number of child variable entries and
 * hence should be lazy loaded (i.e. array elements, table entries, map entries, json elements, etc.)
 *
 * @since 2.0.0
 */
public abstract class IndexedCompoundVariable extends BCompoundVariable {

    public IndexedCompoundVariable(SuspendedContext context, String varName, BVariableType bVarType, Value jvmValue) {
        super(context, varName, bVarType, jvmValue);
    }

    /**
     * Retrieves JDI value representations of the child variables in the given range, either as
     * <ul>
     * <li> a map of child variables (i.e. map entries, json elements, etc.)
     * <li> a list of child variables (i.e. array elements, table entries etc.)
     * </ul>
     * <p>
     * Each compound variable type with indexed child variables must have their own implementation to compute/fetch
     * values.
     *
     * @param start The index of the first variable to return; if omitted children start at 0.
     * @param count The number of variables to return. If count is missing or 0, all variables are returned.
     */
    protected abstract Either<Map<String, Value>, List<Value>> computeChildVariables(int start, int count);

    /**
     * Returns JDI value representations of the child variables in a given range, either as
     * <ul>
     * <li> a map of child variables (i.e. map entries, json elements, etc.)
     * <li> a list of child variables (i.e. array elements, table entries etc.)
     * </ul>
     *
     * @param start The index of the first variable to return; if omitted children start at 0.
     * @param count The number of variables to return. If count is missing or 0, all variables are returned.
     */
    public Either<Map<String, Value>, List<Value>> getIndexedChildVariables(int start, int count) {
        return computeChildVariables(start, count);
    }

    /**
     * Returns the JDI value representation of the child variable in a given index. (i.e. arrays, XML sequence, etc.)
     */
    public Value getChildByIndex(int index) throws DebugVariableException {
        Either<Map<String, Value>, List<Value>> childVariables = computeChildVariables(index, 1);
        if (childVariables.isLeft()) {
            throw new DebugVariableException("Accessing map elements by index is not allowed.");
        } else if (childVariables.isRight()) {
            if (childVariables.getRight() == null || childVariables.getRight().isEmpty()) {
                throw new DebugVariableException("No child variables found with index: '" + index + "'");
            }
            return childVariables.getRight().get(0);
        }
        throw new DebugVariableException("No child variables found with index: '" + index + "'");
    }

    /**
     * Returns the JDI value representation of the child variable for a given key. (i.e. map entry, json element, etc.)
     */
    public Value getChildByName(String key) throws DebugVariableException {
        // Todo - Avoid fetching all children values, which may cause performance impacts.
        Either<Map<String, Value>, List<Value>> childVariables = computeChildVariables(0, getChildrenCount());
        if (childVariables.isRight()) {
            throw new DebugVariableException("Accessing list elements by key is not allowed.");
        }

        if (childVariables.getLeft() == null
                || childVariables.getLeft().isEmpty()
                || !childVariables.getLeft().containsKey(key)) {
            throw new DebugVariableException("No child variables found with key: '" + key + "'");
        }
        return childVariables.getLeft().get(key);
    }

    @Override
    public Variable getDapVariable() {
        if (dapVariable == null) {
            dapVariable = new Variable();
            dapVariable.setName(this.name);
            dapVariable.setType(this.type.getString());
            dapVariable.setValue(computeValue());
            dapVariable.setIndexedVariables(getChildrenCount());
        }
        return dapVariable;
    }
}
