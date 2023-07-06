function test() {
    int[] arr = [];
    int testInt = 0;
    function (int) testFPointer = function (int a) {
        return;
    };

    TestClass cls = new();
    int val = cls.fn3();
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

    function fn3() returns string {
        return "";
    }
    
    function fn4() returns string {
        return "";
    }

    function getFloat() returns float {
        return 1.0;
    }
}
