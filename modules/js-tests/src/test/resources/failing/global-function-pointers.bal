function (string a, int b) returns (string add) glf1 = foo;

function (string a, boolean b) returns (string add) glf2 = function (string a, boolean b) returns(string){
                                                               return a + b;
                                                           };

function (string, int) returns (string) glf3;

function foo (string x, int y) returns (string result) {
    result = x + y;
    return;
}

function fooReverse (string x, int y) returns (string result) {
    result = y + x;
    return;
}

function test1()(string){
    return glf1("test",1);
}

function test2()(string){
    return glf2("test2", true);
}

function test3()(string x, string y, string z){
    glf3 = foo;
    x = glf3("test",3);
    y = foo("test",3);
    glf3 = fooReverse;
    z = glf3("test",3);
    return;
}

function test4()(string){
    glf3 = null;
    return glf3("test",4);
}

function bar (string x, boolean y) returns (string result) {
    result = y + x;
    return;
}

function test5()(string){
    function (string a, boolean b) returns (string add) glf1 = bar;
    return glf1("test5", false);
}
function test6()(string) {
    function (string a, boolean b) returns (string add) glf1 = bar;
    glf2 = glf1;
    return glf2("test6", true);
}

