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
    DummyObject _ = new;
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
    Annot? _ = t.@v1;
    Annot[]? _ = t.@v2;
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
    int _ = add1(2, 3, 3);
    int _ = add2(2, 4, 5);
    Object1 _ = new;
    Object2 _ = new;
}

# Test function doc
# + x - first integer
# + y - second integer
# + z - third integer
# # Deprecated parameters
# + z - deprecated rest parameter
function add3(int x, int y, @deprecated int... z) {
    int _ = z[0];
}

public function typeReturn() returns Foo { // Compile warning because 'Foo' is deprecated
    Foo f = CONST2;
    return f;
}

public function getType() {
    Foo _ = typeReturn();
}

class SimpleClass {

}

function testObjectConstructorWithCodeAnalyzer() {
    SimpleClass|float _ = object SimpleClass {
        @deprecated
        function init() {
        }
    };
}

@deprecated
annotation myAnnot on function;

@myAnnot
function testUsingDeprecatedAnnotation() {

}

function testAccessingDeprecatedAnnotation() {
    typedesc funcType = typeof testUsingDeprecatedAnnotation();
    _ = funcType.@myAnnot;
}

@deprecated
type MyObject client object {
    @deprecated
    int id;

    @deprecated
    remote function getId() returns int;
};

@deprecated
class Person {
    @deprecated
    string name = "john";

    @deprecated
    function getName() returns string {
        return self.name;
    }

    function getAge() {
    }
}

function testUsingDeprecatedFieldsMethodsAndTypes(int i, string s) {
    MyObject obj = client object MyObject {
        @deprecated
        int id = 4;

        remote function getId() returns int {
            return self.id;
        }
    };

    int _ = obj.id;
    int _ = obj->getId();

    Person obj2 = new Person();
    string _ = obj2.name;
    string _ = obj2.getName();
    obj2.getAge(); // should not give a warning
}

@deprecated
function myFunction(int i, string s) {

}

function testUsingDepricatedFunction() {
    myFunction(1, "hello");
}

type Employee record {|
    @deprecated
    string name;
    int id;
    @deprecated
    Job job;
|};

type Job record {|
    string title;
    @deprecated
    int experiance;
|};

public function testDeprecatedRecordFields() {
    Employee employee = {name: "John", id: 112, job: {title: "SE", experiance: 2}};
    _ = employee.name; // warning
    _ = employee.id;
    _ = employee.job; // warning
    _ = employee.job.title; // warning
    _ = employee.job.experiance; // warning
}

# Employee2 record
type Employee2 record {|
    # This is the description of the `Employee2`'s `name` field.
    # # Deprecated
    # This field is deprecated
    @deprecated
    string name;

    # This is the description of the `Employee2`'s `id` field.
    int id;

    # This is the description of the `Employee2`'s `job` field.
    # # Deprecated
    # This field is deprecated
    @deprecated
    Job job;
|};

public function testDeprecatedRecordFieldsWithDocumentation() {
    Employee2 employee2 = {name: "John", id: 112, job: {title: "SE", experiance: 2}};
    _ = employee2.name; // warning
    _ = employee2.id;
    _ = employee2.job; // warning
}
