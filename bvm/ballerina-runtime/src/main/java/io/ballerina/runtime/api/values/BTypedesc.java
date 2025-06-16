/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.scheduling.Strand;

/**
 * <p>
 * Ballerina runtime value representation of a {@link Type}.
 *
 * {@code BTypedesc} is used to describe type of a value in Ballerina.
 * For example {@code BTypedesc} of number 5 is {@code int}, where as {@code BTypedesc} of a record value is the
 * record type that used to create this particular value instance.
 * </p>
 *
 * @since 1.1.0
 */
public interface BTypedesc extends BValue {

    /**
     * Returns the {@code Type} of the value describe by this type descriptor.
     *
     * @return describing type
     */
    Type getDescribingType();
}
