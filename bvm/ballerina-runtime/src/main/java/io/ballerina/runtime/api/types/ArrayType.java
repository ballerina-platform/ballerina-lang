/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.types;

/**
 * {@code BArrayType} represents a type of an arrays in Ballerina.
 * <p>
 * Arrays are defined using the arrays constructor [] as follows:
 * TypeName[]
 * <p>
 * All arrays are unbounded in length and support 0 based indexing.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unchecked")
public interface ArrayType extends SelectivelyImmutableReferenceType {

    Type getElementType();

    boolean hasFillerValue();

    ArrayState getState();

    int getSize();

    /**
     * Enum to hold the state of an array.
     *
     * @since 2.0.0
     */
    enum ArrayState {
        CLOSED((byte) 1),
        INFERRED((byte) 2),
        OPEN((byte) 3);

        byte value;

        ArrayState(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return this.value;
        }

    }
}
