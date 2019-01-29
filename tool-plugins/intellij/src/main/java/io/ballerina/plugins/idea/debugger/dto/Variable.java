/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.debugger.dto;

import java.util.List;

/**
 * Represent a variable.
 */
public class Variable {

    private String scope, name;
    private String type, value;
    private List<Variable> children;

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

    public List<Variable> getChildren() {
        return children;
    }

    public void setChildren(List<Variable> children) {
        this.children = children;
    }

    public boolean isNumber() {
        return type != null && "BInteger".equals(type);
    }

    public boolean isString() {
        return type != null && "BString".equals(type);
    }

    public boolean isBoolean() {
        return type != null && "BBoolean".equals(type);
    }
}
