function refTypeAccessTestTrivialEqualityPositiveCase() (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    if ((temp_int) == (temp_int_1)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclared() (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    type temp_int_type = (temp_int);
    type temp_int_1_type = (temp_int_1);

    if (temp_int_type == temp_int_1_type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclaredWithVar() (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    var temp_int_type = (temp_int);
    var temp_int_1_type = (temp_int_1);

    if (temp_int_type == temp_int_1_type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityNegativeCase() (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if ((temp_int) == (temp_str)) {
        return 1;
    } else {
       return 2;
    }
}

function refTypeAccessTestTrivialNotEqualityCase() (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if ((temp_int) != (temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypeNegativeCase() (int) {
    any temp_int = 2;
    string temp_str = "dummy";
    if ((temp_int) == (temp_str)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypePositiveCase() (int) {
    any any_type_var = "dummy";
    string temp_str = "dummy";
    if ((any_type_var) == (temp_str)) {
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
    if ((int_map["index"]) == (string_map["index"])) {
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
    if ((int_array[0]) == (string_array[0])) {
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
    if ((int_array) == (string_array)) {
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
    if ((int_array) == (int_array_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructAccessCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};

    if ((jack.name) == (jack.age)) {
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

    if ((jack) == (neal)) {
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

    if ((jack) != (neal)) {
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

    if ((jack) == (dog)) {
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

    if ((jack) != (dog)) {
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

    if ((jack.name) == (dog.name)) {
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

    if ((jack.name) == (dog.age)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestJSONEqualityCase() (int) {
    json json_object = {"dummy":"dummy"};
    json json_array = [1,2,3];
    if ((json_object) == (json_array)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTypeAsReturnValue() (int) {
    json json_object = {"dummy":"dummy"};
    json json_array = [1,2,3];
    if ((json_object) == getType(json_array)) {
        return 1;
    } else {
        return 2;
    }
}

function getType(any variable)(type){
    return (variable);
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
    if ((jsonMulti) == (intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayPositiveCase() (int) {
    json[][][][][][][] jsonMulti = [];
    json[][][][][][][] jsonMulti_2 = [];
    if ((jsonMulti) == (jsonMulti_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionCase() (int) {
    json[][][][][][][] jsonMulti = [];
    json[][][][][][] jsonMulti_2 = [];
    if ((jsonMulti) == (jsonMulti_2)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionCaseTwo() (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][] intMulti = [];
    if ((jsonMulti) == (intMulti)) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestMultiArrayDifferentDimensionNotEqualityCase() (int) {
    json[][][][][][][] jsonMulti = [];
    int[][][][][][] intMulti = [];
    if ((jsonMulti) != (intMulti)) {
        return 1;
    } else {
        return 2;
    }
}