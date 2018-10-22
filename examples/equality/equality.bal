import ballerina/io;

type Employee record {
    string name;
    int id;
};

type Person record {
    string name;
};

@final Employee globalEmployee = { name: "John", id: 2102 };

public function main() {
    // `==` could be used with values of compatible `anydata` types, and serves as a deep value equality check
    int i1 = 1;
    int i2 = 1;
    boolean isEqual = i1 == i2;
    io:println(i1, " == ", i2, " is ", isEqual);

    int i3 = 2;
    isEqual = i1 == i3;
    io:println(i1, " == ", i3, " is ", isEqual);

    (string|int, float, boolean) t1 = (1, 1.0, false);
    (int, float|string, boolean) t2 = (1, 1.0, false);
    isEqual = t1 == t2;
    io:println(t1, " == ", t2, " is ", isEqual);

    (int, float|string, boolean) t3 = (11, 1.0, true);
    isEqual = t1 == t3;
    io:println(t1, " == ", t3, " is ", isEqual);

    Employee e1 = { name: "Jane", id: 1100 };
    Employee e2 = { name: "Jane", id: 1100 };
    isEqual = e1 == e2;
    io:println(e1, " == ", e2, " is ", isEqual);

    Employee e3 = { name: "Anne", id: 1100 };
    isEqual = e1 == e3;
    io:println(e1, " == ", e3, " is ", isEqual);

    // `===` could be used with values of compatible types, and serves as a reference equality check
    Employee e4 = getGlobalEmployee();
    Person e5 = getGlobalEmployee();
    boolean isRefEqual = e4 === e5;
    io:println("e4 === e5 is ", isRefEqual);

    e4 = { name: "John", id: 2102 };
    isRefEqual = e4 === e5;
    io:println("e4 === e5 is ", isRefEqual);

    // `===` for non-reference types is the same as `==`
    float f1 = 1.1;
    float f2 = 1.1;
    isRefEqual = f1 === f2;
    io:println("f1 === f2 is ", isRefEqual);

    f2 = 12.1;
    isRefEqual = f1 === f2;
    io:println("f1 === f2 is ", isRefEqual);
}

function getGlobalEmployee() returns Employee {
    return globalEmployee;
}
