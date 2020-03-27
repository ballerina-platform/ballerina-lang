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

# Test function doc
# + x - first integer
# + y - second integer
# + z - third integer
# + return - Returns the sum
# # Deprecated parameters
# + x - deprecated parameter
# + y - deprecated parameter
# # Deprecated
# function is deprecated
@deprecated
function add1(@deprecated public int x, @deprecated int y, @deprecated int z) returns int { // Compile error
    return 5;
}

# Test function doc
# + x - first integer
# + y - second integer
# + z - third integer
# + return - Returns the sum
function add2(@deprecated public int x, @deprecated int y, @deprecated int z) returns int { // Compile error
    return 4;
}

# Test function doc
# + x - first integer
# + y - second integer
# + z - third integer
# + return - Returns the sum
# # Deprecated parameters
# + x - deprecated parameter
# + y - deprecated parameter
# + z - deprecated parameter        // Compile error
# # Deprecated
# function is deprecated
@deprecated
function add3(@deprecated public int x, @deprecated int y, public int z) returns int {
    return 2;
}

# Test object doc
# + xxx - This is int
# # Deprecated parameters
# + xxx - deprecated
# # Deprecated
@deprecated
type DummyObj object {

    @deprecated
    public int xxx = 0;
};

const string CONST3 = "CONST3";
const string CONST4 = "CONST4";

public type T1 CONST3|CONST4;

# The `OBject1` is a user-defined object.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
# # Deprecated parameters
# + fieldOne - deprecated
# # Deprecated
@deprecated
public type Object1 object {

    @deprecated
    public string fieldOne = "Foo";
    @deprecated     // Compile error
    T1 t = CONST3;
    public string fieldTwo = "";
};

# The `Object2` is a user-defined object.
#
# + fieldOne - first field.
# + fieldTwo - second field.
# + t - third field
# # Deprecated parameters
# + fieldOne - deprecated
# + t - deprecated          // Compiler error
public type Object2 object {

    @deprecated
    public string fieldOne = "Foo";
    public T1 t = CONST3;
    public string fieldTwo = "";
};

# The `Object3` is a user-defined object.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
public type Object3 object {

    @deprecated             // Compiler error
    public string fieldOne = "Foo";
    T1 t = CONST3;
    public string fieldTwo = "";
};
