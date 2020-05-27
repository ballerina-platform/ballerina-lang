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

package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.Value;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.HashMap;

/**
 * Ballerina xml variable type.
 */
public class BXmlItem extends BCompoundVariable {

    private final ObjectReferenceImpl jvmValueRef;

    public BXmlItem(Value value, Variable dapVariable) {
         this.jvmValueRef = value instanceof ObjectReferenceImpl ? (ObjectReferenceImpl) value : null;
        dapVariable.setType(BVariableType.XML.getString());
        dapVariable.setValue(this.getValue());
        this.setDapVariable(dapVariable);
        this.computeChildVariables();
    }

    @Override
    public String getValue() {
        return jvmValueRef.toString();
    }

    @Override
    public void computeChildVariables() {
        // Todo
        this.setChildVariables(new HashMap<>());
    }
}
