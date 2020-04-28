// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Returns a new Java array instance with the specified element type and dimensions. This function completes abruptly
# with a `panic` if the specified handle refers to a Java null or if zero dimensions have been provided.
# ```ballerina
# handle stringClass = check java:getClass("java.lang.String");
# handle StrArray = java.arrays:newInstance(stringClass, 4);
# ```
#
# + class - The element type of the array
# + dimensions - The dimensions of the array
# + return - The new Java array instance
public function newInstance(public handle class, int ...dimensions) returns handle = @java:Method {
    class: "java.lang.reflect.Array",
    paramTypes: ["java.lang.Class", {class: "int", dimensions:1}]
} external;

# Returns a `handle`, which refers to the element at the specified index in the given Java array. This function
# completes abruptly with a `panic` if the specified handle refers to a Java null or if the handle does not refer
# to a Java array.
# ```ballerina
# handle words = getSortedArray();
# handle firstWord = java.arrays:get(words, 0);
# ```
#
# + array - The `handle`, which refers to the Java array
# + index - The index of the element to be returned
# + return - The `handle`, which refers to the element at the specified position in the Java array
public function get(public handle array, public int index) returns handle = @java:Method {
    class: "java.lang.reflect.Array"
} external;


# Replaces the indexed element at the specified index in the given Java array with the specified element. This
# function completes abruptly with a `panic` if the specified handle refers to a Java null or if the handle does
# not refer to a Java array.
# ```ballerina
# handle strArray = getStringArray();
# java.arrays:set(strArray, 0, java:fromString("Ballerina"));
# ```
#
# + array - The `handle`, which refers to the Java array
# + index - The index of the element to be replaced
# + element - The element to be stored at the specified index
public function set(public handle array, public int index, public handle element) = @java:Method {
    class: "java.lang.reflect.Array"
} external;

# Returns the length of the given Java array.
# ```ballerina
# handle array = getArray();
# int length = java.arrays:getLength(array);
# ```
#
# + array - The `handle`, which refers to the Java array
# + return - The length of the given Java array
public function getLength(public handle array) returns int = @java:Method {
    class: "java.lang.reflect.Array"
} external;

# Returns a Ballerina array for a handle that holds a Java array.
# ```ballerina
# int[] array = <int[]>java.arrays:getArrayFromHandle(handleValue);
# ```
#
# + array - The `handle`, which refers to the Java array
# + elementType - Optional parameter to specify the element type that is referenced by the handle
# + return - Ballerina array for the provided handle
public function getArrayFromHandle(handle array, typedesc<any> elementType = handle) returns any[] {
    any[] returnArray = [];
    if (!java:isNull(array)) {
        int count = getLength(array);
        foreach int i in 0 ... count-1 {
            any element = get(array, i);
            if (element is int|boolean|float|byte) {
                returnArray[i] = element;
            } else if (element is handle) {
                if (elementType is typedesc<string>) {
                    returnArray[i] = java:toString(element);
                } else if (elementType is typedesc<handle>) {
                    returnArray[i] = element;
                }
            }
        }
    }
    return returnArray;
}

# Returns a handle value representation for a Ballerina array.
# ```ballerina
# handle|error handleValue = java.arrays:getHandleFromArray(arg, "char");
# ```
#
# + array - Ballerina array which is to be converted to a handle reference
# + className - Java class name or the primitive type of the array elements referenced by the handle
# + return - The `handle|error` after the conversion
public function getHandleFromArray(any[] array, string className) returns handle|error {
    handle returnHandle = newInstance(check java:getClass(className), array.length());
    int count=0;
    while (count < array.length()) {
        if (className == "char" && array is byte[]) {
            set(returnHandle, count, wrapByteToChar(array[count]));
            count+=1;
        } else if (className == "char" && array is int[]) {
            set(returnHandle, count, wrapIntToChar(array[count]));
            count+=1;
        } else if (className == "char" && array is float[]) {
            set(returnHandle, count, wrapFloatToChar(array[count]));
            count+=1;
        } else if (className == "short" && array is byte[]) {
            set(returnHandle, count, wrapByteToShort(array[count]));
            count+=1;
        } else if (className == "short" && array is int[]) {
            set(returnHandle, count, wrapIntToShort(array[count]));
            count+=1;
        } else if (className == "short" && array is float[]) {
            set(returnHandle, count, wrapFloatToShort(array[count]));
            count+=1;
        } else if (className == "long" && array is byte[]) {
            set(returnHandle, count, wrapByteToLong(array[count]));
            count+=1;
        } else if (className == "long" && array is int[]) {
            set(returnHandle, count, wrapIntToLong(array[count]));
            count+=1;
        } else if (className == "long" && array is float[]) {
            set(returnHandle, count, wrapFloatToLong(array[count]));
            count+=1;
        } else if (className == "double" && array is byte[]) {
            set(returnHandle, count, wrapByteToDouble(array[count]));
            count+=1;
        } else if (className == "double" && array is float[]) {
            set(returnHandle, count, wrapFloatToDouble(array[count]));
            count+=1;
        } else if (array is byte[]) {
            set(returnHandle, count, wrapByte(array[count]));
            count+=1;
        } else if (array is int[]) {
            set(returnHandle, count, wrapInt(array[count]));
            count+=1;
        } else if (array is boolean[]) {
            set(returnHandle, count, wrapBoolean(array[count]));
            count+=1;
        } else if (array is float[]) {
            set(returnHandle, count, wrapFloat(array[count]));
            count+=1;
        } else if (array is string[]) {
            set(returnHandle, count, java:fromString(array[count]));
            count+=1;
        } else if (array is java:JObject[]) {
            java:JObject jObject = array[count];
            set(returnHandle, count, jObject.getHandle());
            count+=1;
        } else {
            return error("{ballerina/java.arrays} Handle from array conversion is not allowed for this type");
        }
    }
    return returnHandle;
}
