import ballerina/module1;

function testAnon() {
    function (int) returns string myFunc = function(int arg)  returns string {return "abc";};
    string result  = myFunc().
}
