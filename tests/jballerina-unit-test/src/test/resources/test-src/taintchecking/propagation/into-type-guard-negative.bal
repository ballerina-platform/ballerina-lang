public function main(string... args) {
    [string, int]|error x = foo(args);
    if (x is [string, int]) {
        [string, int][s, _] = x;
        sec(s);
    }
}

function foo(string[] str) returns [string, int]|error {
    return [str[0], 1];
}

function sec(@untainted string arg) {

}
