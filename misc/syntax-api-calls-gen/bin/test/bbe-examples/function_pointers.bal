import ballerina/io;

function add(int v1, int v2) returns int {
    return v1 + v2;
}

function multiply(int v1, int v2) returns int {
    return v1 * v2;
}

// Here, the function pointer is used as a parameter. A function pointer can be invoked similar
// to how a normal function is invoked.
function process(function (int, int) returns int func, int v1, int v2)
                                                            returns int {
    return func(v1, v2);
}

public function main() {
    // The function name `add` serves as a function pointer argument in the
    // call to the `process()` function. Function names can be thought of as final variables
    // since although you can use them like a regular variable, you cannot
    // modify the values they are associated with.
    io:println("Process Add 1, 2: ", process(add, 1, 2));
    io:println("Process Multiply 3, 4: ", process(multiply, 3, 4)); 
}
