/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm;

/**
 * Responsible for performing specific shift operations.
 *
 * @since 1.2.0
 */
public class ShiftUtils {

    public static int byteRightShift(int value, int amount) {
        byte byteValue = (byte) value;
        int shiftIntResult = byteValue >> amount;
        return Byte.toUnsignedInt((byte) shiftIntResult);
    }

    public static long unsigned8RightShift(long value, int amount) {
        byte byteValue = (byte) value;
        int shiftIntResult = byteValue >> amount;
        return Byte.toUnsignedLong((byte) shiftIntResult);
    }

    public static long unsigned16RightShift(long value, int amount) {
        short shortValue = (short) value;
        int shiftIntResult = shortValue >> amount;
        return Short.toUnsignedLong((short) shiftIntResult);
    }

    public static long unsigned32RightShift(long value, int amount) {
        int shortValue = (int) value;
        int shiftIntResult = shortValue >> amount;
        return Integer.toUnsignedLong(shiftIntResult);
    }

    public static long signed8UnsignedRightShift(long value, int amount) {
        if (value >= 0) {
            return value >>> amount;
        }

        return Byte.toUnsignedLong((byte) value) >>> amount;
    }

    public static long signed16UnsignedRightShift(long value, int amount) {
        if (value >= 0) {
            return value >>> amount;
        }

        return Short.toUnsignedLong((short) value) >>> amount;
    }

    public static long signed32UnsignedRightShift(long value, int amount) {
        if (value >= 0) {
            return value >>> amount;
        }

        return Integer.toUnsignedLong((int) value) >>> amount;
    }
}
