function returnAnonFunc() returns function (int i, string s) returns int {
    function (int i, string s) returns int anonFunc = function (int i, string s) returns int {
        return 0;
    };

    return anonFunc;
}

public function main() {
    int intVal = 2;
    string strVal = "Hello World";

    function (int i, string s) returns int anonFunc = function (int i1, string s1) returns int {
        return 1;
    };

    function (int i, string s) returns int testFunc = 
}
