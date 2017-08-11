/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.util.debugger.dto;

import org.ballerinalang.bre.nonblocking.debugger.VariableInfo;

/**
 * DTO class representing variables in the stack upon a debug hit.
 *
 * @since 0.8.0
 */
public class VariableDTO {

    private String scope, name;
    private String type, value;

    public VariableDTO() {
    }

    public VariableDTO(VariableInfo vinfo) {
        this.scope = vinfo.getScope();
        this.name = vinfo.getName();
        this.type = vinfo.getType();
        this.value = vinfo.getbValueSting();
    }


    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
