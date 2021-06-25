import ballerina/module1;

type E1 error<map<anydata>>;
type E2 error<map<anydata>>;
type E3 E1|E2;
type T4 E1|int;

enum Colour {
    RED, GREEN, BLUE
}

const CONST1 = "Test Const";

function bar (string | int | boolean i)  returns (string | int | boolean) {
    string | int | boolean var1 =  "Test";
    [string, int, boolean] var2 = ["Foo", 12, true];
    int var4 = 12;
    string var5 = "test";
    Person p = { name : "Tom", age : 10};

    match var4 {
        v
    }

    return 1;
}

type Person record {
    string name;
    int age;
};
