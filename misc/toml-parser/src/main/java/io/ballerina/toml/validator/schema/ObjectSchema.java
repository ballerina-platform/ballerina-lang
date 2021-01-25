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

package io.ballerina.toml.validator.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents Object schema in JSON schema.
 *
 * @since 2.0.0
 */
public class ObjectSchema extends AbstractSchema {
    private String description;
    private boolean hasAdditionalProperties;
    private Map<String, AbstractSchema> properties;
    private List<String> required;

    public ObjectSchema(Type type) {
        super(type);
        this.hasAdditionalProperties = true;
        this.properties = new HashMap<>();
    }

    public ObjectSchema(Type type, Map<String, AbstractSchema> properties) {
        super(type);
        this.hasAdditionalProperties = true;
        this.properties = properties;
    }

    public ObjectSchema(Type type, String description, boolean hasAdditionalProperties,
                        Map<String, AbstractSchema> properties, List<String> required) {
        super(type);
        this.description = description;
        this.hasAdditionalProperties = hasAdditionalProperties;
        this.properties = properties;
        this.required = required;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public boolean hasAdditionalProperties() {
        return hasAdditionalProperties;
    }

    public Map<String, AbstractSchema> properties() {
        return properties;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public List<String> required() {
        return required;
    }
}
