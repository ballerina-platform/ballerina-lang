import ballerina.io;
import ballerina.time;

struct Employee {
    int id;
    string name;
}

struct Job {
    string description;
}

Employee globalEmployee;
Employee[] globalEmployeeArray = [];
int employeeIndex = 0;

function testStreamPublishingAndSubscription () (Employee origEmployee, Employee publishedEmployee,
                                                Employee newEmployee) {
    stream<Employee> s2 = {};
    s2.subscribe(function (Employee e) {
                     globalEmployee = e;
                 });
    origEmployee = globalEmployee;
    stream<Employee> s1 = {};
    s1.subscribe(function (Employee e) {
        globalEmployee = e;
    });
    publishedEmployee = { id:1234, name:"Maryam" };
    s1.publish(publishedEmployee);
    int startTime = time:currentTime().time;
    while (globalEmployee == null && time:currentTime().time - startTime <1000) {
        //allow for value update
    }
    newEmployee = globalEmployee;
    return;
}
