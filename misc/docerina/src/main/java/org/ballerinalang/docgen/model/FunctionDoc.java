/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentable node for Functions.
 */
public class FunctionDoc extends Documentable {

    public final List<Field> parameters;
    public final List<Variable> returnParams;
    public final boolean isFunction;
    public final boolean isConstructor;

    /**
     * Constructor.
     * @param name function name.
     * @param description description.
     * @param children children if any.
     * @param parameters parameters of the function.
     * @param returnParams return parameters of the function.
     */
    public FunctionDoc(String name, String description, ArrayList<Documentable> children, List<Field> parameters,
                       List<Variable> returnParams) {
        super(name, "fw-function", description, children);
        this.parameters = parameters;
        this.returnParams = returnParams;
        if (name.equals("init")) {
            isConstructor = true;
            super.icon = "fw-constructor";
        } else {
            isConstructor = false;
        }
        isFunction = !isConstructor;
    }
}
