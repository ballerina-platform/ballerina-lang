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
# int[] array = <int[]> check jarrays:fromHandle(arrayHandle, "int");
# ```
#
# + array - The `handle`, which refers to the Java array
# + jType - The `string` parameter provided to specify the Java array element type
# + bType - The optional `string` parameter provided to specify the Ballerina array element type
# + return - Ballerina array `any[]|error` for the provided handle
public function fromHandle(handle array, string jType, string bType = "default") returns any[]|error {
    int count = getLength(array);
    any[] returnArray = [];
    if (!java:isNull(array)) {
        if (jType == "string") {
            string[] returnStringArray = [];
            foreach int i in 0 ... count-1 {
                string? element = java:toString(<handle>get(array, i));
                if (element is string) {
                    returnStringArray[i] = element;
                }
            }
            return returnStringArray;
        } else if (jType == "handle") {
            handle[] returnHandleArray = [];
            foreach int i in 0 ... count-1 {
                returnHandleArray[i] = get(array, i);
            }
            return returnHandleArray;
        } else if (jType == "boolean") {
            boolean[] returnBooleanArray = [];
            foreach int i in 0 ... count-1 {
                boolean element = getBBooleanFromJBoolean(get(array, i));
                returnBooleanArray[i] = element;
            }
            return returnBooleanArray;
        } else if (jType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJFloat(get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "double") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJDouble(get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "int" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJInt(get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "int" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJInt(get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "byte" && (bType == "default" || bType == "byte")) {
            byte[] returnByteArray = [];
            foreach int i in 0 ... count-1 {
                byte element = getBByteFromJByte(get(array, i));
                returnByteArray[i] = element;
            }
            return returnByteArray;
        } else if (jType == "byte" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJByte(get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "byte" && bType == "int") {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJByte(get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "short" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJShort(get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "short" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJShort(get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "long" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJLong(get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "long" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJLong(get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "char" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJChar(get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "char" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJChar(get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else {
            return error("{ballerina/java.arrays} Handle to array conversion cannot be applied on the provided types");
        }
    }
    return returnArray;
}

# Returns a handle value representation for a Ballerina array.
# ```ballerina
# handle handleValue = check java.arrays:toHandle(array, "char");
# ```
#
# + array - Ballerina array which is to be converted to a handle reference
# + jType - Java class name or the primitive type of the array elements referenced by the handle
# + return - The `handle|error` after the conversion
public function toHandle(any[] array, string jType) returns handle|error {
    handle returnHandle = newInstance(check java:getClass(jType), array.length());
    int count=0;
    while (count < array.length()) {
        if (array is byte[] && jType == "char") {
            set(returnHandle, count, wrapByteToChar(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "short") {
            set(returnHandle, count, wrapByteToShort(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "long") {
            set(returnHandle, count, wrapByteToLong(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "int") {
            set(returnHandle, count, wrapByteToInt(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "float") {
            set(returnHandle, count, wrapByteToFloat(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "double") {
            set(returnHandle, count, wrapByteToDouble(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "byte") {
            set(returnHandle, count, wrapByteToByte(array[count]));
            count+=1;
        } else if (array is int[] && jType == "byte") {
            set(returnHandle, count, wrapIntToByte(array[count]));
            count+=1;
        } else if (array is int[] && jType == "char") {
            set(returnHandle, count, wrapIntToChar(array[count]));
            count+=1;
        } else if (array is int[] && jType == "short") {
            set(returnHandle, count, wrapIntToShort(array[count]));
            count+=1;
        } else if (array is int[] && jType == "long") {
            set(returnHandle, count, wrapIntToLong(array[count]));
            count+=1;
        } else if (array is int[] && jType == "int") {
            set(returnHandle, count, wrapIntToInt(array[count]));
            count+=1;
        } else if (array is float[] && jType == "char") {
            set(returnHandle, count, wrapFloatToChar(array[count]));
            count+=1;
        } else if (array is float[] && jType == "short") {
            set(returnHandle, count, wrapFloatToShort(array[count]));
            count+=1;
        } else if (array is float[] && jType == "long") {
            set(returnHandle, count, wrapFloatToLong(array[count]));
            count+=1;
        } else if (array is float[] && jType == "double") {
            set(returnHandle, count, wrapFloatToDouble(array[count]));
            count+=1;
        } else if (array is float[] && jType == "byte") {
            set(returnHandle, count, wrapFloatToByte(array[count]));
            count+=1;
        } else if (array is float[] && jType == "int") {
            set(returnHandle, count, wrapFloatToInt(array[count]));
            count+=1;
        } else if (array is float[] && jType == "float") {
            set(returnHandle, count, wrapFloatToFloat(array[count]));
            count+=1;
        } else if (array is boolean[]) {
            set(returnHandle, count, wrapBooleanToBoolean(array[count]));
            count+=1;
        } else if (array is string[]) {
            set(returnHandle, count, java:fromString(array[count]));
            count+=1;
        } else if (array is java:JObject[]) {
            java:JObject jObject = array[count];
            set(returnHandle, count, jObject.jObj);
            count+=1;
        } else {
            return error("{ballerina/java.arrays} Array to handle conversion cannot be applied on the provided types");
        }
    }
    return returnHandle;
}
