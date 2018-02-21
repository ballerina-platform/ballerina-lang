import ballerina.io;
function refTypeAccessTestTrivialEqualityPositiveCase() (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    if ((typeof temp_int) == (typeof temp_int_1)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclared() (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    type temp_int_type = (typeof temp_int);
    type temp_int_1_type = (typeof temp_int_1);

    if (temp_int_type == temp_int_1_type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclaredWithVar() (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    var temp_int_type = (typeof temp_int);
    var temp_int_1_type = (typeof temp_int_1);

    if (temp_int_type == temp_int_1_type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityNegativeCase() (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if ((typeof temp_int) == (typeof temp_str)) {
        return 1;
    } else {
       return 2;
    }
}

function refTypeAccessTestTrivialNotEqualityCase() (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if ((typeof temp_int) != (typeof temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypeNegativeCase() (int) {
    any temp_int = 2;
    string temp_str = "dummy";
    if ((typeof temp_int) == (typeof temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypePositiveCase() (int) {
    any any_type_var = "dummy";
    string temp_str = "dummy";
    if ((typeof any_type_var) == (typeof temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMapAccessCase() (int) {
    map int_map = {};
    int_map["index"] = 2;
    map string_map = {};
    string_map["index"] = "dummy";
    if ((typeof int_map["index"]) == (typeof string_map["index"])) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestArrayAccessCase() (int) {
    int[] int_array = [];
    int_array[0] = 2;
    string[] string_array = [];
    string_array[0] = "dummy";
    if ((typeof int_array[0]) == (typeof string_array[0])) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestArrayEqualityCase() (int) {
    int[] int_array = [];
    int_array[0] = 2;
    string[] string_array = [];
    string_array[0] = "dummy";
    if ((typeof int_array) == (typeof string_array)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestArrayEqualityPositiveCase() (int) {
    int[] int_array = [];
    int_array[0] = 2;
    int[] int_array_2 = [];
    int_array_2[0] = 10;
    if ((typeof int_array) == (typeof int_array_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructAccessCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};

    if ((typeof jack.name) == (typeof jack.age)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeEqualityCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    Person neal;
    neal = {name:"Neal", age:9};

    if ((typeof jack) == (typeof neal)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeNotEqualityCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    Person neal;
    neal = {name:"Neal", age:9};

    if ((typeof jack) != (typeof neal)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeNegativeEqualityCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    Animal dog;
    dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack) == (typeof dog)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeNegativeNotEqualityCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    Animal dog;
    dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack) != (typeof dog)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructFieldTypeEqualityCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    Animal dog;
    dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack.name) == (typeof dog.name)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructFieldTypeNotEqualityCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    Animal dog;
    dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack.name) == (typeof dog.age)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestJSONEqualityCase() (int) {
    json json_object = {"dummy":"dummy"};
    json json_array = [1,2,3];
    if ((typeof json_object) == (typeof json_array)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTypeAsReturnValue() (int) {
    json json_object = {"dummy":"dummy"};
    json json_array = [1,2,3];
    if ((typeof json_object) == getType(json_array)) {
        return 1;
    } else {
        return 2;
    }
}

function getType(any variable)(type){
    return (typeof variable);
}

struct Person {
    string name;
    int age;
}

struct Animal {
    string name;
    int size;
    int age;
}

function refTypeAccessTestMultiArrayNegativeCase() (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][][] intMulti = [];
    if ((typeof jsonMulti) == (typeof intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayPositiveCase() (int) {
    json[][][][][][][] jsonMulti = [];
    json[][][][][][][] jsonMulti_2 = [];
    if ((typeof jsonMulti) == (typeof jsonMulti_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionCase() (int) {
    json[][][][][][][] jsonMulti = [];
    json[][][][][][] jsonMulti_2 = [];
    if ((typeof jsonMulti) == (typeof jsonMulti_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionCaseTwo() (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][] intMulti = [];
    if ((typeof jsonMulti) == (typeof intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionNotEqualityCase() (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][] intMulti = [];
    if ((typeof jsonMulti) != (typeof intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function typeToAnyImplicitCast() (any, type) {
    int i = 5;
    type t = (typeof i);
    any typeOfInt = t;
    return typeOfInt, t;
}

function typeToAnyExplicitCast() (any, type, any) {
    int i = 5;
    type t = (typeof i);
    return (any)t, t, t;
}

function anyToTypeExplicitCast() (type, any) {
    int i = 5;
    any typeOfInt = (typeof i);
    var t, _ = (type)typeOfInt;
    return t, typeOfInt;
}

function getTypeStringValue()(type){
    int value = 4;
    return (typeof value);
}

function getStructTypeStringValue()(type){
    Person jack;
    jack = {name:"Jack", age:25};
    return (typeof jack);
}

function testTypeAccessExprValueType() (int) {
    int intValue;
    if((typeof intValue) == (typeof int)){
        return 1;
    } else {
        return 0;
    }
}

function testTypeAccessExprValueTypeNegative() (int) {
    int intValue;
    type int_t = typeof intValue;
    type string_t = typeof string;
    if(int_t == string_t){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprValueTypeArrayNegative() (int) {
    string[] strValue;
    if((typeof strValue) == (typeof int[])){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprValueTypeArray() (int) {
    int[] intValue;
    if((typeof intValue) == (typeof int[])){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprStructWithValue() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    if((typeof jack) == (typeof Person)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprStructWithValueNegative() (int) {
    Person jack;
    jack = {name:"Jack", age:25};
    if((typeof jack) == (typeof Animal)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprTwoStructTypes() (int) {
    if((typeof Person) == (typeof Animal)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprSameStructType() (int) {
    if((typeof Person) == (typeof Person)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeOfJson() (type, type, type, type, type, type){
    json j1 = {"foo":"bar"}; // object type
    json j2 = [1, "foo", true];
    json j3 = "foo";
    json j4 = 3;
    json j5 = 7.65;
    json j6 = true;
    
    return typeof j1, typeof j2, typeof j3, typeof j4, typeof j5, typeof j6;
}

function testCheckTypeOfJson() (json, json[], string, int, float, boolean){
    json j1 = {"foo":"bar"}; // object type
    json j2 = [1, "foo", true];
    json j3 = "foo";
    json j4 = 3;
    json j5 = 7.65;
    json j6 = true;
    
    json j;
    json[] ja;
    string s;
    int a;
    float f;
    boolean b;
    
    if (typeof j1 == typeof json) {
        j = j1;
    }

    io:println(typeof j2);
    io:println(typeof json[]);
    if (typeof j2 == typeof json[]) {
        var ja, e = (json[]) j2;
        io:println(e);
    } else {
        io:println("error on json[] cast");
    }

    if (typeof j3 == typeof string) {
        s, _ = (string) j3;
    }
    
    if (typeof j4 == typeof int) {
        a, _ = (int) j4;
    }

    if (typeof j5 == typeof float) {
        f, _ = (float) j5;
    }
    
    if (typeof j6 == typeof boolean) {
        b, _ = (boolean) j6;
    }
    
    return j, ja, s, a, f, b;
}

function testTypeOfStructArray() (type, type, type) {
    Person[] p = [{}, {}];
    return typeof p, typeof Person[], typeof Person[][];
}
