/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.nativeimpl.io.channels.base;

/**
 * Defines how the bytes will be represented for different data types.
 */
public enum Representation {
    /**
     * Represents a 64 bit value.
     */
    BIT_64(8),
    /**
     * Represents a 32 bit value.
     */
    BIT_32(4),
    /**
     * Represents a 16 bit value.
     */
    BIT_16(2),
    /**
     * Represents a variable value which does not have s size defined.
     */
    VARIABLE(-1);

    private int numberOfBytes;

    Representation(int numberOfBytes) {
        this.numberOfBytes = numberOfBytes;
    }

    public int getNumberOfBytes() {
        return numberOfBytes;
    }

    /**
     * finds a matching representation for the provided input string.
     *
     * @param representation the size representation string.
     * @return the corresponding representation.
     */
    public static Representation find(String representation) {
        switch (representation) {
            case "64b":
                return BIT_64;
            case "32b":
                return BIT_32;
            case "16b":
                return BIT_16;
            default:
                return BIT_64;
        }
    }
}
