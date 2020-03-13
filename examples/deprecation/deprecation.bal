// The constant `CITY` is marked as deprecated using the `@deprecated` annotation and the markdown documentation `# # Deprecated`.
# Define a constant to represent the city.
# # Deprecated
# This constant is deprecated as this information is not needed.
@deprecated
const string CITY = "COLOMBO";
const string COUNTRY = "Sri Lanka";

// The type `Address` is marked as deprecated using the `@deprecation` annotation.
@deprecated
public type Address CITY|COUNTRY;

// The object `Data` is marked as deprecated using `@deprecated` annotation and the markdown documentation `# # Deprecated`.
# The `Data` is a user-defined object.
#
# + firstName - This is the description of the `Data`'s `firstName` field.
# + lastName - This is the description of the `Data`'s `lastName` field.
# # Deprecated
# This object is deprecated as information representation is changed.
# Users of this object should instead use the object `StudentData`.
@deprecated
public type Data object {
    public string firstName = "Andrew";
    public string lastName = "John";
    // Usage of deprecated type 'Address' and deprecated constant 'CITY'
    Address addr = CITY;

    // The object function 'updateFirstName' is deprecated using '@deprecation' annotation
    @deprecated
    public function updateFirstName(string name) {
        self.firstName = name;
    }
};

// Usage of the deprecated `Data` object, `Address` type, and constant `CITY` constant as parameters.
public function dataInitializer(Data data, Address addr, string city = CITY) {
    // Usage of deprecated object function `updateFirstName`.
    data.updateFirstName("");
}

// The object `Data` is marked as deprecated using `@deprecated` annotation and the markdown documentation `# # Deprecated`.
# This function updates the object `Data`.
#
# # Deprecated
# This function is deprecated as affected object is deprecated.
@deprecated
public function updateData() {
    // Usage of deprecate object `Data`.
    Data data = new;
    // Usage of deprecated type `Address`.
    Address addr = COUNTRY;
    // Usage of the deprecated `dataInitializer` function.
    dataInitializer(data, addr);
}

public function main() {
    // Usage of deprecated function `updateData`.
    updateData();
}
