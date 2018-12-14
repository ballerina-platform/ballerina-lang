import ballerina/io;

public function main() {
    io:println("Documentation attachments are non executable constructs.");
}

// This is the documentation attachment for object `DummyObject`.
# `DummyObject` is a user defined object type in your Ballerina program.
# This `DummyObject` is used to hold two `string` data fields and one attached
# function definition `doThatOnObject` that performs a certain functionality
# on the associated `DummyObject` instance.
#
# + fieldOne - This is the description for the `DummyObject`'s field `fieldOne`.
# + fieldTwo - This is the description for the `DummyObject`'s field `fieldTwo`.
public type DummyObject abstract object {

    public string fieldOne;
    public string fieldTwo;

    // This is the documentation attachment for function `doThatOnObject`.
    # `doThatOnObject` is an attached function for the object `DummyObject`.
    #
    # + paramOne - This is the description for the parameter of
    #              `doThatOnObject` function.
    # + return - This is the description for the return value of
    #            `doThatOnObject` function.
    public function doThatOnObject(string paramOne) returns (boolean);
};

// This is the documentation attachment for record `DummyType`.
# `DummyType` is a user defined record type in your Ballerina program.
# This `DummyType` is used to hold a `string` type data field and an `int` type
# data field.
#
# + fieldOne - This is the description for the `DummyType`'s field `fieldOne`.
# + fieldTwo - This is the description for the `DummyType`'s field `fieldTwo`.
public type DummyType record {
    string fieldOne;
    int fieldTwo;
};
