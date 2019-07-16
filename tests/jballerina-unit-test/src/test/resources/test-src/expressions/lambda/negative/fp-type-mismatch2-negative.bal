function test1() returns (boolean) {
     function (string, int) returns (boolean) foo = function (string x, boolean y) returns (boolean){
                                             return true;
                                           };
     return foo("this test fails at line", 2);
}
