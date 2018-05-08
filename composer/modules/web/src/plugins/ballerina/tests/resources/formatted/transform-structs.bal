import ballerina/lang.strings;
import ballerina/lang.system;
import ballerina/doc;

@doc:Description {value:"Defining Employee struct"}
struct Employee {
    string name;
    int age;
    string address;
}

@doc:Description {value:"Defining Person struct"}
struct Person {
    string first_name;
    string last_name;
    int age;
    string city;
}

function main (string... args) {
    //Initialize Person variable person
    Person person = {first_name:"John", last_name:"Doe", age:30, city:"London"};

    //Initialize an empty Employee as employee
    Employee employee = {};

    //Transform statement using person as input to transform values of employee which is the output
    transform {
        // Convert city of person to upper case and assign to address of employee
        employee.address = strings:toLowerCase(person.city);
        // Convert first name of person to lower case and assign to name of employee
        employee.name = strings:toLowerCase(person.first_name);
        // Assign age of person to age of employee
        employee.age = person.age;
    }

    //Print employee details
    system:println("Employee details : ");
    system:println("Name : " + employee.name);
    system:println("Address : " + employee.address);
    system:println("Age : " + employee.age);
}
