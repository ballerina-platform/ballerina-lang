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

import java.util.Optional;

/**
 * Represents numeric schema in JSON schema.
 *
 * @since 2.0.0
 */
public class NumericSchema extends AbstractSchema {
    private Double minimum;
    private Double maximum;

    public NumericSchema(Type type) {
        super(type);
    }

    public NumericSchema(Type type, Double minimum, Double maximum) {
        super(type);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Optional<Double> minimum() {
        return Optional.ofNullable(minimum);
    }

    public Optional<Double> maximum() {
        return Optional.ofNullable(maximum);
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
