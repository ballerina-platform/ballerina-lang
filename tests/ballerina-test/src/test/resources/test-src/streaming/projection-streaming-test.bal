import ballerina.io;
import ballerina.runtime;

struct Employee {
    string name;
    int age;
    string status;
}

struct Teacher {
    string name;
    int age;
    string status;
    string batch;
    string school;
}

Employee[] globalEmployeeArray = [];
int employeeIndex = 0;
stream<Employee> employeeStream = {};
stream<Teacher> teacherStream = {};

streamlet projectionStreamlet () {
    query q1 {
        from teacherStream
        select name, age, status
        insert into employeeStream
    }
}


function testProjectionQuery () (Employee []) {

    projectionStreamlet pStreamlet = {};

    Teacher t1 = {name:"Raja", age:25, status:"single", batch:"LK2014", school:"Hindu College"};
    Teacher t2 = {name:"Shareek", age:33, status:"single", batch:"LK1998", school:"Thomas College"};
    Teacher t3 = {name:"Nimal", age:45, status:"married", batch:"LK1988", school:"Ananda College"};

    employeeStream.subscribe(printEmployeeNumber);

    teacherStream.publish(t1);
    teacherStream.publish(t2);
    teacherStream.publish(t3);

    runtime:sleepCurrentWorker(1000);
    return globalEmployeeArray;
}

function printEmployeeNumber (Employee e) {
    io:println("printEmployeeName function invoked for Employee event for Employee employee name:" + e.name);
    addToGlobalEmployeeArray(e);
}

function addToGlobalEmployeeArray (Employee e) {
    globalEmployeeArray[employeeIndex] = e;
    employeeIndex = employeeIndex + 1;
}