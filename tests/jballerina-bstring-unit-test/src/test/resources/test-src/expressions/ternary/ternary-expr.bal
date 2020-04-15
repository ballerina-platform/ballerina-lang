function test1 (int value) returns (int) {
    int x = value > 10 ? 15 : 5;
    return x;
}

function test2 (int value) returns (string) {
    var x = 10 <= value ? "large" : "small";
    return x;
}

function test3 (int value) returns (float) {
    float x = value == 10 ? 10.5 : 9.5;
    return x;
}

function test4 (int value) returns (string) {
    if (value == 20 ? true : false) {
        return "if";
    }
    return "else";
}

function test5 (int value) returns (string) {
    return foo(10, value == 10 ? "ten" : "other", value != 10);
}

function foo (int a, string b, boolean c) returns (string) {
    return a.toString() + b + c.toString();
}

type Person record {
    string name;
    string location?;
};

function test6 (string s) returns (string) {
    Person p = {name : s == "admin" ? "super" : "tom"};
    return p.name;
}

function test7 (string s) returns (int|error) {
    map<any> m = {"data" : s == "one" ? 1 : 2};
    var y = <int>m["data"];
    return y;
}

function test8 (string s) returns (string) {
    string x = string `hello ${s == "world" ? "world...!!" : "everyone..!"}`;
    return x;
}

function test9 (string s) returns (string) {
    return s == "a" ? bax() : bar();
}

function bax () returns (string) {
    return "bax";
}

function bar () returns (string) {
    return "bar";
}

function test10 (string s) returns (Person) {
    Person tom = {name : "tom", location : "US"};
    Person bob = {name : "bob", location : "UK"};
    return s == "tom" ? tom : bob;
}

function testNestedTernary1 (int value) returns [string, string] {
    string s1 = value > 70 ? "morethan70" : value > 40 ? "morethan40" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 70 ? "morethan70" : (value > 40 ? "morethan40" : (value > 20 ? "morethan20" : "lessthan20"));
    return [s1, s2];
}

function testNestedTernary2 (int value) returns [string, string] {
    string s1 = value > 40 ? value > 70 ? "morethan70" : "lessthan70" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 40 ? (value > 70 ? "morethan70" : "lessthan70") : (value > 20 ? "morethan20" : "lessthan20");
    return [s1, s2];
}

function testNestedTernary3 (int value) returns [string, string] {
    string s1 = value < 40 ? value > 20 ? value < 30 ? "lessthan30" : "morethan30" : "lessthan20" : value > 45 ? "morethan45" : "lessthan45";
    string s2 = value < 40 ? (value > 20 ? (value < 30 ? "lessthan30" : "morethan30") : "lessthan20") : (value > 45 ? "morethan45" : "lessthan45");
    return [s1, s2];
}

function testNestedTernary4 (int value) returns [string, string] {
    string s1 = value > 40 ? value > 70 ? "morethan70" : value > 50 ? "morethan50" : "lessthan50" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 40 ? (value > 70 ? "morethan70" : (value > 50 ? "morethan50" : "lessthan50")) : (value > 20 ? "morethan20" : "lessthan20");
    return [s1, s2];
}

function testNestedTernary5 (int value) returns [string, string] {
    string s1 = value > 50 ? "morethan50" : value > 30 ? value > 40 ? "morethan40" : "lessthan40" : value > 20 ? "morethan20" : "lessthan20";
    string s2 = value > 50 ? "morethan50" : (value > 30 ? (value > 40 ? "morethan40" : "lessthan40") : (value > 20 ? "morethan20" : "lessthan20"));
    return [s1, s2];
}

function testErrorInTernary() returns int|error {
    int x = 8;
    var result = x > 5 ? decrement(x) : increment(x);
    return result;
}

function decrement(int i) returns error|int {
    return i - 1;
}

function increment(int i) returns error|int {
    return i + 1;
}
