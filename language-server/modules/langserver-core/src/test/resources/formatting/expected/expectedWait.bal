import ballerina/io;
import ballerina/runtime;

function waitTest1() returns any {
    future<int> f1 = start add_panic(20, 66);
    any result = wait {f1};
    return result;
}

function waitTest27() returns map<anydata> {
    future<int> f1 = start add_1(100, 100);
    future<string> f2 = start concat("mello");
    future<string> f3 = start concat("sunshine");

    record {|
        int id = 0;
        string name = "default";
        string...;
    |} anonRec = wait {id: f1, name: f2, greet: f3};

    map<anydata> m = {
    };
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    m["greet"] = anonRec.greet;
    return m;
}

function concat(string name) returns string {
    return "hello " + name;
}

function add_1(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 9999999) {
        l = l + 1;
    }
    return k;
}

function add_panic(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 9999999) {
        l = l + 1;
    }
    if (true) {
        error err = error("err from panic");
        panic err;
    }
    return k;
}

function waitTest3() returns any {
    future<int> f1 = start add_1(5, 2);
    future<int> f3 = start add_1(5, 2);
    future<string> f5 = start concat("bar");

    any result = wait {f1, f3, f5};
    return result;
}

function waitTest2() returns map<int|string> {
    future<int> f1 = start add_1(5, 2);
    future<string> f4 = start concat("foo");
    future<int> f3 = start add_1(5, 2);
    future<int|string> f7 = start concat("xyz");

    map<int|string> result =
    wait
    {
    f1
    ,
    str1
    :
    f4
    ,
    f3
    :
    f3
    ,
    str2
    :
    f7
    }
    ;
    return result;
}

function waitTest4() returns int {
    future<int> f1 = start add_1(5, 2);
    future<int> f2 = start add_1(10, 12);
    int result = wait f1 | f2;
    return result;
}

function waitTest5() returns int {
    future<int> f1 = start add_1(5, 2);
    future<int> f2 = start add_1(10, 12);
    int result =
    wait
    f1
    |
    f2
    ;
    return result;
}

function waitTest6() returns int {
    future<int> f1 = start add_1(5, 2);
    int result = wait f1;
    return result;
}

function waitTest7() returns int {
    future<int> f1 = start add_1(5, 2);
    int result =
    wait
    f1
    ;
    return result;
}

function waitTest8() returns int|string {
    future<int|string> f7 = start concat("xyz");
    int|string result = wait fuInt() | f7;
    return result;
}

function waitTest9() returns int|string {
    future<int|string> f7 = start concat("xyz");
    int|string result =
    wait
    fuInt()
    |
    f7
    ;
    return result;
}

function waitTest10() returns int|string|boolean {
    worker w1 returns int {
        future<int> f1 = start add_1(5, 2);
        future<int> f2 = start add_1(50, 100);
        runtime:sleep(3000);
        int r = wait f1 | f2;
        return r;
    }
    worker w2 returns int|string {
        future<int> f1 = start add_1(50, 10);
        future<string> f2 = start concat("foo");
        int|string r = wait f1 | f2;
        return r;
    }
    worker w3 returns int|string|boolean {
        future<int> f1 = start add_1(6, 6);
        future<string> f2 = start concat("bar");
        future<boolean> f3 = start status();
        int|string|boolean r = wait f1 | f2 | f3;
        runtime:sleep(2000);
        return r;
    }
    int|string|boolean result = wait w1 | w2
    |
    w3
    ;
    return result;
}

function waitTest11() returns map<anydata> {
    future<string> f5 = start concat("foo");
    record {int id; string name;} anonRec = wait {id: fuInt(), name: f5};

    map<anydata> m = {};
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    return m;
}

function waitTest12() returns map<anydata> {
    future<string> f5 = start concat("foo");
    record {int id; string name;} anonRec =
    wait
    {
    id
    :
    fuInt()
    ,
    name
    :
    f5
    }
    ;

    map<anydata> m = {};
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    return m;
}
