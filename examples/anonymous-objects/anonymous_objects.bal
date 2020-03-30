import ballerina/io;

public type Person record {
    string name;
    int age;
    // This is an anonymous object type descriptor. All the fields and
    // methods are made public. This is done to allow the declaration of
    // variables equivalent to this object type (since two public object types
    // are considered unequivalent if either of the objects have
    // any private members).
    object {
        public string city;
        public string country;

        public function __init(string city, string country) {
            self.city = city;
            self.country = country;
        }

        public function value() returns string {
            return self.city + ", " + self.country;
        }
    } address;
};

public function main() {
    // There is no difference in how objects of anonymous types are created.
    Person john = {
        name: "John Doe",
        age: 25,
        address: new ("Colombo", "Sri Lanka")
    };
    io:println(john.address.city);

    // Since anonymous objects do not have a type name associated with them,
    // the object descriptor itself has to be specified when declaring
    // variables of an anonymous object type.
    object {
        public string city;
        public string country;

        public function __init(string city, string country) {
            self.city = city;
            self.country = country;
        }

        public function value() returns string {
            return self.city + ", " + self.country;
        }
    } adr = new ("London", "UK");

    Person jane = {name: "Jane Doe", age: 20, address: adr};
    io:println(jane.address.country);
}
