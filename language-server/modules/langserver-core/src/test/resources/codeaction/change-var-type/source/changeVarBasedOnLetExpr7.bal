import ballerina/module1;

function foo() {
    string i = let string x = "s" in module1:function3(1, 2, 1.1);
}

function getString(string s) returns string {
    return s;
}
