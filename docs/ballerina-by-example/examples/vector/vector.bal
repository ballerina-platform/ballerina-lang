import ballerina.collections;
import ballerina.io;

function main (string[] args) {
    // Initialize the vector with a new array.
    collections:Vector vec = {vec:[]};

    // Add elements to the vector. Elements of any type can be added to a vector.
    vec.add("Ballerina");
    vec.add("Vector");
    // Vector accepts 'null' as a valid value.
    vec.add(null);

    // size() function returns the size of the vector.
    io:println("No. of elements in the vector: " + vec.size());

    // The insert() function can be used to insert elements at the user specified index.
    // An IndexOutOfRangeError will be thrown if the specified index does not lie within 0 and the size of the vector (both inclusive).
    vec.insert("Collections", 1);

    // The get() function returns the element at the specified index. As with insert(), this can throw an IndexOutOfRangeError
    // if index is not in the range: 0 <= index < vector.size().
    any elem;
    elem = vec.get(2);
    io:println(elem);

    elem = vec.get(3);
    // As it can be seen, 'null' values are allowed.
    io:println(elem);

    // The remove() function removes the element at the specified index and returns the element.
    // This function also may throw an IndexOutOfRangeError if index is not in the range: 0 <= index < vector.size().
    elem = vec.remove(0);
    io:println(elem);

    // The replace() function replaces the element at the specified index (and returns it) with the provided element.
    // This will also throw an IndexOutOfRangeError if the specified index is out of bounds.
    elem = vec.replace("Foo", 1);
    io:println(elem);

    // The clear() function clears all the elements from the vector.
    vec.clear();
    io:println(vec.size());
}
