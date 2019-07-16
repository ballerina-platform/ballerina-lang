function testClosureWithTupleTypes([string, float, string] g) returns (function (string, [string, float, string]) returns (string)){
    return function (string x, [string, float, string] y) returns (string) {
       var [i, j, k] = y;
       var [l, m, n] = g;
       return x + i + j + k + l + m + 
    };
}