/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.io.channels.base;

import org.ballerinalang.stdlib.io.utils.IOConstants;

/**
 * Defines how the bytes will be represented for different data types.
 */
public enum Representation {
    /**
     * Represents a 64 bit value.
     */
    BIT_64(8, Byte.SIZE),
    /**
     * Represents a 32 bit value.
     */
    BIT_32(4, Byte.SIZE),
    /**
     * Represents a 16 bit value.
     */
    BIT_16(2, Byte.SIZE),
    /**
     * Represents a variable value which does not have a size defined.
     */
    VARIABLE(-1, IOConstants.PROTO_BUF_BASE),
    /**
     * If representation is none.
     */
    NONE(-1, -1);

    private int numberOfBytes;

    private int base;

    Representation(int numberOfBytes, int base) {
        this.numberOfBytes = numberOfBytes;
        this.base = base;
    }

    public int getNumberOfBytes() {
        return numberOfBytes;
    }

    public int getBase() {
        return base;
    }
}
