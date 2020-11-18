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
package io.ballerina.compiler.api.types;

import java.util.List;
import java.util.Optional;

/**
 * Represents a function type descriptor.
 *
 * @since 2.0.0
 */
public interface FunctionTypeDescriptor extends BallerinaTypeDescriptor {

    /**
     * Get the required parameters.
     *
     * @return {@link List} of required parameters
     */
    List<Parameter> requiredParams();

    /**
     * Get the defaultable parameters.
     *
     * @return {@link List} of defaultable parameters
     */
    List<Parameter> defaultableParams();

    /**
     * Get the rest parameter.
     *
     * @return {@link Optional} rest parameter
     */
    Optional<Parameter> restParam();

    /**
     * Get the return type.
     *
     * @return {@link Optional} return type
     */
    Optional<BallerinaTypeDescriptor> returnTypeDescriptor();
}
