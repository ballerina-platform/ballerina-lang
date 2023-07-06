public function foo() returns int {
    return 10;
}

class MyClass {
    function foo() {
    }
}

string 'from = "contact@ballerina.io";

type StructuredName record {
    string firstName;
    string lastName;
};

type Name StructuredName|string;

public type Employee record {
    string name;
    readonly int age;
    boolean married;
    float salary;
};

public type CountryCode LK|US;

# The `DummyObject` is a user-defined object.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
public type DummyObject object {
    # This is fieldOne
    public string fieldOne;
    public string fieldTwo;

    // This is the documentation attachment of the `doThatOnObject` function.
    # The `doThatOnObject` function is attached to the `DummyObject` object.
    #
    # + paramOne - This is the description of the parameter of
    # the `doThatOnObject` function.
    # + return - This is the description of the return value of
    # the `doThatOnObject` function.
    public function doThatOnObject(string paramOne) returns boolean;
};

public enum PET {
    DOG = "Dog"
}
