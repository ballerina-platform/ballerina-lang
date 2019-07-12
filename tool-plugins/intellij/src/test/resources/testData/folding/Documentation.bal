import <fold text='...'>ballerina/io;</fold>

// This is the documentation attachment for the `DummyObject` object.
<fold text='# ...'># `DummyObject` is a user defined object.
# This `DummyObject` has two `string` data fields and one attached
# function definition `doThatOnObject` that performs a certain functionality
# on the associated `DummyObject` instance.
#
# + fieldOne - This is the description for the `DummyObject`'s field `fieldOne`.
# + fieldTwo - This is the description for the `DummyObject`'s field `fieldTwo`.</fold>
public type DummyObject abstract object <fold text='{...}'>{

    public string fieldOne;
    public string fieldTwo;

    // This is the documentation attachment for function `doThatOnObject`.
    <fold text='# ...'># `doThatOnObject` is an attached function for the object `DummyObject`.
    #
    # + paramOne - This is the description for the parameter of
    #              `doThatOnObject` function.
    # + return - This is the description for the return value of
    #            `doThatOnObject` function.</fold>
    public function doThatOnObject(string paramOne) returns boolean;
}</fold>;

// This is the documentation attachment for the `DummyRecord` record.
<fold text='# ...'># `DummyRecord` is a user defined record.
# This `DummyRecord` has a `string` data field and an `int` data field.
#
# + fieldOne - This is the description for `DummyRecord`'s field `fieldOne`.
# + fieldTwo - This is the description for `DummyRecord`'s field `fieldTwo`.</fold>
public type DummyRecord record {
    string fieldOne;
    int fieldTwo;
};
