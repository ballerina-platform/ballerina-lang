import ballerina.collections;

function main (string[] args) {
    // Initialize the vector with a new array.
    collections:Vector vec = {vec:[]};

    // Add elements to the vector. Elements of any type can be added to a vector.
    vec.add("Ballerina");
    vec.add("Vector");
    // Vector accepts 'null' as a valid value.
    vec.add(null);

    // size() function returns the size of the vector.
    println("No. of elements in the vector: " + vec.size());

    // The insert() function can be used to insert elements at the user specified index.
    // An IndexOutOfRangeError will be returned if the specified index does not lie within 0 and the size of the vector (both inclusive).
    // In this specific case, the error is ignored.
    _ = vec.insert("Collections", 1);

    // The get() function returns the element at the specified index. As with insert(), this can return an IndexOutOfRangeError
    // if index is not in the range: 0 <= index < vector.size().
    any elem;
    elem, _ = vec.get(2);
    println(elem);

    elem, _ = vec.get(3);
    // As it can be seen, 'null' values are allowed.
    println(elem);

    // The remove() function removes the element at the specified index and returns the element.
    // This function also may return an IndexOutOfRangeError if index is not in the range: 0 <= index < vector.size().
    elem, _ = vec.remove(0);
    println(elem);
}
