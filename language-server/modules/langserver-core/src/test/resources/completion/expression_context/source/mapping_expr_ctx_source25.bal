import ballerina/module1;

function testFunction() returns Employee {
    Employee[] emArray = [{
        
    }];
}

type Employee record {
    readonly record {
        string first;
        string last;
    } name;
    int salary;
};
