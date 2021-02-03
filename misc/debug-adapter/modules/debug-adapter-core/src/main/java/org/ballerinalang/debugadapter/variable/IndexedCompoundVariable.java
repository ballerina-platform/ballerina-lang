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
 * Implementation for ballerina variable types with indexed child variables (i.e. array elements, table entries, etc).
 *
 * @since 2.0.0
 */
public abstract class IndexedCompoundVariable extends BCompoundVariable {

    private Either<Map<String, Value>, List<Value>> indexedChildVariables;

    public IndexedCompoundVariable(SuspendedContext context, String varName, BVariableType bVarType, Value jvmValue) {
        super(context, varName, bVarType, jvmValue);
    }

    /**
     * Returns JDI value representations the child variables in the given range, either as
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
    protected abstract Either<Map<String, Value>, List<Value>> computeIndexedChildVariables(int start, int count);

    @Override
    public Variable getDapVariable() {
        if (dapVariable == null) {
            dapVariable = new Variable();
            dapVariable.setName(this.name);
            dapVariable.setType(this.type.getString());
            dapVariable.setValue(computeValue());
            dapVariable.setIndexedVariables((long) getChildrenCount());
        }
        return dapVariable;
    }

    public Either<Map<String, Value>, List<Value>> getIndexedChildVariables(int start, int count) {
        return computeIndexedChildVariables(start, count);
    }

    public Value getChildByIndex(int index) throws DebugVariableException {

        Either<Map<String, Value>, List<Value>> indexedChildVariables = computeIndexedChildVariables(index, 1);

        if (indexedChildVariables.isLeft()) {
            if (indexedChildVariables.getLeft() == null || indexedChildVariables.getLeft().isEmpty()) {
                throw new DebugVariableException("No child variables found with index: '" + index + "'");
            }
            return indexedChildVariables.getLeft().get(indexedChildVariables.getLeft().keySet().iterator().next());
        } else if (indexedChildVariables.isRight()) {
            if (indexedChildVariables.getRight() == null || indexedChildVariables.getRight().isEmpty()) {
                throw new DebugVariableException("No child variables found with index: '" + index + "'");
            }
            return indexedChildVariables.getRight().get(index);
        }
        throw new DebugVariableException("No child variables found with index: '" + index + "'");
    }
}
