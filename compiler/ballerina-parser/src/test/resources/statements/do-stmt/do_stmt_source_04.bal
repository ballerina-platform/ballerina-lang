public function foo() {
    int i = 0;
    do
        i = i + 1;
        error err = error("Custom error thrown explicitly.");
        fail err;
}
