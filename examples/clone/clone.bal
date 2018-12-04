public type Person record {
    string name;
    int age;
    boolean married;
    float salary;
    Address address;
};

public type Address record {
    string country;
    string state;
    string city;
    string street;
};

public function main() returns (Person, Person, string) {
    Address address = {
        country : "USA",
        state: "NC",
        city: "Raleigh",
        street: "Daniels St"
    };

    Person person = {
        name: "Alex",
        age: 24,
        married: false,
        salary: 8000.0,
        address: address
    };

    var result = person.clone();
    string refCheck = "";
    if (result !== person) {
        refCheck = "Source and Clone are at two different memory locations";
    }
    return (person, result, refCheck);
}
