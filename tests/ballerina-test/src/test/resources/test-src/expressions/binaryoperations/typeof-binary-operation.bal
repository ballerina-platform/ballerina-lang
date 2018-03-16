function compareWithValueTypeBasicIf() (int) {
    int temp_int = 2;
    if (temp_int typeof int) {
        return 1;
    } else {
        return 2;
    }
}

function compareWithTypeTypeBasicIf() (int) {
    int temp_int = 2;
    type temp_int_type = (typeof temp_int);

    if (temp_int_type typeof type) {
        return 1;
    } else {
        return 2;
    }
}

function compareWithTypeTypeBasicIfWithVar() (int) {
    int temp_int = 2;
    var temp_int_type = (typeof temp_int);

    if (temp_int_type typeof type) {
        return 1;
    } else {
        return 2;
    }
}

function compareIntWithStringBasicIf() (int) {
    int temp_int = 2;
    if (temp_int typeof string) {
        return 1;
    } else {
        return 2;
    }
}

function compareIntDefinedAsAnyBasicIf() (int) {
    any temp_int = 2;
    if (temp_int typeof string) {
        return 1;
    } else {
        return 2;
    }
}

function compareIntInsideMapBasicIf() (int) {
    map int_map = {};
    int_map["index"] = 2;
    if (int_map["index"] typeof string) {
        return 1;
    } else {
        return 2;
    }
}

function compareIntInsideArrayBasicIf() (int) {
    int[] int_array = [];
    int_array[0] = 2;
    if (int_array[0] typeof string) {
        return 1;
    } else {
        return 2;
    }
}

function compareArrayTypeBasicIfFalse() (int) {
    int[] int_array = [];
    int_array[0] = 2;
    if (int_array typeof string[]) {
        return 1;
    } else {
        return 2;
    }
}

function compareArrayTypeBasicIfTrue() (int) {
    string[] string_array = [];
    string_array[0] = "dddd";
    if (string_array typeof string[]) {
        return 1;
    } else {
        return 2;
    }
}

function compareIntInsideStruct() (int) {
    Person jack;
    jack = {name:"Jack", age:25};

    if (jack.name typeof int) {
        return 1;
    } else {
        return 2;
    }
}

function compareIntInsideStructTrue() (int) {
    Person jack;
    jack = {name:"Jack", age:25};

    if (jack.name typeof string) {
        return 1;
    } else {
        return 2;
    }
}

function compareStructTypeBasicIfTrue() (int) {
    Person jack;
    jack = {name:"Jack", age:25};

    if (jack typeof Person) {
        return 1;
    } else {
        return 2;
    }
}

function compareStructTypeBasicIfFail() (int) {
    Person jack;
    jack = {name:"Jack", age:25};

    if (jack typeof Animal) {
        return 1;
    } else {
        return 2;
    }
}

function compareJsonArrayBasicIf() (int) {
    json json_array = [1,2,3];
    if (json_array typeof json[]) {
        return 1;
    } else {
        return 2;
    }
}

function compareJsonObjectBasicIf() (int) {
    json json_object = {"dummy":"dummy"};
    if (json_object typeof json) {
        return 1;
    } else {
        return 2;
    }
}

function compareAsReturnJsonArray() (int) {
    json json_array = [1,2,3];
    if (getType(json_array)) {
        return 1;
    } else {
        return 2;
    }
}

function getType(any variable)(boolean){
    return variable typeof json[];
}

function compareAsReturnJsonObject() (int) {
    json json_object = {"dummy":"dummy"};
    if (getType(json_object)) {
        return 1;
    } else {
        return 2;
    }
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

function compareMultiArrayJsonNegative() (int) {
    json[][][][][][][] jsonMulti = [];
    if (jsonMulti typeof int[][][][][][][]) {
        return 1;
    } else {
        return 2;
    }
}

function compareMultiArrayJsonPositive() (int) {
    json[][][][][][][] jsonMulti = [];
    if (jsonMulti typeof json[][][][][][][]) {
        return 1;
    } else {
        return 2;
    }
}

function compareMultiArrayJsonDifferentDimensions() (int) {
    json[][][][][][][] jsonMulti = [];
    if (jsonMulti typeof json[][][][]) {
        return 1;
    } else {
        return 2;
    }
}

function compareMultiArrayIntNegative() (int) {
    int[][][][][][][] intMulti = [];
    if (intMulti typeof float[][][][][][][]) {
        return 1;
    } else {
        return 2;
    }
}

function compareMultiArrayIntPositive() (int) {
    int[][][][][][][] intMulti = [];
    if (intMulti typeof int[][][][][][][]) {
        return 1;
    } else {
        return 2;
    }
}

function compareMultiArrayIntDifferentDimensions() (int) {
    int[][][][][][][] intMulti = [];
    if (intMulti typeof int[][][][]) {
        return 1;
    } else {
        return 2;
    }
}

function testTypeOfStructArray() (boolean, boolean, boolean) {
    Person[] p = [{}, {}];

    return (p typeof Person), (p typeof Person[]), (p typeof Person[][]);
}

struct Software {
    string name;
    string des;
}

struct Middleware {
    string name;
}

function getTypePreserveWhenCast()(boolean, boolean, boolean, boolean){
    Software s = {name:"WSO2", des:"ESB"};
    Middleware m = (Middleware)s;

    return (m typeof Middleware), (m typeof Software), (s typeof Middleware), (s typeof Software);
}

function recursiveTypeOf() (boolean) {
    int val1 = 4;
    return val1 typeof int typeof boolean;
}

function testTypeOfAsParam()(int) {
    string val = "hhhh";
    return typeofValAsParam(val typeof string);
}

function typeofValAsParam(boolean param) (int) {
    if (param) {
        return 1;
    }
    return 2;
}
