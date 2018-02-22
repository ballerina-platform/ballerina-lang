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

package org.ballerinalang.swagger.model;

import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.Schema;

import java.util.Map;
import java.util.Set;

/**
 * This class wraps the {@link Schema} from swagger models inorder to overcome complications
 * while populating handlebar templates.
 */
public class BalSchema {
    private Schema oasSchema;
    private Set<Map.Entry<String, Schema>> properties;
    private boolean isArraySchema = false;

    public BalSchema buildFromSchema(Schema schema, Map<String, Schema> allSchemas) {
        this.oasSchema = schema;

        // Keep track of array type schema definitions
        if (schema instanceof ArraySchema) {
            this.isArraySchema = true;
            this.properties = null;
        } else {
            Set<Map.Entry<String, Schema>> entries = schema.getProperties().entrySet();

            // change conflicting swagger data types to ballerina data types
            for (Map.Entry entry : entries) {
                Schema prop = (Schema) entry.getValue();

                // Extract reference type objects
                if (prop.get$ref() != null) {
                    String type = prop.get$ref().substring(prop.get$ref().lastIndexOf("/") + 1);
                    prop.setType(type);
                } else {
                    switch (prop.getType()) {
                        case "integer":
                            prop.setType("int");
                            break;
                        case "number":
                            prop.setType("float");
                            break;
                        case "object":
                            prop.setType("any");
                            break;
                        default:
                            break;
                    }
                }
            }

            this.properties = entries;
        }

        return this;
    }

    public Schema getOasSchema() {
        return oasSchema;
    }

    public Set<Map.Entry<String, Schema>> getProperties() {
        return properties;
    }

    public boolean isArraySchema() {
        return isArraySchema;
    }
}
