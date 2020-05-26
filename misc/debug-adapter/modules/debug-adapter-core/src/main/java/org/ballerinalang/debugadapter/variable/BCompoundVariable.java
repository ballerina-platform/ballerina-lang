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

import java.util.Map;

/**
 * Base implementation for ballerina compound variable types.
 */
public abstract class BCompoundVariable implements BVariable {

    private Variable dapVariable;
    private Map<String, Value> childVariables;

    public BCompoundVariable() {
        this(null);
    }

    public BCompoundVariable(Variable dapVariable) {
        this.dapVariable = dapVariable;
        this.childVariables = null;
    }

    /**
     * Returns the value of the variable variable instance in string form. Each extended variable type must have their
     * own implementation to compute/fetch the value.
     */
    public abstract void computeChildVariables();

    public Map<String, Value> getChildVariables() {
        return childVariables;
    }

    public void setChildVariables(Map<String, Value> childVariables) {
        this.childVariables = childVariables;
    }

    @Override
    public Variable getDapVariable() {
        return dapVariable;
    }

    @Override
    public void setDapVariable(Variable dapVariable) {
        this.dapVariable = dapVariable;
    }
}
