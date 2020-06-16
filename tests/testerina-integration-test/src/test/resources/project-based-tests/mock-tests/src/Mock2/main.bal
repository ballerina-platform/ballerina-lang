import ballerina/io;

public function main() {
    io:println("FunctionMocking Test : Multiple Modules");
}

// Returns (int)
public function addFn(int a, int b) returns (int) {
    return a + b;
}
