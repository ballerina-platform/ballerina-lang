int i;
string s;
int a;


function __init() {
    i = foo();
}

function foo() returns int {
    a = 8;
    return 8;
}

function testModuleVarDeclNegative() {
    int x = a;
    string str = s;
}