import ballerina.lang.system;
import ballerina.doc;

@doc:Description {value:"Defining Person struct."}
struct Person {
    string name;
    int age;
    string city;
}

function main (string[] args) {
    // Create a new json which is constrained by Person struct. Only the fields
    // defined in the Person struct can be accessed. If we try to access a non
    // existing field, it will produce a compilation error.
    json<Person> person = {name:"Jon"};

    // We can access fields defined in the Person struct without any issue.
    person.age = 25;
    person.city = "Colombo";
    system:println(person);

    // We can cast this constrained json to a json.
    json employee = (json)person;
    // After that, we can add new elements to the json.
    employee.profession = "Software Engineer";
    system:println(employee);
}
