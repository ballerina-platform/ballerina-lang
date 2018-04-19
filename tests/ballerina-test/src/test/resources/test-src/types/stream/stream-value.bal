import ballerina/log;
import ballerina/time;

stream<Employee> globalEmployeeStream;

type Employee {
    int id,
    string name,
};

type Job {
    string description,
};

function testInvalidStreamDeclaration () {
    stream t1;
}

function testInvalidObjectPublishingToStream () {
    stream<Employee> s1;
    Job j1 = { description:"Dummy Description 1" };
    s1.publish(j1);
}

function testSubscriptionFunctionWithIncorrectObjectParameter () {
    stream<Employee> s1;
    s1.subscribe(printJobDescription);
}

Employee globalEmployee;
Employee[] globalEmployeeArray = [];
int employeeIndex = 0;

function testGlobalStream () returns (Employee, Employee, Employee) {
    Employee origEmployee = globalEmployee;
    globalEmployeeStream.subscribe(assignGlobalEmployee);
    Employee publishedEmployee = { id:5678, name:"Maryam" };
    globalEmployeeStream.publish(publishedEmployee);
    int startTime = time:currentTime().time;
    while (globalEmployee.id == 0 && time:currentTime().time - startTime < 1000) {
        //allow for value update
    }
    Employee newEmployee = globalEmployee;
    return (origEmployee, publishedEmployee, newEmployee);
}

function testStreamPublishingAndSubscriptionForObject () returns (Employee, Employee, Employee) {
    Employee origEmployee = globalEmployee;
    stream<Employee> s1;
    s1.subscribe(assignGlobalEmployee);
    Employee publishedEmployee = { id:1234, name:"Maryam" };
    s1.publish(publishedEmployee);
    int startTime = time:currentTime().time;
    while (globalEmployee.id == 0 && time:currentTime().time - startTime < 1000) {
        //allow for value update
    }
    Employee newEmployee = globalEmployee;
    return (origEmployee, publishedEmployee, newEmployee);
}

function testStreamPublishingAndSubscriptionForMultipleObjectEvents () returns (Employee[], Employee[]) {
    stream<Employee> s1;
    s1.subscribe(addToGlobalEmployeeArray);
    Employee e1 = { id:1234, name:"Maryam" };
    Employee e2 = { id:2345, name:"Aysha" };
    Employee e3 = { id:3456, name:"Sumayya" };
    Employee[] publishedEmployees = [e1, e2, e3];
    s1.publish(e1);
    s1.publish(e2);
    s1.publish(e3);
    int startTime = time:currentTime().time;
    while (lengthof globalEmployeeArray < 3 && time:currentTime().time - startTime < 3000) {
        //allow for value update
    }
    return (publishedEmployees, globalEmployeeArray);
}

function printJobDescription (Job j) {
    log:printInfo(j.description);
}

function assignGlobalEmployee (Employee e) {
    globalEmployee = e;
}

function addToGlobalEmployeeArray (Employee e) {
    globalEmployeeArray[employeeIndex] = e;
    employeeIndex = employeeIndex + 1;
}
