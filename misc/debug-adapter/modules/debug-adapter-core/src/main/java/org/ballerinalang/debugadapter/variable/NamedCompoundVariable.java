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

import java.util.Map;

/**
 * Implementation for ballerina variable types with a known/limited number of child elements. (i.e. error variable
 * fields, object fields, etc.)
 *
 * @since 2.0.0
 */
public abstract class NamedCompoundVariable extends BCompoundVariable {

    private Map<String, Value> namedChildVariables;

    public NamedCompoundVariable(SuspendedContext context, String varName, BVariableType bVarType, Value jvmValue) {
        super(context, varName, bVarType, jvmValue);
    }

    /**
     * Retrieves JDI value representations of all the child variables, as a map of named child variables (i.e. error
     * variable entries, object fields, record fields).
     * <p>
     * Each compound variable type with named child variables must have their own implementation to compute/fetch
     * values.
     */
    protected abstract Map<String, Value> computeChildVariables();

    /**
     * Retrieves JDI value representations of all the child variables, as a map of named child variables (i.e. error
     * variable entries, object fields, record fields).
     */
    public Map<String, Value> getNamedChildVariables() {
        if (namedChildVariables == null) {
            namedChildVariables = computeChildVariables();
        }
        return namedChildVariables;
    }

    /**
     * Returns the JDI value representation of the child variable for a given name.
     */
    public Value getChildByName(String name) throws DebugVariableException {
        if (namedChildVariables == null) {
            namedChildVariables = computeChildVariables();
        }

        if (!namedChildVariables.containsKey(name)) {
            for (Map.Entry<String, Value> childVariable : namedChildVariables.entrySet()) {
                String unicodeOfSlash = "&0092";
                String escaped = childVariable.getKey()
                        .replaceAll(String.format("\\%s(\\%s)?", unicodeOfSlash, unicodeOfSlash), "$1");
                if (escaped.equals(name)) {
                    return childVariable.getValue();
                }
            }
            throw new DebugVariableException("No child variables found with name: '" + name + "'");
        }
        return namedChildVariables.get(name);
    }

    @Override
    public Variable getDapVariable() {
        if (dapVariable == null) {
            dapVariable = new Variable();
            dapVariable.setName(this.name);
            dapVariable.setType(this.type.getString());
            dapVariable.setValue(computeValue());
            dapVariable.setNamedVariables(getChildrenCount());
        }
        return dapVariable;
    }
}
