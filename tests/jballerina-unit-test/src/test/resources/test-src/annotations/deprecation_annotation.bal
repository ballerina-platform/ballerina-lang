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
public class DummyObject {

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
}

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

type Annot record {
    string foo;
    int bar?;
};

# Annotation for record 'Annot'
#
# # Deprecated
@deprecated
public annotation Annot v1 on type, class;
@deprecated
public annotation Annot[] v2 on class;
@deprecated
public annotation map<int> v4 on object function, function, class;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testAnnotationDeprecation() {
    typedesc<any> t = typeof a;
    Annot? annot1 = t.@v1;
    Annot[]? annot2 = t.@v2;
}

@v1 {
    foo: strValue
}
@v2 {
    foo: "v2 value 1"
}
@v2 {
    foo: "v2 value 2"
}
class T2 {
    string name = "ballerina";

    @v4 {
        val: 42
    }
    public function objMethod() {
    }
}

@v4 {
        val: 42
}
public function deprecated_annotation_func() {
}

# Test function doc
# + x - first integer
# + y - second integer
# + z - third integer
# + return - Returns the sum
# # Deprecated parameters
# + x - deprecated parameter
# + y - deprecated parameter
# + z - deprecated parameter
# # Deprecated
# function is deprecated
@deprecated
function add1(@deprecated int x, @deprecated int y, @deprecated int z) returns int {
    return x + y + z;
}

@deprecated
function add2(@deprecated int x, @deprecated int y, @deprecated int z) returns int {
    return x + y + z;
}

const string CONST3 = "CONST3";
const string CONST4 = "CONST4";

public type TYPE1 CONST3|CONST4;

@deprecated
public class Object1 {

    @deprecated
    public string fieldOne = "Foo";
    @deprecated
    TYPE1 t = CONST3;
    public string fieldTwo = "";

    public function doThatOnObject(string paramOne, TYPE1 t) {
        self.fieldOne = paramOne;
        self.t = t;
    }
}

# The `Object2` is a user-defined object.
#
# + fieldTwo - This is the description of the `Object2`'s `fieldTwo` field.
public class Object2 {

    # This is the description of the `Object2`'s `fieldOne` field.
    # # Deprecated
    # This field is deprecated
    @deprecated
    public string fieldOne = "Foo";
    # This is the description of the `Object2`'s `fieldTwo` field.
    # # Deprecated
    # This field is deprecated
    @deprecated
    TYPE1 t = CONST3;
    public string fieldTwo = "";

    public function doThatOnObject(string paramOne, TYPE1 t) {
        self.fieldOne = paramOne;
        self.t = t;
    }
}

public class Object3 {

    # This is the description of the `Object2`'s `fieldOne` field.
    # # Deprecated
    # This field is deprecated
    @deprecated
    public string fieldOne = "Foo";
    # This is the description of the `Object2`'s `fieldTwo` field.
    # # Deprecated
    # This field is deprecated
    @deprecated
    TYPE1 t = CONST3;
    public string fieldTwo = "";

    public function doThatOnObject(string paramOne, TYPE1 t) {
        self.fieldOne = paramOne;
        self.t = t;
    }
}

public function func5() {
    int x1 = add1(2, 3, 3);
    int x2 = add2(2, 4, 5);
    Object1 obj1 = new;
    Object2 obj2 = new;
}

# Test function doc
# + x - first integer
# + y - second integer
# + z - third integer
# # Deprecated parameters
# + z - deprecated rest parameter
function add3(int x, int y, @deprecated int... z) {
    int n = z[0];
}

public function typeReturn() returns Foo { // Compile warning because 'Foo' is deprecated
    Foo f = CONST2;
    return f;
}

public function getType() {
    Foo f = typeReturn();
}
