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
    // This record type binding pattern will destructure a `record` of type `Person` and create three variables as follows:
    // The value of the field `name` in the `Person` record will be set to a new `string` variable `firstName`.
    // The value of the field `age` in the `Person` record will be set to a new `int` variable `personAge`.
    // `...otherDetails` is a rest parameter. Since `Person` is an open record, a new `map<anydata>` variable
    // `otherDetails` will be created with the remaining field values that have not been matched in the record binding pattern.
    Person { name: firstName, age: personAge, ...otherDetails } = getPerson();
    io:println("Name: " + firstName);
    io:println("Age: " + personAge);
    io:println("Other Details: " + io:sprintf("%s", otherDetails));

    // If no variable name is given for a field, a variable will be created with the same name as the field.
    // i.e. `Person {name, age}` is same as Person `{name: name, age: age}`.
    // Since a rest parameter is not given, all remaining fields are ignored.
    Person { name, age } = getPerson();
    io:println("Name: " + name);
    io:println("Age: " + age);

    // Record type binding patterns can be used with `var` to infer the type from the right hand side.
    // Since the types of the new variables are based on the type of the type binding pattern, using `var` will
    // infer the types from the right hand side.
    var { name: vFirstName, age: vPersonAge, ...vOtherDetails } = getPerson();
    // Type of `vFirstName` is inferred as `string`.
    io:println("Name: " + vFirstName);
    // Type of `vPersonAge` is inferred as `int`.
    io:println("Age: " + vPersonAge);
    // Type of `vOtherDetails` will be `map<anydata>`.
    io:println("Other Details: " + io:sprintf("%s", vOtherDetails));

    // The `!...` symbol will specify that there should not be any other fields other than `name` and `age`, hence
    // `Employee` should be a closed record.
    Employee { name: empName, age: empAge, !... } = getEmployee();
    io:println("Name: " + empName);
    io:println("Age: " + empAge);

    // Binding patterns are recursive in nature. `Capital`, which is a field type of `Country` can also be destructured as follows:
    var { name: countryName, capital: { name: capitalName, !... } } = getCountry();
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
