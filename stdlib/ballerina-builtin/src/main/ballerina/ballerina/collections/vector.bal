package ballerina.collections;

@Description { value: "Vector is a resizable collection type which provides list operations."}
public struct Vector {
    // TODO: Make these private once private struct field support is available. Also, make use of init to initialize this
    any[] vec;
    int size = 0;
}

@Description { value: "An error which is returned when the user attempts to access an element which is out of the Vector's range."}
@Field { value : "msg: The error message"}
@Field { value : "cause: The cause for the error"}
@Field { value : "stackTrace: The stack trace of the error"}
public struct IndexOutOfRangeError {
    string msg;
    error cause;
    StackFrame[] stackTrace;
}

@Description { value:"Adds the specified element to the end of the vector."}
@Param { value: "v: The vector to which the element will be added"}
@Param { value: "element: The element to be added"}
public function <Vector v> add (any element) {
    v.vec[v.size] = element;
    v.size = v.size + 1;
}

@Description { value:"Clears all the elements from the vector."}
@Param { value: "v: The vector to be cleared"}
public function <Vector v> clear() {
    v.vec = [];
    v.size = 0;
}

@Description { value:"Retrieves the element at the specified position of the vector."}
@Param { value: "v: The vector from which the element will be retrieved"}
@Param { value: "index: The position of the element to retrieve"}
@Return { value:"The element at the specified position."}
@Return { value:"Returned if the specified index is not within 0 (inclusive) and the size of the vector (exclusive)."}
public function <Vector v> get (int index) (any, IndexOutOfRangeError) {
    IndexOutOfRangeError err = validateRange(v.size, index);

    if (err != null) {
        return null, err;
    }

    return v.vec[index], null;
}

@Description { value:"Inserts the given element at the position specified. All the elements (including the one currently in the position specified) to the right of the specified position are shifted to the right."}
@Param { value: "v: The vector to which the element will be inserted"}
@Param { value: "element: The element to insert"}
@Param { value: "index: The position to insert the element to"}
@Return { value:"Returned if the specified index is not within 0 and the size of the vector (both inclusive)."}
public function <Vector v> insert (any element, int index) (IndexOutOfRangeError) {
    IndexOutOfRangeError err = validateRange(v.size + 1, index); // range validated for the new vector size

    if (err != null) {
        return err;
    }

    shiftRight(v, index);
    v.vec[index] = element;
    v.size = v.size + 1;
    return null;
}

@Description { value:"Removes and returns the element at the position specified. All the elements to the right of the specified position are shifted to the left."}
@Param { value: "v: The vector from which the element will be removed"}
@Param { value: "index: The position to remove the element from"}
@Return { value:"The element at the specified position."}
@Return { value:"Returned if the specified index is not within 0 (inclusive) and the size of the vector (exclusive)."}
public function <Vector v> remove (int index) (any, IndexOutOfRangeError) {
    IndexOutOfRangeError err = validateRange(v.size, index);

    if (err != null) {
        return null, err;
    }

    any element = v.vec[index];
    shiftLeft(v, index);
    v.size = v.size - 1;
    return element, null;
}

@Description { value:"Replaces the element at the position specified with the provided element."}
@Param { value: "v: The vector in which the element will be replaced"}
@Param { value: "element: The replacement element"}
@Param { value: "index: The position of the element to be replaced"}
@Return { value:"The element which was originally at the specified position"}
@Return { value:"Returned if the specified index is not within 0 (inclusive) and the size of the vector (exclusive)."}
public function <Vector v> replace(any element, int index) (any, IndexOutOfRangeError) {
    IndexOutOfRangeError err = validateRange(v.size, index);

    if (err != null) {
        return null, err;
    }

    any currentElement = v.vec[index];
    v.vec[index] = element;

    return currentElement, null;
}

@Description { value:"Returns the size of the vector."}
@Param { value: "v: The vector of which to look-up the size"}
@Return { value:"The size of the vector"}
public function <Vector v> size() (int) {
    return v.size;
}

function shiftRight (Vector v, int index) {
    v.vec[v.size] = null; // assigning null so that v.size-th array index is valid
    int i = index + 1;
    any current = v.vec[index];
    any previous;

    while (i < v.size) {
        previous = v.vec[i];
        v.vec[i] = current;
        current = previous;
        i = i + 1;
    }

    v.vec[index] = null; // Since the element at index-th position was shifted to right, the index-th position should be empty
    v.vec[i] = current;
}

function shiftLeft (Vector v, int index) {
    int i = index;

    while (i < v.size - 1) {
        v.vec[i] = v.vec[i + 1];
        i = i + 1;
    }

    v.vec[i] = null;
}

function validateRange (int vectorSize, int index) (IndexOutOfRangeError) {
    if (index >= vectorSize || index < 0) {
        IndexOutOfRangeError err = {msg:"Index out of range: " + index};
        return err;
    }

    return null;
}
