import ballerina/module1;

function testFunction() returns Employee {
    Employee|int|string testVar = {
           
    }
}

type Employee record {
    readonly record {
        string first;
        string last;
    } name;
    int salary;
};
