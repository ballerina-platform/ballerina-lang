import asmaj/pkgB;

public function main() {
    int a = 5;
    int b = 10;
    int c = a + b;

    foo(c);
}

public function foo(int x) {
    string msg = "The sum is: " + x.toString();
    pkgB:foo(x);
}
