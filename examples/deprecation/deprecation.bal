// The constant `CITY` is marked as deprecated using the `@deprecated` annotation and the markdown
// documentation `# # Deprecated`.
# Define a constant to represent the city.
# # Deprecated
# This constant is deprecated as this information is not needed.
@deprecated
const string CITY = "COLOMBO";

const string COUNTRY = "Sri Lanka";

// The type `Address` is marked as deprecated using the `@deprecation` annotation.
@deprecated
public type Address CITY|COUNTRY;

// The object `Person` is marked as deprecated using `@deprecated` annotation and the markdown
// documentation `# # Deprecated`.
# The `Person` is a user-defined object.
#
# + firstName - The first name of the person.
# + lastName - The last name of the person.
# # Deprecated
# This object is deprecated as information representation is changed.
# Users of this object should instead use the object `StudentData`.
@deprecated
public type Person object {
    public string firstName = "Andrew";
    public string lastName = "John";
    // Usage of the deprecated type 'Address' and the deprecated constant `CITY`.
    Address addr = CITY;

    // The object method `updateFirstName` is deprecated using the `@deprecation` annotation.
    @deprecated
    public function updateFirstName(string name) {
        self.firstName = name;
    }

};

// Usage of the deprecated object `Person`, type `Address`, and constant `CITY` as parameters.
public function initializer(Person person, Address addr, string city = CITY) {

    // Usage of deprecated object method `updateFirstName`.
    person.updateFirstName("");

}

// The function `updatePerson` is marked as deprecated using `@deprecated` annotation and the markdown
// documentation `# # Deprecated`.
# This function updates the object `Person`.
#
# # Deprecated
# This function is deprecated as affected object is deprecated.
@deprecated
public function updatePerson() {
    // Usage of deprecate object `Person`.
    Person person = new;

    // Usage of deprecated type `Address`.
    Address addr = COUNTRY;

    initializer(person, addr);
}

public function main() {
    // Usage of deprecated function `updatePerson`.
    updatePerson();

}
