import ballerina/io;

function getStringFunc1(string functionX) returns (function (string) returns (function (string) returns (string))) {
    return (string functionY) => (function (string) returns (string)) {
        return (string functionZ) => (string) {
            return functionY + functionX + functionZ + functionR;
        };
    };
}

function threeLevelTest() returns (int) {
    var addFunc1 = (int funcInt1) => (int) {
        var addFunc2 = (int funcInt2) => (int) {
            var addFunc3 = (int methodInt3, int methodInt2, int methodInt1, int funcInt3) => (int) {
                return funcInt3 + methodInt1 + methodInt2 + methodInt3;
            };
            return addFunc3(7, 23, 2, 8) + methodInt3;
        };
        return addFunc2(4) + funcInt1;
    };
    return addFunc1(6);
}
