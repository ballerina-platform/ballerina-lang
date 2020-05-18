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
 * Base interface for ballerina variable types.
 */
public interface BVariable {

    /**
     * Returns the value of the variable instance in string form. Each variable type implementation can have their
     * own implementation to compute/fetch its value.
     *
     * @return value of the variable instance, in string form.
     */
    String getValue();

    /**
     * Returns variable information in a Debug Adapter Protocol compatible variable instance.
     *
     * @return a DAP compatible variable instance of the ballerina variable instance.
     */
    Variable getDapVariable();

    /**
     * Assigns the DAP representation of the given ballerina variable instance.
     */
    void setDapVariable(Variable dapVariable);
}
