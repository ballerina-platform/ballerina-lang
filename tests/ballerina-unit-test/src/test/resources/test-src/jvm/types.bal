function testIntWithoutArgs() returns int {
   int b = 7;
   return b;
}

function testIntWithArgs(int a) returns int {
   int b = 5 + a;
   return b;
}

function testStringWithoutArgs() returns string {
   string s1 = "Hello";
   return s1;
}

function testStringWithArgs(string s) returns string {
   string s1 = "Hello" + s;
   return s1;
}

function testArray(string str) returns int[] {
    //string[] s1 = [str];
    int[] a = [];
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    int[5] e = [1, 2, 3, 4, 5];
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    a[0] = b[2];
    return a;
}