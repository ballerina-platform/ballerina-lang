function (string a, int b) returns (string) glf1 = foo;

function (string a, boolean b) returns (string) glf2 = function (string a, boolean b) returns (string) {
                                                               return a + b.toString();
                                                           };

function (string, int) returns (string) glf3 = function (string a, int b) returns (string) {
    return "llll";
};

function foo (string x, int y) returns (string) {
    string result = x + y.toString();
    return result;
}

function fooReverse (string x, int y) returns (string) {
    string result = y.toString() + x;
    return result;
}

function test1() returns (string) {
    return glf1("test",1);
}

function test2() returns (string){
    return glf2("test2", true);
}

function test3() returns [string, string, string] {
    glf3 = foo;
    string x = glf3("test",3);
    string y = foo("test",3);
    glf3 = fooReverse;
    string z = glf3("test",3);
    return [x, y, z];
}

function bar (string x, boolean y) returns (string) {
    string result = y.toString() + x;
    return result;
}

function test5() returns (string){
    function (string a, boolean b) returns (string) glf4 = bar;
    return glf4("test5", false);
}
function test6() returns (string) {
    function (string a, boolean b) returns (string) glf4 = bar;
    glf2 = glf4;
    return glf2("test6", true);
}
