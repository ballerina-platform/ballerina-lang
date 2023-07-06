function mapInitTest() returns (map<any>) {
    map<any> animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4":"Dog"};
    return animals;
}

function testNestedMapInit () returns (map<any>) {
    map<map<any>> m = {"info":{"city":"Colombo", "country":"SriLanka"}};
    return m;
}

function testMapInitWithJson () returns (map<any>) {
    json j = {"city":"Colombo", "country":"SriLanka"};
    map<any> m = {"name":"Supun", "info":j};
    return m;
}

function testComplexMapInit() returns (map<any>) {
    map<any> address = {city:"CA", "country":"USA"};
    int[] intArray = [7,8,9];
    map<map<any>>[]  addressArray = [
                         {address:{city:"Colombo", "country":"SriLanka"}},
                         {address:{city:"Kandy", "country":"SriLanka"}},
                         {address:{city:"Galle", "country":"SriLanka"}}
                         ];
    map<any> m = { name:"Supun",
              age:25,
              gpa:2.81,
              status:true,
              info:(),
              address:address,
              intArray:intArray,
              addressArray:addressArray
            };
    return m;
}

function mapInitWithIdentifiersTest() returns (map<any>) {
    string a = "key1";
    map<any> animals = { a: "Lion", [a]: "Cat", [getKey()]: "Dog" };
    return animals;
}

function getKey() returns (string) {
	return "key2";
}

function testEmptyMap() returns (map<any>) {
    map<any> emptyMap = {};
    mapFunction({});
    return emptyMap;
}

function mapFunction(map<any> m) {

}

string iValue = "i";

function testExpressionAsKeys() returns boolean {
    map<anydata> b = { s: "hello", [iValue]: 1, [getChar("f")]: 2.0, [getChar("b")]: true };
    return b["s"] == "hello" && b["i"] == 1 && b["f"] == 2.0 && b["b"] == true;
}

string mapValue = "";

function testExpressionAsKeysWithSameKeysDefinedAsLiteralsOrFieldNames() returns boolean {
    map<string|float> b = {
        f: 1.0,
        [getChar("f")]: 4.0,
        [getChar("s")]: addStringToMapValue(" world"),
        [getChar("s")]: addStringToMapValue(" from Ballerina"),
        s: addStringToMapValue("hello")
    };
    return b["s"] == "hello world from Ballerina" && b["f"] == 4.0;
}

function getChar(string st) returns string {
    return st;
}

function addStringToMapValue(string s) returns string {
    mapValue = mapValue + s;
    return mapValue;
}
