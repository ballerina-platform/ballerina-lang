/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.toml.validator;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.validator.validations.AdditionalPropertiesVisitor;
import io.ballerina.toml.validator.validations.TypeCheckerVisitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains the validation logic for AdditionalProperties in the JSON schema.
 *
 * @since 2.0.0
 */
public class TomlValidator {

    private final RootSchema rootSchema;

    public TomlValidator(RootSchema rootSchema) {
        this.rootSchema = rootSchema;
    }

    public void validate(Toml toml) {
        Map<String, Schema> rootSchemaProperties = rootSchema.getProperties();
        processProperties(rootSchemaProperties, toml.getRootNode());
    }

    private void processProperties(Map<String, Schema> schema, TomlTableNode tableNode) {
        Set<Map.Entry<String, Schema>> entries = schema.entrySet();
        for (Map.Entry<String, Schema> entry : entries) {
            String key = entry.getKey();
            Schema value = entry.getValue();

            TopLevelNode topLevelNode = tableNode.children().get(key);
            if (topLevelNode != null) {
                if (value.getType() == TypeEnum.OBJECT) {
                    AdditionalPropertiesVisitor additionalPropertiesVisitor =
                            new AdditionalPropertiesVisitor((ObjectSchema) value);
                    topLevelNode.accept(additionalPropertiesVisitor);
                }

                TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(value, key);
                topLevelNode.accept(typeCheckerVisitor);
            }

            if (value.getType() == TypeEnum.OBJECT) {
                if (topLevelNode != null && topLevelNode.kind() == TomlType.TABLE) {
                    processProperties(((ObjectSchema) value).getProperties(), (TomlTableNode) topLevelNode);
                }
            } else if (value.getType() == TypeEnum.ARRAY) {
                if (topLevelNode != null && topLevelNode.kind() == TomlType.TABLE_ARRAY) {
                    Schema items = ((ArraySchema) value).getItems();
                    TomlTableArrayNode tableArrayNode = (TomlTableArrayNode) topLevelNode;
                    List<TomlTableNode> children = tableArrayNode.children();
                    for (TomlTableNode child : children) {
                        processProperties(((ObjectSchema) items).getProperties(), child);
                    }
                }
            }
        }
    }
}
