/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.connector.api;

import org.ballerinalang.model.types.BType;

/**
 * This is the object which holds resource parameter details.
 *
 * @since 0.94
 */
public class ParamDetail {
    private String varName;
    private BType varType;

    public ParamDetail(BType variableType, String variableName) {
        this.varType = variableType;
        this.varName = variableName;
    }

    /**
     * This method will return parameter name.
     *
     * @return varName parameter name.
     */
    public String getVarName() {
        return varName;
    }

    /**
     * This method will return parameter type.
     *
     * @return varType parameter type.
     */
    public BType getVarType() {
        return varType;
    }
}
