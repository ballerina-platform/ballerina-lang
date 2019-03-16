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

int globalVar = 7;

function getGlobalVar() returns int {
    return globalVar;
}

function testArray(string str) returns int {
    int[] a = [];
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8];
    string[3] e = ["c", "d", "e"];
    int[][] iarray = [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    Grades[] gs = arrayFunc(e);
    a[0] = b[2];
    return a[0];
}

public type Grades record {
   string name;
   int physics;
   int chemistry?;
};

function arrayFunc(string[] strs) returns Grades[] {
    Grades g = {name: strs[0], physics: 75, chemistry: 65};
    Grades g1 = {name: strs[1], physics: 75, chemistry: 65};
    Grades g2 = {name: strs[2], physics: 75, chemistry: 65};

    Grades[] grds = [g,g1,g2];
    return grds;
}