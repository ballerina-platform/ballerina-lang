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
package org.wso2.ballerina.core.model.types;

/**
 * {@code ArrayType} represents an array
 * @param <T> type of the array
 * @since 1.0.0
 */
public class ArrayType<T> extends AbstractType {

    private T[] thisArray;
    private int size;

    /**
     * Constructor for array inline initialization
     * @param args variable number of initial values
     */
    @SuppressWarnings("unchecked")
    public ArrayType(T... args) {
        thisArray = (T[]) new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            thisArray[i] = args[i];

        }
    }

    /**
     * Constructor for creating an array with size
     * @param size number of elements to be stored in this array
     */
    @SuppressWarnings("unchecked")
    public ArrayType(int size) {
        thisArray = (T[]) new Object[size];
        this.size = size;
    }

    /**
     * Constructor for creating an empty array
     *
     */
    public ArrayType() {

    }

    /**
     * Insert a value into a given position
     * @param index position
     * @param value value
     */
    public void insert(int index, T value) {
        thisArray[index] = value;
    }

    /**
     * Retrieve a value from a given index
     * @param index position
     * @return return value
     */
    public T get(int index) {
        return thisArray[index];
    }

    /**
     * Retrieve the size of the array
     * @return returns the size
     */
    public int size() {
        return size;
    }

}
