# Define a constant
# # Deprecated
# This constant is deprecated
@deprecated
const string CONST1 = "CONST1";
const string CONST2 = "CONST2";

# Define a type
# # Deprecated
# This type is deprecated
@deprecated
public type Foo CONST1|CONST2;

# The `DummyObject` is a user-defined object.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
# # Deprecated
# This object is deprecated
@deprecated
public type DummyObject object {

    public string fieldOne = "Foo";
    Foo foo = CONST1;
    public string fieldTwo = "";
    # The `doThatOnObject` function is attached to the `DummyObject` object.
    #
    # + paramOne - This is the description of the parameter of
    #              the `doThatOnObject` function.
    # # Deprecated
    @deprecated
    public function doThatOnObject(string paramOne) {
    }
};

# This function initialize the object
#
# # Deprecated
# This function is deprecated
@deprecated
public function func1() {
    DummyObject obj = new;
}

public function func2(DummyObject obj, Foo foo, string str = CONST1) {
    func1();
}

@deprecated
public function func3() {
}

public function func4() {
    func3();
}
