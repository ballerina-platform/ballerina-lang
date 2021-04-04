import ballerina/module1;

function testFunction() {
    module1:TestClass1 c1 = check new module1:T
}

function doTask() returns int {
    return 1;
}
