/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.types.Type;

/**
 * <p>
 * Represent an array in ballerina.
 * </p>
 * 
 * @since 1.1.0
 */
public interface BArray extends BRefValue, BCollection {

    /**
     * Get value in the given array index.
     * @param index array index
     * @return array value
     */
    Object get(long index);

    /**
     * Get ref value in the given index.
     * @param index array index
     * @return array value
     */
    Object getRefValue(long index);

    /**
     * Get ref value in the given index. Do a filling-read if required.
     *
     * @param index array index
     * @return array value
     */
    Object fillAndGetRefValue(long index);

    /**
     * Get int value in the given index.
     * @param index array index
     * @return array element
     */
    long getInt(long index);

    /**
     * Get boolean value in the given index.
     * @param index array index
     * @return array element
     */
    boolean getBoolean(long index);

    /**
     * Get byte value in the given index.
     * @param index array index
     * @return array element
     */
    byte getByte(long index);

    /**
     * Get float value in the given index.
     * @param index array index
     * @return array element
     */
    double getFloat(long index);

    /**
     * Get string value in the given index.
     * @param index array index
     * @return array element
     */
    @Deprecated
    String getString(long index);

    /**
     * Get string value in the given index.
     * @param index array index
     * @return array element
     */
    BString getBString(long index);

    /**
     * Add ref value to the given array index.
     * @param index array index
     * @param value value to be added
     */
    void add(long index, Object value);

    /**
     * Add int value to the given array index.
     * @param index array index
     * @param value value to be added
     */
    void add(long index, long value);

    /**
     * Add boolean value to the given array index.
     * @param index array index
     * @param value value to be added
     */
    void add(long index, boolean value);

    /**
     * Add byte value to the given array index.
     * @param index array index
     * @param value value to be added
     */
    void add(long index, byte value);


    /**
     * Add double value to the given array index.
     * @param index array index
     * @param value value to be added
     */
    void add(long index, double value);

    /**
     * Add string value to the given array index.
     * @param index array index
     * @param value value to be added
     */
    @Deprecated
    void add(long index, String value);

   /**
    * Add string value to the given array index.
    *
    * @param index array index
    * @param value value to be added
    */
    void add(long index, BString value);

    /**
     * Append value to the existing array.
     * @param value value to be appended
     */
    void append(Object value);

    /**
     * Removes and returns first member of an array.
     * @return the value that was the first member of the array
     */
    Object shift();

    /**
     * Removes and returns first member of an array.
     *
     * @param index array index
     * @return the value that was the first member of the array
     */
    Object shift(long index);

    /**
     * Adds values to the start of an array.
     * @param values values to add to the start of the array
     */
    void unshift(Object[] values);

    /**
     * Get ref values array.
     * @return ref value array
     */
    Object[] getValues();

    /**
     * Get a copy of byte array.
     * @return byte array
     */
    byte[] getBytes();

    /**
     * Get a copy of string array.
     * @return string array
     */
    String[] getStringArray();

    /**
     * Get a copy of int array.
     * @return int array
     */
    long[] getIntArray();

    /**
     * Get {@code BType} of the array elements.
     * @return element type
     */
    Type getElementType();

    Type getIteratorNextReturnType();

    boolean isEmpty();

    BArray slice(long startIndex, long endIndex);

    void setLength(long i);
}
