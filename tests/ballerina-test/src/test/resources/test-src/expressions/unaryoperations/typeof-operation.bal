import ballerina/io;
function refTypeAccessTestTrivialEqualityPositiveCase() returns (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    if ((typeof temp_int) == (typeof temp_int_1)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclared() returns (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    typedesc temp_int_type = (typeof temp_int);
    typedesc temp_int_1_type = (typeof temp_int_1);

    if (temp_int_type == temp_int_1_type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclaredWithVar() returns (int) {
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

function refTypeAccessTestTrivialEqualityNegativeCase() returns (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if ((typeof temp_int) == (typeof temp_str)) {
        return 1;
    } else {
       return 2;
    }
}

function refTypeAccessTestTrivialNotEqualityCase() returns (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if ((typeof temp_int) != (typeof temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypeNegativeCase() returns (int) {
    any temp_int = 2;
    string temp_str = "dummy";
    if ((typeof temp_int) == (typeof temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypePositiveCase() returns (int) {
    any any_type_var = "dummy";
    string temp_str = "dummy";
    if ((typeof any_type_var) == (typeof temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMapAccessCase() returns (int) {
    map int_map;
    int_map["index"] = 2;
    map string_map;
    string_map["index"] = "dummy";
    if ((typeof int_map["index"]) == (typeof string_map["index"])) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestArrayAccessCase() returns (int) {
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

function refTypeAccessTestArrayEqualityCase() returns (int) {
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

function refTypeAccessTestArrayEqualityPositiveCase() returns (int) {
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

function refTypeAccessTestStructAccessCase() returns (int) {
    Person jack = {name:"Jack", age:25};

    if ((typeof jack.name) == (typeof jack.age)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeEqualityCase() returns (int) {
    Person jack = {name:"Jack", age:25};
    Person neal = {name:"Neal", age:9};

    if ((typeof jack) == (typeof neal)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeNotEqualityCase() returns (int) {
    Person jack = {name:"Jack", age:25};
    Person neal = {name:"Neal", age:9};

    if ((typeof jack) != (typeof neal)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeNegativeEqualityCase() returns (int) {
    Person jack = {name:"Jack", age:25};
    Animal dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack) == (typeof dog)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructTypeNegativeNotEqualityCase() returns (int) {
    Person jack = {name:"Jack", age:25};
    Animal dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack) != (typeof dog)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructFieldTypeEqualityCase() returns (int) {
    Person jack = {name:"Jack", age:25};
    Animal dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack.name) == (typeof dog.name)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructFieldTypeNotEqualityCase() returns (int) {
    Person jack = {name:"Jack", age:25};
    Animal dog = {name:"Doggy", size:1, age:4};

    if ((typeof jack.name) == (typeof dog.age)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestJSONEqualityCase() returns (int) {
    json json_object = {"dummy":"dummy"};
    json json_array = [1,2,3];
    if ((typeof json_object) == (typeof json_array)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTypeAsReturnValue() returns (int) {
    json json_object = {"dummy":"dummy"};
    json json_array = [1,2,3];
    if ((typeof json_object) == getType(json_array)) {
        return 1;
    } else {
        return 2;
    }
}

function getType(any variable)returns (typedesc){
    return (typeof variable);
}

type Person {
    string name,
    int age;
};

type Animal {
    string name,
    int size,
    int age;
};

function refTypeAccessTestMultiArrayNegativeCase() returns (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][][] intMulti = [];
    if ((typeof jsonMulti) == (typeof intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayPositiveCase() returns (int) {
    json[][][][][][][] jsonMulti = [];
    json[][][][][][][] jsonMulti_2 = [];
    if ((typeof jsonMulti) == (typeof jsonMulti_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionCase() returns (int) {
    json[][][][][][][] jsonMulti = [];
    json[][][][][][] jsonMulti_2 = [];
    if ((typeof jsonMulti) == (typeof jsonMulti_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionCaseTwo() returns (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][] intMulti = [];
    if ((typeof jsonMulti) == (typeof intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionNotEqualityCase() returns (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][] intMulti = [];
    if ((typeof jsonMulti) != (typeof intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function typeToAnyImplicitCast() returns (any, typedesc) {
    int i = 5;
    typedesc t = (typeof i);
    any typeOfInt = t;
    return (typeOfInt, t);
}

function typeToAnyExplicitCast() returns (any, typedesc, any) {
    int i = 5;
    typedesc t = (typeof i);
    return (<any>t, t, t);
}

function anyToTypeExplicitCast() returns (typedesc, any) {
    int i = 5;
    any typeOfInt = (typeof i);
    var t = check <typedesc>typeOfInt;
    return (t, typeOfInt);
}

function getTypeStringValue() returns (typedesc){
    int value = 4;
    return (typeof value);
}

function getStructTypeStringValue() returns (typedesc){
    Person jack = {name:"Jack", age:25};
    return (typeof jack);
}

function testTypeAccessExprValueType() returns (int) {
    int intValue;
    if((typeof intValue) == (typeof int)){
        return 1;
    } else {
        return 0;
    }
}

function testTypeAccessExprValueTypeNegative() returns (int) {
    int intValue;
    typedesc int_t = typeof intValue;
    typedesc string_t = typeof string;
    if(int_t == string_t){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprValueTypeArrayNegative() returns (int) {
    string[] strValue = [];
    if((typeof strValue) == (typeof int[])){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprValueTypeArray() returns (int) {
    int[] intValue = [];
    if((typeof intValue) == (typeof int[])){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprStructWithValue() returns (int) {
    Person jack = {name:"Jack", age:25};
    if((typeof jack) == (typeof Person)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprStructWithValueNegative() returns (int) {
    Person jack = {name:"Jack", age:25};
    if((typeof jack) == (typeof Animal)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprTwoStructTypes() returns (int) {
    if((typeof Person) == (typeof Animal)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeAccessExprSameStructType() returns (int) {
    if((typeof Person) == (typeof Person)){
       return 1;
    } else {
       return 0;
    }
}

function testTypeOfJson() returns (typedesc, typedesc, typedesc, typedesc, typedesc, typedesc){
    json j1 = {"foo":"bar"}; // object type
    json j2 = [1, "foo", true];
    json j3 = "foo";
    json j4 = 3;
    json j5 = 7.65;
    json j6 = true;

    return (typeof j1, typeof j2, typeof j3, typeof j4, typeof j5, typeof j6);
}

function testCheckTypeOfJson() returns (json, json[], string, int, float, boolean){
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
        ja = check <json[]> j2;
    } else {
        io:println("error on json[] cast");
    }

    if (typeof j3 == typeof string) {
        s = check <string> j3;
    }

    if (typeof j4 == typeof int) {
        a = check <int> j4;
    }

    if (typeof j5 == typeof float) {
        f = check <float> j5;
    }

    if (typeof j6 == typeof boolean) {
        b = check <boolean> j6;
    }

    return (j, ja, s, a, f, b);
}

function testTypeOfStructArray() returns (typedesc, typedesc, typedesc) {
    Person[] p = [{}, {}];
    return (typeof p, typeof Person[], typeof Person[][]);
}

type Software {
    string name,
    string des;
};

type Middleware {
    string name;
};

function getTypePreserveWhenCast() returns (int){
    Software s = {name:"WSO2", des:"ESB"};
    Middleware m = <Middleware>s;
    if (typeof s == typeof m){
        return 1;
    } else {
        return 0;
    }
}
