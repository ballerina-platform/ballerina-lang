error ex;

function test1 () {
    Foo|error k = <Foo> bar;
    match k {
        Foo x => string result1 = x + ex;
        error y => string result2 = y + ex;
    }
}

function test2(){
    int foo = 10;
    Float|error k = <Float> foo;
    match k {
        Float x => string result1 = x + ex;
        error y => string result2 = y + ex;
    }
}

function test3(){
    string|error k = <string> foo;
    match k {
        string x => string result1 = x + ex;
        error y => string result2 = y + ex;
    }
}

function test4 () {
    Foo|error k = <Foo> bar;
    match k {
        Foo x => string result1 = x + ex;
        error y => string result2 = y + ex;
    }
}

function test5(){
    int foo = 10;
    Float|error k = <Float> foo;
    match k {
        Float x => string result1 = x + ex;
        error y => string result2 = y + ex;
    }
}

function test6(){
    string|error k = <string> foo;
    match k {
        string x => string result1 = x + ex;
        error y => string result2 = y + ex;
    }
}

function test7 () {
    Float|error|() x = <Float> fooo;
}

function test8 () {
    float|error|() x = <float> 10;
}

function test9(){
    any a = 1;
    string|error|() x = <string> a;
}

function test10 () {
    var x = <Foo> bar;
    string result1 = x + ex;
}