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

@Description { value:"Adds the specified element to the end of the list."}
@Param { value: "v: The vector to which the element will be added"}
@Param { value: "element: The element to be added"}
public function <Vector v> add (any element) {
    v.vec[v.size] = element;
    v.size = v.size + 1;
}

@Description { value:"Retrieves the element at the specified position of the vector."}
@Param { value: "v: The vector from which the element will be retrieved"}
@Param { value: "index: The position of the element to retrieve"}
@Return { value:"Returns the element at the specified position."}
@Return { value:"Returns an IndexOutOfRangeError if the specifed index is not within 0 and the size of the vector (both inclusive)."}
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
@Return { value:"Returns an IndexOutOfRangeError if the specifed index is not within 0 and the size of the vector (both inclusive)."}
public function <Vector v> insert (any element, int index) (IndexOutOfRangeError) {
    IndexOutOfRangeError err = validateRange(v.size, index);

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
@Return { value:"Returns the element at the specified position."}
@Return { value:"Returns an IndexOutOfRangeError if the specifed index is not within 0 and the size of the vector (both inclusive)."}
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

@Description { value:"Returns the size of the vector."}
@Param { value: "v: The vector of which to look-up the size"}
@Return { value:"The size of the vector"}
public function <Vector v> size() (int) {
    return v.size;
}

function shiftRight (Vector v, int index) {
    int i = index + 1;
    any current = v.vec[index];
    any previous;

    while (i < v.size) {
        previous = v.vec[i];
        v.vec[i] = current;
        current = previous;
        i = i + 1;
    }

    v.vec[index] = null;
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
        IndexOutOfRangeError err = {msg:"Index out of bounds: " + index};
        return err;
    }

    return null;
}
