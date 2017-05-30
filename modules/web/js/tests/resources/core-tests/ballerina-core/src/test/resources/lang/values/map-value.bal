function testMapWithAny() (string){
    map animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4":"Dog"};
    any animal = animals["animal1"];
    return (string) animal;
}

function testMapWithMap() (string){
    map list1 = {"item1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    map list2 = list1;
    return (string) list2["item1"];
}

function testMapWithAnyValue() (int){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    any value = 5;
    list1["item5"] = value;
    return (int) list1["item5"];
}

function testMapWithAnyDifferentValue() (any){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    any value = "aString";
    list1["item5"] = value;
    return list1["item5"];
}

function testMapWithBinaryExpression() (int){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    int value = (int) list1["item1"] + (int) list1["item2"];
    return value;
}

function testMapWithFunctionInvocations() (string){
    map list1 = {"list1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    string value = testEcho( (string) list1["list1"]);
    return value;
}

function testMapWithAnyFunctionInvocations() (string){
    map list1 = {"list1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    string value = testEchoAny(list1["item2"]);
    return value;
}

function testEcho(string value)(string){
    return value;
}

function testEchoAny(any value)(string){
    return (string) value;
}
