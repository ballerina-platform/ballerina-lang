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

package org.ballerinalang.jvm.values.api;

import org.ballerinalang.jvm.types.BType;

/**
 * <p>
 * The {@link BStream} represents a stream in Ballerina.
 * </p>
 *
 * @since 1.2.0
 */
public interface BStream extends BIterator<Object>, BRefValue {
    /**
     * Returns the constrained {@code BType} of the stream.
     *
     * @return constrained type
     */
    BType getConstraintType();

    /**
     * Returns a stream which applies a filtering condition on the input stream.
     *
     * @param stream The input stream being filtered
     * @param filterFunc The function pointer which represents the filtering condition
     * @return The output stream
     */
    BStream filter(BStream stream, BFunctionPointer<Object, Boolean> filterFunc);

    /**
     * Returns a new stream which applies a mapping condition on the input stream.
     *
     * @param stream The input stream being mapped
     * @param mapFunc The function pointer which represents the mapping condition
     * @return The output stream
     */
    BStream map(BStream stream, BFunctionPointer<Object, Object> mapFunc);

    /**
     * Combines the members of an stream using a combining function. The combining function takes the combined value so
     * far and a member of the stream, and returns a new combined value.
     *
     * @param reduceFunc The function pointer representing the user provided reduce function
     * @param initialValue The initial value of reduce function
     * @return The reduced value
     */
    Object reduce(BFunctionPointer<Object, Object> reduceFunc, Object initialValue);

    /**
     * Applies a function to each member of a stream.
     * The parameter 'func' is applied to each member of stream 'strm' in order.
     *
     * @param foreachFunc The function which is applied to each member in stream 'strm'
     */
    void forEach(BFunctionPointer<Object, Object> foreachFunc);
}
