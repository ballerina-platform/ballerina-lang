# Define a constant
# # Deprecated
# This constant is deprecated
@deprecated
public const string C1 = "C1";
public const string C2 = "C2";

# Define a type
# # Deprecated
# This type is deprecated
@deprecated
public type Bar C1|C2;

# The `DummyObject` is a user-defined object.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
# # Deprecated
# This class is deprecated
@deprecated
public class DummyObject1 {

    public string fieldOne = "Foo";
    public string fieldTwo = "";
}

public class DummyObject2 {

    public string fieldOne = "Foo";
    public string fieldTwo = "";

    @deprecated
    public int id = 5;
    
    # The `doThatOnObject` function is attached to the `DummyObject` object.
    #
    # + paramOne - This is the description of the parameter of
    #              the `doThatOnObject` function.
    # # Deprecated
    @deprecated
    public function doThatOnObject(string paramOne) {
    }
}

# This function initialize the object
#
# + return - Return string
# # Deprecated
# This function is deprecated
@deprecated
public function deprecated_func() returns string {
    return "";
}

# Define an annotation
# # Deprecated
# This annotation is deprecated
@deprecated
public annotation deprecatedAnnotation on function;

# Define an client object
# # Deprecated
# This client object is deprecated
@deprecated
public type MyClientObject client object {
    @deprecated
    remote function remoteFunction();
};
