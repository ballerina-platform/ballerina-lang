/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.util.exceptions.BLangFreezeException;
import io.ballerina.runtime.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * <p>
 * Represents all the reference types.
 * </p>
 * 
 * @since 1.1.0
 */
public interface BRefValue extends BValue {

    /**
     * Method to perform a deep copy, recursively copying all structural values and their members.
     *
     * @param refs The map which keep track of the references of already cloned values in cycles
     *
     * @return  A new copy of the value
     */
    Object copy(Map<Object, Object> refs);

    /**
     * Method to performs a deep copy, recursively copying all structural values and their members but the created
     * clone is a read-only value.
     *
     * @param refs The map which keep track of the references of already cloned values in cycles
     *
     * @return  A new copy of the value
     */
    Object frozenCopy(Map<Object, Object> refs);

    /**
     * Method to returns an integer representing the number of items that a value contains, where the meaning of item
     * depends on the basic type of value.
     *
     * @return  Length of the given value
     */
    default int size() {
        return -1;
    }

    /**
     * Method to retrieve if the {@link BRefValue} is frozen, if applicable. Compile time checks ensure
     * that the check is only possible on structured basic types.
     *
     * @return Flag indicating whether the value is frozen or not
     */
    default boolean isFrozen() {
        return this.getType().isReadOnly();
    }

    /**
     * Sets the freeze status of {@link BRefValue}, to disallow further modification. This method does not check if
     * the {@link BRefValue} is in the middle of freezing by another process.
     */
     default void freezeDirect() {
        throw new BLangFreezeException("'freezeDirect()' not allowed on '" + getType() + "'");
    }

    /**
     * Default serialize implementation for {@link BRefValue}.
     *
     * @param outputStream Represent the output stream that the data will be written to.
     */
    default void serialize(OutputStream outputStream) {
        try {
            outputStream.write(StringUtils.getJsonString(this).getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new BallerinaException("error occurred while serializing data", e);
        }
    }
}
