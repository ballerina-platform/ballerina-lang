import ballerina/io;

type Address record {
    string street;
    string city;
    string state;
    string zipcode;
};

type Person record {
    string name;
    int age;
    Address address;
};

public function main() {    
    // A record-typed binding pattern, which declares the variables `name`, `age`, and
    // `address` of type `string`, `int`, and `Address` respectively. The fields of the record value
    // given by the assignment expression provides the values for the variables. 
    // Since `Person` is an open record, the `...otherFields` variable represents a rest parameter of 
    // type `map<anydata|error>`, which holds remaining fields of the record that had not been matched.
    Person {
        name: myName, age: myAge, address: myAddress, ...otherFields
        } = getPerson();
    io:println("My Name: ", myName, " My Age: ", myAge,
                " My Address: ", myAddress, " Other Fields: ", otherFields);

    // If a field name is not given, the name of the variable will be considered as the field name as well
    // i.e., `Person {name, age, address}` is same as Person `{name: name, age: age, address: address}`.
    // Since a rest parameter is not given, all the remaining fields are ignored.
    Person {name, age, address} = getPerson();
    io:println("Name: ", name, " Age: ", age, " Address: ", address);

    // Record-typed binding patterns can be used with `var` to infer the type from the context. In the current situation,
    // the type is resolved by the value presented from the assignment expression.    
    var {street, city, state, zipcode} = getAddress();
    io:println("City: ", city, " State: ", state, " State: ", state,
                                                    " Zip Code: ", zipcode);
}

function getAddress() returns Address {
    Address address = {street: "380 Lakewood Dr.", city: "Desoto",
                                                state: "TX", zipcode: "75115"};
    return address;
}

function getPerson() returns Person {
    Address address = getAddress();
    Person person = {name: "Jack Smith", age: 23, address,
                                            "occupation": "Software Engineer"};
    return person;
}
