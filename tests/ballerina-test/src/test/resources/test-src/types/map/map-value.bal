function testMapWithAny() returns (string){
    map animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4":"Dog"};
    any animal = animals["animal1"];
    string animalString = <string> animal;
    return animalString;
}

function testMapWithMap() returns (string){
    map list1 = {"item1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    map list2 = list1;
    string list2String = <string> list2["item1"];
    return list2String;
}

function testMapWithAnyValue() returns (int){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    any value = 5;
    list1["item5"] = value;
    var intVal = check <int> list1["item5"];
    return intVal;
}

function testMapWithAnyDifferentValue() returns (any){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    any value = "aString";
    list1["item5"] = value;
    return list1["item5"];
}

function testMapWithBinaryExpression() returns (int){
    map list1 = {"item1": 1, "item2": 2, "item3": 3, "item4": 4};
    int intItem1= check <int> list1["item1"];
    int intItem2= check <int> list1["item2"];
    int value = intItem1 + intItem2;
    return value;
}

function testMapWithFunctionInvocations() returns (string){
    map list1 = {"list1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    string list1String = <string> list1["list1"];
    string value = testEcho(list1String);
    return value;
}

function testMapWithAnyFunctionInvocations() returns (string){
    map list1 = {"list1":"item1", "item2":"item2", "item3":"item3", "item4":"item4"};
    string value = testEchoAny(list1["item2"]);
    return value;
}

function testMapOrder() returns (map)
{
  map m ;
  m["key1"] = "Element 1";
  m["key2"] = "Element 2";
  m["key3"] = "Element 3";
    return m;

}

function testEcho(string value)returns (string){
    return value;
}

function testEchoAny(any value)returns (string){
    string stringVal = <string> value;
    return stringVal;
}

function testMapSynchronization() returns (int)
{
    map m;

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
