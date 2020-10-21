import ballerina/io;

public function add(int x, int y) returns int {
    int a;
    int b;
    int c;
    a = x;
    b = y;
    c = a + b;
    return c;
}

public type Person record {
};

public type Employee object {
};
