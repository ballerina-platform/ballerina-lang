/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.FPValue;

/**
 * <p>
 * The {@link BStream} represents a stream in Ballerina.
 * </p>
 *
 * @since 1.1.0
 */
public interface BStream extends BRefValue {
    /**
     * Returns the constrained {@code BType} of the stream.
     *
     * @return constrained type
     */
    BType getConstraintType();

    /**
     * Method to publish to a topic representing the stream in the broker.
     *
     * @param strand the strand in which the data being published
     * @param data the data to publish to the stream
     */
    void publish(Strand strand, Object data);

    /**
     * Method to register a subscription to the underlying topic representing the stream in the broker.
     *
     * @param functionPointer represents the function pointer reference for the function to be invoked on receiving
     *                        messages
     */
    void subscribe(FPValue<Object[], Object> functionPointer);
}
