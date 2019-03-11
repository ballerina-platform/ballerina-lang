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