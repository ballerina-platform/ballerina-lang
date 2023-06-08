/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.types.Type;

/**
 * <p>
 * The {@link BStream} represents a stream in Ballerina.
 * </p>
 *
 * @since 1.2.0
 */
public interface BStream extends BValue {
    /**
     * Returns the constrained {@code Type} of the stream.
     *
     * @return constrained type
     */
    Type getConstraintType();

    /**
     * Returns the completion {@code Type} of the stream.
     *
     * @return completion type
     */
    Type getCompletionType();

    /**
     * Returns the underlying iterator of the stream.
     *
     * @return iterator object
     */
    BObject getIteratorObj();
}
