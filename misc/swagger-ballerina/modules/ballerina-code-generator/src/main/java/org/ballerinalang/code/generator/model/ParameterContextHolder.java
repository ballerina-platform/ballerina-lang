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

package org.ballerinalang.code.generator.model;

import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * Context Model for holding resource parameter information.
 */
public class ParameterContextHolder {
    private String type;
    private String name;
    private String defaultValue;
    private String example;

    /**
     * Build a readable parameter model from a Ballerina <code>VariableNode</code>.
     *
     * @param parameter {@code VariableNode} with parameter definition
     * @return built Parameter context model
     */
    public static ParameterContextHolder buildContext(VariableNode parameter) {
        ParameterContextHolder context = new ParameterContextHolder();
        TypeNode type = parameter.getTypeNode();

        if (type instanceof BLangValueType) {
            context.type = ((BLangValueType) parameter.getTypeNode()).getTypeKind().typeName();
        } else if (type instanceof BLangUserDefinedType) {
            context.type = ((BLangUserDefinedType) parameter.getTypeNode()).getTypeName().getValue();
        }

        // Ignore Connection and InRequest parameters
        if (context.isIgnoredType(context.type)) {
            return null;
        }
        context.name = parameter.getName().toString();
        context.defaultValue = context.getDefaultValue(context.type, context.name);
        // examples are not yet supported
        context.example = "";
        return context;
    }

    /**
     * Check if the provided type is one of ignored variable type.
     * {@code endpoint} and {@code Request} type should be ignored without identifying as
     * resource parameters
     *
     * @param type Variable type
     * @return True if {@code type} is ignored type. False otherwise.
     */
    private boolean isIgnoredType(String type) {
        // type of endpoint is returned as null
        if (type == null  || "Request".equals(type)) {
            return true;
        }

        return false;
    }

    /**
     * Retrieve a default value for provided variable {@code type}.
     *
     * @param type variable type
     * @param name name of the variable. This value will be used as the default value
     *             for string type variables.
     * @return default ballerina value for the {@code type}
     */
    private String getDefaultValue(String type, String name) {
        String defaultValue;

        switch (type) {
            case "int":
                defaultValue = "0";
                break;
            case "float":
                defaultValue = "0.0";
                break;
            case "boolean":
                defaultValue = "false";
                break;
            case "blob":
                // TODO: 3/6/18 Handle blob initialization properly after #5045 is fixed
            case "string":
                defaultValue = '\"' + name + '\"';
                break;
            default:
                defaultValue = "{}";
                break;
        }

        return defaultValue;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getExample() {
        return example;
    }
}
