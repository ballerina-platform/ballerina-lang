// The constant 'CONST1' is deprecated using '@deprecated' annotation and markdown documentation '# # Deprecated'
# Define a constant
# # Deprecated
# This constant is deprecated
@deprecated
const string CONST1 = "CONST1";
const string CONST2 = "CONST2";

// The type 'Foo' is deprecated using '@deprecation' annotation
@deprecated
public type Foo CONST1|CONST2;

// The object 'DummyObject' is deprecated using '@deprecated' annotation and markdown documentation '# # Deprecated'
# The `DummyObject` is a user-defined object.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
# # Deprecated
# This object is deprecated
@deprecated
public type DummyObject object {
    public string fieldOne = "Foo";
    // Usage of deprecated type 'Foo' and deprecated constant 'CONST1'
    Foo foo = CONST1;
    public string fieldTwo = "";

    // The object function 'doThatOnObject' is deprecated using '@deprecation' annotation
    @deprecated
    public function doThatOnObject(string paramOne) {}
};

// The object 'DummyObject' is deprecated using '@deprecated' annotation and markdown documentation '# # Deprecated'
# This function initialize the object
#
# # Deprecated
# This function is deprecated
@deprecated
public function objectInitializer() {
    // Usage of deprecate object 'DummyObject'
    DummyObject obj = new;
    // Usage of deprecated function object 'doThatOnObject'
    obj.doThatOnObject(“”);
}

// Usage of deprecated object 'DummyObject', type 'Foo', and constant 'CONST1' as parameters
public function func2(DummyObject obj, Foo foo, string str = CONST1) {
    // Usage of deprecated function 'objectInitializer'
    objectInitializer();
}
