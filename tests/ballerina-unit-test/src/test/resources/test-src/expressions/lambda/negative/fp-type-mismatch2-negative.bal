function test1() returns (boolean) {
     function (string, int) returns (boolean) foo = (string x, boolean y) => (boolean){
                                             return true;
                                           };
     return foo("this test fails at line", 2);
}
