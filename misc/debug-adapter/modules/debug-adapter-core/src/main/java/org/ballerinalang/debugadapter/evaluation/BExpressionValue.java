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

package org.ballerinalang.debugadapter.evaluation;

import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

/**
 * Ballerina specific wrapper implementation for JDI Values.
 */
public class BExpressionValue {

    private final SuspendedContext context;
    private final Value jdiValue;
    private BVariable bVariable;

    public BExpressionValue(SuspendedContext context, Value jdiValue) {
        this.context = context;
        this.jdiValue = jdiValue;
        this.bVariable = null;
    }

    public BVariableType getType() {
        if (bVariable == null) {
            bVariable = VariableFactory.getVariable(context, jdiValue);
        }
        return bVariable.getBType();
    }

    public String getStringValue() {
        if (bVariable == null) {
            bVariable = VariableFactory.getVariable(context, jdiValue);
        }
        return bVariable.getDapVariable().getValue();
    }

    public Value getJdiValue() {
        return jdiValue;
    }
}
