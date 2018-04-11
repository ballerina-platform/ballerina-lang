function (string a, int b) returns (string) glf1 = foo;

function (string a, boolean b) returns (string) glf2 = (string a, boolean b) => (string){
                                                               return a + b;
                                                           };

function (string, int) returns (string) glf3 = (string a, int b) => (string) {
    return "llll";
};

function foo (string x, int y) returns (string) {
    string result = x + y;
    return result;
}

function fooReverse (string x, int y) returns (string) {
    string result = y + x;
    return result;
}

function test1() returns (string){
    return glf1("test",1);
}

function test2() returns (string){
    return glf2("test2", true);
}

function test3() returns (string, string, string){
    glf3 = foo;
    string x = glf3("test",3);
    string y = foo("test",3);
    glf3 = fooReverse;
    string z = glf3("test",3);
    return (x, y, z);
}

function bar (string x, boolean y) returns (string) {
    string result = y + x;
    return result;
}

function test5() returns (string){
    function (string a, boolean b) returns (string) glf1 = bar;
    return glf1("test5", false);
}
function test6() returns (string) {
    function (string a, boolean b) returns (string) glf1 = bar;
    glf2 = glf1;
    return glf2("test6", true);
}

