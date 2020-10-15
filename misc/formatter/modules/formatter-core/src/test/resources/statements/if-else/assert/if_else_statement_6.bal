public function foo() {
    if (true) {
        (string|error) txt = rp.getTextPayload();
        // 2nd level if block
        if (txt is string) {
            io:println("response.../n", txt);
        }
    }
}

function bar() {
    int a = 9;
    // hello
}
