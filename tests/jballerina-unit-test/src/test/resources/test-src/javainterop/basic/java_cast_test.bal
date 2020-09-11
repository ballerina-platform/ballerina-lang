import ballerina/java;

type String1Typedesc typedesc<String1>;
type String2Typedesc typedesc<String2>;
type String3Typedesc typedesc<String3>;
type String4Typedesc typedesc<String4>;
type ArrayList1Typedesc typedesc<ArrayList1>;

// Correct use case.
public function testJavaCastFunction() returns string|error {
    ArrayList1 arrayList = newArrayList1();
    String1 strValue = newString1("cast this object");
    _ = arrayList.add(strValue);
    Object1 result = arrayList.get(0);
    String1 castedValue = <String1>check java:cast(result, String1Typedesc);
    return castedValue.toString();
}

// Incorrect Java class cast.
public function testIncorrectJavaCast() returns string|error {
    String1 strValue = newString1("cast this object");
    ArrayList1 castedValue = <ArrayList1>check java:cast(strValue, ArrayList1Typedesc);
    return castedValue.toString();
}

// Typedesc is an object without a handle argument for initialization.
public function testJavaCastForInvalidTypedesc3() returns string|error {
    ArrayList1 arrayList = newArrayList1();
    String1 strValue = newString1("cast this object");
    _ = arrayList.add(strValue);
    Object1 result = arrayList.get(0);
    String4 castedValue = <String4>check java:cast(result, String4Typedesc);
    return castedValue.toString();
}

// Incorrect class name in typedesc object annotation.
public function testJavaCastForInvalidClass1() returns string|error {
    ArrayList1 arrayList = newArrayList1();
    String3 strValue = newString3("cast this object");
    _ = arrayList.add(strValue);
    Object1 result = arrayList.get(0);
    String3 castedValue = <String3>check java:cast(result, String3Typedesc);
    return castedValue.toString();
}

// Incorrect class name in cast object annotation.
public function testJavaCastForInvalidClass2() returns string|error {
    ArrayList3 arrayList = newArrayList3();
    String1 strValue = newString1("cast this object");
    _ = arrayList.add(strValue);
    Object3 result = arrayList.get(0);
    String1 castedValue = <String1>check java:cast(result, String1Typedesc);
    return castedValue.toString();
}

// Empty `jObj` value.
public function testJavaCastFunctionNulljObj() returns string|error {
    ArrayList1 arrayList = newArrayList1();
    String1 strValue = newString1("cast this object");
    _ = arrayList.add(strValue);
    Object1 result = arrayList.get(0);
    result.jObj = java:createNull();
    String1 castedValue = <String1>check java:cast(result, String1Typedesc);
    return castedValue.toString();
}

// Missing `@java:Binding` annotation in typedesc.
public function testJavaCastMissingAnnotation1() returns string|error {
    ArrayList1 arrayList = newArrayList1();
    String2 strValue = newString2("cast this object");
    _ = arrayList.add(strValue);
    Object1 result = arrayList.get(0);
    String2 castedValue = <String2>check java:cast(result, String2Typedesc);
    return castedValue.toString();
}

// Missing `@java:Binding`annotation in object.
public function testJavaCastMissingAnnotation2() returns string|error {
    ArrayList2 arrayList = newArrayList2();
    String1 strValue = newString1("cast this object");
    _ = arrayList.add(strValue);
    Object2 result = arrayList.get(0);
    String1 castedValue = <String1>check java:cast(result, String1Typedesc);
    return castedValue.toString();
}

// String Case1: Correct object
@java:Binding {
  'class: "java.lang.String"
}
public class String1 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
}

// Object Case1: Correct object
@java:Binding {
  'class: "java.lang.Object"
}
public class Object1 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }
}

// ArrayList Case1: Correct object
@java:Binding {
  'class: "java.util.ArrayList"
}
public class ArrayList1 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }

    public function add(Object1 arg0) returns boolean {
        return java_util_ArrayList_add(self.jObj, arg0.jObj);
    }

    public function get(int arg0) returns Object1 {
        Object1 obj = new(java_util_ArrayList_get(self.jObj, arg0));
        return obj;
    }
}

// String Case2: object with missing annotation
public class String2 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
}

// Object Case2: object with missing annotation
public class Object2 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }
}

// ArrayList Case2: using an Object2
@java:Binding {
  'class: "java.util.ArrayList"
}
public class ArrayList2 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }

    public function add(Object2 arg0) returns boolean {
        return java_util_ArrayList_add(self.jObj, arg0.jObj);
    }

    public function get(int arg0) returns Object2 {
        Object2 obj = new(java_util_ArrayList_get(self.jObj, arg0));
        return obj;
    }
}

// String Case3: wrong class in annotation
@java:Binding {
  'class: "java.lang.Str"
}
public class String3 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
}

// Object Case3: wrong class in annotation
@java:Binding {
  'class: "java.lang.Objecte"
}
public class Object3 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }
}

// ArrayList Case3: using an Object3
@java:Binding {
  'class: "java.util.ArrayList"
}
public class ArrayList3 {

    *java:JObject;

    public function init(handle obj) {
        self.jObj = obj;
    }

    public function add(Object3 arg0) returns boolean {
        return java_util_ArrayList_add(self.jObj, arg0.jObj);
    }

    public function get(int arg0) returns Object3 {
        Object3 obj = new(java_util_ArrayList_get(self.jObj, arg0));
        return obj;
    }
}

// String Case4: Object with incorrect initialization type
@java:Binding {
  'class: "java.lang.String"
}
public class String4 {

    *java:JObject;

    public function init(string obj) {
        self.jObj = java:fromString(obj);
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
}

public function newArrayList1() returns ArrayList1 {
    handle obj = java_util_ArrayList_newArrayList();
    ArrayList1 _arrayList = new(obj);
    return _arrayList;
}

public function newArrayList2() returns ArrayList2 {
    handle obj = java_util_ArrayList_newArrayList();
    ArrayList2 _arrayList = new(obj);
    return _arrayList;
}

public function newArrayList3() returns ArrayList3 {
    handle obj = java_util_ArrayList_newArrayList();
    ArrayList3 _arrayList = new(obj);
    return _arrayList;
}

public function newString1(string arg0) returns String1 {
    handle obj = java_lang_String_newString(java:fromString(arg0));
    String1 _string = new(obj);
    return _string;
}

public function newString2(string arg0) returns String2 {
    handle obj = java_lang_String_newString(java:fromString(arg0));
    String2 _string = new(obj);
    return _string;
}

public function newString3(string arg0) returns String3 {
    handle obj = java_lang_String_newString(java:fromString(arg0));
    String3 _string = new(obj);
    return _string;
}

function java_util_ArrayList_newArrayList() returns handle = @java:Constructor {
    'class: "java.util.ArrayList",
    paramTypes: []
} external;

function java_util_ArrayList_add(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "add",
    'class: "java.util.ArrayList",
    paramTypes: ["java.lang.Object"]
} external;

function java_util_ArrayList_get(handle receiver, int arg0) returns handle = @java:Method {
    name: "get",
    'class: "java.util.ArrayList",
    paramTypes: ["int"]
} external;

function java_lang_String_newString(handle arg0) returns handle = @java:Constructor {
    'class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;
