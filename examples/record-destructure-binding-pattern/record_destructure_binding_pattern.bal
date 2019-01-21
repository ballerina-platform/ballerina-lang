import ballerina/io;

type Person record {
    string name;
    int age;
    string country;
};

type Employee record {
    string name;
    int age;
    !...;
};

type Country record {
    string name;
    Capital capital;
};

type Capital record {
    string name;
    !...;
};

public function main() {

    string firstName;
    int personAge;
    map<anydata> otherDetails = {};
    // This record de-structure binding pattern will destructure a `record` of type `Person` and assign the values
    // to three variable references as follows:
    // The value of the field `name` in the `Person` record will be assigned to the variable `firstName`.
    // The value of the field `age` in the `Person` record will be assigned to the variable `personAge`.
    // `...otherDetails` is a rest parameter. Since `Person` is an open record, the remaining field values that have
    // not been matched in the record binding pattern, will be assigned as a `map<anydata>` to the variable `otherDetails`.
    { name: firstName, age: personAge, ...otherDetails } = getPerson();
    io:println("Name: " + firstName);
    io:println("Age: " + personAge);
    io:println("Other Details: " + io:sprintf("%s", otherDetails));

    string name;
    int age;
    // If no variable name is given for a field, the value of the field will be assigned to a variable reference of the same name as the field.
    // i.e. {name, age} is same as {name: name, age: age}.
    // Since a rest parameter is not given, all remaining fields are ignored.
    { name, age } = getPerson();
    io:println("Name: " + name);
    io:println("Age: " + age);

    string empName;
    int empAge;
    // The `!...` symbol will specify that there should not be any other fields other than `name` and `age`, hence
    // `Employee` should be a closed record.
    { name: empName, age: empAge, !... } = getEmployee();
    io:println("Name: " + empName);
    io:println("Age: " + empAge);

    string countryName;
    string capitalName;
    // Binding patterns are recursive in nature. `Capital`, which is a field type of `Country` can also be destructured as follows:
    { name: countryName, capital: {name: capitalName, !... }} = getCountry();
    io:println("Country Name: " + countryName);
    io:println("Capital Name: " + capitalName);
}

function getPerson() returns Person {
    Person person = { name: "Peter", age: 28, country: "Sri Lanka",
                      occupation: "Software Engineer" };
    return person;
}

function getEmployee() returns Employee {
    Employee employee = { name: "John", age: 26 };
    return employee;
}

function getCountry() returns Country {
    Capital capital = { name: "Colombo" };
    Country country = { name: "Sri Lanka", capital: capital };
    return country;
}
