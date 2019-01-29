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

/**
 * Documentable node for Global Variables.
 */
public class GlobalVariableDoc extends Documentable {
    public final boolean isGlobalVariable;
    public final String dataType;
    public final String href;

    /**
     * Construct.
     *
     * @param name        global variable name.
     * @param description description.
     * @param children    children if any.
     * @param dataType    data type of the global variable.
     * @param href        link to data type.
     */
    public GlobalVariableDoc(String name, String description, ArrayList<Documentable> children, String dataType,
                             String href) {
        super(name, "fw-globe", description, children);
        this.dataType = dataType;
        this.href = href;
        isGlobalVariable = true;
    }
}
