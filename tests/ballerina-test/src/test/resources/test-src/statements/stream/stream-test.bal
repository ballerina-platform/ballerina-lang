import ballerina.io;
import ballerina.runtime;

stream<Employee> s1 = {};
                 stream<Employee> s2 = {};

function test () (int) {

    Employee e1 = {name:"Maryam", employeeNumber:25163};
    io:println("Created struct 'e1' for Employee: " + e1.name);
    Employee e2 = {name:"Aysha", employeeNumber:12344};
    io:println("Created struct 'e2' for Employee: " + e2.name);


    io:println("Created stream 's1' accepting Employees");


    io:println("Created stream 's2' accepting Employees");

    s1.subscribe(printName);
    io:println("Subscribed to stream 's1' with function printName");

    s2.subscribe(printEmployeeNumber);
    io:println("Subscribed to stream 's2' with function printEmployeeNumber\n************************************************************");

    s1.publish(e1);
    io:println("Published 'e1' to stream 's1'");
    runtime:sleepCurrentWorker(1000);
    s2.publish(e2);
    io:println("Published 'e2' to stream 's2'");
    runtime:sleepCurrentWorker(1000);

    return 0;
}

struct Employee {
    string name;
    int employeeNumber;
}

function printName (Employee e) {
    io:println("printName function invoked for Employee event for Employee name: " + e.name);
}

function printEmployeeNumber (Employee e) {
    io:println("printEmployeeNumber function invoked for Employee event for Employee employeeNumber:" + e.employeeNumber);
}