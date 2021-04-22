import ballerina/io;

public type Address object {
    public string city;
    public string country;

    public function value() returns string;
};

public type Person record {
    string name;
    int age;
    Address address;
};

public function main() {
    // There is no difference in how objects of anonymous types are created.
    Person john = {
        name: "John Doe",
        age: 25,
        // This object constructor expression indicates that this creates an subtype of the object of type `Address`.
        // This is similar to the type reference in class definitions and object-type definitions.
        address: object Address {
                     // In object constructor expressions, the `init` method does not take any parameters.
                     public function init() {
                         self.city = "Colombo";
                         self.country = "Sri Lanka";
                     }

                     public function value() returns string {
                         return self.city + ", " + self.country;
                     }
                 }
    };
    io:println("City: ", john.address.city);

    // The type reference in an object constructor expression is optional.
    Address adr = object {
                      public string city;
                      public string country;

                      public function init() {
                          self.city = "London";
                          self.country = "UK";
                      }

                      public function value() returns string {
                          return self.city + ", " + self.country;
                      }
                  };

    Person jane = {name: "Jane Doe", age: 20, address: adr};
    io:println("Address: ", jane.address.country);
}
