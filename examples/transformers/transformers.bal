import ballerina/io;

@Description {value:"Define an `Employee` struct."}
type Employee {
    string name;
    int age;
    string address;
};

@Description {value:"Define a `Person` struct."}
type Person {
    string firstName;
    string lastName;
    int age;
    string city;
    string street;
};

@Description {value:"Define a default transformer to converting from `Person` type to `Employee` type."}
transformer <Person p, Employee e> {
    e.name = p.firstName + " " + p.lastName;
    e.age = p.age;
    e.address = p.street + "," + p.city.toUpperCase();
}

@Description {value:"Define a named transformer for converting from `Person` type to `Employee` type."}
transformer <Person p, Employee e> setCityToNewYork() {
    e.name = p.firstName + " " + p.lastName;
    e.age = p.age;
    e.address = p.street + ", " + "New York";
}

@Description {value:"Define a named transformer which takes input parameters for converting from `Person` type to `Employee` type."}
transformer <Person p, Employee e> insertCountry(string country) {
    e.name = p.firstName + " " + p.lastName;
    e.age = p.age;
    e.address = p.street + "," + p.city.toUpperCase() + ", " + country;
}

function main (string[] args) {
    //Initialize`Person` variable person.
    Person person = {firstName:"John", lastName:"Doe", age:30, city:"London"};

    //Using the default transformer to convert from type `Person` to type `Employee` is similar to the conversion syntax.
    Employee employee = <Employee>person;
    io:println(employee);

    //The named transformer can be explicitly provided inside the conversion syntax, to convert `Person` to `Employee`.
    employee = <Employee, setCityToNewYork()>person;
    io:println(employee);

    //Using the named transformer to convert `Person` to `Employee` by passing parameters.
    employee = <Employee, insertCountry("UK")>person;
    io:println(employee);
}
