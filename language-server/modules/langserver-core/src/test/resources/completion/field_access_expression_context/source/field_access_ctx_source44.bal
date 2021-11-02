function test() {
    int[] arr = [];
    int testInt = 0;
    function (int) testFPointer = function(int a) {
        return;
    };

    TestClass cls = new ();
    int val = cls.
}

function utilizeInt(int hello) {

}

type Rec record {|
    int field1;
    int field2?;
|};

class TestClass {
    int int1;
    float float1;
    string field1;
    string field2;

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

    function fn5() returns Rec {
        return {field1: 0};
    }

    function getFloat() returns float {
        return 1.0;
    }
}
