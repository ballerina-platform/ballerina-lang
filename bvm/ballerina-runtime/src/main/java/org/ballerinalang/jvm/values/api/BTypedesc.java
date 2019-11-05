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
package org.ballerinalang.jvm.values.api;

import org.ballerinalang.jvm.types.BType;

/**
 * <p>
 * Ballerina runtime value representation of a *type*.
 *
 * {@code typedesc} is used to describe type of a value in Ballerina.
 * For example {@code typedesc} of number 5 is {@code int}, where as {@code typedesc} of a record value is the
 * record type that used to create this particular value instance.
 * </p>
 *
 * @since 1.1.0
 */
public interface BTypedesc extends BRefValue {

    /**
     * Returns the {@code BType} of the value describe by this type descriptor.
     *
     * @return describing type
     */
    BType getDescribingType();
}
