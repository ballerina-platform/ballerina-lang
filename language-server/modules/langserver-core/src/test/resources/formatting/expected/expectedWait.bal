import ballerina/runtime;
import ballerina/io;

function waitTest1() returns fourthRec {
    future<int> f1 = start add_panic(20, 66);
    fourthRec result = wait {id: f1};
    return result;
}

function waitTest27() returns map<anydata> {
    future<int> f1 = start add_1(100, 100);
    future<string> f2 = start concat("mello");
    future<string> f3 = start concat("sunshine");

    record {
        int id = 0;
        string name = "default";
        string...;
    } anonRec = wait {id: f1, name : f2, greet: f3};

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
