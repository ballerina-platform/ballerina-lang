import ballerina/module1;

function parse(string str) returns int? { // Results in a warning.
    int|error a = int:fromString(str);
    if a is int {
        return a;
    }}

class className {
    function parse(string str) returns int? { // Results in a warning.
        int|error a = int:fromString(str);
        if a is int {
            return a;
        }
    }
}

service on new module1:Listener(3390) {
    remote function parse(string str) returns int? { // Results in a warning.
        int|error a = int:fromString(str);
        if a is int {
            return a;
        }
    }

    resource function parse . (string str) returns int? { // Results in a warning.
        int|error a = int:fromString(str);
        if a is int {
            return a;
        }
    }
}

public function main() {
    object {} o = object {
        function parse(string str) returns int? { // Results in a warning.
            int|error a = int:fromString(str);
            if a is int {
                return a;
            }
        }
    };
}

type TestObject1 object {
    function parse(string str) returns int?;
};

class TestClass2 {
    *TestObject1;

    function parse(string str) returns int? {
        int|error a = int:fromString(str);
        if a is int {
            return a;
        }
    }
}

function testFunction() returns int? {}
