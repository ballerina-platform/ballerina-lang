function test() {
    string[] x = bar([2], (i) => foo(i));
}

function bar(int[] i, function(int val) returns string func) returns string[] {
    return [func(2)];
}
