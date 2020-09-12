function bar(@untainted string? arg) {

}

function baz() returns @tainted string {
    return "";
}

function foo(string s, string... rest) {
    bar(rest[0]);
}

public function main(string... argv) {
    foo(argv[0], argv[1]);
}

class Obj {
    function bar(@untainted string... arg) {

    }

    function bar2(string arg0, @untainted string... argv) {

    }

    function foo(string s, string... rest) {
        self.bar(baz());
        self.bar2("", baz());
    }

}
