/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload.context;

import freemarker.ext.beans.TemplateAccessible;
import io.ballerina.shell.utils.StringUtils;

import java.util.Map;

/**
 * A class to denote a variable declaration.
 * The {@code isNew} defines whether the variable was newly added.
 * For old variables, there should be an entry in the static memory class.
 */
public class VariableContext {
    private final String name;
    private final String type;
    private final boolean isNew;

    private VariableContext(String name, String type, boolean isNew) {
        this.name = StringUtils.quoted(name);
        this.type = type;
        this.isNew = isNew;
    }

    /**
     * Creates a new variable with the given type.
     *
     * @param variableEntry A map entry indicating the variable name and type.
     * @return Context for a new variable.
     */
    public static VariableContext newVar(Map.Entry<String, String> variableEntry) {
        return new VariableContext(variableEntry.getKey(), variableEntry.getValue(), true);
    }

    /**
     * Creates a variable with given type for a existing variable.
     *
     * @param name Name of the variable.
     * @param type Type string representation.
     * @return Context for a old variable.
     */
    public static VariableContext oldVar(String name, String type) {
        return new VariableContext(name, type, false);
    }

    @TemplateAccessible
    public String getName() {
        return name;
    }

    @TemplateAccessible
    public String getType() {
        return type;
    }

    @TemplateAccessible
    public boolean isNew() {
        return isNew;
    }
}

