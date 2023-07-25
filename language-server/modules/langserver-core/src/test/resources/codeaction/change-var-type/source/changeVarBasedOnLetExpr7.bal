import ballerina/module1;

function foo() {
    int i = let string x = "s" in module1:function7(1, 2);
}

function getString(string s) returns string {
    return s;
}
