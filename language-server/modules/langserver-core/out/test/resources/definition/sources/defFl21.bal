function twoLevelTest() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = function (int funcInt1) returns (int) {
        int methodInt2 = 23;
        var addFunc2 = function (int funcInt2) returns (int) {
            return funcInt2 + methodInt1 + methodInt2 + funcInt1;
        };
        return addFunc2.call(5) + funcInt1;
    };
    return addFunc1;
}