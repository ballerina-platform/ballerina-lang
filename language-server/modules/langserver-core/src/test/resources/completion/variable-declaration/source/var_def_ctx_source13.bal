type MyType record {|
    int a;
    int b;;
|};

function getInt()  returns int {
    return 1;
}

function getString() returns string {
    return "hello";
}

function myFunc(int a, string b, function () returns int func, MyType myType) {
    int var1 = 1;
    string var2 = "hello";
    int myInt =
}
