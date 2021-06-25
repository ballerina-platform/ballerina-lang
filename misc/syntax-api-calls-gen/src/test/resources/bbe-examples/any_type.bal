import ballerina/io;
import ballerina/math;
import ballerina/time;

class Person {
    string fname;
    string lname;

    function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

// This function returns a value of the `any` type.
function lookupInfo(string id) returns any {
    if id == "pi" {
        return math:PI;
    } else if id == "date" {
        return time:currentTime().toString();
    } else if id == "bio" {
        return new Person("Jane", "Doe");
    }
}

public function main() {
    // In this example, the variable named `any1` of the `any` type holds
    // a `Person` object.
    any any1 = new Person("John", "Doe");

    // Before anything useful can be done with `any1`, it is required to ascertain
    // its type. A type cast or a type guard can be used for this.
    Person john = <Person> any1;
    io:println("Full name: ", john.getFullName());

    if any1 is Person {
        io:println("First name: ", any1.fname);
    }

    // Variables of type `any` can hold values of any type except for `error`.
    int[] intArray = [1, 3, 5, 6];
    any anyArray = intArray;
    io:println(anyArray);

    io:println(lookupInfo("pi"));
    io:println(lookupInfo("date"));
    Person person = <Person> lookupInfo("bio");
    io:println(person.getFullName());
}
