import ballerina.doc;
import ballerina.lang.system;

@doc:Description {
    value:"Defining Person struct."
}
struct Person {
    string name;
    int age;
    string city;
}

function main (string[] args) {
    // Create a new json which is constrained by Person struct.
    json<Person> person = {};

    // We can access fields defined in the Person struct without any issue.
    person.name = "Jon";
    person.age = 25;
    person.city = "Colombo";
    system:println(person);

    // If we access a field which is not defined in the Person struct, it will cause a compilation error. Eg:-
    // person.address = "";
}
