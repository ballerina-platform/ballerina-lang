/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.shell.invoker.classload.GlobalVariable;
import io.ballerina.shell.utils.StringUtils;

/**
 * A class to denote a variable declaration.
 * The {@code isNew} defines whether the variable was newly added.
 * For old variables, there should be an entry in the static memory class.
 *
 * @since 2.0.0
 */
public class VariableContext {
    private final String name;
    private final String type;
    private final boolean isNew;
    private final boolean isAny;

    private VariableContext(String name, String type, boolean isNew, boolean isAny) {
        this.name = StringUtils.quoted(name);
        this.type = type;
        this.isNew = isNew;
        this.isAny = isAny;
    }

    /**
     * Creates a new variable with the given type.
     *
     * @param variableEntry A map entry indicating the variable name and type.
     * @return Context for a new variable.
     */
    public static VariableContext newVar(GlobalVariable variableEntry) {
        return new VariableContext(variableEntry.getVariableName(), variableEntry.getType(), true,
                variableEntry.getElevatedType().isAssignableToAny());
    }

    /**
     * Creates a variable with given type for a existing variable.
     *
     * @param variableEntry A map entry indicating the variable name and type.
     * @return Context for a old variable.
     */
    public static VariableContext oldVar(GlobalVariable variableEntry) {
        return new VariableContext(variableEntry.getVariableName(), variableEntry.getType(), false,
                variableEntry.getElevatedType().isAssignableToAny());
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

    @TemplateAccessible
    public boolean isAny() {
        return isAny;
    }
}

