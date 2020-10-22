public function foo() {
    int i = 0;
    while (true) {
        if (i == 3) {
            i = i + 2;
            continue;
        }
        i = i + 1;
    }
}
