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

import java.util.Map;
import java.util.Optional;

/**
 * Represents String schema in JSON schema.
 *
 * @since 2.0.0
 */
public class StringSchema extends PrimitiveValueSchema<String> {
    private final String pattern;
    private final Integer minLength;
    private final Integer maxLength;

    public StringSchema(Type type, Map<String, String> message, String pattern, String defaultValue, Integer minLength
            , Integer maxLength) {
        super(type, message, defaultValue);
        this.pattern = pattern;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public Optional<String> pattern() {
        return Optional.ofNullable(pattern);
    }

    public Optional<Integer> minLength() {
        return Optional.ofNullable(minLength);
    }

    public Optional<Integer> maxLength() {
        return Optional.ofNullable(maxLength);
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
