import ballerina/io;

public function main() {
    io:println("Documentation attachments are non executable constructs.");
}

// This is the documentation attachment of the `DummyObject` object.
# The `DummyObject` is a user-defined object.
# This `DummyObject` has two `string` data fields and a
# function definition (i.e., `doThatOnObject`), which performs a certain
# functionality on the associated `DummyObject` instance.
# This is a reference to the type `DummyRecord`
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
public type DummyObject abstract object {

    public string fieldOne;
    public string fieldTwo;

    // This is the documentation attachment of the `doThatOnObject` function.
    # The `doThatOnObject` function is attached to the `DummyObject` object.
    #
    # + paramOne - This is the description of the parameter of
    #              the `doThatOnObject` function.
    # + return - This is the description of the return value of
    #            the `doThatOnObject` function.
    public function doThatOnObject(string paramOne) returns boolean;
};

// This is the documentation attachment of the `DummyRecord` record.
# `DummyRecord` is a user-defined record.
# This `DummyRecord` has a `string` data field and an `int` data field.
# This is a reference to a function `dummyFunction`.
#
# + fieldOne - This is the description of the `DummyRecord`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyRecord`'s `fieldTwo` field.
public type DummyRecord record {
    string fieldOne;
    int fieldTwo;
};

// This is the documentation attachment of the `dummyFunction` function.
# This function adds parameter `x` and parameter `y`.
# + x - First parameter.
# + y - Second parameter.
# + return - The sum of the two parameters.
public function dummyFunction(int x, int y) returns int {
    return x + y;
}
