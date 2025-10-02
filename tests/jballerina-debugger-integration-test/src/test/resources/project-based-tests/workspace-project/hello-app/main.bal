import debugger_wp_tests/pkgA;

public function main() {
    int a = 5;
    int b = 10;
    int c = a + b;

    pkgA:foo(c);
    int d = c * 2;
}
