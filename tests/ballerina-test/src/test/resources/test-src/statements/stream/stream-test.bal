import ballerina.io;
import ballerina.runtime;

struct Person {
    string name;
    int age;
    string status;
}

struct Office {
    string name;
    int age;
}

stream<Employee> s1 = {};
stream<Employee> s2 = {};
stream<Person> testStream = {};
stream<Office> officeStream = {};

streamlet smohan() {

    query q1 {

    from testStream
    where age > 0
    select name, age
    group by name
    insert into officeStream
    }
}


function test () (int) {

    smohan s1Object = {};

    Employee e1 = {name:"Maryam", employeeNumber:25163};
    io:println("Created struct 'e1' for Employee: " + e1.name);
    Employee e2 = {name:"Aysha", employeeNumber:12344};
    io:println("Created struct 'e2' for Employee: " + e2.name);

    officeStream.subscribe(printName);
    io:println("Subscribed to stream 's1' with function printName");

    Person p1 = {name:"Mohan", age:29, status:"Hello"};
    testStream.publish(p1);




    //io:println("Created stream 's1' accepting Employees");
    //
    //
    //io:println("Created stream 's2' accepting Employees");
    //
    //s1.subscribe(printName);
    //io:println("Subscribed to stream 's1' with function printName");
    //
    //s2.subscribe(printEmployeeNumber);
    //io:println("Subscribed to stream 's2' with function printEmployeeNumber\n************************************************************");
    //
    //s1.publish(e1);
    //io:println("Published 'e1' to stream 's1'");
    runtime:sleepCurrentWorker(1000);
    //s2.publish(e2);


    io:println("Published 'e2' to stream 's2'");
    runtime:sleepCurrentWorker(1000);

    return 0;
}

struct Employee {
    string name;
    int employeeNumber;
}

function printName (Office e) {
    io:println("printName function invoked for Employee event for Employee name: " + e.name  + e.age);
}

function printEmployeeNumber (Employee e) {
    io:println("printEmployeeNumber function invoked for Employee event for Employee employeeNumber:" + e.employeeNumber);
}