function foo() {
    wait a;
    wait a | b | c;
    wait a | b + c | bar();

    x = wait a;
    x = wait a | b | c;
    x = wait a | b + c | bar();
}
