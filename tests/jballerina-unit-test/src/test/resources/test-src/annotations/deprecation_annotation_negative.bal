# Define a constant
# This constant is deprecated
@deprecated
const string CONST1 = "CONST1";
const string CONST2 = "CONST2";

# Define a type
# # Deprecated
# This type is deprecated
public type Foo CONST1|CONST2;

# The `DummyObject` is a user-defined object.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
# + fieldThree - This is the description of the `DummyObject`'s `fieldThree` field.
@deprecated
public type DummyObject object {

    public string fieldOne = "Foo";
    public string fieldTwo = "Foo";
    public string fieldThree = "";
    # The `doThatOnObject` function is attached to the `DummyObject` object.
    #
    # + paramOne - This is the description of the parameter of
    #              the `doThatOnObject` function.
    # # Deprecated
    public function doThatOnObject(string paramOne) {
    }
};

# This function initialize the object
#
# This function is deprecated
@deprecated
public function func1() {
}
