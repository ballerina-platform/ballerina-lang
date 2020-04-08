import ballerina/io;

// The constant `LKA` is marked as deprecated using the `@deprecated` annotation.
// Since it has documentation, the `# # Deprecated` documentation needs to be added as well.
// This will appear in the generated documentation.
# Country code for Sri Lanka.
# # Deprecated
# This constant is deprecated. Use the constant `LK` instead.
@deprecated
public const string LKA = "LKA";

# New country code for Sri Lanka.
public const string LK = "LK";

# Country code for USA.
public const string USA = "USA";

// The type definition contains both the deprecated constant and its replacement.
public type CountryCode LK|LKA|USA;

// The `Address` record type is deprecated, but does not have the `# # Deprecated` documentation.
@deprecated
public type Address record {|
    string street;
    string city;
    CountryCode countryCode;
|};

// The `Person` object type is deprecated, but does not have the `# # Deprecated` documentation.
@deprecated
public type Person object {
    public string firstName = "John";
    public string lastName = "Doe";
    // Usage of the deprecated record `Address`.
    Address addr = {
        street: "Palm Grove",
        city: "Colombo 3",
        // Usage of the deprecated constant `LKA`.
        countryCode: LKA
    };

    // Object methods can also be deprecated.
    @deprecated
    public function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

};

// The function `createPerson` is marked as deprecated using the `@deprecated` annotation.
// Since it has documentation, the `# # Deprecated` documentation needs to be added as well.
# Creates and returns a `Person` object given the parameters.
#
# + fname - First name of the person
# + lname - Last name of the person
# + street - Street the person is living at
# + city - The city the person is living in
# + countryCode - The country code for the country the person is living in
# + return - A new `Person` object
#
# # Deprecated
# This function is deprecated since the `Person` type is deprecated.
@deprecated
public function createPerson(string fname, string lname, string street,
                             string city, CountryCode countryCode) returns Person {

    // Usage of the deprecated object `Person`.
    Person p = new;

    p.firstName = fname;
    p.lastName = lname;
    p.addr = {street, city, countryCode};
    return p;
}

public function main() {
    // Usage of the deprecated object `Person` and function `createPerson`.
    Person p = createPerson("Jane", "Doe", "Castro Street", "Mountain View", USA);

    // Usage of the deprecated object method `getFullName`
    io:println(p.getFullName());

}
