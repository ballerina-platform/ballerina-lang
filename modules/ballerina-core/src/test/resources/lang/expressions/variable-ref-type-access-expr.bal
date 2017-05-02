function refTypeAccessTestTrivialEqualityPositiveCase() (int) {
    int temp_int = 2;
    int temp_int_1 = 5;
    if (temp_int.type == temp_int_1.type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialEqualityNegativeCase() (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if (temp_int.type == temp_str.type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestTrivialNotEqualityCase() (int) {
    int temp_int = 2;
    string temp_str = "dummy";
    if (temp_int.type != temp_str.type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypeNegativeCase() (int) {
    any temp_int = 2;
    string temp_str = "dummy";
    if (temp_int.type == temp_str.type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestAnyTypePositiveCase() (int) {
    any any_type_var = "dummy";
    string temp_str = "dummy";
    if (any_type_var.type == temp_str.type) {
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
    if (int_map["index"].type == string_map["index"].type) {
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
    if (int_array[0].type == string_array[0].type) {
        return 1;
    } else {
        return 2;
    }
}

function refTypeAccessTestStructAccessCase() (int) {
    Person jack;
    jack = {name:"Jack", age:25};

    if (jack.name.type == jack.age.type) {
        return 1;
    } else {
        return 2;
    }
}

struct Person {
    string name;
    int age;
}


