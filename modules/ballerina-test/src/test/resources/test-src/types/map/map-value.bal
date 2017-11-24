function testMapWithAny() (string){
    map animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4":"Dog"};
    any animal = animals["animal1"];
    string animalString;
    animalString, _ = (string) animal;
    return animalString;
}

function testMapWithMap() (string){
    map list1 = {"item1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    map list2 = list1;
    string list2String;
    list2String, _ = (string) list2["item1"];
    return list2String;
}

function testMapWithAnyValue() (int){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    any value = 5;
    list1["item5"] = value;
    int intVal;
    intVal, _ = (int) list1["item5"];
    return intVal;
}

function testMapWithAnyDifferentValue() (any){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    any value = "aString";
    list1["item5"] = value;
    return list1["item5"];
}

function testMapWithBinaryExpression() (int){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    int intItem1;
    int intItem2;
    intItem1, _ = (int) list1["item1"];
    intItem2, _ = (int) list1["item2"];
    int value = intItem1 + intItem2;
    return value;
}

function testMapWithFunctionInvocations() (string){
    map list1 = {"list1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    string list1String;
    list1String, _ = (string) list1["list1"];
    string value = testEcho(list1String);
    return value;
}

function testMapWithAnyFunctionInvocations() (string){
    map list1 = {"list1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    string value = testEchoAny(list1["item2"]);
    return value;
}

function testMapOrder() (map)
{
  map m = {};
  m["key1"] = "Element 1";
  m["key2"] = "Element 2";
  m["key3"] = "Element 3";
    return m;

}

function testEcho(string value)(string){
    return value;
}

function testEchoAny(any value)(string){
    string stringVal;
    stringVal, _ = (string) value;
    return stringVal;
}

function testMapSynchronization()(int)
{
    map m = {};

    fork {
        worker w2 {
            int i = 0;
            while (i < 1000) {
                string key = "a" + i;
                string value = "foo" + i;
                m[key] = value;
                i = i + 1;
            }
        }
        worker w3 {
            int j = 0;
            while (j < 1000) {
                string key = "b" + j;
                string value = "bar" + j;
                m[key] = value;
                j = j + 1;
            }
        }
    } join (all) (map results) {
        return lengthof m;
    }
}
