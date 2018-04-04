import ballerina/io;

function getStringFunc1(string functionX) returns (function (string) returns (function (string) returns (string))) {
    return function (string functionY) returns (function (string) returns (string)) {
        return function (string functionZ) returns (string) {
            return functionY + functionX + functionZ + functionR;
        };
    };
}

function threeLevelTest() returns (int) {
    var addFunc1 = function (int funcInt1) returns (int) {
        var addFunc2 = function (int funcInt2) returns (int) {
            var addFunc3 = function (int methodInt3, int methodInt2, int methodInt1, int funcInt3) returns (int) {
                return funcInt3 + methodInt1 + methodInt2 + methodInt3;
            };
            return addFunc3(7, 23, 2, 8) + methodInt3;
        };
        return addFunc2(4) + funcInt1;
    };
    return addFunc1(6);
}
