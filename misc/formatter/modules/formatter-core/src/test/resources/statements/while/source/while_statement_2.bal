public function foo() {
    int i = 0;
    while (i < 3) {
        i = i + 1;
    }
    while (true) {
        i = 6;
    }

    on    fail   var   e   {
       // this will be executed if the block-stmt following do fails
       // which will happen if and only if one of the two
       // check actions fails
       // type of e will be union of the error types that can be
       // returned by foo and bar

        io:println("whoops");
           return e;}
}
