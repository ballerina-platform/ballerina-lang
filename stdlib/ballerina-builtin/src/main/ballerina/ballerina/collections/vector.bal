// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package ballerina.collections;

@Description { value: "Vector is a resizable collection type which provides list operations."}
public struct Vector {
    // TODO: Make these private once private struct field support is available. Also, make use of init to initialize this
    any[] vec;
    int vectorSize = 0;
}

@Description { value: "An error which is returned when the user attempts to access an element which is out of the Vector's range."}
@Field { value : "message: The error message"}
@Field { value : "cause: The cause for the error"}
public struct IndexOutOfRangeError {
    string message;
    error cause;
}

@Description { value:"Adds the specified element to the end of the vector."}
@Param { value: "v: The vector to which the element will be added"}
@Param { value: "element: The element to be added"}
public function <Vector v> add (any element) {
    v.vec[v.vectorSize] = element;
    v.vectorSize = v.vectorSize + 1;
}

@Description { value:"Clears all the elements from the vector."}
@Param { value: "v: The vector to be cleared"}
public function <Vector v> clear() {
    v.vec = [];
    v.vectorSize = 0;
}

@Description { value:"Retrieves the element at the specified position of the vector."}
@Param { value: "v: The vector from which the element will be retrieved"}
@Param { value: "index: The position of the element to retrieve"}
@Return { value:"The element at the specified position."}
public function <Vector v> get (int index) (any) {
    validateRange(v.vectorSize, index);
    return v.vec[index];
}

@Description { value:"Inserts the given element at the position specified. All the elements (including the one currently in the position specified) to the right of the specified position are shifted to the right."}
@Param { value: "v: The vector to which the element will be inserted"}
@Param { value: "element: The element to insert"}
@Param { value: "index: The position to insert the element to"}
public function <Vector v> insert (any element, int index) {
    validateRange(v.vectorSize + 1, index); // range validated for the new vector size
    shiftRight(v, index);
    v.vec[index] = element;
    v.vectorSize = v.vectorSize + 1;
}

@Description { value:"Checks whether the specified vector is empty."}
@Param { value: "v: The vector to be checked if its empty"}
@Return { value:"Returns true if there aren't any elements in the vector"}
public function <Vector v> isEmpty() (boolean) {
    return v.vectorSize == 0;
}

@Description { value:"Removes and returns the element at the position specified. All the elements to the right of the specified position are shifted to the left."}
@Param { value: "v: The vector from which the element will be removed"}
@Param { value: "index: The position to remove the element from"}
@Return { value:"The element at the specified position."}
public function <Vector v> remove (int index) (any) {
    validateRange(v.vectorSize, index);
    any element = v.vec[index];
    shiftLeft(v, index);
    v.vectorSize = v.vectorSize - 1;
    return element;
}

@Description { value:"Replaces the element at the position specified with the provided element."}
@Param { value: "v: The vector in which the element will be replaced"}
@Param { value: "element: The replacement element"}
@Param { value: "index: The position of the element to be replaced"}
@Return { value:"The element which was originally at the specified position"}
public function <Vector v> replace(any element, int index) (any) {
    validateRange(v.vectorSize, index);
    any currentElement = v.vec[index];
    v.vec[index] = element;
    return currentElement;
}

@Description { value:"Returns the size of the vector."}
@Param { value: "v: The vector of which to look-up the size"}
@Return { value:"The size of the vector"}
public function <Vector v> size() (int) {
    return v.vectorSize;
}

function shiftRight (Vector v, int index) {
    v.vec[v.vectorSize] = null; // assigning null so that v.size-th array index is valid
    int i = v.vectorSize;

    while (index < i) {
        v.vec[i] = v.vec[i-1];
        i = i - 1;
    }
}

function shiftLeft (Vector v, int index) {
    int i = index;

    while (i < v.vectorSize - 1) {
        v.vec[i] = v.vec[i + 1];
        i = i + 1;
    }
    v.vec[v.vectorSize - 1] = null;
}

function validateRange (int vectorSize, int index) {
    if (index >= vectorSize || index < 0) {
        IndexOutOfRangeError err = {message:"Index out of range: " + index};
        throw err;
    }
}
