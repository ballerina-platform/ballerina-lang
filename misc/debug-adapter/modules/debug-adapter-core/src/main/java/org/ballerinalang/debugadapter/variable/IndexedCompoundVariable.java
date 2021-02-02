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

import java.util.List;

/**
 * Implementation for ballerina variable types with indexed child variables (i.e. array elements, table entries, etc).
 *
 * @since 2.0.0
 */
public abstract class IndexedCompoundVariable extends BCompoundVariable {

    private List<Value> indexedChildVariables;

    public IndexedCompoundVariable(SuspendedContext context, String varName, BVariableType bVarType, Value jvmValue) {
        super(context, varName, bVarType, jvmValue);
    }

    /**
     * Returns JDI value representations the child variables in the given range, as a list of indexed child variables
     * (i.e. array elements, table entries etc.)
     * <p>
     * Each compound variable type with indexed child variables must have their own implementation to compute/fetch
     * values.
     *
     * @param start The index of the first variable to return; if omitted children start at 0.
     * @param count The number of variables to return. If count is missing or 0, all variables are returned.
     */
    protected abstract List<Value> computeIndexedChildVariables(int start, int count);

    public List<Value> getIndexedChildVariables(int start, int count) {
        if (indexedChildVariables == null) {
            indexedChildVariables = computeIndexedChildVariables(start, count);
        }
        return indexedChildVariables;
    }

    public Value getChildByIndex(int index) throws DebugVariableException {
        if (indexedChildVariables == null) {
            indexedChildVariables = computeIndexedChildVariables(index, 1);
        }
        if (indexedChildVariables.size() < index) {
            throw new DebugVariableException("No child variables found with index: '" + index + "'");
        }
        return indexedChildVariables.get(index);
    }
}
