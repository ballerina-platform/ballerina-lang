/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.validator.schema.AbstractSchema;
import io.ballerina.toml.validator.schema.ArraySchema;
import io.ballerina.toml.validator.schema.BooleanSchema;
import io.ballerina.toml.validator.schema.NumericSchema;
import io.ballerina.toml.validator.schema.PrimitiveValueSchema;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.toml.validator.schema.SchemaVisitor;
import io.ballerina.toml.validator.schema.StringSchema;
import io.ballerina.toml.validator.schema.Type;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Generates a Toml string with the default values supported.
 *
 * @since 2.0.0
 */
public class BoilerplateGenerator extends SchemaVisitor {

    private String parentTableKey;
    private final Map<String, DocumentMemberDeclarationNode> nodes;
    private String key;

    public BoilerplateGenerator(Schema rootSchema) {
        this.nodes = new LinkedHashMap<>();
        Map<String, AbstractSchema> children = rootSchema.properties();
        for (Map.Entry<String, AbstractSchema> entry : children.entrySet()) {
            String key = entry.getKey();
            AbstractSchema value = entry.getValue();
            if (value.type() == Type.OBJECT || value.type() == Type.ARRAY) {
                this.parentTableKey = key;
            } else {
                this.key = key;
            }
            value.accept(this);
        }
    }

    @Override
    public void visit(Schema objectSchema) {
        Map<String, AbstractSchema> children = objectSchema.properties();
        if (!isDefaultValuesExist(children)) {
            return;
        }
        if (!isTableHeaderSkippable(children)) {
            nodes.put(this.parentTableKey, SampleNodeGenerator.createTable(this.parentTableKey,
                    objectSchema.description().orElse(null)));
        }
        String parentKey = this.parentTableKey;
        for (Map.Entry<String, AbstractSchema> entry : children.entrySet()) {
            String key = entry.getKey();
            AbstractSchema value = entry.getValue();
            if (value.type() == Type.OBJECT || value.type() == Type.ARRAY) {
                this.parentTableKey = this.parentTableKey + "." + key;
            } else {
                this.key = key;
            }
            value.accept(this);
            this.parentTableKey = parentKey;
        }
    }

    @Override
    public void visit(ArraySchema arraySchema) {
        AbstractSchema items = arraySchema.items();
        if (items.type() == Type.OBJECT) {
            nodes.put(this.parentTableKey, SampleNodeGenerator.createTableArray(this.parentTableKey,
                    arraySchema.description().orElse(null)));
            Map<String, AbstractSchema> children = ((Schema) items).properties();
            for (Map.Entry<String, AbstractSchema> entry : children.entrySet()) {
                String key = entry.getKey();
                AbstractSchema value = entry.getValue();
                if (value.type() == Type.OBJECT) {
                    this.parentTableKey = this.parentTableKey + "." + key;
                } else {
                    this.key = key;
                    value.accept(this);
                }
            }
        } else {
            items.accept(this);
        }
    }

    @Override
    public void visit(BooleanSchema booleanSchema) {
        Optional<Boolean> defaultValue = booleanSchema.defaultValue();
        if (defaultValue.isEmpty()) {
            return;
        }
        Boolean val = defaultValue.get();
        String key = this.parentTableKey + "." + this.key;
        nodes.put(key, SampleNodeGenerator.createBooleanKV(this.key, val, booleanSchema.description().orElse(null)));
    }

    @Override
    public void visit(NumericSchema numericSchema) {
        Optional<Double> defaultValue = numericSchema.defaultValue();
        if (defaultValue.isEmpty()) {
            return;
        }
        Double val = defaultValue.get();
        String key = this.parentTableKey + "." + this.key;
        nodes.put(key, SampleNodeGenerator.createNumericKV(this.key, numericPrettyPrint(val),
                numericSchema.description().orElse(null)));
    }

    @Override
    public void visit(StringSchema stringSchema) {
        Optional<String> defaultValue = stringSchema.defaultValue();
        if (defaultValue.isEmpty()) {
            return;
        }
        String val = defaultValue.get();
        String key = this.parentTableKey + "." + this.key;
        nodes.put(key, SampleNodeGenerator.createStringKV(this.key, val, stringSchema.description().orElse(null)));
    }

    public Map<String, DocumentMemberDeclarationNode> getNodes() {
        return nodes;
    }

    private static String numericPrettyPrint(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%s", d);
        }
    }

    private static boolean isTableHeaderSkippable(Map<String, AbstractSchema> children) {
        for (AbstractSchema value : children.values()) {
            if (!(value.type() == Type.OBJECT || value.type() == Type.ARRAY)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDefaultValuesExist(Map<String, AbstractSchema> children) {
        for (AbstractSchema value : children.values()) {
            //If Anything other than array or table
            if (!(value.type() == Type.OBJECT || value.type() == Type.ARRAY)) {
                if (((PrimitiveValueSchema) value).defaultValue().isPresent()) {
                    return true;
                }
            }

            if (value.type() == Type.OBJECT) {
                Schema table = (Schema) value;
                if (isDefaultValuesExist(table.properties())) {
                    return true;
                }
            }

            if (value.type() == Type.ARRAY) {
                ArraySchema arraySchema = (ArraySchema) value;
                AbstractSchema items = arraySchema.items();
                if (items.type() == Type.OBJECT) {
                    Schema table = (Schema) items;
                    if (isDefaultValuesExist(table.properties())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
