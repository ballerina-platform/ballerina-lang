function test1(){
    int i = 0;
    int x = 0;
    while (i < 5) {
        i  = i + 1;
        if(i > 2){
            next;
        }
    }
}

function test2(){
    int x = 0;
    foo;
    bar();
    x;
}

function baz() returns error|int|() => ();

function testChecking() returns error? {
    check baz();
    checkpanic baz();

    int|error x;
    check x;
    checkpanic x;
}

function testErrorConstructor() {
    error();
}
