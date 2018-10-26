/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.values;

import org.ballerinalang.util.exceptions.BLangFreezeException;

/**
 * The {@code BRefType} represents a reference type value in Ballerina.
 *
 * @param <T> actual value of this reference type
 * @since 0.8.0
 */
public interface BRefType<T> extends BValue {

    T value();

    /**
     * Method to freeze a {@link BValue}, to disallow further modification.
     *
     * @return The frozen self instance
     */
    default BRefType freeze() {
        throw new BLangFreezeException("freeze not allowed on '" + getType() + "'");
    }

    /**
     * Method to retrieve if the {@link BValue} is frozen, if applicable. Compile time checks ensure that the check
     * is only possible on structured basic types.
     *
     * @return Whether the value is frozen
     */
    default boolean isFrozen() {
        throw new BLangFreezeException("isFrozen not allowed on '" + getType() + "'");
    }
}
