import ballerina/java;

// Correct use case.
public function testJavaCastFunction() returns string|error {
    ArrayList arrayList = newArrayList1();
    String strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object result = arrayList.get(0);
    String castedValue = <String>check java:cast(result, typedesc<String>);
    return castedValue.toString();
}

// Incorrect Java class cast.
public function testIncorrectJavaCast() returns string|error {
    String strValue = newString("cast this object");
    ArrayList castedValue = <ArrayList>check java:cast(strValue, typedesc<ArrayList>);
    return castedValue.toString();
}

// Typedesc is not an object.
public function testJavaCastForInvalidTypedesc1() returns string|error {
    ArrayList arrayList = newArrayList1();
    String strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object result = arrayList.get(0);
    ArrayList castedValue = <ArrayList>check java:cast(result, typedesc<string>);
    return castedValue.toString();
}

// Typedesc is an object but not a JObject.
public function testJavaCastForInvalidTypedesc2() returns string|error {
    ArrayList arrayList = newArrayList1();
    String strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object result = arrayList.get(0);
    NotAJObject castedValue = <NotAJObject>check java:cast(result, typedesc<NotAJObject>);
    return castedValue.toString();
}

// Incorrect class name in typedesc object annotation.
public function testJavaCastForInvalidClass1() returns string|error {
    ArrayList arrayList = newArrayList1();
    String3 strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object result = arrayList.get(0);
    String3 castedValue = <String3>check java:cast(result, typedesc<String3>);
    return castedValue.toString();
}

// Incorrect class name in cast object annotation.
public function testJavaCastForInvalidClass2() returns string|error {
    ArrayList3 arrayList = newArrayList3();
    String strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object3 result = arrayList.get(0);
    String castedValue = <String>check java:cast(result, typedesc<String>);
    return castedValue.toString();
}

// Empty `jObj` value.
public function testJavaCastFunctionNulljObj() returns string|error {
    ArrayList arrayList = newArrayList1();
    String strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object result = arrayList.get(0);
    result.jObj = java:createNull();
    String castedValue = <String>check java:cast(result, typedesc<String>);
    return castedValue.toString();
}

// Missing `@java:Binding` annotation in typedesc.
public function testJavaCastMissingAnnotation1() returns string|error {
    ArrayList arrayList = newArrayList1();
    String2 strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object result = arrayList.get(0);
    String2 castedValue = <String2>check java:cast(result, typedesc<String2>);
    return castedValue.toString();
}

// Missing `@java:Binding`annotation in object.
public function testJavaCastMissingAnnotation2() returns string|error {
    ArrayList2 arrayList = newArrayList2();
    String strValue = newString("cast this object");
    _ = arrayList.add(strValue);
    Object2 result = arrayList.get(0);
    String castedValue = <String>check java:cast(result, typedesc<String>);
    return castedValue.toString();
}

// Input parameter is not an object.
public function testJavaCastForInvalidObject1() returns string|error {
    string strValue = "cast this object";
    String castedValue = <String>check java:cast(strValue, typedesc<String>);
    return castedValue.toString();
}

// Input parameter is not a JObject.
public function testJavaCastForInvalidObject2() returns string|error {
    NotAJObject notAJObject = new("NotAJObject");
    String castedValue = <String>check java:cast(notAJObject, typedesc<String>);
    return castedValue.toString();
}

// String Case1: Correct object
@java:Binding {
  class: "java.lang.String"
}
public type String object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
};

// Object Case1: Correct object
@java:Binding {
  class: "java.lang.Object"
}
public type Object object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }
};

// ArrayList Case1: Correct object
@java:Binding {
  class: "java.util.ArrayList"
}
public type ArrayList object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }

    public function add(Object arg0) returns boolean {
        return java_util_ArrayList_add(self.jObj, arg0.jObj);
    }

    public function get(int arg0) returns Object {
        Object obj = new(java_util_ArrayList_get(self.jObj, arg0));
        return obj;
    }
};

// String Case2: object with missing annotation
public type String2 object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
};

// Object Case2: object with missing annotation
public type Object2 object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }
};

// ArrayList Case2: using an Object2
@java:Binding {
  class: "java.util.ArrayList"
}
public type ArrayList2 object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }

    public function add(Object2 arg0) returns boolean {
        return java_util_ArrayList_add(self.jObj, arg0.jObj);
    }

    public function get(int arg0) returns Object2 {
        Object2 obj = new(java_util_ArrayList_get(self.jObj, arg0));
        return obj;
    }
};

// String Case3: wrong class in annotation
@java:Binding {
  class: "java.lang.Str"
}
public type String3 object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
};

// Object Case3: wrong class in annotation
@java:Binding {
  class: "java.lang.Objecte"
}
public type Object3 object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }
};

// ArrayList Case3: using an Object3
@java:Binding {
  class: "java.util.ArrayList"
}
public type ArrayList3 object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }

    public function add(Object3 arg0) returns boolean {
        return java_util_ArrayList_add(self.jObj, arg0.jObj);
    }

    public function get(int arg0) returns Object3 {
        Object3 obj = new(java_util_ArrayList_get(self.jObj, arg0));
        return obj;
    }
};

public type NotAJObject object {

    string obj;

    public function __init(string obj) {
        self.obj = obj;
    }
};

public function newArrayList1() returns ArrayList {
    handle obj = java_util_ArrayList_newArrayList();
    ArrayList _arrayList = new(obj);
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

public function newString(string arg0) returns String {
    handle obj = java_lang_String_newString(java:fromString(arg0));
    String _string = new(obj);
    return _string;
}

function java_util_ArrayList_newArrayList() returns handle = @java:Constructor {
    class: "java.util.ArrayList",
    paramTypes: []
} external;

function java_util_ArrayList_add(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "add",
    class: "java.util.ArrayList",
    paramTypes: ["java.lang.Object"]
} external;

function java_util_ArrayList_get(handle receiver, int arg0) returns handle = @java:Method {
    name: "get",
    class: "java.util.ArrayList",
    paramTypes: ["int"]
} external;

function java_lang_String_newString(handle arg0) returns handle = @java:Constructor {
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;
