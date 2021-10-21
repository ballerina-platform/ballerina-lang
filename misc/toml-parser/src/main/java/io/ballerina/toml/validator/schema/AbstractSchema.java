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

import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the base class for all the sub schemas in json schema.
 *
 * @since 2.0.0
 */
public abstract class AbstractSchema {

    private final Type type;
    private final Map<String, String> message;
    private final CompositionSchema compositionSchemas;

    public AbstractSchema(Type type, Map<String, String> message, CompositionSchema compositionSchemas) {
        this.type = type;
        this.message = message;
        this.compositionSchemas = compositionSchemas;
    }

    public Type type() {
        return type;
    }

    public Map<String, String> message() {
        return message;
    }

    public Optional<CompositionSchema> compositionSchemas() {
        return Optional.ofNullable(compositionSchemas);
    }

    public abstract void accept(SchemaVisitor visitor);

    public <T extends TomlNode> List<Diagnostic> validate(T givenValueNode, String key) {
        if (this.compositionSchemas().isPresent()) {
            CompositionSchema compositionSchema = this.compositionSchemas().get();
            return compositionSchema.validate(givenValueNode, key);
        }
        return Collections.emptyList();
    }
}
