function test1() returns (boolean) {
     function (string, int) returns (boolean) foo = test2;
     return foo("this test fails at line", 2);
}

function test2(string a, float b) returns (boolean){
    return false;
}