function mapInitTest() returns (map) {
    map animals;
    animals = {"animal1":"Lion", "animal2":"Cat", "animal3":"Leopard", "animal4":"Dog"};
    return animals;
}

function testNestedMapInit () returns (map) {
    map<map> m = {"info":{"city":"Colombo", "country":"SriLanka"}};
    return m;
}

function testMapInitWithJson () returns (map) {
    json j = {"city":"Colombo", "country":"SriLanka"};
    map m = {"name":"Supun", "info":j};
    return m;
}

function testComplexMapInit() returns (map) {
    map address = {city:"CA", "country":"USA"};
    int[] intArray = [7,8,9];
    map<map>[]  addressArray = [
                         {address:{city:"Colombo", "country":"SriLanka"}},
                         {address:{city:"Kandy", "country":"SriLanka"}},
                         {address:{city:"Galle", "country":"SriLanka"}}
                         ];
    map m = { name:"Supun",
              age:25,
              gpa:2.81,
              status:true,
              info:null,
              address:address,
              intArray:intArray,
              addressArray:addressArray
            };
    return m;
}

function mapInitWithIdentifiersTest() returns (map) {
    string a = "key1";
    map animals = {a:"Lion", (a):"Cat", getKey():"Dog"};
    return animals;
}

function getKey() returns (string) {
	return "key2";
}