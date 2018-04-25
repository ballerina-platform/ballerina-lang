import ballerina/io;

int globalInt = 5;

// Basic example where a lambda with a if block access its outer scope variables.
function basicClosure() returns (function (int) returns (int)) {
    int a = 3;
    var foo =  (int b) => (int) {
        int c = 34;
        if (b == 3) {
            c = c + b + a + globalInt;
        }
        return c + a;
    };
    return foo;
}

// Example function with multiple level of lambda functions within, which the inner most lambda has access to all of
// it outer scope variables.
function multilevelClosure() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = (int funcInt1) => (int) {
        int methodInt2 = 23;
        var addFunc2 = (int funcInt2) => (int) {
            int methodInt3 = 7;
            var addFunc3 = (int funcInt3) => (int) {
                return funcInt3 + funcInt2 + funcInt1 + methodInt1 + methodInt2 + methodInt3;
            };
            return addFunc3(8) + funcInt2 + funcInt1;
        };
        return addFunc2(4) + funcInt1;
    };
    return addFunc1;
}

// Example which shows how function pointers are passes around with closures, where inner scope lambdas accessing its
// outer scope variables.
function functionPointers(int functionIntX) returns (function (int) returns (function (int) returns (int))) {
    return (int functionIntY) => (function (int) returns (int)) {
        return (int functionIntZ) => (int) {
            return functionIntX + functionIntY + functionIntZ;
        };
    };
}


// Example function where inner scope variables can shadow its outer scope variables along with closure support.
function variableShadow(int a) returns function (float) returns (string){
    int b = 4;
    float f = 5.6;

    if (a < 10) {
        int a = 4;
        b = a + b + <int>f;
    }

    var foo = (float f) => (string) {
        if (a > 8) {
            int a = 6;
            b = a + <int>f + b;
        }
        return "Ballerina" + b;
    };
    return foo;
}

function main(string... args) {
    // Invoke the function that shows basic closure support.
    var foo = basicClosure();
    int result1 = foo(3);
    io:println("Answer: " + result1);

    // This function invocation shows how multiple level of lambda functions within with closure support.
    var bar = multilevelClosure();
    int result2 = bar(5);
    io:println("Answer: " + result2);

    // This function invocation shows how function pointers with closures are passes around.
    var baz1 = functionPointers(7);
    var baz2 = baz1(5);
    int result3 = baz2(3);
    io:println("Answer: " + result3);

    // This function invocation shows how variable shadows along with closures are supported.
    var qux = variableShadow(9);
    string result4 = qux(3.4);
    io:println("Answer: " + result4);
}
