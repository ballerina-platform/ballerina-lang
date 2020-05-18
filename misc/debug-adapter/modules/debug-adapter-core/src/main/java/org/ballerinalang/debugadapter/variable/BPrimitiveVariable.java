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

import org.eclipse.lsp4j.debug.Variable;

/**
 * Base implementation for ballerina primitive variable types.
 */
public abstract class BPrimitiveVariable implements BVariable {

    private Variable dapVariable;

    @Override
    public Variable getDapVariable() {
        return dapVariable;
    }

    @Override
    public void setDapVariable(Variable dapVariable) {
        this.dapVariable = dapVariable;
    }
}
