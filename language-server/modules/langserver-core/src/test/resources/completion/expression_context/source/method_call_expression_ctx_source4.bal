import ballerina/module1;

function test() {
    int[] arr = [];
    int testInt = 0;
    function (int) testFPointer = function (int a) {
        return;
    };

    TestClass cls = new();
    cls.testFunction1(module1:a)
}

function utilizeInt(int hello) {
    
}

class TestClass {
    function testFunction1(int param) returns int {
        return param + 1;
    }

    function testFunction2(function (int fParam) returns int param, int val) returns int {
        return param(val);
    }
}
