import ballerina.io;

@Description {value:"Defining Person struct."}
struct Person {
    string name;
    int age;
    string city;
}

function main (string[] args) {
    // Create a new JSON which is constrained by the Person struct. Only the
    // fields defined in the Person struct can be accessed. If we try to
    // access a non existing field, it will produce a compilation error.
    json<Person> person = {name:"Jon"};

    // We can access fields defined in the Person struct without any issue.
    person.age = 25;
    person.city = "Colombo";
    io:println(person);

    // We can assign this Constrained JSON to a JSON. This will allow us to
    // add new elements which are not in the struct.
    json employee = person;

    // After that, we can add new elements to the json.
    employee.profession = "Software Engineer";
    io:println(employee);
}
