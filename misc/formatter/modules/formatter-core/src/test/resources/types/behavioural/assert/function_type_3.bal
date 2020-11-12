final int x = 1;

final map<int> & readonly y = {
    one: 1,
    two: 2
};

isolated function foo(int i, map<int> j) returns int {
    int a = i; // ok, `i` is a param
    int b = j["two"] ?: 2; // ok, even though `j` is mutable, it is a param

    int c = x; // ok, even though we access a module-level var, it is final and immutable
    int? d = y["one"]; // ok, even though we access a module-level var, it is final and immutable

    int e = bar(); // ok, since `bar` is also `isolated`

    Baz baz = new;
    int f = baz.getInt(); // ok, since `baz` is local variable and `getInt` of `Baz` is `isolated`

    return a + b + c + <int>d + e + f; // ok, can return a value
}

isolated function bar() returns int => 34;
