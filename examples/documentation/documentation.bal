import ballerina/io;

public function main() {
    io:println("Documentation attachments are non executable constructs.");
}

// This is the documentation attachment for the `DummyObject` object.
# `DummyObject` is a user defined object.
# This `DummyObject` has two `string` data fields and one attached
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
    public function doThatOnObject(string paramOne) returns boolean;
};

// This is the documentation attachment for the `DummyRecord` record.
# `DummyRecord` is a user defined record.
# This `DummyRecord` has a `string` data field and an `int` data field.
#
# + fieldOne - This is the description for `DummyRecord`'s field `fieldOne`.
# + fieldTwo - This is the description for `DummyRecord`'s field `fieldTwo`.
public type DummyRecord record {
    string fieldOne;
    int fieldTwo;
};
